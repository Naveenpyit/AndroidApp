package com.example.myapplication.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CategoryModel {

    @SerializedName("n_id")
    private String id;

    @SerializedName("c_name")
    private String name;

    @SerializedName("c_short_name")
    private String shortName;

    @SerializedName("c_random")
    private String random;

    @SerializedName("c_image")
    private String image;

    @SerializedName("j_section")
    private List<SectionModel> sections;

    // Getters
    public String getId()                    { return id; }
    public String getName()                  { return name; }
    public String getShortName()             { return shortName; }
    public String getRandom()                { return random; }
    public String getImage()                 { return image; }
    public List<SectionModel> getSections()  { return sections; }
}