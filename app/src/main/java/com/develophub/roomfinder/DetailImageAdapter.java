package com.develophub.roomfinder;

import android.content.Context;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import java.util.List;

public class DetailImageAdapter extends RecyclerView.Adapter<DetailImageAdapter.ImageViewHolder> {

    private final Context context;
    private final List<String> imageList; // List of Base64 or URL strings
    private static final String TAG = "DetailImageAdapter";

    public DetailImageAdapter(Context context, List<String> imageList) {
        this.context = context;
        this.imageList = imageList;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // item_image_slider_detail.xml ko inflate karein
        View view = LayoutInflater.from(context).inflate(R.layout.item_image_slider_detail, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String imageData = imageList.get(position);

        if (imageData != null && !imageData.isEmpty()) {
            if (imageData.startsWith("http") || imageData.startsWith("https")) {
                // A. Agar data URL hai
                Glide.with(context)
                        .load(imageData)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.logo)
                        .error(R.drawable.like)
                        .into(holder.imageView);
            } else {
                // B. Agar data Base64 string hai
                try {
                    byte[] imageBytes = Base64.decode(imageData, Base64.DEFAULT);
                    Glide.with(context)
                            .asBitmap()
                            .load(imageBytes)
                            .placeholder(R.drawable.logo)
                            .error(R.drawable.like)
                            .into(holder.imageView);
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "Base64 decoding failed for image at position " + position + ": " + e.getMessage());
                    Glide.with(context).load(R.drawable.like).into(holder.imageView);
                }
            }
        } else {
            // Data missing hai
            Glide.with(context).load(R.drawable.logo).into(holder.imageView);
        }
    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            // ImageView ko item_image_slider_detail.xml se bind karein
            imageView = itemView.findViewById(R.id.detail_slider_image);
        }
    }
}