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

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Locale;

public class RoommateAdapter extends RecyclerView.Adapter<RoommateAdapter.VH> {

    private Context context;
    private ArrayList<RoommateModel> original;
    private ArrayList<RoommateModel> filtered;

    public RoommateAdapter(Context context, ArrayList<RoommateModel> list) {
        this.context = context;
        this.original = (list != null) ? new ArrayList<>(list) : new ArrayList<>();
        this.filtered = new ArrayList<>(this.original);
    }

    // ⭐ CRITICAL FIX: Firebase se naya data aane par dono lists ko update karne ke liye
    public void updateOriginalList(ArrayList<RoommateModel> newList) {
        this.original.clear();
        if (newList != null) {
            this.original.addAll(newList);
        }
        // Naya data aane par filtered list ko bhi refresh karein
        resetFilters();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_roommate, parent, false);
        return new VH(v);
    }

    private Integer getIntegerFromObject(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Number) return ((Number) obj).intValue();
        if (obj instanceof String) {
            try { return Integer.parseInt((String) obj); } catch (NumberFormatException e) { return null; }
        }
        return null;
    }

    private String getStringFromObject(Object obj) {
        return (obj != null) ? String.valueOf(obj) : null;
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        RoommateModel m = filtered.get(pos);

        // --- UI Binding ---
        h.tvName.setText(m.getName() != null ? m.getName() : "Unknown");
        h.tvLocationDetail.setText("Location: " + (m.getLocation() != null ? m.getLocation() : "N/A"));

        Integer budgetInteger = getIntegerFromObject(m.getBudget());
        h.tvBudget.setText(budgetInteger != null ? String.format(Locale.getDefault(), "₹ %,d /month", budgetInteger) : "₹ 0 /month");
        h.tvDescription.setText(m.getAbout() != null ? m.getAbout() : "No description provided.");

        // Gender UI
        h.rbMale.setEnabled(false);
        h.rbFemale.setEnabled(false);
        String gender = m.getGender() != null ? m.getGender().trim() : "";
        if ("Male".equalsIgnoreCase(gender)) {
            h.rbMale.setChecked(true); h.rbFemale.setChecked(false);
        } else if ("Female".equalsIgnoreCase(gender)) {
            h.rbMale.setChecked(false); h.rbFemale.setChecked(true);
        } else {
            h.rgGender.clearCheck();
        }

        // Image Loading
        String imageBase64 = m.getImageBase64();
        if (imageBase64 != null && !imageBase64.isEmpty()) {
            try {
                byte[] decoded = Base64.decode(imageBase64, Base64.DEFAULT);
                Bitmap bmp = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                h.ivProfile.setImageBitmap(bmp);
            } catch (Exception e) {
                h.ivProfile.setImageResource(R.drawable.user);
            }
        } else {
            h.ivProfile.setImageResource(R.drawable.user);
        }

        // --- Item Click (RoommateDetailActivity) ---
        h.itemView.setOnClickListener(v -> {
            String roommateId = m.getId();
            if (roommateId == null) {
                Toast.makeText(context, "Listing ID missing", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent i = new Intent(context, RoommateDetailActivity.class);
            i.putExtra("roommateId", roommateId); // ⭐ Correct Key
            context.startActivity(i);
        });

        // --- Favorite Button (Contact Button logic) ---
        h.btnContact.setOnClickListener(v -> {
            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                Toast.makeText(context, "Please login first", Toast.LENGTH_SHORT).show();
                return;
            }
            if (m.getId() == null) return;

            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            // Store in user favorites path
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Favorites")
                    .child(userId).child("roommates").child(m.getId());

            ref.setValue(m).addOnCompleteListener(task -> {
                if (task.isSuccessful()) Toast.makeText(context, "Added to Favorites!", Toast.LENGTH_SHORT).show();
            });
        });
    }

    @Override
    public int getItemCount() {
        return filtered.size();
    }

    public static class VH extends RecyclerView.ViewHolder {
        ImageView ivProfile;
        MaterialButton btnContact;
        TextView tvName, tvLocationDetail, tvBudget, tvDescription, tvRatingValue;
        RatingBar ratingBar;
        RadioGroup rgGender;
        RadioButton rbMale, rbFemale;

        public VH(@NonNull View itemView) {
            super(itemView);
            ivProfile = itemView.findViewById(R.id.ivProfile);
            btnContact = itemView.findViewById(R.id.btnContact);
            tvName = itemView.findViewById(R.id.tvName);
            tvLocationDetail = itemView.findViewById(R.id.tvLocationDetail);
            tvBudget = itemView.findViewById(R.id.tvBudget);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            rgGender = itemView.findViewById(R.id.rgGender);
            rbMale = itemView.findViewById(R.id.rbMale);
            rbFemale = itemView.findViewById(R.id.rbFemale);
        }
    }

    // ---------------------------------------------------------------------
    // FILTERING & RESET
    // ---------------------------------------------------------------------

    public void resetFilters() {
        filtered.clear();
        filtered.addAll(original);
        notifyDataSetChanged();
    }

    public void applyFilters(String locQuery, String gender, String minB, String maxB) {
        final String loc = locQuery != null ? locQuery.toLowerCase().trim() : "";
        final int min = (minB != null && !minB.isEmpty()) ? Integer.parseInt(minB) : 0;
        final int max = (maxB != null && !maxB.isEmpty()) ? Integer.parseInt(maxB) : Integer.MAX_VALUE;

        ArrayList<RoommateModel> tempList = new ArrayList<>();
        for (RoommateModel m : original) {
            boolean matches = true;

            // Location
            if (!loc.isEmpty() && (m.getLocation() == null || !m.getLocation().toLowerCase().contains(loc))) matches = false;

            // Gender
            if (matches && !gender.equals("Any") && (m.getGender() == null || !m.getGender().equalsIgnoreCase(gender))) matches = false;

            // Budget
            if (matches) {
                Integer b = getIntegerFromObject(m.getBudget());
                int curB = (b != null) ? b : 0;
                if (curB < min || curB > max) matches = false;
            }

            if (matches) tempList.add(m);
        }
        filtered.clear();
        filtered.addAll(tempList);
        notifyDataSetChanged();
    }
}