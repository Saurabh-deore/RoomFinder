package com.develophub.roomfinder;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;

public class MyRoomListingsFragment extends Fragment {

    private static final String TAG = "MyRoomListingsFrag";

    private RecyclerView rvListings;
    private TextView tvEmptyList;

    // ⭐ FIX 1: RoommateAdapter को RoomAdapter से बदलें
    private RoomAdapter adapter;

    // ⭐ FIX 2: RoommateModel की जगह RoomModel का उपयोग करें
    private ArrayList<RoomModel> list;
    private DatabaseReference dbRef;
    private String currentUserId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_listing_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvListings = view.findViewById(R.id.rvListingsFragment);
        tvEmptyList = view.findViewById(R.id.tvEmptyList);
        tvEmptyList.setText("You have no Room listings.");

        // ⭐ FIX 3: RoomModel के साथ लिस्ट को इनिशियलाइज़ करें
        list = new ArrayList<>();

        // ⭐ FIX 4: RoomAdapter का उपयोग करें (जो RoomModel लिस्ट स्वीकार करता है)
        adapter = new RoomAdapter(getContext(), list);

        rvListings.setLayoutManager(new LinearLayoutManager(getContext()));
        rvListings.setAdapter(adapter);

        // --- बाकी Null Check लॉजिक ---
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Log.e(TAG, "User is not logged in. Cannot fetch listings.");
            tvEmptyList.setText("Please login to view your listings.");
            rvListings.setVisibility(View.GONE);
            tvEmptyList.setVisibility(View.VISIBLE);
            return;
        }

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.d(TAG, "CURRENT LOGGED-IN UID: " + currentUserId);

        dbRef = FirebaseDatabase.getInstance().getReference("Rooms");

        fetchListings();
    }

    private void fetchListings() {
        dbRef.orderByChild("ownerId").equalTo(currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        list.clear();

                        if (!snapshot.exists() || snapshot.getChildrenCount() == 0) {
                            Log.w(TAG, "No room listings found for UID: " + currentUserId);
                            updateUI();
                            return;
                        }

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            try {
                                // ⭐ FIX 5: RoomModel का उपयोग करें (यह लाइन पहले से सही थी)
                                RoomModel m = ds.getValue(RoomModel.class);

                                if (m != null) {
                                    m.setId(ds.getKey());
                                    list.add(m);
                                } else {
                                    Log.w(TAG, "Data mapping failed for key: " + ds.getKey());
                                }

                            } catch (DatabaseException e) {
                                Log.e(TAG, "Skipping bad Room listing data due to type mismatch: " + e.getMessage());
                            }
                        }

                        adapter.notifyDataSetChanged();
                        updateUI();
                        Log.i(TAG, "Successfully loaded " + list.size() + " room listings.");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Database error: " + error.getMessage());
                    }
                });
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