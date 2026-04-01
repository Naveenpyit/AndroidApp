package com.example.myapplication.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class VerifyOtpResponse {

    @SerializedName("n_status")  private int    nStatus;
    @SerializedName("c_message") private String cMessage;
    @SerializedName("j_data")    private List<TokenData> jData;

    public int             getNStatus() { return nStatus; }
    public String          getCMessage(){ return cMessage; }
    public List<TokenData> getJData()   { return jData; }

    public static class TokenData {

        @SerializedName("j_token")  private String jToken;
        @SerializedName("j_access") private String jAccess;
        @SerializedName("j_login")  private List<LoginData> jLogin;

        public String          getJToken()  { return jToken; }
        public String          getJAccess() { return jAccess; }
        public List<LoginData> getJLogin()  { return jLogin; }
    }


    public static class LoginData {

        @SerializedName("n_user_id")     private String nUserId;
        @SerializedName("c_user_name")   private String cUserName;
        @SerializedName("n_user_mobile") private String nUserMobile;

        public String getNUserId()     { return nUserId; }
        public String getCUserName()   { return cUserName; }
        public String getNUserMobile() { return nUserMobile; }
    }
}
