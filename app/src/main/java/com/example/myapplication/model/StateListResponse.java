package com.example.myapplication.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class StateListResponse {

    @SerializedName("n_status")
    private int nStatus;

    @SerializedName("c_message")
    private String cMessage;

    @SerializedName("j_data")
    private List<StateData> jData;

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

    public List<StateData> getJData() {
        return jData;
    }

    public void setJData(List<StateData> jData) {
        this.jData = jData;
    }

    public static class StateData {
        @SerializedName("n_id")
        private String nId;

        @SerializedName("c_state")
        private String cStateName;
        public String getNId() {
            return nId;
        }

        public void setNId(String nId) {
            this.nId = nId;
        }

        public String getCStateName() {
            return cStateName;
        }

        public void setCStateName(String cStateName) {
            this.cStateName = cStateName;
        }

        @Override
        public String toString() {
            return cStateName;
        }
    }
}

