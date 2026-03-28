package com.example.myapplication.model;


import java.util.List;


public class VerifyOtpRequest {

    private String n_mobile;
    private String n_otp;

    public VerifyOtpRequest(String n_mobile, String n_otp) {
        this.n_mobile = n_mobile;
        this.n_otp = n_otp;
    }

    public String getN_mobile() {
        return n_mobile;
    }

    public void setN_mobile(String n_mobile) {
        this.n_mobile = n_mobile;
    }

    public String getN_otp() {
        return n_otp;
    }

    public void setN_otp(String n_otp) {
        this.n_otp = n_otp;
    }
}
