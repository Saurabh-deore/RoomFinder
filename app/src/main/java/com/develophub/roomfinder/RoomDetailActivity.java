package com.develophub.roomfinder;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class RoomDetailActivity extends AppCompatActivity {

    private static final String TAG = "RoomDetailActivity";
    private TextView tvOwner, tvLocation, tvRent, tvContact, tvRoomType;
    private RecyclerView rvImages;
    private ImageButton btnAddFavoriteRoom;
    private Button btnCallOwner, btnOpenMap;
    private Toolbar toolbar;

    private String roomId;
    private String owner, location, mapLink, contact, roomType;
    private int rentValue = 0;
    private ArrayList<String> images = new ArrayList<>();

    private boolean isFavorite = false;
    private DatabaseReference favoriteRef;

    // ⭐ NEW: RoomModel2 ऑब्जेक्ट को स्टोर करने के लिए
    private RoomModel2 currentRoomModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_detail);

        // --- User Check ---
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Please log in to use the favorites feature.", Toast.LENGTH_LONG).show();
        }

        // --- Toolbar Setup ---
        toolbar = findViewById(R.id.toolbar_room_detail);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // --- Views Binding ---
        tvOwner = findViewById(R.id.tvOwnerDetail);
        tvLocation = findViewById(R.id.tvLocationDetail);
        tvRent = findViewById(R.id.tvRentDetail);
        tvContact = findViewById(R.id.tvContactDetail);
        tvRoomType = findViewById(R.id.tvRoomTypeDetail);
        rvImages = findViewById(R.id.rvRoomImagesDetail);
        btnAddFavoriteRoom = findViewById(R.id.btnAddFavoriteRoom);

        btnCallOwner = findViewById(R.id.btnCallOwner);
        btnOpenMap = findViewById(R.id.btnOpenMap);

        // --- Intent Data Retrieval ---
        roomId = getIntent().getStringExtra("roomId");
        owner = getIntent().getStringExtra("owner");
        location = getIntent().getStringExtra("location");
        mapLink = getIntent().getStringExtra("mapLink");
        contact = getIntent().getStringExtra("contact");
        roomType = getIntent().getStringExtra("roomType");
        rentValue = getIntent().getIntExtra("rent", 0);

        // Null-safe defaults
        if (owner == null) owner = "N/A";
        if (location == null) location = "N/A";
        if (mapLink == null) mapLink = "";
        if (contact == null) contact = "N/A";
        if (roomType == null) roomType = "N/A";

        // Set Text
        tvOwner.setText(owner);
        tvLocation.setText(location);
        tvRent.setText("₹" + String.valueOf(rentValue) + " / month");
        tvContact.setText(contact);
        tvRoomType.setText(roomType);

        // ⭐ NEW: currentRoomModel को initialize करें
        // हम Rent को String के बजाय Object (Integer) के रूप में पास कर रहे हैं
        currentRoomModel = new RoomModel2(owner, location, rentValue, null); // Image अभी null है

        // --- Firebase Image Fetching & RecyclerView Setup ---
        if (roomId != null && !roomId.isEmpty()) {
            fetchRoomImages(roomId);

            // Favorites Status Check and Database Reference setup
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                setupFavoriteReference();
            }
        } else {
            rvImages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            rvImages.setAdapter(new com.develophub.roomfinder.RoomImagesAdapter2(this, images));
            Toast.makeText(this, "Room ID missing. Cannot load images.", Toast.LENGTH_LONG).show();
        }

        // --- Listeners ---
        btnAddFavoriteRoom.setOnClickListener(v -> toggleRoomFavoriteStatus());
        btnCallOwner.setOnClickListener(v -> callOwner(contact));
        btnOpenMap.setOnClickListener(v -> openMapLocation(mapLink));
    }

    // ... (setupFavoriteReference, updateFavoriteButtonUI, callOwner, openMapLocation methods remain unchanged)

    /**
     * Favorites स्टेटस को टॉगल करता है (जोड़ता है या हटाता है)।
     */
    private void toggleRoomFavoriteStatus() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Please log in to use the favorites feature.", Toast.LENGTH_LONG).show();
            return;
        }

        if (isFavorite) {
            // Remove from Favorites
            favoriteRef.removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Removed from Favorites", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to remove from Favorites.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Add to Favorites
            // ⭐ FIX: पूरा RoomModel2 ऑब्जेक्ट सेव करें ताकि FavoritesAdapter इसे सीधे पढ़ सके।

            // Image को भी RoomModel2 में जोड़ें (ताकि कार्ड पर इमेज दिखे)
            if (!images.isEmpty()) {
                currentRoomModel.setImage(images.get(0));
            } else {
                currentRoomModel.setImage(null); // या कोई डिफ़ॉल्ट placeholder
            }

            // RoomModel2 को सेव करें
            favoriteRef.setValue(currentRoomModel).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Added to Favorites", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to add to Favorites: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


    /**
     * Firebase से Room ID का उपयोग करके images list (Base64 strings) को Fetch करता है।
     */
    private void fetchRoomImages(String roomId) {
        DatabaseReference imagesRef = FirebaseDatabase.getInstance()
                .getReference("Rooms")
                .child(roomId)
                .child("images");

        imagesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                images.clear();
                if (snapshot.exists()) {
                    for(DataSnapshot imgSnap : snapshot.getChildren()){
                        String base64 = imgSnap.getValue(String.class);
                        if (base64 != null) {
                            images.add(base64);
                        }
                    }
                }

                // इमेजेस लोड होने के बाद RoomModel में पहली इमेज भी सेट करें
                if (!images.isEmpty()) {
                    currentRoomModel.setImage(images.get(0));
                }

                // इमेजेस लोड होने के बाद Adapter और RecyclerView को सेट करें
                rvImages.setLayoutManager(new LinearLayoutManager(RoomDetailActivity.this, LinearLayoutManager.HORIZONTAL, false));
                com.develophub.roomfinder.RoomImagesAdapter2 imagesAdapter = new com.develophub.roomfinder.RoomImagesAdapter2(RoomDetailActivity.this, images);
                rvImages.setAdapter(imagesAdapter);

                // PagerSnapHelper अटैच करें
                SnapHelper snapHelper = new PagerSnapHelper();
                snapHelper.attachToRecyclerView(rvImages);

                if (images.isEmpty()) {
                    Toast.makeText(RoomDetailActivity.this, "No images found for this room.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RoomDetailActivity.this, "Failed to load images: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // ... (Rest of the methods: setupFavoriteReference, updateFavoriteButtonUI, callOwner, openMapLocation)

    private void setupFavoriteReference() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Favorite reference structure: Favorites/USER_ID/rooms/ROOM_ID
        favoriteRef = FirebaseDatabase.getInstance()
                .getReference("Favorites")
                .child(userId)
                .child("rooms") // type of listing
                .child(roomId);

        favoriteRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // यदि snapshot मौजूद है, तो लिस्टिंग Favorite है
                isFavorite = snapshot.exists();
                updateFavoriteButtonUI();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to read favorite status: " + error.getMessage());
            }
        });
    }

    private void updateFavoriteButtonUI() {
        if (isFavorite) {
            // ⭐ Replace 'heart_shape_filled' with your filled heart drawable resource
            btnAddFavoriteRoom.setImageResource(R.drawable.like);
            btnAddFavoriteRoom.setBackgroundTintList(null); // Optional: remove background tint if needed
        } else {
            // ⭐ Replace 'heart_shape_outline2' with your outline heart drawable resource
            btnAddFavoriteRoom.setImageResource(R.drawable.heart);
            btnAddFavoriteRoom.setBackgroundTintList(null); // Optional: remove background tint if needed
        }
    }

    private void callOwner(String contactNumber) {
        if (contactNumber != null && !contactNumber.isEmpty() && !contactNumber.equals("N/A")) {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + contactNumber));
            startActivity(intent);
        } else {
            Toast.makeText(this, "Contact number not available.", Toast.LENGTH_SHORT).show();
        }
    }

    private void openMapLocation(String url) {
        if (url == null || url.isEmpty()) {
            Toast.makeText(this, "Map location link is not available for this room.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Uri mapUri;
            if (url.startsWith("http://") || url.startsWith("https://")) {
                mapUri = Uri.parse(url);
            }
            else {
                String query = Uri.encode(url);
                mapUri = Uri.parse("geo:0,0?q=" + query);
            }

            Intent intent = new Intent(Intent.ACTION_VIEW, mapUri);

            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(this, "Google Maps app not found or link is invalid.", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error opening map: Invalid link format.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
}