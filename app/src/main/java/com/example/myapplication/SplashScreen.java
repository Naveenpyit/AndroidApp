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

import com.example.myapplication.activity.BusinessDetailsActivity;
import com.example.myapplication.activity.LoginPage;
import com.example.myapplication.activity.MainActivity;
import com.example.myapplication.activity.OnboadingScreen;
import com.example.myapplication.activity.OwnerDetailsActivity;
import com.example.myapplication.activity.SetupStepsActivity;

public class SplashScreen extends AppCompatActivity {

    private ImageView logoimage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash_screen);

        logoimage = findViewById(R.id.logoimage);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.slide_in_left);
        logoimage.setAnimation(animation);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {

            SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);

            boolean isFirstTime = prefs.getBoolean("is_first_time", true);
            boolean isLoggedIn = prefs.getBoolean("is_logged_in", false);
            String lastScreen = prefs.getString("last_screen", "");

            // 🔥 FLOW CONTROL
            if (isFirstTime) {

                startActivity(new Intent(SplashScreen.this, OnboadingScreen.class));

            } else if (!isLoggedIn) {

                startActivity(new Intent(SplashScreen.this, LoginPage.class));

            } else {

                // Resume last screen
                switch (lastScreen) {

                    case "OwnerDetails":
                        startActivity(new Intent(SplashScreen.this, OwnerDetailsActivity.class));
                        break;

                    case "BusinessDetails":
                        startActivity(new Intent(SplashScreen.this, BusinessDetailsActivity.class));
                        break;

                    case "SetupSteps":
                        startActivity(new Intent(SplashScreen.this, SetupStepsActivity.class));
                        break;

                    case "Main":
                        startActivity(new Intent(SplashScreen.this, MainActivity.class));
                        break;

                    default:
                        startActivity(new Intent(SplashScreen.this, MainActivity.class));
                        break;
                }
            }

            finish(); // only once!

        }, 2000);
    }
}