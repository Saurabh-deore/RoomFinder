package com.develophub.roomfinder;

import android.content.Context;
import android.graphics.Bitmap; // ✅ नया: Bitmap के लिए
import android.graphics.BitmapFactory; // ✅ नया: BitmapFactory के लिए
import android.util.Base64; // ✅ नया: Base64 के लिए
import android.util.Log; // Log के लिए
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

// ❌ Glide Import हटा दिया गया
// import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class ImageSliderAdapter extends RecyclerView.Adapter<ImageSliderAdapter.SliderViewHolder> {

    private Context context;
    private List<String> imageUrls; // इसमें अब Base64 स्ट्रिंग्स होंगी
    private static final String TAG = "ImageSliderAdapter";

    // Constructor
    public ImageSliderAdapter(Context context, List<String> imageUrls) {
        this.context = context;
        this.imageUrls = (imageUrls != null) ? imageUrls : new ArrayList<>();
    }

    @NonNull
    @Override
    public SliderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate a simple ImageView layout
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.slider_item, parent, false);
        return new SliderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SliderViewHolder holder, int position) {
        // यह Base64 स्ट्रिंग होगी
        String base64ImageString = imageUrls.get(position);

        // ⭐ FIX: Base64 डिकोडिंग लॉजिक
        if (base64ImageString != null && !base64ImageString.isEmpty()) {
            try {
                // 1. Base64 स्ट्रिंग को byte array में डिकोड करें
                byte[] decodedBytes = Base64.decode(base64ImageString, Base64.DEFAULT);

                // 2. byte array को Bitmap में बदलें
                Bitmap imageBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);

                if (imageBitmap != null) {
                    // 3. ImageView में Bitmap सेट करें
                    holder.imageView.setImageBitmap(imageBitmap);
                } else {
                    // डिकोड विफल होने पर
                    holder.imageView.setImageResource(R.drawable.user);
                    Log.e(TAG, "Bitmap decoding failed for Base64 string.");
                }
            } catch (IllegalArgumentException e) {
                // Base64 स्ट्रिंग अमान्य होने पर
                holder.imageView.setImageResource(R.drawable.user);
                Log.e(TAG, "Base64 decoding error: " + e.getMessage());
            } catch (Exception e) {
                holder.imageView.setImageResource(R.drawable.user);
                Log.e(TAG, "General decoding error: " + e.getMessage());
            }
        } else {
            holder.imageView.setImageResource(R.drawable.user);
        }
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    static class SliderViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public SliderViewHolder(@NonNull View itemView) {
            super(itemView);
            // सुनिश्चित करें कि R.id.slider_image आपके slider_item.xml में ImageView की ID है
            imageView = itemView.findViewById(R.id.slider_image);
        }
    }
}