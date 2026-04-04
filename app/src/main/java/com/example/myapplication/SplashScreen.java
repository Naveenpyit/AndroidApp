package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.activity.LoginPage;
import com.example.myapplication.activity.MainActivity;
import com.example.myapplication.activity.OnboadingScreen;
import com.example.myapplication.activity.SetupStepsActivity;
import com.example.myapplication.model.RegisterDetailsRequest;
import com.example.myapplication.model.RegisterDetailsResponse;
import com.example.myapplication.network.ApiService;
import com.example.myapplication.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SplashScreen extends AppCompatActivity {

    private ImageView logoimage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash_screen);

        logoimage = findViewById(R.id.logoimage);
        Animation animation = AnimationUtils.loadAnimation(
                this, R.anim.slide_in_left);
        logoimage.setAnimation(animation);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {

            SharedPreferences prefs =
                    getSharedPreferences("app_prefs", MODE_PRIVATE);

            boolean isFirstTime  = prefs.getBoolean("is_first_time", true);
            boolean isLoggedIn   = prefs.getBoolean("is_logged_in", false);
            String  savedMobile  = prefs.getString("mobile", "");

            if (isFirstTime) {

                startActivity(new Intent(SplashScreen.this,
                        OnboadingScreen.class));
                finish();

            } else if (!isLoggedIn || savedMobile.isEmpty()) {

                goToLogin();

            } else {

                checkRegistrationStatus(savedMobile);
            }

        }, 2000);
    }


    private void checkRegistrationStatus(String mobile) {
        ApiService apiService = RetrofitClient.getClient(this);

        apiService.getRegisterDetails(new RegisterDetailsRequest(mobile))
                .enqueue(new Callback<RegisterDetailsResponse>() {

                    @Override
                    public void onResponse(Call<RegisterDetailsResponse> call,
                                           Response<RegisterDetailsResponse> res) {

                        if (!res.isSuccessful() || res.body() == null
                                || res.body().getNStatus() != 1
                                || res.body().getJData() == null) {
                            // API fail → LoginPage
                            goToLogin();
                            return;
                        }

                        RegisterDetailsResponse.JData data =
                                res.body().getJData();

                        int nOwner    = 0;
                        int nBusiness = 0;
                        int nAddress  = 0;

                        if (data.getIsInformations() != null) {
                            nOwner    = data.getIsInformations().getNOwner();
                            nBusiness = data.getIsInformations().getNBusiness();
                            nAddress  = data.getIsInformations().getNAddress();
                        }

                        if (nOwner == 1 && nBusiness == 1 && nAddress == 1) {
                            // ✅ All done → MainActivity
                            goToMain();
                        } else {
                            // ✅ Incomplete → SetupStepsActivity
                            goToSetup(mobile);
                        }
                    }

                    @Override
                    public void onFailure(Call<RegisterDetailsResponse> call,
                                          Throwable t) {
                        // Network error → LoginPage
                        goToLogin();
                    }
                });
    }

    private void goToMain() {
        Intent i = new Intent(SplashScreen.this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    private void goToSetup(String mobile) {
        Intent i = new Intent(SplashScreen.this, SetupStepsActivity.class);
        i.putExtra("mobile", mobile);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    private void goToLogin() {
        Intent i = new Intent(SplashScreen.this, LoginPage.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }
}