package com.develophub.roomfinder;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PgAdapter extends RecyclerView.Adapter<PgAdapter.PgViewHolder> implements Filterable {

    private final Context context;
    private List<PgModel> pgList; // Ye list screen par dikhti hai
    private List<PgModel> pgListFull; // Ye original copy hai search ke liye
    private static final String TAG = "PgAdapter";
    private final String currentUserId;
    private final boolean isMyListingsScreen;

    public PgAdapter(Context context, List<PgModel> pgList) {
        this(context, pgList, false);
    }

    public PgAdapter(Context context, List<PgModel> pgList, boolean isMyListingsScreen) {
        this.context = context;
        this.pgList = pgList;
        this.pgListFull = new ArrayList<>(pgList); // Initial copy
        this.isMyListingsScreen = isMyListingsScreen;

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            this.currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            this.currentUserId = null;
        }
    }

    // ⭐ CRITICAL FIX: Firebase se data aane par dono lists ko update karne ke liye
    public void updateList(List<PgModel> newList) {
        this.pgList = new ArrayList<>(newList);
        this.pgListFull = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PgViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_pg_card, parent, false);
        return new PgViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PgViewHolder holder, int position) {
        PgModel currentPg = pgList.get(position);

        holder.tvPgName.setText(currentPg.getName());
        holder.tvPgLocation.setText(currentPg.getFullAddress());

        String rentText = "Rent: N/A";
        if (currentPg.getMonthlyRent() != null) {
            rentText = String.format(Locale.getDefault(), "₹ %,d/Month", currentPg.getMonthlyRent());
        }
        holder.tvPgRent.setText(rentText);
        holder.tvOccupancyType.setText(currentPg.getOccupancyType());

        // Image Loading Logic
        String base64ImageString = currentPg.getFirstImageUrl();
        if (base64ImageString != null && !base64ImageString.isEmpty()) {
            try {
                byte[] decodedBytes = Base64.decode(base64ImageString, Base64.DEFAULT);
                Bitmap imageBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                if (imageBitmap != null) {
                    holder.imgPgPhoto.setImageBitmap(imageBitmap);
                } else {
                    holder.imgPgPhoto.setImageResource(R.drawable.user);
                }
            } catch (Exception e) {
                holder.imgPgPhoto.setImageResource(R.drawable.user);
            }
        } else {
            holder.imgPgPhoto.setImageResource(R.drawable.user);
        }

        // Delete Button Logic
        String pgOwnerId = currentPg.getOwnerId();
        if (isMyListingsScreen && currentUserId != null && currentUserId.equals(pgOwnerId)) {
            holder.btnDeleteImage.setVisibility(View.VISIBLE);
            holder.btnDeleteImage.setOnClickListener(v -> showDeleteConfirmationDialog(currentPg, position));
        } else {
            holder.btnDeleteImage.setVisibility(View.GONE);
        }

        // Buttons Click Listeners
        holder.btnViewDetails.setOnClickListener(v -> {
            Intent intent = new Intent(context, PgDetailsActivity.class);
            intent.putExtra("pg_id", currentPg.getPgId());
            context.startActivity(intent);
        });

        holder.btnContactOwner.setOnClickListener(v -> {
            String phoneNumber = currentPg.getContactNumber();
            if (phoneNumber != null && !phoneNumber.isEmpty()) {
                Intent dialIntent = new Intent(Intent.ACTION_DIAL);
                dialIntent.setData(Uri.parse("tel:" + phoneNumber));
                context.startActivity(dialIntent);
            } else {
                Toast.makeText(context, "Contact number not available.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDeleteConfirmationDialog(PgModel pgModel, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Delete PG Listing")
                .setMessage("Are you sure?")
                .setPositiveButton("Delete", (dialog, which) -> performDelete(pgModel, position))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performDelete(PgModel pgModel, int position) {
        if (pgModel.getPgId() == null) return;
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("pgs");
        dbRef.child(pgModel.getPgId()).removeValue()
                .addOnSuccessListener(aVoid -> {
                    pgList.remove(position);
                    notifyItemRemoved(position);
                    pgListFull.remove(pgModel);
                    Toast.makeText(context, "Deleted!", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public int getItemCount() {
        return pgList.size();
    }

    // ===============================================
    // SEARCH FILTER LOGIC
    // ===============================================
    @Override
    public Filter getFilter() {
        return pgFilter;
    }

    private final Filter pgFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<PgModel> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                // Agar search box khali hai toh poora data dikhao
                filteredList.addAll(pgListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase(Locale.getDefault()).trim();

                for (PgModel item : pgListFull) {
                    // Safety checks null pointers ke liye
                    String name = item.getName() != null ? item.getName().toLowerCase() : "";
                    String address = item.getFullAddress() != null ? item.getFullAddress().toLowerCase() : "";
                    String city = item.getCity() != null ? item.getCity().toLowerCase() : "";

                    if (name.contains(filterPattern) || address.contains(filterPattern) || city.contains(filterPattern)) {
                        filteredList.add(item);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            pgList.clear();
            if (results.values != null) {
                pgList.addAll((List<PgModel>) results.values);
            }
            notifyDataSetChanged();
        }
    };

    public static class PgViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPgPhoto;
        TextView tvPgName, tvPgLocation, tvPgRent, tvOccupancyType;
        Button btnViewDetails, btnContactOwner, btnDeleteImage;

        public PgViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPgPhoto = itemView.findViewById(R.id.imgPgPhoto);
            tvPgName = itemView.findViewById(R.id.tvPgName);
            tvPgLocation = itemView.findViewById(R.id.tvPgLocation);
            tvPgRent = itemView.findViewById(R.id.tvPgRent);
            tvOccupancyType = itemView.findViewById(R.id.tvOccupancyType);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
            btnContactOwner = itemView.findViewById(R.id.btnContactOwner);
            btnDeleteImage = itemView.findViewById(R.id.btnDeleteImage);
        }
    }
}