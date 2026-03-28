package com.example.myapplication.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.myapplication.adapter.OnBoadingAdapter;
import com.example.myapplication.R;
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator;

public class OnboadingScreen extends AppCompatActivity {

    private ViewPager2 viewpager;
    private TextView skip;
    private ImageButton next;
    private DotsIndicator dots_indicator;

    private Handler handler = new Handler();

    // Onboarding slide data
    String[] header = {
            "Let's Get Started",
            "Let's Get Started",
            "Let's Get Started"
    };

    int[] image = {
            R.drawable.onboard1,
            R.drawable.onboard2,
            R.drawable.onboard3
    };

    String[] title = {
            "Bulk Pricing Made Simple",
            "Easy Reorders & Fast Delivery",
            "Partner with Tom Hiddle"
    };

    String[] subtitle = {
            "Get customized bulk pricing to fit your purchasing goals.",
            "Quickly replenish your stock with hassle-free reordering and reliable shipping",
            "Join a trusted network enhancing your business growth and opportunities."
    };

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_onboading_screen);


        viewpager = findViewById(R.id.viewpager);
        skip = findViewById(R.id.skip);
        next = findViewById(R.id.next);
        dots_indicator = findViewById(R.id.dots_indicator);

        OnBoadingAdapter adapter = new OnBoadingAdapter(this, header, image, title, subtitle);
        viewpager.setAdapter(adapter);
        dots_indicator.setViewPager2(viewpager);


        setupStatusBar();
        autoSlide();

        next.setOnClickListener(v -> {
            int currentPosition = viewpager.getCurrentItem();
            if (currentPosition < 2) {

                viewpager.setCurrentItem(currentPosition + 1, true);
            } else {

                finishOnboarding();
            }
        });

        skip.setOnClickListener(v -> finishOnboarding());
    }

    private void finishOnboarding() {

        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        prefs.edit()
                .putBoolean("is_first_time", false)
                .putBoolean("onboarding_completed", true)
                .apply();

        handler.removeCallbacksAndMessages(null);

        startActivity(new Intent(this, LoginPage.class));
        finish();
    }

    private void autoSlide() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                int currentItem = viewpager.getCurrentItem();
                int nextItem = (currentItem + 1) % 3;
                viewpager.setCurrentItem(nextItem, true);
                handler.postDelayed(this, 3000);
            }
        }, 3000);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    public void onBackPressed() {
    }
}