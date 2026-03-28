package com.example.myapplication.model;

import com.google.gson.annotations.SerializedName;

public class SendOtpResponse {

    @SerializedName("n_status")
    private int status;

    @SerializedName("c_message")
    private String message;

    @SerializedName("n_user")
    private String n_user;

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getN_user() {
        return n_user;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setN_user(String n_user) {
        this.n_user = n_user;
    }

    @Override
    public String toString() {
        return "SendOtpResponse{" +
                "status=" + status +
                ", message='" + message + '\'' +
                ", n_user='" + n_user + '\'' +
                '}';
    }
}