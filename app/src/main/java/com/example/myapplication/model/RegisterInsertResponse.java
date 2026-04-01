package com.example.myapplication.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class RegisterInsertResponse {

    @SerializedName("n_status")
    private int nStatus;

    @SerializedName("c_message")
    private String cMessage;

    @SerializedName("j_data")
    private List<JData> jData;

    public int getNStatus()      { return nStatus; }
    public String getCMessage()  { return cMessage; }
    public List<JData> getJData(){ return jData; }

    public static class JData {
        @SerializedName("n_screen_type")
        private int nScreenType;

        public int getNScreenType() { return nScreenType; }
    }
}