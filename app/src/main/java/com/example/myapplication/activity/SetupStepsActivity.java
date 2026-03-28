package com.example.myapplication.activity;

import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import com.example.myapplication.R;
import com.google.android.material.button.MaterialButton;

public class SetupStepsActivity extends AppCompatActivity {

    private MaterialButton btnContinue;
    private TextView tvSkip;

    LinearLayout business_doc,continue_skip,owner_details,business_det;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_steps);
        business_doc = findViewById(R.id.business_doc);
        continue_skip = findViewById(R.id.continue_skip);
        owner_details = findViewById(R.id.owner_details);
        business_det = findViewById(R.id.business_det);
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
        btnContinue = findViewById(R.id.btnContinue);
        tvSkip = findViewById(R.id.tvSkip);
    }

    private void setupClickListeners() {
        btnContinue.setOnClickListener(v -> {
            Intent intent = new Intent(this, OwnerDetailsActivity.class);
            startActivity(intent);
        });

        tvSkip.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            Toast.makeText(this, "Skip functionality", Toast.LENGTH_SHORT).show();
        });
        owner_details.setOnClickListener(v->{
            Intent intent = new Intent(this, OwnerDetailsActivity.class);
            startActivity(intent);
        });
        business_doc.setOnClickListener(v->{
            continue_skip.setVisibility(VISIBLE);
        });

        business_det.setOnClickListener(v->{
            Intent intent = new Intent(this, BusinessDetailsActivity.class);
            startActivity(intent);
        });
    }
}