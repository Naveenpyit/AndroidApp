package com.example.myapplication.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenManager {

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    public TokenManager(Context context){
        pref = context.getSharedPreferences("TOKEN_PREF", Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void saveToken(String token){
        editor.putString("TOKEN", token);
        editor.apply();
    }

    public void saveAccess(String access){
        editor.putString("ACCESS", access);
        editor.apply();
    }

    public void saveUserId(String userId){
        editor.putString("USER_ID", userId);
        editor.apply();
    }

    public String getToken(){
        return pref.getString("TOKEN", null);
    }

    public String getAccess(){
        return pref.getString("ACCESS", null);
    }

    public String getUserId(){
        return pref.getString("USER_ID", null);
    }

    public void clear(){
        editor.clear().apply();
    }
}