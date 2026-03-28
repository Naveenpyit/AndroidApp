package com.example.myapplication.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.model.CartModel;

import java.util.ArrayList;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    public interface CartChangeListener {
        void onCartChanged();
    }

    private final Context context;
    private final ArrayList<CartModel> list;
    private final CartChangeListener listener;

    public CartAdapter(Context context, ArrayList<CartModel> list,
                       CartChangeListener listener) {
        this.context  = context;
        this.list     = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_cart, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        CartModel item = list.get(position);

        h.tvName.setText(item.getName());

        h.tvMeta.setText(
                "Pack : " + item.getPack()
                        + "  Category : " + item.getCategory()
                        + "  SKU : " + item.getSku());

        updatePriceLabel(h, item);

        h.tvQty.setText(String.valueOf(item.getQuantity()));

        // Checkbox
        h.checkBox.setOnCheckedChangeListener(null); // clear old listener first
        h.checkBox.setChecked(item.isSelected());
        h.checkBox.setOnCheckedChangeListener((btn, checked) -> {
            item.setSelected(checked);
            listener.onCartChanged();
        });

        // Image
        Glide.with(context)
                .load(item.getImageUrl())
                .placeholder(R.drawable.ic_launcher_background)
                .centerCrop()
                .into(h.ivImage);

        // Decrease qty
        h.btnMinus.setOnClickListener(v -> {
            if (item.getQuantity() > 1) {
                item.setQuantity(item.getQuantity() - 1);
                h.tvQty.setText(String.valueOf(item.getQuantity()));
                updatePriceLabel(h, item);
                listener.onCartChanged();
            }
        });

        // Increase qty
        h.btnPlus.setOnClickListener(v -> {
            item.setQuantity(item.getQuantity() + 1);
            h.tvQty.setText(String.valueOf(item.getQuantity()));
            updatePriceLabel(h, item);
            listener.onCartChanged();
        });

        // Remove
        h.tvRemove.setOnClickListener(v -> {
            int pos = h.getAdapterPosition();
            if (pos != RecyclerView.NO_ID && pos < list.size()) {
                list.remove(pos);
                notifyItemRemoved(pos);
                notifyItemRangeChanged(pos, list.size());
                listener.onCartChanged();
            }
        });
    }

    // ── Update price label ────────────────────────────────────────────────────
    private void updatePriceLabel(ViewHolder h, CartModel item) {
        h.tvPrice.setText(
                "₹" + String.format("%.2f", item.getGrandTotal())
                        + "  (₹" + String.format("%.2f", item.getLineTotal())
                        + " + " + String.format("%.2f", item.getGstAmount()) + " GST)");
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox  checkBox;
        ImageView ivImage;
        TextView  tvName, tvMeta, tvPrice, tvQty, tvRemove;
        TextView  btnMinus, btnPlus;

        ViewHolder(@NonNull View v) {
            super(v);
            checkBox  = v.findViewById(R.id.checkbox);
            ivImage   = v.findViewById(R.id.iv_cart_image);
            tvName    = v.findViewById(R.id.tv_cart_name);
            tvMeta    = v.findViewById(R.id.tv_cart_meta);
            tvPrice   = v.findViewById(R.id.tv_cart_price);
            tvQty     = v.findViewById(R.id.tv_qty);
            tvRemove  = v.findViewById(R.id.tv_remove);
            btnMinus  = v.findViewById(R.id.btn_minus);
            btnPlus   = v.findViewById(R.id.btn_plus);
        }
    }
}