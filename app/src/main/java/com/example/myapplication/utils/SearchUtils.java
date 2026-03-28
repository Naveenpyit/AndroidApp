package com.example.myapplication.utils;

import com.example.myapplication.model.ProductModel;

import java.util.ArrayList;
import java.util.List;

public class SearchUtils {

    /**
     * Filters and sorts productList so that:
     * 1. Items whose name STARTS WITH the query come first
     * 2. Items whose name CONTAINS the query come next
     * 3. Non-matching items are excluded
     *
     * @param allProducts  Full original list (never modified)
     * @param query        Search text typed by user
     * @return             Sorted filtered list — matches at top
     */
    public static List<ProductModel> filterAndSort(List<ProductModel> allProducts, String query) {

        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>(allProducts); // return full list when query empty
        }

        String lowerQuery = query.toLowerCase().trim();

        List<ProductModel> startsWithList = new ArrayList<>(); // highest priority
        List<ProductModel> containsList   = new ArrayList<>(); // second priority

        for (ProductModel product : allProducts) {
            String name = product.getName() != null
                    ? product.getName().toLowerCase() : "";
            String category = product.getCategoryName() != null
                    ? product.getCategoryName().toLowerCase() : "";

            if (name.startsWith(lowerQuery) || category.startsWith(lowerQuery)) {
                startsWithList.add(product);                  // ← TOP of results
            } else if (name.contains(lowerQuery) || category.contains(lowerQuery)) {
                containsList.add(product);                    // ← below startsWith
            }
            // no match → excluded
        }

        // Merge: startsWith first, then contains
        List<ProductModel> result = new ArrayList<>();
        result.addAll(startsWithList);
        result.addAll(containsList);
        return result;
    }
}