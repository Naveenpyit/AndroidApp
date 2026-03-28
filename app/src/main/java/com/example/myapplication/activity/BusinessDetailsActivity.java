package com.example.myapplication.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import com.example.myapplication.R;
import com.google.android.material.button.MaterialButton;

public class BusinessDetailsActivity extends AppCompatActivity {

    private RadioGroup rgBusinessType, rgIdentity;
    private RadioButton rbRetailer, rbWholesaler, rbDistributor;
    private RadioButton rbPAN, rbGST;
    private EditText etGSTNumber;
    private FrameLayout flUploadImage;
    private MaterialButton btnSaveContinue;
    private ImageView toolbarBack;

    private String fullName, mobileNumber, email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_details);
        setupStatusBar();
        getIntentData();
        initializeViews();
        setupClickListeners();
    }

    private void setupStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            getWindow().setStatusBarColor(
                    getResources().getColor(R.color.red_primary, getTheme()));
            WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView())
                    .setAppearanceLightStatusBars(false);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
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
        rbRetailer      = findViewById(R.id.rbRetailer);
        rbWholesaler    = findViewById(R.id.rbWholesaler);
        rbDistributor   = findViewById(R.id.rbDistributor);
        rgIdentity      = findViewById(R.id.rgIdentity);
        rbPAN           = findViewById(R.id.rbPAN);
        rbGST           = findViewById(R.id.rbGST);
        etGSTNumber     = findViewById(R.id.etGSTNumber);
        flUploadImage   = findViewById(R.id.flUploadImage);
        btnSaveContinue = findViewById(R.id.btnSaveContinue);
        toolbarBack     = findViewById(R.id.toolbarBack);

        // ✅ Force red background on button programmatically (safe for all API levels)
        btnSaveContinue.setBackgroundResource(R.drawable.bg_button_red);
        btnSaveContinue.setTextColor(getResources().getColor(android.R.color.white));
    }

    private void setupClickListeners() {
        btnSaveContinue.setOnClickListener(v -> validateAndContinue());
        toolbarBack.setOnClickListener(v -> onBackPressed());
        flUploadImage.setOnClickListener(v -> uploadImage());

        // ── Update hint text based on PAN/GST selection ──────────────────────
        rgIdentity.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbPAN) {
                etGSTNumber.setHint("Enter PAN Number here...");
            } else {
                etGSTNumber.setHint("Enter GST Number here...");
            }
        });
    }

    private void validateAndContinue() {

        // Validate business type
        String businessType = "";
        if (rbRetailer.isChecked())    businessType = "Retailer";
        else if (rbWholesaler.isChecked()) businessType = "Wholesaler";
        else if (rbDistributor.isChecked()) businessType = "Distributor";

        if (businessType.isEmpty()) {
            Toast.makeText(this, "Please select business type", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate GST/PAN number
        String identificationNumber = etGSTNumber.getText().toString().trim();
        String identityType = rbGST.isChecked() ? "GST" : "PAN";

        if (identificationNumber.isEmpty()) {
            Toast.makeText(this, "Please enter " + identityType + " number",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ Navigate to GstVerifyActivity (verification pending screen)
        Intent intent = new Intent(this, GstVerifyScreen.class);
        intent.putExtra("fullName",     fullName);
        intent.putExtra("mobileNumber", mobileNumber);
        intent.putExtra("email",        email);
        intent.putExtra("businessType", businessType);
        intent.putExtra("gstNumber",    identificationNumber);
        intent.putExtra("identityType", identityType); // "GST" or "PAN"
        startActivity(intent);
    }

    private void uploadImage() {
        Toast.makeText(this, "Upload functionality to be implemented",
                Toast.LENGTH_SHORT).show();
    }
}