package com.example.myapplication.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import com.example.myapplication.R;

public class GstVerifyScreen extends AppCompatActivity {

    private static final String TAG = "GstVerifyScreen";

    private TextView  tvGstNumber;
    private Button    btnContinue;
    private ImageView toolbarBack;

    private static final String GST_REGEX =
            "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$";
    private static final String PAN_REGEX =
            "^[A-Z]{5}[0-9]{4}[A-Z]{1}$";

    // ── Data ───────────────────────────────────────────────────────────────────
    private String  fullName     = "";
    private String  mobileNumber = "";
    private String  email        = "";
    private String  gstNumber    = "";
    private String  identityType = ""; // "PAN" or "GST"
    private boolean isValid      = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gst_verify);

        setupStatusBar();
        initViews();
        readIntentData();
        displayData();
        setupClickListeners();
    }



    private void initViews() {
        tvGstNumber = findViewById(R.id.tv_gst_number);
        btnContinue = findViewById(R.id.btn_back_to_setup);
        toolbarBack = findViewById(R.id.toolbarBack);
    }


    private void readIntentData() {
        fullName     = safe(getIntent().getStringExtra("fullName"));
        mobileNumber = safe(getIntent().getStringExtra("mobileNumber"));
        email        = safe(getIntent().getStringExtra("email"));
        gstNumber    = safe(getIntent().getStringExtra("gstNumber"));
        identityType = safe(getIntent().getStringExtra("identityType"));

        isValid = validateNumber(gstNumber, identityType);
    }

    // ─────────────────────────────────────────────
    // Display
    // ─────────────────────────────────────────────

    private void displayData() {
        boolean isPAN = "PAN".equals(identityType);
        String  label = isPAN ? "PAN No" : "GST No";

        tvGstNumber.setText(label + "\n" + gstNumber);


        btnContinue.setText(isValid ? "Continue" : "Re-enter Details");

        if (!isValid) {
            showInvalidAlert();
        }
    }



    private void setupClickListeners() {

        toolbarBack.setOnClickListener(v -> goBackToBusiness());

        btnContinue.setOnClickListener(v -> {
            if (isValid) {
                goToDeliveryAddress();
            } else {
                showInvalidAlert();
            }
        });
    }



    private boolean validateNumber(String number, String type) {
        if (number == null || number.trim().isEmpty()) return false;
        String trimmed = number.trim().toUpperCase();
        return "PAN".equals(type)
                ? trimmed.matches(PAN_REGEX)
                : trimmed.matches(GST_REGEX);
    }



    private void showInvalidAlert() {
        boolean isPAN   = "PAN".equals(identityType);
        String  type    = isPAN ? "PAN" : "GST";
        String  message = isPAN
                ? "The PAN number you entered is invalid.\n\n"
                + "Valid format: ABCDE1234F\n"
                + "(5 letters · 4 digits · 1 letter)"
                : "The GST number you entered is invalid.\n\n"
                + "Valid format: 29AAACC1206D2ZB\n"
                + "(15-character alphanumeric)";

        new AlertDialog.Builder(this)
                .setTitle("Invalid " + type + " Number")
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Re-enter", (dialog, which) -> {
                    dialog.dismiss();
                    goBackToBusiness();
                })
                .show();
    }



    private void goToDeliveryAddress() {
        Intent i = new Intent(this, DeliveryAddressActivity.class);
        i.putExtra("fullName",     fullName);
        i.putExtra("mobileNumber", mobileNumber);
        i.putExtra("email",        email);
        startActivity(i);
        finish();
    }

    private void goBackToBusiness() {
        Intent i = new Intent(this, BusinessDetailsActivity.class);
        i.putExtra("fullName",     fullName);
        i.putExtra("mobileNumber", mobileNumber);
        i.putExtra("email",        email);
        startActivity(i);
        finish();
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