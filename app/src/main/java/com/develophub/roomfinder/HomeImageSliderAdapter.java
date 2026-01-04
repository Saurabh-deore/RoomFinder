package com.develophub.roomfinder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * ViewPager2 ke liye adapter jo Dashboard par Drawable images dikhayega.
 */
public class HomeImageSliderAdapter extends RecyclerView.Adapter<HomeImageSliderAdapter.SliderViewHolder> {

    // --- A. डेटा मॉडल (Data Model) ---
    // Ab yeh String URL ki jagah Integer Resource ID lega
    public static class ImageSliderItem {
        public int imageRes; // String imageUrl se badal kar int imageRes kiya gaya
        public String title;
        public String subtitle;

        public ImageSliderItem(int imageRes, String title, String subtitle) {
            this.imageRes = imageRes;
            this.title = title;
            this.subtitle = subtitle;
        }
    }

    private final List<ImageSliderItem> sliderItems;

    public HomeImageSliderAdapter(List<ImageSliderItem> sliderItems) {
        this.sliderItems = sliderItems;
    }

    @NonNull
    @Override
    public SliderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_image_slider, parent, false);
        return new SliderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SliderViewHolder holder, int position) {
        ImageSliderItem item = sliderItems.get(position);

        // --- Drawable Image Set Karna ---
        // Glide ki zaroorat nahi hai local images ke liye
        holder.imageView.setImageResource(item.imageRes);

        // --- Text Set Karein ---
        holder.tvTitle.setText(item.title);
        holder.tvSubtitle.setText(item.subtitle);

        // --- Click Handling ---
        holder.itemView.setOnClickListener(v -> {
            Toast.makeText(holder.itemView.getContext(), "Clicked: " + item.title, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return sliderItems.size();
    }

    // --- B. View Holder Class ---
    static class SliderViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView tvTitle;
        TextView tvSubtitle;

        public SliderViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.slider_image);
            tvTitle = itemView.findViewById(R.id.slider_title);
            tvSubtitle = itemView.findViewById(R.id.slider_subtitle);
        }
    }
}