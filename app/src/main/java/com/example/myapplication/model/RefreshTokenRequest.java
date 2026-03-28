package com.example.myapplication.model;

public class RefreshTokenRequest {

    private String j_access;

    public RefreshTokenRequest(String j_access) {
        this.j_access = j_access;
    }

    public String getJ_access() {
        return j_access;
    }

    public void setJ_access(String j_access) {
        this.j_access = j_access;
    }
}
