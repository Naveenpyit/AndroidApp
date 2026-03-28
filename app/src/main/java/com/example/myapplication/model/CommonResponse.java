package com.example.myapplication.model;

import com.google.gson.annotations.SerializedName;

public class CommonResponse {

    @SerializedName("status")
    private int status;

    @SerializedName("n_status")
    private int n_status;

    @SerializedName("message")
    private String message;

    @SerializedName("c_message")
    private String c_message;

    @SerializedName("data")
    private Object data;

    public int getStatus() {
        // Try both field names
        return status != 0 ? status : n_status;
    }

    public String getMessage() {
        return message != null ? message : c_message;
    }

    public Object getData() {
        return data;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "CommonResponse{" +
                "status=" + getStatus() +
                ", message='" + getMessage() + '\'' +
                '}';
    }
}