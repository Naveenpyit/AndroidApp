package com.example.myapplication.utils;

import android.content.Context;

import androidx.appcompat.app.AlertDialog;

import com.example.myapplication.model.FilterModel;

import java.util.ArrayList;
import java.util.List;

public class FilterDialogHelper {

    public interface FilterDialogListener {
        void onFilterSelected(List<String> selectedIds, String displayText);
    }

    public interface SortDialogListener {
        void onSortSelected(String sortId, String displayText);
    }

    public static void showFilterDialog(Context context, String filterName,
                                        List<FilterModel.FilterOption> options,
                                        FilterDialogListener listener) {
        if (options == null || options.isEmpty()) return;

        String[] optionNames = new String[options.size()];
        String[] optionIds = new String[options.size()];
        boolean[] checkedItems = new boolean[options.size()];

        for (int i = 0; i < options.size(); i++) {
            FilterModel.FilterOption opt = options.get(i);
            optionNames[i] = opt.getOptionName() + " (" + opt.getCount() + ")";
            optionIds[i] = opt.getOptionId();
            checkedItems[i] = opt.isSelected();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Select " + filterName)
                .setMultiChoiceItems(optionNames, checkedItems, (dialog, which, isChecked) -> {
                    checkedItems[which] = isChecked;
                })
                .setPositiveButton("Apply", (dialog, which) -> {
                    StringBuilder selected = new StringBuilder();
                    List<String> ids = new ArrayList<>();

                    for (int i = 0; i < optionNames.length; i++) {
                        if (checkedItems[i]) {
                            if (selected.length() > 0) selected.append(", ");
                            selected.append(optionNames[i].split(" \\(")[0]);
                            ids.add(optionIds[i]);
                        }
                    }

                    if (listener != null) {
                        listener.onFilterSelected(ids, selected.toString());
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    public static void showSortDialog(Context context, List<FilterModel.FilterOption> options,
                                      SortDialogListener listener) {
        if (options == null || options.isEmpty()) return;

        String[] optionNames = new String[options.size()];
        String[] optionIds = new String[options.size()];

        for (int i = 0; i < options.size(); i++) {
            FilterModel.FilterOption opt = options.get(i);
            optionNames[i] = opt.getOptionName();
            optionIds[i] = opt.getOptionId();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Sort By")
                .setSingleChoiceItems(optionNames, -1, (dialog, which) -> {
                    if (listener != null) {
                        listener.onSortSelected(optionIds[which], optionNames[which]);
                    }
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }
}