package com.example.myapplication.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class VerifyOtpResponse {

    @SerializedName("n_status")
    private int n_status;

    @SerializedName("c_message")
    private String c_message;

    @SerializedName("n_user")
    private String n_user;  // ✅ Add this

    @SerializedName("j_data")
    private List<TokenData> j_data;

    public int getN_status() {
        return n_status;
    }

    public String getC_message() {
        return c_message;
    }

    public String getN_user() {
        return n_user;
    }

    public List<TokenData> getJ_data() {
        return j_data;
    }

    public void setN_status(int n_status) {
        this.n_status = n_status;
    }

    public void setC_message(String c_message) {
        this.c_message = c_message;
    }

    public void setN_user(String n_user) {
        this.n_user = n_user;
    }

    public void setJ_data(List<TokenData> j_data) {
        this.j_data = j_data;
    }

    // ── Inner class for token data ──
    public static class TokenData {

        @SerializedName("j_token")
        private String j_token;

        @SerializedName("j_access")
        private String j_access;

        public String getJ_token() {
            return j_token;
        }

        public String getJ_access() {
            return j_access;
        }

        public void setJ_token(String j_token) {
            this.j_token = j_token;
        }

        public void setJ_access(String j_access) {
            this.j_access = j_access;
        }
    }

    @Override
    public String toString() {
        return "VerifyOtpResponse{" +
                "n_status=" + n_status +
                ", c_message='" + c_message + '\'' +
                ", n_user='" + n_user + '\'' +
                ", j_data=" + j_data +
                '}';
    }
}