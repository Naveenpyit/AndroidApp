package com.example.myapplication.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;

public class ListItemsRequest {

    @SerializedName("n_user")
    private String userId;

    @SerializedName("n_page")
    private String page;

    @SerializedName("n_limit")
    private String limit;

    @SerializedName("c_search")
    private String search;

    @SerializedName("j_filter")
    private List<Map<String, List<String>>> filter;

    public ListItemsRequest(String userId, String page, String limit,
                            String search,
                            List<Map<String, List<String>>> filter) {
        this.userId = userId;
        this.page   = page;
        this.limit  = limit;
        this.search = search;
        this.filter = filter;
    }
}