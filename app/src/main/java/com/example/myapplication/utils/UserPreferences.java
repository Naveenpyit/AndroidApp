package com.example.myapplication.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class UserPreferences {

    private static final String PREF_NAME = "user_prefs";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_MOBILE = "mobile";
    private static final String KEY_TOKEN = "token";

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    public UserPreferences(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    // ── Save User ID ──
    public void saveUserId(String userId) {
        editor.putString(KEY_USER_ID, userId);
        editor.apply();
    }

    // ── Get User ID ──
    public String getUserId() {
        return prefs.getString(KEY_USER_ID, "");
    }

    // ── Save Mobile ──
    public void saveMobile(String mobile) {
        editor.putString(KEY_MOBILE, mobile);
        editor.apply();
    }

    // ── Get Mobile ──
    public String getMobile() {
        return prefs.getString(KEY_MOBILE, "");
    }

    // ── Save Token ──
    public void saveToken(String token) {
        editor.putString(KEY_TOKEN, token);
        editor.apply();
    }

    // ── Get Token ──
    public String getToken() {
        return prefs.getString(KEY_TOKEN, "");
    }

    // ── Clear All ──
    public void clearAll() {
        editor.clear();
        editor.apply();
    }

    // ── Is User Logged In ──
    public boolean isLoggedIn() {
        return !getUserId().isEmpty();
    }
}