package com.develophub.roomfinder;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton; // ImageButton के लिए इंपोर्ट
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.appbar.CollapsingToolbarLayout;
// Firebase Imports
import com.google.firebase.auth.FirebaseAuth; // Firebase Auth के लिए इंपोर्ट
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Locale;

public class PgDetailsActivity extends AppCompatActivity {

    private static final String TAG = "PgDetailsActivity";

    private String pgId;
    private PgModel currentPg;

    private ViewPager2 vpImageSlider;
    private CollapsingToolbarLayout toolbarLayout;
    private TextView tvDetailRent, tvDetailDeposit, tvDetailOccupancy, tvDetailDescription, tvDetailAddress, tvOwnerNameContact;
    private GridLayout gridLayoutFacilities;
    private Button btnDetailCall, btnDetailMessage;

    // ⭐ NEW: Favorite Button
    private ImageButton btnToggleFavoritePg;
    private boolean isFavorite = false;
    private DatabaseReference favoriteRef; // Firebase reference to the current PG's favorite status

    // Firebase Instance
    private DatabaseReference databaseRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pg_details);

        mAuth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference("pgs");

        // 1. Initialize Views
        initializeViews();
        setupToolbar();

        // 2. Get Data from Intent
        pgId = getIntent().getStringExtra("pg_id");

        if (pgId != null && !pgId.isEmpty()) {
            loadPgDetails(pgId);

            // ⭐ NEW: Favorites Setup
            if (mAuth.getCurrentUser() != null) {
                setupFavoriteReference();
                btnToggleFavoritePg.setOnClickListener(v -> togglePgFavoriteStatus());
            } else {
                btnToggleFavoritePg.setVisibility(View.GONE); // यदि यूजर लॉगिन नहीं है
            }
        } else {
            Toast.makeText(this, "Error: PG ID not found.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initializeViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbarLayout = findViewById(R.id.toolbar_layout);
        vpImageSlider = findViewById(R.id.vpImageSlider);
        tvDetailRent = findViewById(R.id.tvDetailRent);
        tvDetailDeposit = findViewById(R.id.tvDetailDeposit);
        tvDetailOccupancy = findViewById(R.id.tvDetailOccupancy);
        tvDetailDescription = findViewById(R.id.tvDetailDescription);
        tvDetailAddress = findViewById(R.id.tvDetailAddress);
        tvOwnerNameContact = findViewById(R.id.tvOwnerNameContact);
        gridLayoutFacilities = findViewById(R.id.gridLayoutFacilities);
        btnDetailCall = findViewById(R.id.btnDetailCall);
        btnDetailMessage = findViewById(R.id.btnDetailMessage);

        // ⭐ NEW: Favorite Button Initialization
        btnToggleFavoritePg = findViewById(R.id.btnToggleFavoritePg);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
            toolbar.setNavigationOnClickListener(v -> finish());
        }
    }

    // 3. Firebase Realtime Database से डेटा लोड करें
    private void loadPgDetails(String id) {
        databaseRef.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    currentPg = snapshot.getValue(PgModel.class);

                    if (currentPg != null) {
                        currentPg.setPgId(id); // ID को मॉडल में स्टोर करें
                        bindDataToViews(currentPg);
                        setupImageSlider(currentPg.getImageUrls());
                        setupActionButtons(currentPg.getContactNumber());
                    } else {
                        Toast.makeText(PgDetailsActivity.this, "Failed to parse PG data.", Toast.LENGTH_LONG).show();
                        finish();
                    }
                } else {
                    Toast.makeText(PgDetailsActivity.this, "PG listing not found.", Toast.LENGTH_LONG).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error fetching PG details: " + error.getMessage());
                Toast.makeText(PgDetailsActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    // ⭐ NEW: Favorites Status Check and Database Reference setup
    private void setupFavoriteReference() {
        String userId = mAuth.getCurrentUser().getUid();

        // Favorite reference structure: Favorites/USER_ID/pgs/PG_ID
        favoriteRef = FirebaseDatabase.getInstance()
                .getReference("Favorites")
                .child(userId)
                .child("pgs") // PG के लिए "pgs" नोड का उपयोग करें
                .child(pgId);

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

    /**
     * Favorites बटन के आइकॉन को अपडेट करता है (भरा हुआ या खाली हार्ट)।
     */
    private void updateFavoriteButtonUI() {
        if (isFavorite) {
            // यदि यह आपके drawable में 'like' (भरा हुआ हार्ट) है
            btnToggleFavoritePg.setImageResource(R.drawable.like);
            btnToggleFavoritePg.setColorFilter(getResources().getColor(android.R.color.holo_red_dark)); // या कोई और रंग
        } else {
            // यदि यह आपके drawable में 'heart' (आउटलाइन हार्ट) है
            btnToggleFavoritePg.setImageResource(R.drawable.heart);
            btnToggleFavoritePg.setColorFilter(getResources().getColor(android.R.color.darker_gray));
        }
    }


    /**
     * Favorites स्टेटस को टॉगल करता है (जोड़ता है या हटाता है)।
     */
    private void togglePgFavoriteStatus() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Please log in to use the favorites feature.", Toast.LENGTH_LONG).show();
            return;
        }

        if (currentPg == null) {
            Toast.makeText(this, "PG data not loaded yet.", Toast.LENGTH_SHORT).show();
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
            // ⭐ FIX: पूरा PgModel ऑब्जेक्ट सेव करें (रूममेट की तरह)
            // PgModel को सेव करें
            favoriteRef.setValue(currentPg).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Added to Favorites", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to add to Favorites: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // 4. Views में डेटा सेट करें
    private void bindDataToViews(PgModel pg) {
        toolbarLayout.setTitle(pg.getName());

        tvDetailRent.setText(String.format(Locale.getDefault(), "₹ %,d / Month", pg.getMonthlyRent()));
        tvDetailDeposit.setText(String.format(Locale.getDefault(), "Deposit: ₹ %,d", pg.getSecurityDeposit()));
        tvDetailOccupancy.setText(pg.getOccupancyType());

        tvDetailDescription.setText(pg.getDescription());
        tvDetailAddress.setText(pg.getFullAddress());
        tvOwnerNameContact.setText(String.format("Owner Contact: %s", pg.getContactNumber()));

        if (pg.getFacilities() != null) {
            populateFacilities(pg.getFacilities());
        }
    }

    // 5. Image Slider सेटअप करें
    private void setupImageSlider(List<String> imageUrls) {
        if (imageUrls != null && !imageUrls.isEmpty()) {
            vpImageSlider.setAdapter(new ImageSliderAdapter(this, imageUrls));
        } else {
            vpImageSlider.setVisibility(View.GONE);
        }
    }

    // 6. Facilities को GridLayout में जोड़ें
    private void populateFacilities(List<String> facilities) {
        gridLayoutFacilities.removeAllViews();
        for (String facility : facilities) {
            TextView textView = new TextView(this);
            textView.setText("✅ " + facility);
            textView.setTextSize(14f);
            textView.setTextColor(getResources().getColor(android.R.color.black, null));

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(0, 0, 16, 16);
            textView.setLayoutParams(params);

            gridLayoutFacilities.addView(textView);
        }
    }

    // 7. Call और Message बटन के लिए लॉजिक
    private void setupActionButtons(String phoneNumber) {
        btnDetailCall.setOnClickListener(v -> {
            if (phoneNumber != null && !phoneNumber.isEmpty()) {
                Intent dialIntent = new Intent(Intent.ACTION_DIAL);
                dialIntent.setData(Uri.parse("tel:" + phoneNumber));
                startActivity(dialIntent);
            } else {
                Toast.makeText(this, "Phone number unavailable.", Toast.LENGTH_SHORT).show();
            }
        });

        btnDetailMessage.setOnClickListener(v -> {
            if (currentPg == null) return;
            Intent smsIntent = new Intent(Intent.ACTION_VIEW);
            smsIntent.setData(Uri.parse("sms:" + phoneNumber));
            smsIntent.putExtra("sms_body", String.format("Hi, I saw your listing for '%s' (ID: %s) on the app. Is it available?", currentPg.getName(), pgId));
            startActivity(smsIntent);
        });
    }

    // Toolbar Back Button Handling
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}