package com.develophub.roomfinder;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class ListingDetailsActivity extends AppCompatActivity {

    private static final String TAG = "ListingDetailsActivity";

    // --- 1. UI Views (New IDs from activity_listing_details.xml) ---
    private Toolbar toolbar;
    private RecyclerView rvRoomImagesDetail; // For Image Slider
    private TextView tvRentDetail, tvRoomTypeDetail, tvLocationDetail, tvOwnerDetail, tvContactDetail;
    private Button btnOpenMap, btnCallOwner;
    private ImageButton btnAddFavoriteRoom;

    private String listingId;
    private String contactNumber;
    private String mapLink;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listing_details);

        // --- 2. Views Initialization ---
        // Toolbar Setup
        toolbar = findViewById(R.id.toolbar_room_detail);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Room Details");
        }
        toolbar.setNavigationOnClickListener(v -> finish()); // Back button functionality

        // Content Views
        rvRoomImagesDetail = findViewById(R.id.rvRoomImagesDetail);
        tvRentDetail = findViewById(R.id.tvRentDetail);
        tvRoomTypeDetail = findViewById(R.id.tvRoomTypeDetail);
        tvLocationDetail = findViewById(R.id.tvLocationDetail);
        tvOwnerDetail = findViewById(R.id.tvOwnerDetail);
        tvContactDetail = findViewById(R.id.tvContactDetail);

        // Bottom Bar Buttons
        btnOpenMap = findViewById(R.id.btnOpenMap);
        btnCallOwner = findViewById(R.id.btnCallOwner);
        btnAddFavoriteRoom = findViewById(R.id.btnAddFavoriteRoom);


        // --- 3. Intent Data Retrieval ---
        listingId = getIntent().getStringExtra("LISTING_ID");

        if (listingId == null || listingId.isEmpty()) {
            Toast.makeText(this, "Error: Listing ID missing.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // --- 4. Data Fetching ---
        fetchListingDetails(listingId);

        // --- 5. Click Listeners ---
        btnCallOwner.setOnClickListener(v -> callOwner());
        btnOpenMap.setOnClickListener(v -> openMap());
        // Favorite button logic will go here
    }

    private void fetchListingDetails(String id) {
        // Data seedhe 'Rooms' node se load hoga
        DatabaseReference detailRef = FirebaseDatabase.getInstance()
                .getReference("Rooms")
                .child(id);

        Log.d(TAG, "Fetching data from path: Rooms/" + id);

        detailRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    RoomDataModel roomData = snapshot.getValue(RoomDataModel.class);

                    if (roomData != null) {
                        displayDetails(roomData);
                    } else {
                        Toast.makeText(ListingDetailsActivity.this, "Listing data is corrupted (RoomDataModel failed).", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(ListingDetailsActivity.this, "Listing not found.", Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Listing ID " + id + " not found at the path.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ListingDetailsActivity.this, "Failed to load details: " + error.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "Database Error: " + error.getMessage());
            }
        });
    }

    private void displayDetails(RoomDataModel item) {
        // --- Data Binding to New Views ---

        // Price and Type
        tvRentDetail.setText(item.getListingPrice());
        tvRoomTypeDetail.setText(item.getRoomType() != null ? item.getRoomType() : "N/A");

        // Location
        String address = item.getAddress();
        tvLocationDetail.setText(address != null ? address : "Location N/A");

        // Owner Info
        tvOwnerDetail.setText(item.getOwner() != null ? item.getOwner() : "Owner N/A");

        // Contact Info (Data store karein aur TextView mein set karein)
        contactNumber = item.getContact();
        tvContactDetail.setText(contactNumber != null ? contactNumber : "Contact N/A");

        // Map Link (Data store karein)



        // --- Image Slider Setup (RecyclerView) ---
        List<String> images = item.getImages();
        if (images != null && !images.isEmpty()) {
            DetailImageAdapter imageAdapter = new DetailImageAdapter(this, images);
            // Horizontal layout manager set karein
            rvRoomImagesDetail.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            rvRoomImagesDetail.setAdapter(imageAdapter);
        } else {
            // Agar images nahi hain, toh RecyclerView ko hide kar sakte hain
            rvRoomImagesDetail.setVisibility(View.GONE);
            Log.w(TAG, "No images found for this listing.");
        }
    }


    // --- Utility Methods for Buttons ---

    private void callOwner() {
        if (contactNumber != null && !contactNumber.isEmpty()) {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + contactNumber));
            try {
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "Cannot initiate call.", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Dial intent failed: " + e.getMessage());
            }
        } else {
            Toast.makeText(this, "Contact number not available.", Toast.LENGTH_SHORT).show();
        }
    }

    private void openMap() {
        if (mapLink != null && !mapLink.isEmpty()) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mapLink));
            // Check if there is an app that can handle this intent
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(this, "No application found to view the map link.", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "Map link is not available.", Toast.LENGTH_SHORT).show();
        }
    }
}