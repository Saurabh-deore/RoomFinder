package com.develophub.roomfinder;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class MyListingsAdapter extends RecyclerView.Adapter<MyListingsAdapter.ViewHolder> {

    private final Context context;
    // FIX 1: List type को PgModel से ListingItemModel में बदला गया
    private final List<ListingItemModel> listingsList;
    private final DeleteListener deleteListener;

    // 1. DeleteListener Interface
    public interface DeleteListener {
        // pgId को listingId या itemId कहना ज़्यादा सही है
        void onDeleteConfirmed(String listingId, int position);
    }

    // Constructor: List type को PgModel से ListingItemModel में बदला गया
    public MyListingsAdapter(Context context, List<ListingItemModel> listingsList, DeleteListener deleteListener) {
        this.context = context;
        this.listingsList = listingsList; // FIX 2: List type अब ListingItemModel है
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_my_listing, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // FIX 3: लिस्ट से डेटा निकालने के लिए listingsList का उपयोग करें
        ListingItemModel model = listingsList.get(position);

        // डेटा सेट करना
        holder.tvTitle.setText(model.getName());
        holder.tvRent.setText("₹" + model.getRent() + " / month");
        holder.tvLocation.setText(model.getAddress());

        // NOTE: इमेज डिस्प्ले लॉजिक यहाँ आएगा। Glide या Picasso का उपयोग करें।

        // ----------------------------------------------------
        // 2. DELETE BUTTON LOGIC
        // ----------------------------------------------------
        holder.btnDelete.setOnClickListener(v -> {

            // Confirmation Dialog दिखाना
            new AlertDialog.Builder(context)
                    .setTitle("Delete Listing: " + model.getName())
                    .setMessage("क्या आप निश्चित हैं कि आप इस लिस्टिंग को हटाना चाहते हैं? यह डेटाबेस से स्थायी रूप से हट जाएगी।")
                    .setPositiveButton("हाँ, Delete Karo", (dialog, which) -> {

                        // DeleteListener के माध्यम से Activity को सूचित करना
                        if (deleteListener != null) {
                            // Activity में परिभाषित Firebase Deletion method को कॉल करना
                            deleteListener.onDeleteConfirmed(model.getId(), holder.getAdapterPosition());
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return listingsList.size();
    }

    // ViewHolder क्लास
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvRent, tvLocation;
        ImageView ivImage;
        ImageButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvListingTitle);
            tvRent = itemView.findViewById(R.id.tvListingRent);
            tvLocation = itemView.findViewById(R.id.tvListingLocation);
            ivImage = itemView.findViewById(R.id.ivListingImage);
            btnDelete = itemView.findViewById(R.id.btnDeleteListing);
        }
    }
}