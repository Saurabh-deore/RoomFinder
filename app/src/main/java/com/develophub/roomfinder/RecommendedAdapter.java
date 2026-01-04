package com.develophub.roomfinder;

import android.content.Context;
import android.content.Intent;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy; // DiskCacheStrategy import

import java.util.List;

public class RecommendedAdapter extends RecyclerView.Adapter<RecommendedAdapter.RecommendationViewHolder> {

    private final Context context;
    private final List<RecommendationItem> recommendedList;
    private static final String TAG = "RecommendedAdapter";

    // कंस्ट्रक्टर
    public RecommendedAdapter(Context context, List<RecommendationItem> recommendedList) {
        this.context = context;
        this.recommendedList = recommendedList;
    }

    @NonNull
    @Override
    public RecommendationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recommendation_card, parent, false);
        return new RecommendationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecommendationViewHolder holder, int position) {
        RecommendationItem currentItem = recommendedList.get(position);

        // 1. डेटा बाइंडिंग
        holder.title.setText(currentItem.getTitle());
        holder.subtitle.setText(currentItem.getSubtitle());
        holder.price.setText(currentItem.getPrice());

        // ⭐ OPTIONAL: Image ko pura dikhane ke liye scale type set karein (agar XML mein nahi hai)
        // holder.image.setScaleType(ImageView.ScaleType.CENTER_CROP);

        // 2. Base64 / URL Image Loading Logic
        String imageData = currentItem.getImageUrl();

        if (imageData != null && !imageData.isEmpty()) {

            // Log data size for debugging
            Log.d(TAG, "Image Data Size for '" + currentItem.getTitle() + "': " + imageData.length() + " bytes.");

            if (imageData.startsWith("http") || imageData.startsWith("https")) {
                // A. Agar data URL hai
                Glide.with(context)
                        .load(imageData)
                        .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache URL images
                        .placeholder(R.drawable.logo)
                        .error(R.drawable.like)
                        .into(holder.image);

            } else {
                // B. Agar data Base64 string hai
                try {
                    if (imageData.length() > 500) { // Check if Base64 string is reasonably long
                        byte[] imageBytes = Base64.decode(imageData, Base64.DEFAULT);

                        Glide.with(context)
                                .asBitmap()
                                .load(imageBytes)
                                .placeholder(R.drawable.logo)
                                .error(R.drawable.like)
                                .into(holder.image);
                    } else {
                        // Base64 string choti hai ya corrupted hai
                        Log.w(TAG, "Base64 string too short or corrupted for: " + currentItem.getTitle());
                        // ⭐ SUDHAR: Glide use karke image clear karein
                        Glide.with(context).load(R.drawable.like).into(holder.image);
                    }
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "Base64 decoding failed for: " + currentItem.getTitle() + ": " + e.getMessage());
                    // ⭐ SUDHAR: Glide use karke image clear karein
                    Glide.with(context).load(R.drawable.like).into(holder.image);
                }
            }
        } else {
            // Image data available nahi hai
            Log.w(TAG, "Image URL is null or empty for item: " + currentItem.getTitle());
            // ⭐ SUDHAR: Glide use karke image clear karein
            Glide.with(context).load(R.drawable.logo).into(holder.image);
        }

        // 3. क्लिक हैंडलिंग
        holder.itemView.setOnClickListener(v -> {
            String listingId = currentItem.getListingId();

            if (listingId != null && !listingId.isEmpty()) {
                Intent intent = new Intent(context, ListingDetailsActivity.class);
                intent.putExtra("LISTING_ID", listingId);
                context.startActivity(intent);
            } else {
                Toast.makeText(context, "Error: Listing ID not found for " + currentItem.getTitle(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return recommendedList.size();
    }

    // ⭐ ViewHolder क्लास
    static class RecommendationViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title;
        TextView subtitle;
        TextView price;

        public RecommendationViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.recommendationImage);
            title = itemView.findViewById(R.id.recommendationTitle);
            subtitle = itemView.findViewById(R.id.recommendationSubtitle);
            price = itemView.findViewById(R.id.recommendationPrice);
        }
    }
}