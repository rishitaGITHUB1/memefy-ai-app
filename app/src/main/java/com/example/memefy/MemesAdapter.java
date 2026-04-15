package com.example.memefy;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MemesAdapter extends RecyclerView.Adapter<MemesAdapter.MemeViewHolder> {

    public interface OnMemeClickListener {
        void onMemeClick(int imageResource, int index);
    }

    private final Context context;
    private final List<Meme> memes;
    private final OnMemeClickListener listener;

    public MemesAdapter(Context context, List<Meme> memes, OnMemeClickListener listener) {
        this.context  = context;
        this.memes    = memes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MemeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_meme, parent, false);
        return new MemeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemeViewHolder holder, int position) {
        Meme meme = memes.get(position);
        holder.memeImage.setImageResource(meme.getImageResource());
        holder.memeCaption.setText(meme.getCaption());
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onMemeClick(meme.getImageResource(), position);
        });
    }

    @Override
    public int getItemCount() { return memes.size(); }

    public static class MemeViewHolder extends RecyclerView.ViewHolder {
        ImageView memeImage;
        TextView  memeCaption;
        public MemeViewHolder(@NonNull View itemView) {
            super(itemView);
            memeImage   = itemView.findViewById(R.id.meme_image);
            memeCaption = itemView.findViewById(R.id.meme_caption);
        }
    }
}