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

public class MyRoommateListingsFragment extends Fragment {

    private static final String TARGET_LISTING_TYPE = "Roommate_Need";
    private static final String TAG = "MyRoommateListingsFrag";

    private RecyclerView rvListings;
    private TextView tvEmptyList;

    // ⭐ ORIGINAL ADAPTER VARIABLE WAS HERE:
    // private com.example.roommates.RoommateAdapter adapter;

    // ⭐ DEBUGGING ADAPTER VARIABLE:
    private DebugRoommateAdapter debugAdapter; // <--- नया अडैप्टर वेरिएबल

    private ArrayList<RoommateModel> list;
    private DatabaseReference dbRef;
    private String currentUserId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // सुनिश्चित करें कि यह fragment_my_listing_list ही है
        return inflater.inflate(R.layout.fragment_my_listing_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvListings = view.findViewById(R.id.rvListingsFragment);
        tvEmptyList = view.findViewById(R.id.tvEmptyList);
        tvEmptyList.setText("You have no listings of this type.");

        list = new ArrayList<>();

        // ⭐ नया Debug Adapter इन्स्टेन्शिएट करें
        debugAdapter = new DebugRoommateAdapter(getContext(), list);

        if (rvListings != null) {
            rvListings.setLayoutManager(new LinearLayoutManager(getContext()));
            // ⭐ RecyclerView को नया Debug Adapter दें
            rvListings.setAdapter(debugAdapter);
        } else {
            Log.e(TAG, "RecyclerView (rvListingsFragment) not found in the layout.");
            return;
        }

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Log.e(TAG, "User is not logged in. Cannot fetch listings.");
            tvEmptyList.setText("Please login to view your listings.");
            rvListings.setVisibility(View.GONE);
            tvEmptyList.setVisibility(View.VISIBLE);
            return;
        }

        dbRef = FirebaseDatabase.getInstance().getReference("Roommates");
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Log.d(TAG, "Fragment view is initialized. Starting fetch...");

        fetchListings();
    }

    private void fetchListings() {
        // Filter by ownerId first
        Query query = dbRef.orderByChild("ownerId").equalTo(currentUserId);

        Log.d(TAG, "Fetching listings for UID: " + currentUserId + " (Filtering by TARGET_LISTING_TYPE)");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();

                if (!snapshot.exists() || snapshot.getChildrenCount() == 0) {
                    Log.w(TAG, "Query returned NO DATA.");
                    updateUI();
                    return;
                }

                for (DataSnapshot ds : snapshot.getChildren()) {
                    try {
                        RoommateModel m = ds.getValue(RoommateModel.class);

                        // FILTER RESTORED: listingType फ़िल्टर को वापस जोड़ा गया
                        if (m != null && TARGET_LISTING_TYPE.equals(m.getListingType())) {
                            m.setId(ds.getKey());
                            list.add(m);
                        }

                    } catch (DatabaseException e) {
                        Log.e(TAG, "Skipping BAD Roommate listing (DATA ERROR): " + e.getMessage());
                    }
                }

                // ⭐ Debug Adapter को नोटिफाई करें
                debugAdapter.notifyDataSetChanged();

                updateUI();
                Log.i(TAG, "Successfully loaded " + list.size() + " roommate listings.");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage());
            }
        });
    }

    private void updateUI() {
        if (rvListings == null || tvEmptyList == null) return;

        if (list.isEmpty()) {
            rvListings.setVisibility(View.GONE);
            tvEmptyList.setVisibility(View.VISIBLE);
        } else {
            rvListings.setVisibility(View.VISIBLE);
            tvEmptyList.setVisibility(View.GONE);
        }
    }
}