package com.example.myapplication.adapter;

import android.content.Context;
import android.graphics.Paint;
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
        void onQtyUpdate(CartModel item, int newQty);
        void onDelete(CartModel item, int position);
    }

    private final Context              context;
    private final ArrayList<CartModel> list;
    private final CartChangeListener   listener;

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

        h.tvName.setText(item.getName() != null ? item.getName() : "");
        h.tvMeta.setText(
                "Pack: " + item.getPackName()
                        + "  |  " + item.getCategoryName()
                        + "  |  SKU: " + item.getSku());

        h.tvPrice.setText("₹" + String.format("%.2f", item.getPriceDouble()));
        h.tvGstNote.setText("(₹" + String.format("%.2f", item.getPriceDouble())
                + " + ₹" + String.format("%.2f", item.getGstDouble()) + " GST)");

        h.tvMrp.setText("MRP: ₹" + String.format("%.2f", item.getMrpDouble()));
        h.tvMrp.setPaintFlags(h.tvMrp.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        h.tvQty.setText(String.valueOf(item.getUiQty()));

        h.btnMinus.setOnClickListener(v -> {
            int pos = h.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            CartModel it = list.get(pos);
            int newQty = it.getUiQty() - 1;
            if (newQty < 1) return;
            it.setUiQty(newQty);
            h.tvQty.setText(String.valueOf(newQty));
            listener.onQtyUpdate(it, newQty);
        });

        h.btnPlus.setOnClickListener(v -> {
            int pos = h.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            CartModel it = list.get(pos);
            int newQty = it.getUiQty() + 1;
            it.setUiQty(newQty);
            h.tvQty.setText(String.valueOf(newQty));
            listener.onQtyUpdate(it, newQty);
        });

        h.tvRemove.setOnClickListener(v -> {
            int pos = h.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION)
                listener.onDelete(list.get(pos), pos);
        });

        h.checkBox.setOnCheckedChangeListener(null);
        h.checkBox.setChecked(item.isSelected());
        h.checkBox.setOnCheckedChangeListener((btn, checked) -> {
            item.setSelected(checked);
            listener.onCartChanged();
        });

        Glide.with(context)
                .load(item.getImageUrl())
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .centerCrop()
                .into(h.ivImage);
    }

    @Override
    public int getItemCount() { return list != null ? list.size() : 0; }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox  checkBox;
        ImageView ivImage;
        TextView  tvName, tvMeta, tvPrice, tvGstNote, tvMrp;
        TextView  tvQty, tvRemove, btnMinus, btnPlus;

        ViewHolder(@NonNull View v) {
            super(v);
            checkBox  = v.findViewById(R.id.checkbox);
            ivImage   = v.findViewById(R.id.iv_cart_image);
            tvName    = v.findViewById(R.id.tv_cart_name);
            tvMeta    = v.findViewById(R.id.tv_cart_meta);
            tvPrice   = v.findViewById(R.id.tv_cart_price);
            tvGstNote = v.findViewById(R.id.tv_cart_gst_note);
            tvMrp     = v.findViewById(R.id.tv_cart_mrp);
            tvQty     = v.findViewById(R.id.tv_qty);
            tvRemove  = v.findViewById(R.id.tv_remove);
            btnMinus  = v.findViewById(R.id.btn_minus);
            btnPlus   = v.findViewById(R.id.btn_plus);
        }
    }
}