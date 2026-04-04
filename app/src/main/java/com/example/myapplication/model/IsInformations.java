package com.example.myapplication.model;

import com.google.gson.annotations.SerializedName;

public  class IsInformations {
    @SerializedName("n_owner")
    private int nOwner;

    @SerializedName("n_business")
    private int nBusiness;

    @SerializedName("n_address")
    private int nAddress;

    public int getNOwner()    { return nOwner; }
    public int getNBusiness() { return nBusiness; }
    public int getNAddress()  { return nAddress; }
}
