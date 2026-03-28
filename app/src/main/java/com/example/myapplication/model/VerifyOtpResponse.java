package com.example.myapplication.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class VerifyOtpResponse {

    @SerializedName("n_status")
    private int nStatus;

    @SerializedName("c_message")
    private String cMessage;

    @SerializedName("j_data")
    private List<TokenData> jData;

    @SerializedName("n_process_type")
    private int nProcessType;

    // Getters
    public int getNStatus() {
        return nStatus;
    }

    public String getCMessage() {
        return cMessage;
    }

    public List<TokenData> getJData() {
        return jData;
    }

    public int getNProcessType() {
        return nProcessType;
    }

    // Inner class
    public static class TokenData {

        @SerializedName("j_token")
        private String jToken;

        @SerializedName("j_access")
        private String jAccess;

        @SerializedName("j_login")
        private List<Object> jLogin;

        public String getJToken() {
            return jToken;
        }

        public String getJAccess() {
            return jAccess;
        }

        public List<Object> getJLogin() {
            return jLogin;
        }
    }
}