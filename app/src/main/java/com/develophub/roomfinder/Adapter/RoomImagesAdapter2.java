package com.develophub.roomfinder;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RoomImagesAdapter2 extends RecyclerView.Adapter<RoomImagesAdapter2.ImageViewHolder> {

    private Context context;
    private ArrayList<String> images;

    // हम अनुमानित RecyclerView item dimensions का उपयोग करते हैं
    private static final int TARGET_WIDTH = 400; // Recycler View Item की अनुमानित चौड़ाई (Pixels)
    private static final int TARGET_HEIGHT = 300; // Recycler View Item की अनुमानित ऊँचाई (Pixels)

    public RoomImagesAdapter2(Context context, ArrayList<String> images) {
        this.context = context;
        this.images = images != null ? images : new ArrayList<>();
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_room_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String base64 = images.get(position);

        if (base64 != null && !base64.isEmpty()) {
            try {
                byte[] decoded = Base64.decode(base64, Base64.DEFAULT);

                // --- 💡 FIX: OOM और Downscaling Logic Start ---

                // 1. BitmapOptions सेट करें ताकि केवल bounds (साइज़) लिया जाए
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeByteArray(decoded, 0, decoded.length, options);

                // 2. inSampleSize की गणना करें
                options.inSampleSize = calculateInSampleSize(options, TARGET_WIDTH, TARGET_HEIGHT);

                // 3. अब, वास्तविक Bitmap को डिकोड करें
                options.inJustDecodeBounds = false;
                Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length, options);

                // --- 💡 FIX: OOM और Downscaling Logic End ---

                holder.ivImage.setImageBitmap(bitmap);

            } catch (Exception e) {
                // Log the exception for debugging
                e.printStackTrace();
                // Fallback image
                holder.ivImage.setImageResource(R.drawable.searchroom);
            }
        } else {
            holder.ivImage.setImageResource(R.drawable.searchroom);
        }
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    // --- Helper function to calculate the downscaling factor (inSampleSize) ---

    /**
     * Calculates how much to downscale the image to save memory.
     * @param options The BitmapFactory.Options containing the original dimensions.
     * @param reqWidth The target width of the ImageView.
     * @param reqHeight The target height of the ImageView.
     * @return The calculated inSampleSize value.
     */
    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {

        // इमेज का वास्तविक साइज़
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // तब तक inSampleSize की गणना करें जब तक कि दोनों dimension required height और width से बड़े रहें
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }


    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivRoomImage);
            // सुनिश्चित करें कि R.id.ivRoomImage सही है
        }
    }
}