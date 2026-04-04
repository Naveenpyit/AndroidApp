package com.example.myapplication.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import com.example.myapplication.R;
import com.example.myapplication.model.SendOtpRequest;
import com.example.myapplication.model.SendOtpResponse;
import com.example.myapplication.network.ApiService;
import com.example.myapplication.network.RetrofitClient;
import com.google.android.material.button.MaterialButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginPage extends AppCompatActivity {

    private MaterialButton btn_submit;
    private EditText emailEditText;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_page);

        btn_submit    = findViewById(R.id.btn_submit);
        emailEditText = findViewById(R.id.emailEditText);

        setupStatusBar();
        setupLoader();

        ApiService apiService = RetrofitClient.getClient(this);

        btn_submit.setOnClickListener(v -> {

            String mobile = emailEditText.getText().toString().trim();

            if (mobile.isEmpty() || mobile.length() < 10) {
                Toast.makeText(this, "Enter a valid mobile number", Toast.LENGTH_SHORT).show();
                return;
            }

            showLoader();

            SendOtpRequest request = new SendOtpRequest(mobile);

            apiService.sendOtp(request).enqueue(new Callback<SendOtpResponse>() {
                @Override
                public void onResponse(Call<SendOtpResponse> call,
                                       Response<SendOtpResponse> response) {
                    hideLoader();

                    if (response.isSuccessful() && response.body() != null) {

                        Toast.makeText(LoginPage.this,
                                "OTP Sent Successfully", Toast.LENGTH_SHORT).show();

                        getSharedPreferences("app_prefs", MODE_PRIVATE)
                                .edit()
                                .putString("mobile", mobile)
                                .apply();

                        Intent i = new Intent(LoginPage.this, Otp_Screen.class);
                        i.putExtra("mobile", mobile);
                        startActivity(i);
                    } else {
                        Toast.makeText(LoginPage.this,
                                "Failed to send OTP. Try again.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<SendOtpResponse> call, Throwable t) {
                    hideLoader();
                    Toast.makeText(LoginPage.this,
                            "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    // Loader
    private void setupLoader() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sending OTP...");
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    }

    private void showLoader() {
        if (progressDialog != null && !progressDialog.isShowing()) {
            progressDialog.show();
        }
    }

    private void hideLoader() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    // Status bar
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