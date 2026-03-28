package com.example.myapplication.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ListCategoryResponse {

    @SerializedName("n_status")
    private int status;

    @SerializedName("c_message")
    private String message;

    @SerializedName("j_data")
    private List<DataModel> data;

    public List<DataModel> getData() {
        return data;
    }
}