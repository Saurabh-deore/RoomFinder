package com.develophub.roomfinder;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;

public class MyPgListingsFragment extends Fragment {

    private static final String TARGET_LISTING_TYPE = "PG";
    private static final String TAG = "MyPgListingsFrag";

    private RecyclerView rvListings;
    private TextView tvEmptyList;
    private PgAdapter adapter;
    private ArrayList<PgModel> list;
    private DatabaseReference dbRef;
    private String currentUserId;

    // ⭐ NEW: Listener Reference
    private ValueEventListener valueEventListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_listing_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvListings = view.findViewById(R.id.rvListingsFragment);
        tvEmptyList = view.findViewById(R.id.tvEmptyList);
        tvEmptyList.setText("You have no PG listings.");

        list = new ArrayList<>();

        // ⭐ FIX: PgAdapter को 'true' Flag के साथ इनिशियलाइज़ करें
        // यह PgAdapter को बताता है कि यह 'My Listings' स्क्रीन है,
        // इसलिए मालिक होने पर Delete बटन दिखाना है।
        adapter = new PgAdapter(getContext(), list, true); // <--- TRUE Flag Added

        rvListings.setLayoutManager(new LinearLayoutManager(getContext()));
        rvListings.setAdapter(adapter);

        // --- User Check ---
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Log.e(TAG, "User is not logged in. Cannot fetch listings.");
            tvEmptyList.setText("Please login to view your listings.");
            rvListings.setVisibility(View.GONE);
            tvEmptyList.setVisibility(View.VISIBLE);
            return;
        }

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        dbRef = FirebaseDatabase.getInstance().getReference("pgs");

        // fetchListings() अब onResume() से कॉल होगा।
    }

    // ⭐ FIX 1: onResume में fetchListings को कॉल करें
    @Override
    public void onResume() {
        super.onResume();
        // यह सुनिश्चित करता है कि जब यूजर वापस इस Fragment पर आता है (जैसे Delete करके),
        // डेटाबेस से डेटा फिर से लोड हो
        if (currentUserId != null) {
            fetchListings();
        }
    }

    // ⭐ FIX 2: onStop में लिसनर को हटा दें (सफाई)
    @Override
    public void onStop() {
        super.onStop();
        if (dbRef != null && valueEventListener != null) {
            dbRef.removeEventListener(valueEventListener);
        }
    }


    private void fetchListings() {
        // पुराने लिसनर को हटा दें (यदि मौजूद हो)
        if (dbRef != null && valueEventListener != null) {
            dbRef.removeEventListener(valueEventListener);
        }

        // ⭐ Fix 3: addValueEventListener का उपयोग करें (Delete होने पर Live Update के लिए)
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    try {
                        PgModel m = ds.getValue(PgModel.class);

                        if (m != null) {
                            // Firebase Realtime DB से ID सेट करें
                            m.setPgId(ds.getKey());
                            list.add(m);
                        }
                    } catch (DatabaseException e) {
                        Log.e(TAG, "Skipping bad PG listing data: " + e.getMessage());
                    }
                }

                adapter.notifyDataSetChanged();
                updateUI();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage());
                Toast.makeText(getContext(), "Failed to load listings: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        };

        dbRef.orderByChild("ownerId").equalTo(currentUserId).addValueEventListener(valueEventListener);
    }

    private void updateUI() {
        if (list.isEmpty()) {
            rvListings.setVisibility(View.GONE);
            tvEmptyList.setVisibility(View.VISIBLE);
        } else {
            rvListings.setVisibility(View.VISIBLE);
            tvEmptyList.setVisibility(View.GONE);
        }
    }
}