package com.example.myapplication.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import com.example.myapplication.R;
import com.example.myapplication.model.RefreshTokenRequest;
import com.example.myapplication.model.RefreshTokenResponse;
import com.example.myapplication.model.VerifyOtpRequest;
import com.example.myapplication.model.VerifyOtpResponse;
import com.example.myapplication.network.ApiService;
import com.example.myapplication.network.RetrofitClient;
import com.example.myapplication.utils.TokenManager;
import com.google.android.material.button.MaterialButton;
import com.chaos.view.PinView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Otp_Screen extends AppCompatActivity {

    private MaterialButton btn_submit;
    private PinView pinview;
    private ProgressDialog progressDialog;

    private ApiService apiService;
    private TokenManager tokenManager;

    private String mobile;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.otp_screen);

        btn_submit = findViewById(R.id.btn_submit);
        pinview    = findViewById(R.id.pinview);

        apiService    = RetrofitClient.getClient(this);
        tokenManager  = new TokenManager(this);


        mobile = getIntent().getStringExtra("mobile");
        if (mobile == null) mobile = "";

        setupLoader();
        setupStatusBar();

        btn_submit.setOnClickListener(v -> {

            String otp = pinview.getText() != null
                    ? pinview.getText().toString().trim() : "";

            if (otp.length() != 6) {
                Toast.makeText(this, "Enter a valid 6-digit OTP",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            showLoader("Verifying OTP...");
            verifyOtp(mobile, otp);
        });
    }


    private void verifyOtp(String mobile, String otp) {

        VerifyOtpRequest request = new VerifyOtpRequest(mobile, otp);

        apiService.verifyOtp(request).enqueue(new Callback<VerifyOtpResponse>() {
            @Override
            public void onResponse(Call<VerifyOtpResponse> call,
                                   Response<VerifyOtpResponse> response) {
                hideLoader();

                if (response.isSuccessful() && response.body() != null) {

                    if (response.body().getN_status() == 1) {

                        String token  = response.body().getJ_data().get(0).getJ_token();
                        String access = response.body().getJ_data().get(0).getJ_access();

                        tokenManager.saveToken(token);
                        tokenManager.saveAccess(access);

                        // Refresh token silently in background
                        refreshToken();

                        Toast.makeText(Otp_Screen.this,
                                "OTP Verified", Toast.LENGTH_SHORT).show();

                        startActivity(new Intent(Otp_Screen.this,
                                SetupStepsActivity.class));
                        finish();

                    } else {
                        Toast.makeText(Otp_Screen.this,
                                response.body().getC_message(),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(Otp_Screen.this,
                            "Verification failed. Try again.",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<VerifyOtpResponse> call, Throwable t) {
                hideLoader();
                Toast.makeText(Otp_Screen.this,
                        "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ── Refresh Token API (silent, no loader) ─────────────────────────────────

    private void refreshToken() {

        String access = tokenManager.getAccess();
        RefreshTokenRequest request = new RefreshTokenRequest(access);

        apiService.refreshToken(request).enqueue(new Callback<RefreshTokenResponse>() {
            @Override
            public void onResponse(Call<RefreshTokenResponse> call,
                                   Response<RefreshTokenResponse> response) {

                if (response.isSuccessful() && response.body() != null) {
                    tokenManager.saveToken(
                            response.body().getJ_data().get(0).getJ_token());
                    tokenManager.saveAccess(
                            response.body().getJ_data().get(0).getJ_access());
                }
            }

            @Override
            public void onFailure(Call<RefreshTokenResponse> call, Throwable t) {
                // Silent failure — token refresh is background task
            }
        });
    }

    // ── Loader ────────────────────────────────────────────────────────────────

    private void setupLoader() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    }

    private void showLoader(String message) {
        if (progressDialog != null && !progressDialog.isShowing()) {
            progressDialog.setMessage(message);
            progressDialog.show();
        }
    }

    private void hideLoader() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    // ── Status Bar ────────────────────────────────────────────────────────────

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