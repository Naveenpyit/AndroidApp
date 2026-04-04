package com.example.myapplication.activity;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.WindowCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.model.RegisterDetailsRequest;
import com.example.myapplication.model.RegisterDetailsResponse;
import com.example.myapplication.model.RegisterInsertRequest;
import com.example.myapplication.model.RegisterInsertResponse;
import com.example.myapplication.network.ApiService;
import com.example.myapplication.network.RetrofitClient;
import com.example.myapplication.utils.TokenManager;
import com.google.android.material.button.MaterialButton;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity {

    private static final String TAG = "EditProfileActivity";
    private static final int REQ_CAMERA = 101;
    private static final int REQ_GALLERY = 102;

    private ImageView ivProfilePic, ivEditProfilePic, toolbarBack;
    private EditText etFullName, etMobileNumber, etEmail, etPanGst;
    private Spinner spinnerBusinessType;
    private RadioButton rbPAN, rbGST;
    private RadioGroup rgIdentity;
    private ImageView ivShopImage;
    private TextView tvUploadShop;
    private LinearLayout llShopImageContainer;
    private RecyclerView rvAddresses;
    private MaterialButton btnBackToSetup;
    private ProgressDialog progressDialog;

    private String mobile = "";
    private String imageBase64 = "";
    private boolean imageChanged = false;
    private Uri cameraUri;

    private ApiService apiService;
    private TokenManager tokenManager;

    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(), result -> {
                        if (result.getResultCode() == RESULT_OK && cameraUri != null)
                            handleImageUri(cameraUri);
                    });

    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(), result -> {
                        if (result.getResultCode() == RESULT_OK
                                && result.getData() != null
                                && result.getData().getData() != null)
                            handleImageUri(result.getData().getData());
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        setupStatusBar();
        apiService = RetrofitClient.getClient(this);
        tokenManager = new TokenManager(this);
        setupLoader();

        mobile = tokenManager.getUserId();
        if (mobile == null || mobile.isEmpty()) {
            mobile = getSharedPreferences("app_prefs", MODE_PRIVATE)
                    .getString("mobile", "");
        }

        initViews();
        setupBusinessTypeSpinner();
        setupIdentityToggle();
        setupClickListeners();

        fetchProfileData();
    }

    private void initViews() {
        ivProfilePic = findViewById(R.id.iv_profile_pic);
        ivEditProfilePic = findViewById(R.id.iv_edit_profile_pic);
        toolbarBack = findViewById(R.id.toolbarBack);
        etFullName = findViewById(R.id.et_full_name);
        etMobileNumber = findViewById(R.id.et_mobile_number);
        etEmail = findViewById(R.id.et_email);
        etPanGst = findViewById(R.id.et_pan_gst);
        spinnerBusinessType = findViewById(R.id.spinner_business_type);
        rbPAN = findViewById(R.id.rbPAN);
        rbGST = findViewById(R.id.rbGST);
        rgIdentity = findViewById(R.id.rgIdentity);
        ivShopImage = findViewById(R.id.iv_shop_image);
        tvUploadShop = findViewById(R.id.tv_upload_shop);
        llShopImageContainer = findViewById(R.id.ll_shop_image_container);
        rvAddresses = findViewById(R.id.rv_addresses);
        btnBackToSetup = findViewById(R.id.btn_back_to_setup);

        etMobileNumber.setEnabled(false);
        rvAddresses.setLayoutManager(new LinearLayoutManager(this));
        rvAddresses.setNestedScrollingEnabled(false);
    }

    private void setupBusinessTypeSpinner() {
        String[] types = {"Retailer", "Wholesaler", "Distributor"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, R.layout.spinner_item, types);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerBusinessType.setAdapter(adapter);
    }

    private void setupIdentityToggle() {
        rgIdentity.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbPAN) {
                etPanGst.setHint("Enter PAN Number");
            } else {
                etPanGst.setHint("Enter GST Number");
            }
            etPanGst.setText("");
        });
    }

    private void setupClickListeners() {
        toolbarBack.setOnClickListener(v -> onBackPressed());
        ivEditProfilePic.setOnClickListener(v -> showImagePickerDialog());
        llShopImageContainer.setOnClickListener(v -> showImagePickerDialog());
        btnBackToSetup.setOnClickListener(v -> saveProfile());
    }

    private void fetchProfileData() {
        showLoader("Loading profile...");

        apiService.getRegisterDetails(new RegisterDetailsRequest(mobile))
                .enqueue(new Callback<RegisterDetailsResponse>() {

                    @Override
                    public void onResponse(Call<RegisterDetailsResponse> call,
                                           Response<RegisterDetailsResponse> res) {
                        hideLoader();

                        if (!res.isSuccessful() || res.body() == null) {
                            Toast.makeText(EditProfileActivity.this,
                                    "Failed to load profile", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        RegisterDetailsResponse body = res.body();

                        if (body.getNStatus() != 1) {
                            Toast.makeText(EditProfileActivity.this,
                                    body.getCMessage(), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        RegisterDetailsResponse.JData data = body.getJData();
                        if (data == null) return;

                        // ✅ Set Owner Details
                        if (data.getOwnerDetails() != null) {
                            RegisterDetailsResponse.OwnerDetails owner = data.getOwnerDetails();
                            etFullName.setText(safe(owner.getCName()));
                            etMobileNumber.setText("+91 " + safe(owner.getNMobile()));
                            etEmail.setText(safe(owner.getCEmail()));
                        }

                        // ✅ Set Business Details
                        if (data.getBusinessDetails() != null) {
                            RegisterDetailsResponse.BusinessDetails business = data.getBusinessDetails();

                            // Business type: "1"=Retailer(0), "2"=Wholesaler(1), "3"=Distributor(2)
                            String nType = safe(business.getNType());
                            switch (nType) {
                                case "1":
                                    spinnerBusinessType.setSelection(0);
                                    break;
                                case "2":
                                    spinnerBusinessType.setSelection(1);
                                    break;
                                case "3":
                                    spinnerBusinessType.setSelection(2);
                                    break;
                                default:
                                    spinnerBusinessType.setSelection(0);
                                    break;
                            }

                            // PAN / GST: "1"=PAN, "2"=GST
                            String verifyType = safe(business.getNVerifyType());
                            if ("1".equals(verifyType)) {
                                rbPAN.setChecked(true);
                                etPanGst.setHint("Enter PAN Number");
                                etPanGst.setText(safe(business.getCPan()));
                            } else if ("2".equals(verifyType)) {
                                rbGST.setChecked(true);
                                etPanGst.setHint("Enter GST Number");
                                etPanGst.setText(safe(business.getCGst()));
                            }

                            // Shop image
                            String imgUrl = safe(business.getCImage());
                            if (!imgUrl.isEmpty() && !imgUrl.endsWith("customer/")) {
                                tvUploadShop.setVisibility(View.GONE);
                                ivShopImage.setVisibility(View.VISIBLE);
                                Glide.with(EditProfileActivity.this)
                                        .load(imgUrl)
                                        .placeholder(R.drawable.logo)
                                        .into(ivShopImage);
                            }
                        }

                        // ✅ Set Address Details
                        if (data.getAddressDetails() != null) {
                            setupAddressCard(data.getAddressDetails());
                        }
                    }

                    @Override
                    public void onFailure(Call<RegisterDetailsResponse> call,
                                          Throwable t) {
                        hideLoader();
                        Log.e(TAG, "fetchProfileData failure: " + t.getMessage());
                        Toast.makeText(EditProfileActivity.this,
                                "Network error", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setupAddressCard(RegisterDetailsResponse.AddressDetails addr) {
        String addressText = safe(addr.getCAddress());
        String pin = safe(addr.getNPincode());
        String addrType = safe(addr.getNAddressType());

        String typeLabel;
        switch (addrType) {
            case "1":
                typeLabel = "Home";
                break;
            case "2":
                typeLabel = "Head Office";
                break;
            case "3":
                typeLabel = "Other";
                break;
            default:
                typeLabel = "Address";
                break;
        }

        TextView tvAddrType = findViewById(R.id.tv_addr_type);
        TextView tvAddrDetails = findViewById(R.id.tv_addr_details);

        if (tvAddrType != null)
            tvAddrType.setText(typeLabel);
        if (tvAddrDetails != null)
            tvAddrDetails.setText(addressText + (pin.isEmpty() ? "" : " - " + pin));
    }

    private void saveProfile() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String panGst = etPanGst.getText().toString().trim();
        boolean isPAN = rbPAN.isChecked();
        String verifyType = isPAN ? "1" : "2";
        String pan = isPAN ? panGst : "";
        String gst = isPAN ? "" : panGst;

        int bTypePos = spinnerBusinessType.getSelectedItemPosition();
        String bType = String.valueOf(bTypePos + 1);

        if (fullName.isEmpty()) {
            etFullName.setError("Enter full name");
            etFullName.requestFocus();
            return;
        }

        showLoader("Saving...");

        // Save owner details
        RegisterInsertRequest ownerReq = RegisterInsertRequest.forOwner(fullName, mobile, email);

        apiService.registerInsert(ownerReq)
                .enqueue(new Callback<RegisterInsertResponse>() {
                    @Override
                    public void onResponse(Call<RegisterInsertResponse> call,
                                           Response<RegisterInsertResponse> res) {

                        if (!res.isSuccessful() || res.body() == null) {
                            hideLoader();
                            Toast.makeText(EditProfileActivity.this,
                                    "Failed to save owner details", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        RegisterInsertResponse body = res.body();
                        if (body.getNStatus() != 1) {
                            hideLoader();
                            Toast.makeText(EditProfileActivity.this,
                                    body.getCMessage(), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Save business details
                        RegisterInsertRequest bizReq = RegisterInsertRequest.forBusiness(
                                mobile, bType, verifyType, pan, gst,
                                imageChanged ? imageBase64 : "");

                        apiService.registerInsert(bizReq)
                                .enqueue(new Callback<RegisterInsertResponse>() {
                                    @Override
                                    public void onResponse(Call<RegisterInsertResponse> c,
                                                           Response<RegisterInsertResponse> r) {
                                        hideLoader();

                                        if (r.isSuccessful() && r.body() != null) {
                                            RegisterInsertResponse bizBody = r.body();
                                            if (bizBody.getNStatus() == 1) {
                                                Toast.makeText(EditProfileActivity.this,
                                                        "Profile updated successfully!",
                                                        Toast.LENGTH_SHORT).show();
                                                finish();
                                            } else {
                                                Toast.makeText(EditProfileActivity.this,
                                                        bizBody.getCMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            Toast.makeText(EditProfileActivity.this,
                                                    "Failed to save business details",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<RegisterInsertResponse> c,
                                                          Throwable t) {
                                        hideLoader();
                                        Toast.makeText(EditProfileActivity.this,
                                                "Network error", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }

                    @Override
                    public void onFailure(Call<RegisterInsertResponse> call,
                                          Throwable t) {
                        hideLoader();
                        Toast.makeText(EditProfileActivity.this,
                                "Network error", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showImagePickerDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_image_picker);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.BOTTOM);
            window.getAttributes().windowAnimations = R.style.BottomDialogAnimation;
        }

        dialog.findViewById(R.id.ll_camera).setOnClickListener(v -> {
            dialog.dismiss();
            openCamera();
        });
        dialog.findViewById(R.id.ll_gallery).setOnClickListener(v -> {
            dialog.dismiss();
            openGallery();
        });
        dialog.findViewById(R.id.tv_cancel).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, REQ_CAMERA);
            return;
        }
        launchCamera();
    }

    private void launchCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) == null) return;
        try {
            String stamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                    Locale.getDefault()).format(new Date());
            File f = File.createTempFile("SHOP_" + stamp, ".jpg",
                    getExternalFilesDir(null));
            cameraUri = FileProvider.getUriForFile(this,
                    getPackageName() + ".fileprovider", f);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            cameraLauncher.launch(intent);
        } catch (IOException e) {
            Toast.makeText(this, "Cannot create image file", Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                        REQ_GALLERY);
                return;
            }
        } else {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQ_GALLERY);
                return;
            }
        }
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }

    private void handleImageUri(Uri uri) {
        try {
            InputStream stream = getContentResolver().openInputStream(uri);
            if (stream == null) return;
            Bitmap bitmap = BitmapFactory.decodeStream(stream);
            stream.close();
            if (bitmap == null) return;

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            imageBase64 = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP);
            imageChanged = true;

            ivShopImage.setImageBitmap(bitmap);
            ivShopImage.setVisibility(View.VISIBLE);
            tvUploadShop.setVisibility(View.GONE);

        } catch (IOException e) {
            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean granted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
        if (requestCode == REQ_CAMERA && granted) launchCamera();
        else if (requestCode == REQ_GALLERY && granted) openGallery();
    }

    private void setupLoader() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    }

    private void showLoader(String msg) {
        if (progressDialog != null && !progressDialog.isShowing()) {
            progressDialog.setMessage(msg);
            progressDialog.show();
        }
    }

    private void hideLoader() {
        if (progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();
    }

    private void setupStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            getWindow().setStatusBarColor(
                    getResources().getColor(R.color.red_primary, getTheme()));
            WindowCompat.getInsetsController(
                            getWindow(), getWindow().getDecorView())
                    .setAppearanceLightStatusBars(false);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(
                    getResources().getColor(R.color.red_primary));
        }
    }

    private String safe(String s) {
        return (s == null) ? "" : s.trim();
    }
}