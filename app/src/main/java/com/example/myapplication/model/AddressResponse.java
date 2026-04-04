package com.example.myapplication.model;


import java.util.List;

public class AddressResponse {

    private int n_status;
    private String c_message;
    private List<AddressData> j_data;

    public int getN_status() {
        return n_status;
    }

    public String getC_message() {
        return c_message;
    }

    public List<AddressData> getJ_data() {
        return j_data;
    }
}