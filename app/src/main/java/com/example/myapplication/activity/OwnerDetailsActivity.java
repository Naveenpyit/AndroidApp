package com.example.myapplication.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import com.example.myapplication.R;
import com.example.myapplication.model.RegisterInsertRequest;
import com.example.myapplication.model.RegisterInsertResponse;
import com.example.myapplication.network.ApiService;
import com.example.myapplication.network.RetrofitClient;
import com.google.android.material.button.MaterialButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OwnerDetailsActivity extends AppCompatActivity {

    private static final String TAG = "OwnerDetailsActivity";

    private EditText       etFullName, etMobileNumber, etEmail;
    private MaterialButton btnSaveContinue;
    private ImageView      toolbarBack;

    private ApiService     apiService;
    private ProgressDialog progressDialog;
    private String         mobile = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_details);

        setupStatusBar();
        apiService = RetrofitClient.getClient(this);
        setupLoader();

        mobile = getIntent().getStringExtra("mobile");
        if (mobile == null) mobile = "";

        initializeViews();
        setupClickListeners();

        // ✅ Mobile prefill (always, read-only)
        if (!mobile.isEmpty()) {
            etMobileNumber.setText(mobile);
            etMobileNumber.setEnabled(false);
        }

        // ✅ Returning user → prefill name & email (editable)
        boolean isPrefilled = getIntent().getBooleanExtra("isPrefilled", false);
        if (isPrefilled) {
            String prefillName  = getIntent().getStringExtra("fullName");
            String prefillEmail = getIntent().getStringExtra("email");

            if (prefillName != null && !prefillName.isEmpty())
                etFullName.setText(prefillName);

            if (prefillEmail != null && !prefillEmail.isEmpty())
                etEmail.setText(prefillEmail);
        }
    }

    private void initializeViews() {
        etFullName      = findViewById(R.id.etFullName);
        etMobileNumber  = findViewById(R.id.etMobileNumber);
        etEmail         = findViewById(R.id.etEmail);
        btnSaveContinue = findViewById(R.id.btnSaveContinue);
        toolbarBack     = findViewById(R.id.toolbarBack);
    }

    private void setupClickListeners() {
        toolbarBack.setOnClickListener(v -> onBackPressed());
        btnSaveContinue.setOnClickListener(v -> validateAndSave());
    }

    // ─────────────────────────────────────────────
    // Validate & Save
    // ─────────────────────────────────────────────

    private void validateAndSave() {
        String fullName     = etFullName.getText().toString().trim();
        String mobileNumber = etMobileNumber.getText().toString().trim();
        String email        = etEmail.getText().toString().trim();

        if (fullName.isEmpty()) {
            Toast.makeText(this, "Please enter full name",
                    Toast.LENGTH_SHORT).show();
            etFullName.requestFocus();
            return;
        }

        if (mobileNumber.isEmpty()) {
            Toast.makeText(this, "Please enter mobile number",
                    Toast.LENGTH_SHORT).show();
            etMobileNumber.requestFocus();
            return;
        }

        if (mobileNumber.length() < 10) {
            Toast.makeText(this, "Enter valid 10-digit mobile number",
                    Toast.LENGTH_SHORT).show();
            etMobileNumber.requestFocus();
            return;
        }

        showLoader("Saving details...");
        saveOwnerDetails(fullName, mobileNumber, email);
    }

    private void saveOwnerDetails(String name, String mob, String email) {
        RegisterInsertRequest req = RegisterInsertRequest.forOwner(name, mob, email);

        apiService.registerInsert(req).enqueue(new Callback<RegisterInsertResponse>() {

            @Override
            public void onResponse(Call<RegisterInsertResponse> call,
                                   Response<RegisterInsertResponse> res) {
                hideLoader();

                if (!res.isSuccessful() || res.body() == null) {
                    Toast.makeText(OwnerDetailsActivity.this,
                            "Failed to save. Try again.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                RegisterInsertResponse body = res.body();
                Log.d(TAG, "register-insert step1: status=" + body.getNStatus()
                        + " msg=" + body.getCMessage());

                if (body.getNStatus() != 1) {
                    Toast.makeText(OwnerDetailsActivity.this,
                            body.getCMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }


                Intent i = new Intent(OwnerDetailsActivity.this,
                        BusinessDetailsActivity.class);
                i.putExtra("fullName",     name);
                i.putExtra("mobileNumber", mob);
                i.putExtra("email",        email);
                startActivity(i);
                finish();
            }

            @Override
            public void onFailure(Call<RegisterInsertResponse> call, Throwable t) {
                hideLoader();
                Log.e(TAG, "register-insert failure: " + t.getMessage());
                Toast.makeText(OwnerDetailsActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ─────────────────────────────────────────────
    // Loader & Status Bar
    // ─────────────────────────────────────────────

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
            WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView())
                    .setAppearanceLightStatusBars(false);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //noinspection deprecation
            getWindow().setStatusBarColor(
                    getResources().getColor(R.color.red_primary));
        }
    }
}