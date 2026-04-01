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
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.WindowCompat;

import com.example.myapplication.R;
import com.example.myapplication.model.RegisterInsertRequest;
import com.example.myapplication.model.RegisterInsertResponse;
import com.example.myapplication.network.ApiService;
import com.example.myapplication.network.RetrofitClient;
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

public class BusinessDetailsActivity extends AppCompatActivity {

    private static final String TAG         = "BusinessDetailsActivity";
    private static final int    REQ_CAMERA  = 101;
    private static final int    REQ_GALLERY = 102;

    // ── Views ──────────────────────────────────────────────────────────────────
    private RadioButton    rbRetailer, rbWholesaler, rbDistributor;
    private RadioGroup     rgIdentity;
    private RadioButton    rbPAN, rbGST;
    private EditText       etGSTNumber;
    private FrameLayout    flUploadImage;
    private ImageView      ivShopPreview;
    private LinearLayout   llUploadPlaceholder, llEditOverlay;
    private TextView       tvImageError;
    private MaterialButton btnSaveContinue;
    private ImageView      toolbarBack;

    // ── State ──────────────────────────────────────────────────────────────────
    private String  fullName, mobileNumber, email;
    private Uri     cameraImageUri;
    private boolean imageSelected = false;
    private String  imageBase64   = "";

    // ── API ────────────────────────────────────────────────────────────────────
    private ApiService     apiService;
    private ProgressDialog progressDialog;

