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

import com.example.myapplication.activity.MainActivity;
import com.example.myapplication.activity.OnboadingScreen;

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

            if (isFirstTime) {
                // First time user → show onboarding
                startActivity(new Intent(this, OnboadingScreen.class));
            } else {
                // Returning user → go to main activity
                startActivity(new Intent(this, MainActivity.class));
            }

            finish(); // prevent going back to splash

        }, 2000);
    }
}