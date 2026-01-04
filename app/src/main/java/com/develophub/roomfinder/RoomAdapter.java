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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List; // ⭐ Added List Import for image check
import java.util.Locale;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.RoomViewHolder> {

    private ArrayList<RoomModel> rooms;
    private Context context;

    public RoomAdapter(Context context, ArrayList<RoomModel> rooms) {
        this.context = context;
        this.rooms = rooms;
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

        // Data Binding
        holder.tvOwner.setText("Owner: " + (room.getOwner() != null ? room.getOwner() : "N/A"));
        holder.tvLocation.setText("Location: " + (room.getLocation() != null ? room.getLocation() : "N/A"));

        // Rent
        String rentText = "Rent: N/A";
        if (room.getRent() != null) {
            rentText = String.format(Locale.getDefault(), "Rent ₹ %,d", room.getRent());
        }
        holder.tvRent.setText(rentText);

        // Image Loading (Base64)
        List<String> images = room.getImages(); // List का उपयोग करें
        if (images != null && !images.isEmpty()) {
            try {
                String base64Image = images.get(0);
                byte[] decoded = Base64.decode(base64Image, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                if (bitmap != null) {
                    holder.ivRoomImage.setImageBitmap(bitmap);
                } else {
                    holder.ivRoomImage.setImageResource(R.drawable.searchroom);
                }
            } catch (Exception e) {
                Log.e("RoomAdapter", "Image Decoding Error: " + e.getMessage());
                holder.ivRoomImage.setImageResource(R.drawable.searchroom);
            }
        } else {
            holder.ivRoomImage.setImageResource(R.drawable.searchroom); // Default image
        }

        // ⭐ DELETE Button Logic (My Listings के लिए)
        // यह सुनिश्चित करता है कि अगर btnDeleteListing NULL है (यानी यह ID लेआउट में नहीं मिली), तो क्रैश न हो।
        if (holder.btnDeleteListing != null) {
            // हम मान रहे हैं कि यह एडॉप्टर My Listings के लिए उपयोग हो रहा है, इसलिए इसे VISIBLE करें
            holder.btnDeleteListing.setVisibility(View.VISIBLE);

            // btnContact को छिपाएँ, लेकिन Null Check के साथ
            if (holder.btnContact != null) {
                holder.btnContact.setVisibility(View.GONE);
            }

            holder.btnDeleteListing.setOnClickListener(v -> {
                new MaterialAlertDialogBuilder(context)
                        .setTitle("Delete Room Listing")
                        .setMessage("Are you sure you want to delete this listing permanently?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            performDelete(room, position);
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                        .show();
            });
        }

        // On click: Open RoomDetailActivity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, RoomDetailActivity.class);

            if (room.getId() == null) {
                Toast.makeText(context, "Error: Room ID missing.", Toast.LENGTH_SHORT).show();
                return;
            }
            intent.putExtra("roomId", room.getId());
            intent.putExtra("owner", room.getOwner());
            intent.putExtra("location", room.getLocation());
            intent.putExtra("rent", room.getRent() != null ? room.getRent() : 0);

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

    // ⭐ DELETE LOGIC METHOD
    private void performDelete(RoomModel model, int position) {
        if (model.getId() == null) {
            Toast.makeText(context, "Error: Room ID not found for deletion.", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Rooms");

        dbRef.child(model.getId()).removeValue()
                .addOnSuccessListener(aVoid -> {
                    // UI से आइटम हटाएँ
                    rooms.remove(position);
                    notifyItemRemoved(position);
                    Toast.makeText(context, "Room listing deleted successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to delete: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("RoomAdapter", "Error deleting room listing: " + e.getMessage());
                });
    }

    @Override
    public int getItemCount() {
        return rooms.size();
    }

    // ViewHolder क्लास
    static class RoomViewHolder extends RecyclerView.ViewHolder {
        ImageView ivRoomImage;
        TextView tvOwner, tvLocation, tvRent;
        Button btnContact;
        Button btnDeleteListing;

        public RoomViewHolder(@NonNull View itemView) {
            super(itemView);
            // सारे IDs खोजें
            ivRoomImage = itemView.findViewById(R.id.ivRoomImage);
            tvOwner = itemView.findViewById(R.id.tvOwner);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvRent = itemView.findViewById(R.id.tvRent);

            btnContact = itemView.findViewById(R.id.btnContact);
            btnDeleteListing = itemView.findViewById(R.id.btnDeleteListing);
        }
    }
}