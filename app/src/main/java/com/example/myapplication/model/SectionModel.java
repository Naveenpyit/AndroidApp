package com.example.myapplication.model;

import com.google.gson.annotations.SerializedName;

public class SectionModel {

    @SerializedName("n_id")
    private int id;

    @SerializedName("c_name")
    private String name;

    @SerializedName("c_short_name")
    private String shortName;

    @SerializedName("c_random")
    private String random;

    @SerializedName("c_image")
    private String image;

    // Getters
    public int getId()         { return id; }
    public String getName()    { return name; }
    public String getShortName() { return shortName; }
    public String getRandom()  { return random; }
    public String getImage()   { return image; }
}