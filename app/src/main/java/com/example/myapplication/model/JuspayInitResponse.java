package com.example.myapplication.model;

public class JuspayInitResponse {

    public int n_status;
    public String c_message;
    public Data j_data;

    public static class Data {
        public String status;
        public String id;
        public String order_id;
        public PaymentLinks payment_links;
    }

    public static class PaymentLinks {
        public String web;
        public String expiry;
    }
}