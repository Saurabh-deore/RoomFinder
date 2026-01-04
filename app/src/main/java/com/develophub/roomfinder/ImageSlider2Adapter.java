package com.develophub.roomfinder;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide; // **Note:** Glide लाइब्रेरी को build.gradle में जोड़ना अनिवार्य है।

import java.util.List;

public class ImageSlider2Adapter extends RecyclerView.Adapter<ImageSlider2Adapter.SliderViewHolder> {

    private final Context context;
    private final List<String> imageUrls;

    // Constructor: Context और Image URLs की लिस्ट प्राप्त करता है
    public ImageSlider2Adapter(Context context, List<String> imageUrls) {
        this.context = context;
        this.imageUrls = imageUrls;
    }

    // 1. ViewHolder Definition
    public static class SliderViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public SliderViewHolder(@NonNull View itemView) {
            super(itemView);
            // onCreateViewHolder में सीधे ImageView का उपयोग किया गया है
            imageView = (ImageView) itemView;
        }
    }

    // 2. onCreateViewHolder: ViewHolder बनाता है
    @NonNull
    @Override
    public SliderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        // एक साधारण ImageView को व्यू के रूप में Inflate करें
        // हम यहाँ dynamic रूप से ImageView बनाते हैं
        ImageView imageView = new ImageView(context);
        imageView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        return new SliderViewHolder(imageView);
    }

    // 3. onBindViewHolder: डेटा (Image URL) को व्यू (ImageView) से जोड़ता है
    @Override
    public void onBindViewHolder(@NonNull SliderViewHolder holder, int position) {
        String imageUrl = imageUrls.get(position);

        // Glide का उपयोग करके URL से इमेज लोड करें
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(imageUrl)
                    // यदि इमेज लोड नहीं होती है, तो यह Placeholder दिखाई देगा
                    .placeholder(R.drawable.user)
                    .into(holder.imageView);
        } else {
            // यदि URL null/empty है
            holder.imageView.setImageResource(R.drawable.user);
        }
    }

    // 4. getItemCount: स्लाइड करने के लिए इमेज की संख्या बताता है
    @Override
    public int getItemCount() {
        return imageUrls.size();
    }
}