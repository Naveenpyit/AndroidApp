package com.example.myapplication.model;

import com.google.gson.annotations.SerializedName;

public class GstVerifyResponse {
    @SerializedName("n_status")
    private int status;

    @SerializedName("c_message")
    private String message;

    @SerializedName("j_data")
    private VerificationData data;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public VerificationData getData() {
        return data;
    }

    public void setData(VerificationData data) {
        this.data = data;
    }

    public static class VerificationData {
        @SerializedName("n_id")
        private String id;

        @SerializedName("c_business_name")
        private String businessName;

        @SerializedName("c_owner_name")
        private String ownerName;

        @SerializedName("is_verified")
        private boolean isVerified;

        @SerializedName("c_message")
        private String verificationMessage;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getBusinessName() {
            return businessName;
        }

        public void setBusinessName(String businessName) {
            this.businessName = businessName;
        }

        public String getOwnerName() {
            return ownerName;
        }

        public void setOwnerName(String ownerName) {
            this.ownerName = ownerName;
        }

        public boolean isVerified() {
            return isVerified;
        }

        public void setVerified(boolean verified) {
            isVerified = verified;
        }

        public String getVerificationMessage() {
            return verificationMessage;
        }

        public void setVerificationMessage(String verificationMessage) {
            this.verificationMessage = verificationMessage;
        }
    }
}

