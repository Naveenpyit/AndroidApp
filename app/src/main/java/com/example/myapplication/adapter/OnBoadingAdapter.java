package com.example.myapplication.adapter;

import static android.view.View.VISIBLE;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.activity.OnboadingScreen;

public class OnBoadingAdapter extends RecyclerView.Adapter<OnBoadingAdapter.ViewHolder> {
    Context context;
    String header[];
    int image[];
    String title[];
    String subtitle[];

    public OnBoadingAdapter(OnboadingScreen onboadingScreen, String[] header, int[] image, String[] title, String[] subtite) {
        this.context = onboadingScreen;
        this.header = header;
        this.image = image;
        this.title = title;
        this.subtitle = subtite;
    }

    @NonNull
    @Override
    public OnBoadingAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.onboarding_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OnBoadingAdapter.ViewHolder holder, int position) {
        holder.lets_started.setText(header[position]);

        // ✅ "Let's Get Started" text in red
        holder.lets_started.setTextColor(Color.parseColor("#E41D27"));

        holder.image.setImageResource(image[position]);
        holder.title.setText(title[position]);
        holder.desc.setText(subtitle[position]);

        // Animations
        holder.lets_started.setTranslationY(600f);
        holder.image.setTranslationY(-600f);
        holder.title.setTranslationX(-600f);
        holder.desc.setTranslationX(600f);

        holder.lets_started.setVisibility(VISIBLE);
        holder.image.setVisibility(VISIBLE);
        holder.title.setVisibility(VISIBLE);
        holder.desc.setVisibility(VISIBLE);

        holder.lets_started.animate().setDuration(700).translationY(0).start();
        holder.image.animate().setDuration(700).translationY(0).start();
        holder.title.animate().setDuration(700).translationX(0).start();
        holder.desc.animate().setDuration(700).translationX(0).start();
    }

    @Override
    public int getItemCount() {
        return title.length;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView lets_started, title, desc;
        private ImageView image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            lets_started = itemView.findViewById(R.id.lets_started);
            title        = itemView.findViewById(R.id.title);
            desc         = itemView.findViewById(R.id.desc);
            image        = itemView.findViewById(R.id.image);
        }
    }
}