    // ── Activity Result: Camera ────────────────────────────────────────────────
    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(), result -> {
                        if (result.getResultCode() == RESULT_OK && cameraImageUri != null) {
                            showImagePreview(cameraImageUri);
                        }
                    });

    // ── Activity Result: Gallery ───────────────────────────────────────────────
    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(), result -> {
                        if (result.getResultCode() == RESULT_OK
                                && result.getData() != null
                                && result.getData().getData() != null) {
                            showImagePreview(result.getData().getData());
                        }
                    });

    // ══════════════════════════════════════════════════════════════════════════
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_details);

        setupStatusBar();
        apiService = RetrofitClient.getClient(this);
        setupLoader();
        getIntentData();
        initializeViews();
        setupClickListeners();
        preFillFromIntent();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Intent data
    // ══════════════════════════════════════════════════════════════════════════
    private void getIntentData() {
        fullName     = safe(getIntent().getStringExtra("fullName"));
        mobileNumber = safe(getIntent().getStringExtra("mobileNumber"));
        email        = safe(getIntent().getStringExtra("email"));
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Views
    // ══════════════════════════════════════════════════════════════════════════
    private void initializeViews() {
        rbRetailer          = findViewById(R.id.rbRetailer);
        rbWholesaler        = findViewById(R.id.rbWholesaler);
        rbDistributor       = findViewById(R.id.rbDistributor);
        rgIdentity          = findViewById(R.id.rgIdentity);
        rbPAN               = findViewById(R.id.rbPAN);
        rbGST               = findViewById(R.id.rbGST);
        etGSTNumber         = findViewById(R.id.etGSTNumber);
        flUploadImage       = findViewById(R.id.flUploadImage);
        ivShopPreview       = findViewById(R.id.iv_shop_preview);
        llUploadPlaceholder = findViewById(R.id.ll_upload_placeholder);
        llEditOverlay       = findViewById(R.id.ll_edit_overlay);
        tvImageError        = findViewById(R.id.tv_image_error);
        btnSaveContinue     = findViewById(R.id.btnSaveContinue);
        toolbarBack         = findViewById(R.id.toolbarBack);

        // ── Because rbRetailer / rbWholesaler / rbDistributor are inside
        //    separate LinearLayouts (not a RadioGroup) in the XML, we must
        //    manually enforce single-selection behaviour. ─────────────────────
        View.OnClickListener businessTypeListener = v -> {
            rbRetailer.setChecked(v.getId() == R.id.rbRetailer);
            rbWholesaler.setChecked(v.getId() == R.id.rbWholesaler);
            rbDistributor.setChecked(v.getId() == R.id.rbDistributor);
        };
        rbRetailer.setOnClickListener(businessTypeListener);
        rbWholesaler.setOnClickListener(businessTypeListener);
        rbDistributor.setOnClickListener(businessTypeListener);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Pre-fill (returning user)
    //   Business type → "1"=Retailer  "2"=Wholesaler  "3"=Distributor
    //   Verify type   → "1"=PAN       "2"=GST
    // ══════════════════════════════════════════════════════════════════════════
    private void preFillFromIntent() {
        String prefillBType      = getIntent().getStringExtra("prefillBType");
        String prefillVerifyType = getIntent().getStringExtra("prefillVerifyType");
        String prefillPan        = getIntent().getStringExtra("prefillPan");
        String prefillGst        = getIntent().getStringExtra("prefillGst");

        if (prefillBType != null && !prefillBType.isEmpty()) {
            rbRetailer.setChecked(false);
            rbWholesaler.setChecked(false);
            rbDistributor.setChecked(false);
            switch (prefillBType) {
                case "1": rbRetailer.setChecked(true);    break;
                case "2": rbWholesaler.setChecked(true);  break;
                case "3": rbDistributor.setChecked(true); break;
            }
        }

        if (prefillVerifyType != null && !prefillVerifyType.isEmpty()) {
            if ("1".equals(prefillVerifyType)) {
                rbPAN.setChecked(true);
                etGSTNumber.setHint("Enter PAN Number here...");
                if (prefillPan != null && !prefillPan.isEmpty())
                    etGSTNumber.setText(prefillPan);
            } else {
                rbGST.setChecked(true);
                etGSTNumber.setHint("Enter GST Number here...");
                if (prefillGst != null && !prefillGst.isEmpty())
                    etGSTNumber.setText(prefillGst);
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Click listeners
    // ══════════════════════════════════════════════════════════════════════════
    private void setupClickListeners() {
        toolbarBack.setOnClickListener(v -> onBackPressed());
        flUploadImage.setOnClickListener(v -> showImagePickerDialog());

        rgIdentity.setOnCheckedChangeListener((group, checkedId) ->
                etGSTNumber.setHint(checkedId == R.id.rbPAN
                        ? "Enter PAN Number here..."
                        : "Enter GST Number here..."));

        btnSaveContinue.setOnClickListener(v -> validateAndSave());
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Validate → register-insert step 2
    // ══════════════════════════════════════════════════════════════════════════
    private void validateAndSave() {

        // ── 1. Business type ──────────────────────────────────────────────────
        //   Retailer = "1" | Wholesaler = "2" | Distributor = "3"
        String businessTypeCode = getBusinessTypeCode();
        if (businessTypeCode.isEmpty()) {
            Toast.makeText(this, "Please select a business type",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // ── 2. PAN / GST ──────────────────────────────────────────────────────
        boolean isPAN      = rbPAN.isChecked();
        String  verifyType = isPAN ? "1" : "2";    // 1=PAN, 2=GST
        String  idNumber   = etGSTNumber.getText().toString().trim();

        if (idNumber.isEmpty()) {
            etGSTNumber.setError("Please enter " + (isPAN ? "PAN" : "GST") + " number");
            etGSTNumber.requestFocus();
            return;
        }

        // ── 3. Shop image ─────────────────────────────────────────────────────
        if (!imageSelected) {
            flUploadImage.setBackgroundResource(R.drawable.bg_upload_dashed_error);
            tvImageError.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Please upload a shop image",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String pan = isPAN ? idNumber : "";
        String gst = isPAN ? "" : idNumber;

        Log.d(TAG, "Saving → businessType=" + businessTypeCode
                + " verifyType=" + verifyType
                + " pan=" + pan + " gst=" + gst);

        showLoader("Saving business details…");

        RegisterInsertRequest req = RegisterInsertRequest.forBusiness(
                mobileNumber, businessTypeCode, verifyType, pan, gst, imageBase64);

        apiService.registerInsert(req).enqueue(new Callback<RegisterInsertResponse>() {

            @Override
            public void onResponse(Call<RegisterInsertResponse> call,
                                   Response<RegisterInsertResponse> res) {
                hideLoader();

                if (!res.isSuccessful() || res.body() == null) {
                    Toast.makeText(BusinessDetailsActivity.this,
                            "Failed to save. Try again.", Toast.LENGTH_SHORT).show();
                    return;
                }

                RegisterInsertResponse body = res.body();
                Log.d(TAG, "step2 → status=" + body.getNStatus()
                        + " msg=" + body.getCMessage());

                if (body.getNStatus() != 1) {
                    Toast.makeText(BusinessDetailsActivity.this,
                            body.getCMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }

                // ✅ Success → go to DeliveryAddressActivity
                Intent i = new Intent(BusinessDetailsActivity.this,
                        DeliveryAddressActivity.class);
                i.putExtra("fullName",     fullName);
                i.putExtra("mobileNumber", mobileNumber);
                i.putExtra("email",        email);
                i.putExtra("businessType", businessTypeCode);
                i.putExtra("identityType", verifyType);
                i.putExtra("gstNumber",    gst.isEmpty() ? pan : gst);
                i.putExtra("panNumber",    pan);
                startActivity(i);
                finish();
            }

            @Override
            public void onFailure(Call<RegisterInsertResponse> call, Throwable t) {
                hideLoader();
                Log.e(TAG, "Network error: " + t.getMessage());
                Toast.makeText(BusinessDetailsActivity.this,
                        "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Maps selected radio button → API code.
     *   Retailer    → "1"
     *   Wholesaler  → "2"
     *   Distributor → "3"
     *
     * We check each button individually because the XML has them inside
     * separate LinearLayouts, not a single RadioGroup.
     */
    private String getBusinessTypeCode() {
        if (rbRetailer.isChecked())    return "1";
        if (rbWholesaler.isChecked())  return "2";
        if (rbDistributor.isChecked()) return "3";
        return "";
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Image picker dialog (bottom sheet)
    // ══════════════════════════════════════════════════════════════════════════
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
            dialog.dismiss(); openCamera();
        });
        dialog.findViewById(R.id.ll_gallery).setOnClickListener(v -> {
            dialog.dismiss(); openGallery();
        });
        dialog.findViewById(R.id.tv_cancel).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Camera
    // ══════════════════════════════════════════════════════════════════════════
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
        if (intent.resolveActivity(getPackageManager()) == null) {
            Toast.makeText(this, "No camera app found", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            File photoFile = createImageFile();
            cameraImageUri = FileProvider.getUriForFile(this,
                    getApplicationContext().getPackageName() + ".fileprovider",
                    photoFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            cameraLauncher.launch(intent);
        } catch (IOException e) {
            Toast.makeText(this, "Unable to create image file", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        String stamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(new Date());
        return File.createTempFile("SHOP_" + stamp, ".jpg", getExternalFilesDir(null));
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Gallery
    // ══════════════════════════════════════════════════════════════════════════
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
        launchGallery();
    }

    private void launchGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Show image preview + encode Base64
    // ══════════════════════════════════════════════════════════════════════════
    private void showImagePreview(Uri uri) {
        try {
            InputStream stream = getContentResolver().openInputStream(uri);
            if (stream == null) {
                Toast.makeText(this, "Could not open image", Toast.LENGTH_SHORT).show();
                return;
            }

            Bitmap bitmap = BitmapFactory.decodeStream(stream);
            stream.close();

            if (bitmap == null) {
                Toast.makeText(this, "Failed to decode image", Toast.LENGTH_SHORT).show();
                return;
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            imageBase64 = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP);

            ivShopPreview.setImageBitmap(bitmap);
            ivShopPreview.setVisibility(View.VISIBLE);
            llUploadPlaceholder.setVisibility(View.GONE);
            llEditOverlay.setVisibility(View.VISIBLE);
            flUploadImage.setBackgroundResource(R.drawable.bg_upload_dashed_success);
            tvImageError.setVisibility(View.GONE);
            imageSelected = true;

        } catch (IOException e) {
            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Permission result
    // ══════════════════════════════════════════════════════════════════════════
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean granted = grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED;

        if      (requestCode == REQ_CAMERA  && granted) launchCamera();
        else if (requestCode == REQ_GALLERY && granted) launchGallery();
        else Toast.makeText(this,
                    requestCode == REQ_CAMERA
                            ? "Camera permission denied"
                            : "Gallery permission denied",
                    Toast.LENGTH_SHORT).show();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Loader & status bar
    // ══════════════════════════════════════════════════════════════════════════
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
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void setupStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            getWindow().setStatusBarColor(
                    getResources().getColor(R.color.red_primary, getTheme()));
            WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView())
                    .setAppearanceLightStatusBars(false);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //noinspection deprecation
            getWindow().setStatusBarColor(
                    getResources().getColor(R.color.red_primary));
        }
    }

    private String safe(String s) {
        return (s == null) ? "" : s.trim();
    }
}