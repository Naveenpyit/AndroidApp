package com.example.myapplication.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.AddressData;

import java.util.List;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.ViewHolder> {

    private Context context;
    private List<AddressData> list;

    public AddressAdapter(Context context, List<AddressData> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public AddressAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.item_address, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddressAdapter.ViewHolder holder, int position) {

        AddressData model = list.get(position);

        // Name
        holder.txtName.setText(model.getC_name());

        // Address
        holder.txtAddress.setText(model.getC_address());

        // Primary Badge
        if ("1".equals(model.getN_address_type())) {
            holder.txtPrimary.setVisibility(View.VISIBLE);
        } else {
            holder.txtPrimary.setVisibility(View.GONE);
        }

        // Edit Click
        holder.editBtn.setOnClickListener(v -> {
            // TODO: Open edit screen
        });

        // Delete Click
        holder.deleteBtn.setOnClickListener(v -> {
            // TODO: Call delete API
        });
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtName, txtAddress, txtPrimary;
        ImageView editBtn, deleteBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtName = itemView.findViewById(R.id.txtName);
            txtAddress = itemView.findViewById(R.id.txtAddress);
            txtPrimary = itemView.findViewById(R.id.txtPrimary);

            editBtn = itemView.findViewById(R.id.editBtn);
            deleteBtn = itemView.findViewById(R.id.deleteBtn);
        }
    }
}