package com.example.myapplication.activity;

import android.Manifest;
import android.app.Dialog;
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
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BusinessDetailsActivity extends AppCompatActivity {

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

    // ── Permission codes ───────────────────────────────────────────────────────
    private static final int REQ_CAMERA  = 101;
    private static final int REQ_GALLERY = 102;

    // ── Activity Result: Camera ────────────────────────────────────────────────
    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && cameraImageUri != null) {
                    showImagePreview(cameraImageUri);
                }
            });

    // ── Activity Result: Gallery ───────────────────────────────────────────────
    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK
                        && result.getData() != null
                        && result.getData().getData() != null) {
                    showImagePreview(result.getData().getData());
                }
            });

    // ══════════════════════════════════════════════════════════════════════════
    //  Lifecycle
    // ══════════════════════════════════════════════════════════════════════════

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_details);

        setupStatusBar();
        getIntentData();
        initializeViews();
        setupClickListeners();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Setup
    // ══════════════════════════════════════════════════════════════════════════

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

    private void getIntentData() {
        fullName     = getIntent().getStringExtra("fullName");
        mobileNumber = getIntent().getStringExtra("mobileNumber");
        email        = getIntent().getStringExtra("email");
    }

    private void initializeViews() {
        rbRetailer           = findViewById(R.id.rbRetailer);
        rbWholesaler         = findViewById(R.id.rbWholesaler);
        rbDistributor        = findViewById(R.id.rbDistributor);
        rgIdentity           = findViewById(R.id.rgIdentity);
        rbPAN                = findViewById(R.id.rbPAN);
        rbGST                = findViewById(R.id.rbGST);
        etGSTNumber          = findViewById(R.id.etGSTNumber);
        flUploadImage        = findViewById(R.id.flUploadImage);
        ivShopPreview        = findViewById(R.id.iv_shop_preview);
        llUploadPlaceholder  = findViewById(R.id.ll_upload_placeholder);
        llEditOverlay        = findViewById(R.id.ll_edit_overlay);
        tvImageError         = findViewById(R.id.tv_image_error);
        btnSaveContinue      = findViewById(R.id.btnSaveContinue);
        toolbarBack          = findViewById(R.id.toolbarBack);
    }

    private void setupClickListeners() {
        toolbarBack.setOnClickListener(v -> onBackPressed());

        flUploadImage.setOnClickListener(v -> showImagePickerDialog());

        rgIdentity.setOnCheckedChangeListener((group, checkedId) ->
                etGSTNumber.setHint(checkedId == R.id.rbPAN
                        ? "Enter PAN Number here..."
                        : "Enter GST Number here..."));

        btnSaveContinue.setOnClickListener(v -> validateAndContinue());
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Custom bottom-sheet image picker dialog
    // ══════════════════════════════════════════════════════════════════════════

    private void showImagePickerDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_image_picker);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.BOTTOM);
            window.getAttributes().windowAnimations = R.style.BottomDialogAnimation;
        }

        // Camera
        dialog.findViewById(R.id.ll_camera).setOnClickListener(v -> {
            dialog.dismiss();
            openCamera();
        });

        // Gallery
        dialog.findViewById(R.id.ll_gallery).setOnClickListener(v -> {
            dialog.dismiss();
            openGallery();
        });

        // Cancel
        dialog.findViewById(R.id.tv_cancel).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Camera
    // ══════════════════════════════════════════════════════════════════════════

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this, new String[]{Manifest.permission.CAMERA}, REQ_CAMERA);
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

            // ✅ Use getApplicationContext().getPackageName() — never hard-code the authority
            cameraImageUri = FileProvider.getUriForFile(
                    this,
                    getApplicationContext().getPackageName() + ".fileprovider",
                    photoFile);

            intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            cameraLauncher.launch(intent);

        } catch (IOException e) {
            Toast.makeText(this, "Unable to create image file", Toast.LENGTH_SHORT).show();
        }
    }

    /** Creates a uniquely-named temp .jpg file in getExternalFilesDir() */
    private File createImageFile() throws IOException {
        String stamp     = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(new Date());
        File   storageDir = getExternalFilesDir(null);
        return File.createTempFile("SHOP_" + stamp, ".jpg", storageDir);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Gallery
    // ══════════════════════════════════════════════════════════════════════════

    private void openGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ → READ_MEDIA_IMAGES
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                        REQ_GALLERY);
                return;
            }
        } else {
            // Android ≤ 12 → READ_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
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
    //  Show image preview inside upload box
    // ══════════════════════════════════════════════════════════════════════════

    private void showImagePreview(Uri uri) {
        try {
            // ✅ BitmapFactory + InputStream works for BOTH camera URI and gallery URI
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

            // Show preview image
            ivShopPreview.setImageBitmap(bitmap);
            ivShopPreview.setVisibility(View.VISIBLE);

            // Hide placeholder, show "Change" overlay
            llUploadPlaceholder.setVisibility(View.GONE);
            llEditOverlay.setVisibility(View.VISIBLE);

            // Green border = success
            flUploadImage.setBackgroundResource(R.drawable.bg_upload_dashed_success);

            // Clear error state
            tvImageError.setVisibility(View.GONE);

            imageSelected = true;

        } catch (IOException e) {
            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Permission result
    // ══════════════════════════════════════════════════════════════════════════

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        boolean granted = grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED;

        if (requestCode == REQ_CAMERA) {
            if (granted) launchCamera();
            else Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();

        } else if (requestCode == REQ_GALLERY) {
            if (granted) launchGallery();
            else Toast.makeText(this, "Gallery permission denied", Toast.LENGTH_SHORT).show();
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Validation & navigation
    // ══════════════════════════════════════════════════════════════════════════

    private void validateAndContinue() {

        // 1. Business type
        String businessType = "";
        if      (rbRetailer.isChecked())    businessType = "Retailer";
        else if (rbWholesaler.isChecked())  businessType = "Wholesaler";
        else if (rbDistributor.isChecked()) businessType = "Distributor";

        if (businessType.isEmpty()) {
            Toast.makeText(this, "Please select a business type", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. GST / PAN number
        String identityType = rbGST.isChecked() ? "GST" : "PAN";
        String idNumber     = etGSTNumber.getText().toString().trim();

        if (idNumber.isEmpty()) {
            Toast.makeText(this,
                    "Please enter your " + identityType + " number",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // 3. Mandatory shop image
        if (!imageSelected) {
            flUploadImage.setBackgroundResource(R.drawable.bg_upload_dashed_error);
            tvImageError.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Please upload a shop image", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ All valid → go to GstVerifyScreen
        Intent intent = new Intent(this, GstVerifyScreen.class);
        intent.putExtra("fullName",     fullName);
        intent.putExtra("mobileNumber", mobileNumber);
        intent.putExtra("email",        email);
        intent.putExtra("businessType", businessType);
        intent.putExtra("gstNumber",    idNumber);
        intent.putExtra("identityType", identityType);
        startActivity(intent);
    }
}