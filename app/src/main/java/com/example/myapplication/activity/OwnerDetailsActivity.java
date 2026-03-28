package com.example.myapplication.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import com.example.myapplication.R;
import com.google.android.material.button.MaterialButton;

public class OwnerDetailsActivity extends AppCompatActivity {

    private EditText etFullName, etMobileNumber, etEmail;
    private MaterialButton btnSaveContinue;
    private ImageView toolbarBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_details);
        setupStatusBar();
        initializeViews();
        setupClickListeners();
    }
    private void setupStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            // Android 15+ (API 35+)
            getWindow().setStatusBarColor(getResources().getColor(R.color.red_primary, getTheme()));

            // Set light status bar icons (white icons for dark background)
            WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView())
                    .setAppearanceLightStatusBars(false);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Android 5.0+ (API 21+)
            getWindow().setStatusBarColor(getResources().getColor(R.color.red_primary));
        }
    }
    private void initializeViews() {
        etFullName = findViewById(R.id.etFullName);
        etMobileNumber = findViewById(R.id.etMobileNumber);
        etEmail = findViewById(R.id.etEmail);
        btnSaveContinue = findViewById(R.id.btnSaveContinue);
        toolbarBack = findViewById(R.id.toolbarBack);
    }

    private void setupClickListeners() {
        btnSaveContinue.setOnClickListener(v -> validateAndContinue());
        toolbarBack.setOnClickListener(v -> onBackPressed());
    }

    private void validateAndContinue() {
        String fullName = etFullName.getText().toString().trim();
        String mobileNumber = etMobileNumber.getText().toString().trim();
        String email = etEmail.getText().toString().trim();

        if (fullName.isEmpty()) {
            Toast.makeText(this, "Please enter full name", Toast.LENGTH_SHORT).show();
            etFullName.requestFocus();
            return;
        }

        if (mobileNumber.isEmpty()) {
            Toast.makeText(this, "Please enter mobile number", Toast.LENGTH_SHORT).show();
            etMobileNumber.requestFocus();
            return;
        }

        if (mobileNumber.length() < 10) {
            Toast.makeText(this, "Please enter valid mobile number (10 digits)", Toast.LENGTH_SHORT).show();
            etMobileNumber.requestFocus();
            return;
        }

        // Navigate to Business Details
        Intent intent = new Intent(this, BusinessDetailsActivity.class);
        intent.putExtra("fullName", fullName);
        intent.putExtra("mobileNumber", mobileNumber);
        intent.putExtra("email", email);
        startActivity(intent);
    }
}