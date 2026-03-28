package com.example.myapplication.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ListCartResponse {

    @SerializedName("n_status")
    private int status;

    @SerializedName("c_message")
    private String message;

    @SerializedName("j_data")
    private List<CartItemModel> data;

    // ── Getters ──
    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public List<CartItemModel> getData() {
        return data;
    }

    // ── Setters ──
    public void setStatus(int status) {
        this.status = status;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setData(List<CartItemModel> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "ListCartResponse{" +
                "status=" + status +
                ", message='" + message + '\'' +
                ", data=" + (data != null ? data.size() : 0) + " items" +
                '}';
    }
}