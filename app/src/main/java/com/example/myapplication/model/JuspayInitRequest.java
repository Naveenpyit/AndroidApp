package com.example.myapplication.model;

public class JuspayInitRequest {
    private String n_user;
    private String n_address;
    private String n_payment_type;

    public JuspayInitRequest(String n_user, String n_address, String n_payment_type) {
        this.n_user = n_user;
        this.n_address = n_address;
        this.n_payment_type = n_payment_type;
    }
}