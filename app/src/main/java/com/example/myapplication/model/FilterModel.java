package com.example.myapplication.model;

public class FilterModel {

    public static class FilterOption {
        private String optionId;
        private String optionName;
        private int count;
        private boolean isSelected;

        public FilterOption(String optionId, String optionName, int count, boolean isSelected) {
            this.optionId = optionId;
            this.optionName = optionName;
            this.count = count;
            this.isSelected = isSelected;
        }

        public String getOptionId() {
            return optionId;
        }

        public void setOptionId(String optionId) {
            this.optionId = optionId;
        }

        public String getOptionName() {
            return optionName;
        }

        public void setOptionName(String optionName) {
            this.optionName = optionName;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public boolean isSelected() {
            return isSelected;
        }

        public void setSelected(boolean selected) {
            isSelected = selected;
        }
    }
}