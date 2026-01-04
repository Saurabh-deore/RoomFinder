package com.develophub.roomfinder;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import java.util.ArrayList;
import java.util.Locale;

public class SearchRoomAdapter extends RecyclerView.Adapter<SearchRoomAdapter.RoomViewHolder> {

    private ArrayList<RoomModel> rooms;
    private Context context;
    private static final String TAG = "SearchRoomAdapter";

    public SearchRoomAdapter(ArrayList<RoomModel> rooms, Context context) {
        this.rooms = rooms;
        this.context = context;
    }

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_room, parent, false);
        return new RoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
        RoomModel room = rooms.get(position);

        // --- Data Binding and Null Safety ---

        // Owner/Location (String)
        holder.tvOwner.setText("Name: " + room.getOwner());
        holder.tvLocation.setText("Location: " + room.getLocation());

        // Rent (Integer/Null Safety)
        String rentText = "Rent: N/A";
        if (room.getRent() != null) {
            // NullPointerException से बचने के लिए Null Check
            rentText = String.format(Locale.getDefault(), "Rent ₹ %,d", room.getRent());
        }
        holder.tvRent.setText(rentText);

        // --- Image Loading (Base64) ---
        if (room.getImages() != null && room.getImages().size() > 0) {
            try {
                String base64Image = room.getImages().get(0);
                byte[] decoded = Base64.decode(base64Image, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                holder.ivRoomImage.setImageBitmap(bitmap);
            } catch (Exception e) {
                Log.e(TAG, "Error decoding Base64 image: " + e.getMessage());
                holder.ivRoomImage.setImageResource(R.drawable.searchroom);
            }
        } else {
            holder.ivRoomImage.setImageResource(R.drawable.searchroom);
        }

        // --- On Click: Open RoomDetailActivity ---
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, RoomDetailActivity.class);

            // 1. ✅ CRITICAL FIX: केवल रूम ID भेजें। पूरा Base64 डेटा न भेजें।
            if (room.getId() == null) {
                Toast.makeText(context, "Error: Room ID missing.", Toast.LENGTH_SHORT).show();
                return;
            }
            intent.putExtra("roomId", room.getId());

            // 2. ✅ बाकी छोटा डेटा भेजें (UI डिस्प्ले के लिए उपयोगी)
            intent.putExtra("owner", room.getOwner());
            intent.putExtra("location", room.getLocation());

            // Null-safe sending (getRent() Integer लौटाता है)
            intent.putExtra("rent", room.getRent() != null ? room.getRent() : 0);

            // contact (Object) को String के रूप में भेजें
            if (room.getContact() != null) {
                intent.putExtra("contact", room.getContact().toString());
            } else {
                intent.putExtra("contact", "N/A");
            }

            intent.putExtra("roomType", room.getRoomType());
            intent.putExtra("mapLink", room.getMapLink());

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return rooms.size();
    }

    static class RoomViewHolder extends RecyclerView.ViewHolder {
        ImageView ivRoomImage;
        TextView tvOwner, tvLocation, tvRent;

        public RoomViewHolder(@NonNull View itemView) {
            super(itemView);
            ivRoomImage = itemView.findViewById(R.id.ivRoomImage);
            tvOwner = itemView.findViewById(R.id.tvOwner);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvRent = itemView.findViewById(R.id.tvRent);
        }
    }
}