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

    private TextView tvGstNumber;
    private Button btnBackToSetup;
    private ImageView toolbarBack;

    // GST format: 2-digit state code + 10-char PAN + 1 digit + Z + 1 alphanumeric
    private static final String GST_REGEX =
            "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$";

    // PAN format: 5 letters + 4 digits + 1 letter
    private static final String PAN_REGEX =
            "^[A-Z]{5}[0-9]{4}[A-Z]{1}$";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gst_verify);

        tvGstNumber    = findViewById(R.id.tv_gst_number);
        btnBackToSetup = findViewById(R.id.btn_back_to_setup);
        toolbarBack    = findViewById(R.id.toolbarBack);

        String gstNumber    = getIntent().getStringExtra("gstNumber");
        String identityType = getIntent().getStringExtra("identityType"); // "GST" or "PAN"

        // Show label + value
        String label = (identityType != null && identityType.equals("PAN")) ? "PAN No" : "GST No";
        tvGstNumber.setText(label + "\n" + (gstNumber != null ? gstNumber : ""));

        setupStatusBar();

        // ── Validate on arrival ───────────────────────────────────────────────
        boolean isValid = validateNumber(gstNumber, identityType);

        if (!isValid) {
            // Show invalid alert → OK goes back to BusinessDetailsActivity
            showInvalidAlert(identityType);
        }

        // ── Toolbar back ──────────────────────────────────────────────────────
        if (toolbarBack != null) {
            toolbarBack.setOnClickListener(v -> finish());
        }

        // ── Bottom button ─────────────────────────────────────────────────────
        btnBackToSetup.setOnClickListener(v -> {
            if (isValid) {
                // Verified → go to DeliveryAddressActivity
                Intent intent = new Intent(GstVerifyScreen.this, DeliveryAddressActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            } else {
                // Not valid → remind user with alert
                showInvalidAlert(identityType);
            }
        });
    }

    // ── Validation ────────────────────────────────────────────────────────────

    private boolean validateNumber(String number, String identityType) {
        if (number == null || number.trim().isEmpty()) return false;

        String trimmed = number.trim().toUpperCase();

        if ("PAN".equals(identityType)) {
            return trimmed.matches(PAN_REGEX);
        } else {
            // Default: GST
            return trimmed.matches(GST_REGEX);
        }
    }

    // ── Alert dialog ──────────────────────────────────────────────────────────

    private void showInvalidAlert(String identityType) {
        String type = ("PAN".equals(identityType)) ? "PAN" : "GST";

        String message;
        if ("PAN".equals(type)) {
            message = "The PAN number you entered is invalid.\n\n"
                    + "Valid format: ABCDE1234F\n"
                    + "(5 letters · 4 digits · 1 letter)";
        } else {
            message = "The GST number you entered is invalid.\n\n"
                    + "Valid format: 29AAACC1206D2ZB\n"
                    + "(15-character alphanumeric)";
        }

        new AlertDialog.Builder(this)
                .setTitle("Invalid " + type + " Number")
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                    // Navigate back to BusinessDetailsActivity
                    Intent intent = new Intent(GstVerifyScreen.this, BusinessDetailsActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                })
                .show();
    }

    // ── Status bar ────────────────────────────────────────────────────────────

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
}