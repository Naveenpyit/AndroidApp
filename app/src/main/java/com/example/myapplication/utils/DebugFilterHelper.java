package com.example.myapplication.utils;

import android.util.Log;

import java.util.List;
import java.util.Map;

public class DebugFilterHelper {

    private static final String TAG = "FILTER_DEBUG";

    public static void logFilters(Map<String, List<String>> selectedFilters) {
        Log.d(TAG, "=== SELECTED FILTERS (" + selectedFilters.size() + ") ===");
        if (selectedFilters.isEmpty()) {
            Log.d(TAG, "  (none)");
        } else {
            for (Map.Entry<String, List<String>> entry : selectedFilters.entrySet()) {
                Log.d(TAG, "  Key: " + entry.getKey()
                        + "  Values: " + entry.getValue());
            }
        }
        Log.d(TAG, "===============================");
    }

    public static void logFinalRequest(List<Map<String, List<String>>> filters) {
        Log.d(TAG, "=== FINAL API REQUEST FILTERS (" + filters.size() + ") ===");
        for (int i = 0; i < filters.size(); i++) {
            for (Map.Entry<String, List<String>> entry : filters.get(i).entrySet()) {
                Log.d(TAG, "  [" + i + "] " + entry.getKey()
                        + " -> " + entry.getValue());
            }
        }
        Log.d(TAG, "==========================================");
    }
}