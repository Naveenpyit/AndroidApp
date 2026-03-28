package com.example.myapplication.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ListItemsResponse {

    @SerializedName("n_status")
    private int status;

    @SerializedName("c_message")
    private String message;

    @SerializedName("j_data")
    private ItemsData data;

    public int getStatus()      { return status; }
    public String getMessage()  { return message; }
    public ItemsData getData()  { return data; }

    public static class ItemsData {

        @SerializedName("n_total_record")
        private int totalRecord;

        @SerializedName("n_offset")
        private int offset;

        @SerializedName("n_limit")
        private int limit;

        @SerializedName("j_result")
        private List<ProductModel> results;

        public int getTotalRecord()          { return totalRecord; }
        public int getOffset()               { return offset; }
        public int getLimit()                { return limit; }
        public List<ProductModel> getResults() { return results; }
    }
}