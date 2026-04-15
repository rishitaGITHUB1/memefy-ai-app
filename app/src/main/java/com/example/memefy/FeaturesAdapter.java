package com.example.memefy;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FeaturesAdapter extends RecyclerView.Adapter<FeaturesAdapter.FeatureViewHolder> {

    private Context context;
    private List<Feature> features;

    public FeaturesAdapter(Context context, List<Feature> features) {
        this.context = context;
        this.features = features;
    }

    @NonNull
    @Override
    public FeatureViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_feature_card, parent, false);
        return new FeatureViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeatureViewHolder holder, int position) {
        Feature feature = features.get(position);
        holder.emoji.setText(feature.getEmoji());
        holder.title.setText(feature.getTitle());
        holder.description.setText(feature.getDescription());
    }

    @Override
    public int getItemCount() {
        return features.size();
    }

    public static class FeatureViewHolder extends RecyclerView.ViewHolder {
        TextView emoji, title, description;

        public FeatureViewHolder(@NonNull View itemView) {
            super(itemView);
            emoji = itemView.findViewById(R.id.feature_emoji);
            title = itemView.findViewById(R.id.feature_title);
            description = itemView.findViewById(R.id.feature_description);
        }
    }
}