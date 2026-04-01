package com.example.myapplication.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import com.example.myapplication.R;
import com.example.myapplication.model.RefreshTokenRequest;
import com.example.myapplication.model.RefreshTokenResponse;
import com.example.myapplication.model.RegisterDetailsRequest;
import com.example.myapplication.model.RegisterDetailsResponse;
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

    private static final String TAG = "Otp_Screen";

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
        pinview = findViewById(R.id.pinview);
        apiService = RetrofitClient.getClient(this);
        tokenManager = new TokenManager(this);

        mobile = getIntent().getStringExtra("mobile");
        if (mobile == null) mobile = "";

        setupLoader();
        setupStatusBar();

        btn_submit.setOnClickListener(v -> {
            String otp = pinview.getText() != null
                    ? pinview.getText().toString().trim() : "";

            if (otp.length() != 6) {
                Toast.makeText(this, "Enter valid 6-digit OTP", Toast.LENGTH_SHORT).show();
                return;
            }

            showLoader("Verifying OTP...");
            verifyOtp(mobile, otp);
        });
    }

    // ✅ VERIFY OTP
    private void verifyOtp(String mobile, String otp) {

        apiService.verifyOtp(new VerifyOtpRequest(mobile, otp))
                .enqueue(new Callback<VerifyOtpResponse>() {

                    @Override
                    public void onResponse(Call<VerifyOtpResponse> call,
                                           Response<VerifyOtpResponse> res) {

                        hideLoader();

                        if (!res.isSuccessful() || res.body() == null
                                || res.body().getNStatus() != 1) {

                            Toast.makeText(Otp_Screen.this,
                                    "Verification failed. Try again.",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // ✅ LOGIN SUCCESS SAVE
                        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
                        prefs.edit()
                                .putBoolean("is_logged_in", true)
                                .putString("last_screen", "SetupSteps")
                                .apply();

                        // ✅ TOKEN SAVE
                        VerifyOtpResponse.TokenData token = res.body().getJData().get(0);
                        tokenManager.saveToken(token.getJToken());
                        tokenManager.saveAccess(token.getJAccess());

                        if (token.getJLogin() != null && !token.getJLogin().isEmpty()) {
                            tokenManager.saveUserId(token.getJLogin().get(0).getNUserId());
                        }

                        refreshTokenSilently();

                        int processType = token.getNProcessType();

                        switch (processType) {

                            case 3:
                                goToMain();
                                break;

                            case 2:
                                showVerificationPendingAlert();
                                break;

                            case 1:
                            default:
                                showLoader("Loading details...");
                                fetchRegisterDetails();
                                break;
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

    // ✅ FETCH REGISTER DETAILS
    private void fetchRegisterDetails() {

        apiService.getRegisterDetails(new RegisterDetailsRequest(mobile))
                .enqueue(new Callback<RegisterDetailsResponse>() {

                    @Override
                    public void onResponse(Call<RegisterDetailsResponse> call,
                                           Response<RegisterDetailsResponse> res) {

                        hideLoader();

                        if (!res.isSuccessful() || res.body() == null) {
                            Toast.makeText(Otp_Screen.this,
                                    "Failed to load details",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        RegisterDetailsResponse body = res.body();

                        if (body.getNStatus() != 1) {
                            Toast.makeText(Otp_Screen.this,
                                    body.getCMessage(), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        routeToScreen(body.getNStep());
                    }

                    @Override
                    public void onFailure(Call<RegisterDetailsResponse> call, Throwable t) {
                        hideLoader();
                        Toast.makeText(Otp_Screen.this,
                                "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ✅ ROUTING
    private void routeToScreen(int step) {

        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);

        if (step == 1) {

            prefs.edit().putString("last_screen", "OwnerDetails").apply();
            startActivity(new Intent(this, OwnerDetailsActivity.class));

        } else if (step == 2) {

            prefs.edit().putString("last_screen", "BusinessDetails").apply();
            startActivity(new Intent(this, BusinessDetailsActivity.class));

        } else if (step == 3) {

            prefs.edit().putString("last_screen", "SetupSteps").apply();
            startActivity(new Intent(this, SetupStepsActivity.class));

        } else {

            prefs.edit().putString("last_screen", "Main").apply();
            startActivity(new Intent(this, MainActivity.class));
        }

        finish();
    }

    // ✅ NAVIGATION
    private void goToMain() {

        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        prefs.edit().putString("last_screen", "Main").apply();

        Intent i = new Intent(this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    private void showVerificationPendingAlert() {
        new AlertDialog.Builder(this)
                .setTitle("Verification Pending")
                .setMessage("Please wait for approval")
                .setCancelable(false)
                .setPositiveButton("OK", (d, w) -> {
                    goToMain();
                })
                .show();
    }

    // ✅ TOKEN REFRESH
    private void refreshTokenSilently() {

        String access = tokenManager.getAccess();
        if (access == null || access.isEmpty()) return;

        apiService.refreshToken(new RefreshTokenRequest(access))
                .enqueue(new Callback<RefreshTokenResponse>() {
                    @Override
                    public void onResponse(Call<RefreshTokenResponse> call,
                                           Response<RefreshTokenResponse> res) {

                        if (res.isSuccessful() && res.body() != null
                                && res.body().getJ_data() != null
                                && !res.body().getJ_data().isEmpty()) {

                            tokenManager.saveToken(res.body().getJ_data().get(0).getJ_token());
                            tokenManager.saveAccess(res.body().getJ_data().get(0).getJ_access());
                        }
                    }

                    @Override
                    public void onFailure(Call<RefreshTokenResponse> call, Throwable t) {
                        Log.e(TAG, "Token refresh failed");
                    }
                });
    }

    // ✅ LOADER
    private void setupLoader() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
    }

    private void showLoader(String msg) {
        progressDialog.setMessage(msg);
        progressDialog.show();
    }

    private void hideLoader() {
        if (progressDialog.isShowing()) progressDialog.dismiss();
    }

    // ✅ STATUS BAR
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