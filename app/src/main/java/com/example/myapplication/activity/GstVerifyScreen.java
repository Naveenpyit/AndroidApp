package com.example.myapplication.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import com.example.myapplication.R;

public class GstVerifyScreen extends AppCompatActivity {

    private TextView tvGstNumber;
    private Button btnBackToSetup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gst_verify);

        tvGstNumber   = findViewById(R.id.tv_gst_number);
        btnBackToSetup = findViewById(R.id.btn_back_to_setup);

        // ── Get GST/PAN number passed from BusinessDetailsActivity ────────────
        String gstNumber    = getIntent().getStringExtra("gstNumber");
        String identityType = getIntent().getStringExtra("identityType"); // "GST" or "PAN"

        // Show label + value
        String label = (identityType != null && identityType.equals("PAN")) ? "PAN No" : "GST No";
        tvGstNumber.setText(label + "\n" + (gstNumber != null ? gstNumber : ""));

        setupStatusBar();

        // ── Back to Setup button ──────────────────────────────────────────────
        btnBackToSetup.setOnClickListener(v -> {
            Intent intent = new Intent(GstVerifyScreen.this, DeliveryAddressActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
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
}