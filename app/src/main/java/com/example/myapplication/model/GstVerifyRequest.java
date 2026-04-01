package com.example.myapplication.model;

public class GstVerifyRequest {
    private String gst_number;
    private String pan_number;
    private String business_type;

    public GstVerifyRequest() {}

    public GstVerifyRequest(String gstNumber, String panNumber, String businessType) {
        this.gst_number = gstNumber;
        this.pan_number = panNumber;
        this.business_type = businessType;
    }

    public String getGst_number() {
        return gst_number;
    }

    public void setGst_number(String gst_number) {
        this.gst_number = gst_number;
    }

    public String getPan_number() {
        return pan_number;
    }

    public void setPan_number(String pan_number) {
        this.pan_number = pan_number;
    }

    public String getBusiness_type() {
        return business_type;
    }

    public void setBusiness_type(String business_type) {
        this.business_type = business_type;
    }
}

