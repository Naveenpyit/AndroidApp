package com.example.myapplication.model;

import com.google.gson.annotations.SerializedName;

public class CityListRequest {

    @SerializedName("n_state")
    private String nState;

    public CityListRequest(String nState) {
        this.nState = nState;
    }

    public String getNState() {
        return nState;
    }

    public void setNState(String nState) {
        this.nState = nState;
    }
}

