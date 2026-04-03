package com.example.myapplication.activity;


import android.app.Application;

import com.google.android.gms.security.ProviderInstaller;

public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            ProviderInstaller.installIfNeeded(getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}