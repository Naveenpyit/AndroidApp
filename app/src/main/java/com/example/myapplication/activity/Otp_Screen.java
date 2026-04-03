package com.example.myapplication.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
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
    private PinView        pinview;
    private ProgressDialog progressDialog;
    private ApiService     apiService;
    private TokenManager   tokenManager;
    private String         mobile = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.otp_screen);

        btn_submit   = findViewById(R.id.btn_submit);
        pinview      = findViewById(R.id.pinview);
        apiService   = RetrofitClient.getClient(this);
        tokenManager = new TokenManager(this);

        mobile = getIntent().getStringExtra("mobile");
        if (mobile == null) mobile = "";

        setupLoader();
        setupStatusBar();

        btn_submit.setOnClickListener(v -> {
            String otp = pinview.getText() != null
                    ? pinview.getText().toString().trim() : "";

            if (otp.length() != 6) {
                Toast.makeText(this, "Enter valid 6-digit OTP",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            showLoader("Verifying OTP...");
            verifyOtp(mobile, otp);
        });
    }

    // ─────────────────────────────────────────────
    // Step 1: Verify OTP
    // ─────────────────────────────────────────────

    private void verifyOtp(String mobile, String otp) {
        apiService.verifyOtp(new VerifyOtpRequest(mobile, otp))
                .enqueue(new Callback<VerifyOtpResponse>() {

                    @Override
                    public void onResponse(Call<VerifyOtpResponse> call,
                                           Response<VerifyOtpResponse> res) {
                        hideLoader();

                        if (!res.isSuccessful() || res.body() == null) {
                            Toast.makeText(Otp_Screen.this,
                                    "Verification failed. Try again.",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        VerifyOtpResponse body = res.body();
                        Log.d(TAG, "n_status=" + body.getNStatus()
                                + " | n_process_type=" + body.getNProcessType());

                        if (body.getNStatus() != 1) {
                            Toast.makeText(Otp_Screen.this,
                                    body.getCMessage(), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Save tokens
                        if (body.getJData() != null && !body.getJData().isEmpty()) {
                            VerifyOtpResponse.TokenData token = body.getJData().get(0);
                            tokenManager.saveToken(token.getJToken());
                            tokenManager.saveAccess(token.getJAccess());

                            if (token.getJLogin() != null
                                    && !token.getJLogin().isEmpty()) {
                                String userId = token.getJLogin().get(0).getNUserId();
                                tokenManager.saveUserId(userId);
                                Log.d(TAG, "Saved userId=" + userId);
                            }
                        }

                        getSharedPreferences("app_prefs", MODE_PRIVATE)
                                .edit()
                                .putBoolean("is_logged_in", true)
                                .apply();

                        refreshTokenSilently();

                        int processType = body.getNProcessType();
                        Log.d(TAG, "processType=" + processType);

                        switch (processType) {

                            case 1:
                                // ✅ Successfully registered → Dashboard
                                goToMain();
                                break;

                            case 2:
                                // ✅ GST/PAN verification pending → Popup
                                showVerificationPendingAlert();
                                break;

                            case 3:
                            default:
                                // ✅ Not registered → fetch details → route
                                showLoader("Loading...");
                                fetchRegisterDetails();
                                break;
                        }
                    }

                    @Override
                    public void onFailure(Call<VerifyOtpResponse> call, Throwable t) {
                        hideLoader();
                        Log.e(TAG, "verifyOtp failure: " + t.getMessage());
                        Toast.makeText(Otp_Screen.this,
                                "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ─────────────────────────────────────────────
    // Step 2: Fetch register-details (process_type=3)
    // ─────────────────────────────────────────────

    private void fetchRegisterDetails() {
        apiService.getRegisterDetails(new RegisterDetailsRequest(mobile))
                .enqueue(new Callback<RegisterDetailsResponse>() {

                    @Override
                    public void onResponse(Call<RegisterDetailsResponse> call,
                                           Response<RegisterDetailsResponse> res) {
                        hideLoader();

                        // API fail அல்லது data இல்லை → OwnerDetails (new user)
                        if (!res.isSuccessful() || res.body() == null
                                || res.body().getNStatus() != 1
                                || res.body().getJData() == null) {
                            Log.d(TAG, "register-details empty → new user");
                            goToOwnerDetails("", mobile, "");
                            return;
                        }

                        RegisterDetailsResponse body = res.body();
                        int step = 0;
                        try {
                           // step = Integer.parseInt(body.getNStep());
                             step = body.getNStep();
                        } catch (Exception ignored) {}

                        Log.d(TAG, "register-details step=" + step);
                        routeByStep(step, body);
                    }

                    @Override
                    public void onFailure(Call<RegisterDetailsResponse> call,
                                          Throwable t) {
                        hideLoader();
                        Log.e(TAG, "register-details failure: " + t.getMessage());
                        goToOwnerDetails("", mobile, "");
                    }
                });
    }

    // ─────────────────────────────────────────────
    // Step 3: Route by step
    // ─────────────────────────────────────────────

    private void routeByStep(int step, RegisterDetailsResponse body) {
        RegisterDetailsResponse.JData data = body.getJData();

        switch (step) {

            case 1:
                // Owner not filled
                goToOwnerDetails("", mobile, "");
                break;

            case 2:
                // Owner done → Business pending (prefill owner data)
                String name  = (data != null && data.getOwnerDetails() != null)
                        ? safe(data.getOwnerDetails().getCName())  : "";
                String email = (data != null && data.getOwnerDetails() != null)
                        ? safe(data.getOwnerDetails().getCEmail()) : "";
                goToBusinessDetails(name, mobile, email, data);
                break;

            case 3:
                // Business done → Delivery address pending (prefill all)
                goToDeliveryAddress(data);
                break;

            default:
                // All done → Dashboard
                goToMain();
                break;
        }
    }

    // ─────────────────────────────────────────────
    // Navigation
    // ─────────────────────────────────────────────

    private void goToMain() {
        getSharedPreferences("app_prefs", MODE_PRIVATE)
                .edit()
                .putBoolean("is_logged_in", true)
                .putString("last_screen", "Main")
                .apply();

        Intent i = new Intent(Otp_Screen.this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    private void goToOwnerDetails(String name, String mob, String email) {
        Intent i = new Intent(Otp_Screen.this, OwnerDetailsActivity.class);
        i.putExtra("mobile",   mob.isEmpty() ? mobile : mob);
        i.putExtra("fullName", name);
        i.putExtra("email",    email);
        startActivity(i);
        finish();
    }

    private void goToBusinessDetails(String name, String mob, String email,
                                     RegisterDetailsResponse.JData data) {
        Intent i = new Intent(Otp_Screen.this, BusinessDetailsActivity.class);
        i.putExtra("fullName",     name);
        i.putExtra("mobileNumber", mob.isEmpty() ? mobile : mob);
        i.putExtra("email",        email);

        if (data != null && data.getBusinessDetails() != null) {
            i.putExtra("prefillBType",      data.getBusinessDetails().getNType());
            i.putExtra("prefillVerifyType", data.getBusinessDetails().getNVerifyType());
            i.putExtra("prefillPan",        data.getBusinessDetails().getCPan());
            i.putExtra("prefillGst",        data.getBusinessDetails().getCGst());
        }
        startActivity(i);
        finish();
    }

    private void goToDeliveryAddress(RegisterDetailsResponse.JData data) {
        Intent i = new Intent(Otp_Screen.this, DeliveryAddressActivity.class);

        if (data != null && data.getOwnerDetails() != null) {
            i.putExtra("fullName",     safe(data.getOwnerDetails().getCName()));
            i.putExtra("mobileNumber", safe(data.getOwnerDetails().getNMobile()));
            i.putExtra("email",        safe(data.getOwnerDetails().getCEmail()));
        }
        if (data != null && data.getAddressDetails() != null) {
            i.putExtra("prefillStateId",  safe(data.getAddressDetails().getNState()));
            i.putExtra("prefillCityId",   safe(data.getAddressDetails().getNCity()));
            i.putExtra("prefillAddress",  safe(data.getAddressDetails().getCAddress()));
            i.putExtra("prefillPin",      safe(data.getAddressDetails().getNPincode()));
            i.putExtra("prefillLat",      safe(data.getAddressDetails().getCLatitude()));
            i.putExtra("prefillLng",      safe(data.getAddressDetails().getCLongitude()));
            i.putExtra("prefillAddrType", "1");
        }
        startActivity(i);
        finish();
    }


    private void showVerificationPendingAlert() {
        new AlertDialog.Builder(this)
                .setTitle("Verification Pending")
                .setMessage("Your account is under verification.\nPlease wait for approval.")
                .setCancelable(false)
                .setPositiveButton("OK", (d, w) -> {
                    d.dismiss();
                    Intent i = new Intent(Otp_Screen.this, LoginPage.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                    finish();
                })
                .show();
    }

    // ─────────────────────────────────────────────
    // Silent Token Refresh
    // ─────────────────────────────────────────────

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
                            tokenManager.saveToken(
                                    res.body().getJ_data().get(0).getJ_token());
                            tokenManager.saveAccess(
                                    res.body().getJ_data().get(0).getJ_access());
                            Log.d(TAG, "Token refreshed silently");
                        }
                    }
                    @Override
                    public void onFailure(Call<RefreshTokenResponse> call, Throwable t) {
                        Log.e(TAG, "Token refresh failed: " + t.getMessage());
                    }
                });
    }

    // ─────────────────────────────────────────────
    // Loader & Status Bar
    // ─────────────────────────────────────────────

    private void setupLoader() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
    }

    private void showLoader(String msg) {
        progressDialog.setMessage(msg);
        if (!progressDialog.isShowing()) progressDialog.show();
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
            getWindow().setStatusBarColor(
                    getResources().getColor(R.color.red_primary));
        }
    }

    private String safe(String s) {
        return (s == null) ? "" : s.trim();
    }
}