package com.example.myapplication.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CityListResponse {

    @SerializedName("n_status")
    private int nStatus;

    @SerializedName("c_message")
    private String cMessage;

    @SerializedName("j_data")
    private List<CityData> jData;

    public int getNStatus() {
        return nStatus;
    }

    public void setNStatus(int nStatus) {
        this.nStatus = nStatus;
    }

    public String getCMessage() {
        return cMessage;
    }

    public void setCMessage(String cMessage) {
        this.cMessage = cMessage;
    }

    public List<CityData> getJData() {
        return jData;
    }

    public void setJData(List<CityData> jData) {
        this.jData = jData;
    }

    public static class CityData {
        @SerializedName("n_id")
        private String nId;

        @SerializedName("c_city_name")
        private String cCityName;

        public String getNId() {
            return nId;
        }

        public void setNId(String nId) {
            this.nId = nId;
        }

        public String getCCityName() {
            return cCityName;
        }

        public void setCCityName(String cCityName) {
            this.cCityName = cCityName;
        }

        @Override
        public String toString() {
            return cCityName;
        }
    }
}

