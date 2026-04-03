package com.example.myapplication.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.model.JuspayInitRequest;
import com.example.myapplication.model.JuspayInitResponse;
import com.example.myapplication.network.ApiService;
import com.example.myapplication.network.RetrofitClient;
import com.example.myapplication.utils.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentActivity extends AppCompatActivity {

    private WebView webView;
    private TokenManager tokenManager;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webView = new WebView(this);
        setContentView(webView);

        tokenManager = new TokenManager(this);

        // WebView setup
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                // 🔥 Payment response redirect
                if (url.contains("handleJuspayResponse")) {
                    Toast.makeText(PaymentActivity.this, "Payment Completed", Toast.LENGTH_SHORT).show();

                    // TODO: call verify API here
                    finish();
                    return true;
                }

                return false;
            }
        });

        initiatePayment();
    }

    private void initiatePayment() {

        ApiService api = RetrofitClient.getClient(this);

        String userId = tokenManager.getUserId(); // ✅ dynamic user id

        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        JuspayInitRequest request = new JuspayInitRequest(
                userId,   // ✅ from TokenManager
                "1",      // address id (dynamic if needed)
                "2"       // payment type
        );

        api.initiatePayment(request).enqueue(new Callback<JuspayInitResponse>() {
            @Override
            public void onResponse(Call<JuspayInitResponse> call, Response<JuspayInitResponse> response) {

                if (response.isSuccessful() && response.body() != null) {

                    JuspayInitResponse res = response.body();

                    if (res.n_status == 1 && res.j_data != null) {

                        String paymentUrl = res.j_data.payment_links.web;

                        if (paymentUrl != null && !paymentUrl.isEmpty()) {
                            webView.loadUrl(paymentUrl); // ✅ load payment page
                        } else {
                            Toast.makeText(PaymentActivity.this, "Invalid payment URL", Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        Toast.makeText(PaymentActivity.this, res.c_message, Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(PaymentActivity.this, "API Error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JuspayInitResponse> call, Throwable t) {
                Toast.makeText(PaymentActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 🔙 Handle back navigation inside WebView
    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}