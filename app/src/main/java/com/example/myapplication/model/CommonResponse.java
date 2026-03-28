package com.example.myapplication.model;

import com.google.gson.annotations.SerializedName;

public class CommonResponse {

    @SerializedName("n_status")
    private int status;

    @SerializedName("c_message")
    private String message;

    public int    getStatus()  { return status; }
    public String getMessage() { return message; }

    @Override
    public String toString() {
        return "CommonResponse{status=" + status + ", message='" + message + "'}";
    }
}