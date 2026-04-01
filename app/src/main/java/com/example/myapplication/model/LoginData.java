package com.example.myapplication.model;

import com.google.gson.annotations.SerializedName;

public  class LoginData {
    @SerializedName("n_user_id")     private String nUserId;
    @SerializedName("c_user_name")   private String cUserName;
    @SerializedName("n_user_mobile") private String nUserMobile;

    public String getNUserId()     { return nUserId; }
    public String getCUserName()   { return cUserName; }
    public String getNUserMobile() { return nUserMobile; }
}

