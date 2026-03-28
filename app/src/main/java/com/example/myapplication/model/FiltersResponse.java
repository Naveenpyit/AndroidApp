package com.example.myapplication.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class FiltersResponse {

    @SerializedName("n_status")  private int status;
    @SerializedName("c_message") private String message;
    @SerializedName("j_data")    private FilterData data;

    public int getStatus() { return status; }
    public String getMessage() { return message; }
    public FilterData getData() { return data; }

    public static class FilterData {

        @SerializedName("j_headings")
        private List<String> headings;

        @SerializedName("j_category_filter")
        private List<CategoryFilter> categoryFilters;

        @SerializedName("j_fit_filter")
        private List<FitFilter> fitFilters;

        @SerializedName("j_price_filter")
        private List<PriceFilter> priceFilters;

        @SerializedName("j_tags_filter")
        private List<TagFilter> tagFilters;

        @SerializedName("j_section_filter")
        private List<SectionFilter> sectionFilters;

        @SerializedName("j_packs_filter")
        private List<PackFilter> packsFilters;

        public List<String> getHeadings() { return headings; }
        public List<CategoryFilter> getCategoryFilters() { return categoryFilters; }
        public List<FitFilter> getFitFilters() { return fitFilters; }
        public List<PriceFilter> getPriceFilters() { return priceFilters; }
        public List<TagFilter> getTagFilters() { return tagFilters; }
        public List<SectionFilter> getSectionFilters() { return sectionFilters; }
        public List<PackFilter> getPacksFilters() { return packsFilters; }
    }

    public static class CategoryFilter {
        @SerializedName("n_id") private String id;
        @SerializedName("c_name") private String name;
        @SerializedName("product_count") private String productCount;
        private boolean selected = false;

        public String getId() { return id; }
        public String getName() { return name; }

        public int getProductCount() {
            try {
                return Integer.parseInt(productCount);
            } catch (Exception e) {
                return 0;
            }
        }

        public boolean isSelected() { return selected; }
        public void setSelected(boolean v) { selected = v; }
    }

    public static class FitFilter {
        @SerializedName("id") private String id;
        @SerializedName("c_fit") private String fit;
        @SerializedName("fit_count") private String fitCount;
        private boolean selected = false;

        public String getId() { return id; }
        public String getFit() { return fit; }

        public int getFitCount() {
            try {
                return Integer.parseInt(fitCount);
            } catch (Exception e) {
                return 0;
            }
        }

        public boolean isSelected() { return selected; }
        public void setSelected(boolean v) { selected = v; }
    }

    public static class PriceFilter {
        @SerializedName("c_price_range") private String priceRange;
        @SerializedName("n_value") private String value;
        private boolean selected = false;

        public String getPriceRange() { return priceRange; }
        public String getValue() { return value; }

        public boolean isSelected() { return selected; }
        public void setSelected(boolean v) { selected = v; }
    }

    public static class TagFilter {
        @SerializedName("n_id") private String id;
        @SerializedName("c_tag") private String tag;
        private boolean selected = false;

        public String getId() { return id; }
        public String getTag() { return tag; }

        public boolean isSelected() { return selected; }
        public void setSelected(boolean v) { selected = v; }
    }

    public static class SectionFilter {
        @SerializedName("n_id") private String id;
        @SerializedName("c_name") private String name;
        private boolean selected = false;

        public String getId() { return id; }
        public String getName() { return name; }

        public boolean isSelected() { return selected; }
        public void setSelected(boolean v) { selected = v; }
    }

    public static class PackFilter {
        @SerializedName("n_id") private String id;
        @SerializedName("c_name") private String name;
        private boolean selected = false;

        public String getId() { return id; }
        public String getName() { return name; }

        public boolean isSelected() { return selected; }
        public void setSelected(boolean v) { selected = v; }
    }
}