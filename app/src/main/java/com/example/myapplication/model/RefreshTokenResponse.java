package com.example.myapplication.model;



import java.util.List;

public class RefreshTokenResponse {

    private int status;
    private String message;
    private List<TokenData> j_data;

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public List<TokenData> getJ_data() {
        return j_data;
    }
}
