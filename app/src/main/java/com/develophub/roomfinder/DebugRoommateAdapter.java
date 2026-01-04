package com.develophub.roomfinder;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.android.material.dialog.MaterialAlertDialogBuilder; // ⭐ Dialog Import

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DebugRoommateAdapter extends RecyclerView.Adapter<DebugRoommateAdapter.DebugVH> {

    private Context context;
    private ArrayList<RoommateModel> list;

    public DebugRoommateAdapter(Context context, ArrayList<RoommateModel> list) {
        this.context = context;
        this.list = (list != null) ? list : new ArrayList<>();
    }

    // --- UTILITY METHODS (for safe object parsing) ---
    private Integer getIntegerFromObject(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Number) {
            return ((Number) obj).intValue();
        }
        if (obj instanceof String) {
            try {
                return Integer.parseInt((String) obj);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
    private String getStringFromObject(Object obj) {
        return (obj != null) ? String.valueOf(obj) : null;
    }

    @NonNull
    @Override
    public DebugVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // ⭐ item_roommate.xml को इन्फ्लेट करें
        View v = LayoutInflater.from(context).inflate(R.layout.item_roommate, parent, false);
        return new DebugVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull DebugVH h, int position) {
        RoommateModel m = list.get(position);

        // --- All Binding with Ultra-Safe Null Checks ---

        // 1. Name and Location
        if (h.tvName != null) {
            String name = m.getName();
            h.tvName.setText(name != null ? name : "Name: Unknown");
        }

        if (h.tvLocationDetail != null) {
            String location = m.getLocation();
            h.tvLocationDetail.setText("Location: " + (location != null ? location : "N/A"));
        }

        // 2. Budget (Safe Object Parsing)
        if (h.tvBudget != null) {
            Integer budgetInteger = getIntegerFromObject(m.getBudget());
            String budgetText;
            if (budgetInteger != null) {
                budgetText = String.format(Locale.getDefault(), "₹ %,d /month", budgetInteger);
            } else {
                budgetText = "₹ 0 /month";
            }
            h.tvBudget.setText(budgetText);
        }

        // 3. Description
        if (h.tvDescription != null) {
            String about = m.getAbout();
            h.tvDescription.setText(about != null ? about : "No description provided.");
        }

        // 4. Gender (Radio Buttons - Disable and Check)
        if (h.rbMale != null) h.rbMale.setEnabled(false);
        if (h.rbFemale != null) h.rbFemale.setEnabled(false);

        String gender = m.getGender() != null ? m.getGender().trim() : "";

        if (h.rgGender != null && h.rbMale != null && h.rbFemale != null) {
            if ("Male".equalsIgnoreCase(gender)) {
                h.rbMale.setChecked(true);
                h.rbFemale.setChecked(false);
            } else if ("Female".equalsIgnoreCase(gender)) {
                h.rbMale.setChecked(false);
                h.rbFemale.setChecked(true);
            } else {
                h.rgGender.clearCheck();
            }
        }

        // 5. Rating (Hardcoded/Dummy value)
        if (h.ratingBar != null) {
            float rating = 4.5f;
            h.ratingBar.setRating(rating);
        }
        if (h.tvRatingValue != null) {
            h.tvRatingValue.setText("4.5");
        }

        // 6. Image Loading Logic
        if (h.ivProfile != null) {
            String imageBase64 = null;
            List<String> images = m.getImages();

            if (images != null && !images.isEmpty()) {
                imageBase64 = images.get(0);
            } else if (m.getFirstImageUrl() != null) {
                imageBase64 = m.getFirstImageUrl();
            }

            if (imageBase64 != null && !imageBase64.trim().isEmpty()) {
                try {
                    byte[] decoded = Base64.decode(imageBase64, Base64.DEFAULT);
                    Bitmap bmp = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                    if (bmp != null) {
                        h.ivProfile.setImageBitmap(bmp);
                    } else {
                        h.ivProfile.setImageResource(R.drawable.user); // Placeholder
                    }
                } catch (Exception e) {
                    Log.e("DebugAdapter", "Image Decoding Error: " + e.getMessage());
                    h.ivProfile.setImageResource(R.drawable.user); // Placeholder
                }
            } else {
                h.ivProfile.setImageResource(R.drawable.user); // Default image
            }
        }

        // 7. Item Click (Assuming RoommateDetailActivity exists)
        h.itemView.setOnClickListener(v -> {
            Intent i = new Intent(context, RoommateDetailActivity.class);
            i.putExtra("name", m.getName());
            i.putExtra("age", getStringFromObject(m.getAge()));
            i.putExtra("gender", m.getGender());
            i.putExtra("budget", getStringFromObject(m.getBudget()));
            i.putExtra("location", m.getLocation());
            i.putExtra("about", m.getAbout());
            i.putStringArrayListExtra("images", m.getImages() != null ? new ArrayList<>(m.getImages()) : null);
            context.startActivity(i);
        });

        // ⭐ DELETE and CONTACT BUTTONS LOGIC STARTS HERE

        // 8. Contact Button: My Listings में छिपाएँ
        if (h.btnContact != null) {
            h.btnContact.setVisibility(View.GONE);
        }

        // 9. DELETE Button Logic: My Listings में दिखाएँ
        if (h.btnDeleteListing != null) {
            h.btnDeleteListing.setVisibility(View.VISIBLE); // ⭐ बटन को VISIBLE करें
            h.btnDeleteListing.setOnClickListener(v -> {
                // कन्फर्मेशन डायलॉग दिखाएँ
                new MaterialAlertDialogBuilder(context)
                        .setTitle("Delete Listing")
                        .setMessage("Are you sure you want to delete this listing? This action cannot be undone.")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            performDelete(m, position);
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                        .show();
            });
        }
    }

    // ⭐ DELETE LOGIC METHOD
    private void performDelete(RoommateModel model, int position) {
        if (model.getId() == null) {
            Toast.makeText(context, "Error: Listing ID not found.", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Roommates");

        dbRef.child(model.getId()).removeValue()
                .addOnSuccessListener(aVoid -> {
                    list.remove(position);
                    notifyItemRemoved(position);
                    Toast.makeText(context, "Listing deleted successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to delete: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("DebugAdapter", "Error deleting listing: " + e.getMessage());
                });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class DebugVH extends RecyclerView.ViewHolder {
        // ⭐ सारे व्यूज को यहाँ घोषित करें
        ImageView ivProfile;
        MaterialButton btnContact, btnDeleteListing; // ⭐ DELETE बटन वेरिएबल
        TextView tvName, tvLocationDetail, tvBudget, tvDescription, tvRatingValue;
        RatingBar ratingBar;
        RadioGroup rgGender;
        RadioButton rbMale, rbFemale;

        public DebugVH(@NonNull View itemView) {
            super(itemView);
            // ⭐ सारे IDs यहाँ हैं
            ivProfile = itemView.findViewById(R.id.ivProfile);
            btnContact = itemView.findViewById(R.id.btnContact);
            btnDeleteListing = itemView.findViewById(R.id.btnDeleteListing); // ⭐ DELETE बटन findViewById

            tvName = itemView.findViewById(R.id.tvName);
            tvLocationDetail = itemView.findViewById(R.id.tvLocationDetail);
            tvBudget = itemView.findViewById(R.id.tvBudget);
            tvDescription = itemView.findViewById(R.id.tvDescription);

            rgGender = itemView.findViewById(R.id.rgGender);
            rbMale = itemView.findViewById(R.id.rbMale);
            rbFemale = itemView.findViewById(R.id.rbFemale);
        }
    }
}