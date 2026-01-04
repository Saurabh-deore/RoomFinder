package com.develophub.roomfinder;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

public class FavoritesActivity extends AppCompatActivity {

    private static final String TAG = "FavoritesActivity";
    private RecyclerView rvFavorites;

    private ArrayList<Object> combinedFavoritesList;
    private com.develophub.roomfinder.FavoritesAdapter adapter;
    private DatabaseReference baseFavRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        rvFavorites = findViewById(R.id.rvFavorites);

        combinedFavoritesList = new ArrayList<>();

        adapter = new com.develophub.roomfinder.FavoritesAdapter(this, combinedFavoritesList);

        rvFavorites.setLayoutManager(new LinearLayoutManager(this));
        rvFavorites.setAdapter(adapter);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            // Favorites/UID
            baseFavRef = FirebaseDatabase.getInstance().getReference("Favorites").child(userId);

            loadAllFavorites();
        } else {
            Toast.makeText(this, "Please log in to view favorites.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void loadAllFavorites() {
        combinedFavoritesList.clear();
        adapter.notifyDataSetChanged(); // UI को साफ़ करें

        // तीनों प्रकार की लिस्टिंग
        String[] favoriteTypes = {"rooms", "roommates", "pgs"};

        // Concurrent लोडिंग को ट्रैक करने के लिए AtomicInteger का उपयोग करें
        AtomicInteger loadingCounter = new AtomicInteger(favoriteTypes.length);

        for (String type : favoriteTypes) {
            loadFavoritesFromStorage(type, loadingCounter);
        }
    }

    /**
     * Favorites/USER_ID/{type} से सीधे पूरा मॉडल डेटा लोड करता है।
     */
    private void loadFavoritesFromStorage(String favNodeType, AtomicInteger loadingCounter) {
        // Favorites/UID/rooms, Favorites/UID/roommates, या Favorites/UID/pgs
        DatabaseReference favRef = baseFavRef.child(favNodeType);

        favRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (!snapshot.exists()) {
                    Log.d(TAG, "No " + favNodeType + " found in favorites.");
                    checkLoadingComplete(loadingCounter);
                    return;
                }

                for (DataSnapshot ds : snapshot.getChildren()) {

                    // ⭐ CRITICAL FIX (Based on Logcat Error):
                    // यदि डेटा Node में RoommateModel ऑब्जेक्ट के बजाय केवल एक बूलियन (True/False) है,
                    // तो deserialization क्रैश हो जाएगा। हम इसे छोड़ देते हैं।
                    if (ds.getValue() instanceof Boolean) {
                        Log.w(TAG, "Skipping favorite ID " + ds.getKey() + " in " + favNodeType + " because its value is a simple Boolean (True/False). Please update your saving logic to store the full object.");
                        continue; // अगले आइटम पर जाएँ
                    }

                    Object model = null;
                    String id = ds.getKey();

                    try {
                        if (favNodeType.equals("rooms")) {
                            model = ds.getValue(RoomModel2.class);
                            if (model == null) Log.e(TAG, "RoomModel2 deserialization failed for ID: " + id);

                        } else if (favNodeType.equals("roommates")) {
                            // RoommateModel को deserialize करने का प्रयास करें
                            model = ds.getValue(RoommateModel.class);
                            if (model != null && model instanceof RoommateModel) {
                                ((RoommateModel) model).setId(id); // ID सेट करें
                                Log.d(TAG, "Roommate Loaded Successfully: " + ((RoommateModel) model).getName());
                            } else {
                                Log.e(TAG, "RoommateModel deserialization failed (null or wrong type) for ID: " + id);
                            }

                        } else if (favNodeType.equals("pgs")) {
                            model = ds.getValue(PgModel.class);
                            if (model != null && model instanceof PgModel) {
                                ((PgModel) model).setPgId(id); // ID सेट करें
                                Log.d(TAG, "PG Loaded Successfully: " + ((PgModel) model).getName());
                            } else {
                                Log.e(TAG, "PgModel deserialization failed (null or wrong type) for ID: " + id);
                            }
                        }

                    } catch (DatabaseException e) {
                        // यह अब बूलियन के कारण नहीं होना चाहिए, लेकिन डेटा टाइप मिसमैच (जैसे Number in Object field) को पकड़ता है।
                        Log.e(TAG, "CRITICAL DB ERROR: Check data types for ID " + id + " in " + favNodeType + ". Error: " + e.getMessage());

                    } catch (Exception e) {
                        Log.e(TAG, "GENERAL ERROR processing " + favNodeType + " ID " + id + ": " + e.getMessage());
                    }

                    if (model != null) {
                        combinedFavoritesList.add(model);
                        // ⭐ NEW CRITICAL LOGGING: यह हमें बताएगा कि क्या जोड़ा गया है
                        Log.d(TAG, "ITEM ADDED: " + model.getClass().getSimpleName() +
                                " at size " + combinedFavoritesList.size());
                    }
                }

                checkLoadingComplete(loadingCounter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load " + favNodeType + " data: " + error.getMessage());
                checkLoadingComplete(loadingCounter);
            }
        });
    }

    /**
     * जांचता है कि सभी प्रकार के Favorites लोड हो गए हैं या नहीं।
     */
    private void checkLoadingComplete(AtomicInteger loadingCounter) {
        if (loadingCounter.decrementAndGet() == 0) {
            Log.d(TAG, "All Favorites loaded. Total items: " + combinedFavoritesList.size());

            Collections.shuffle(combinedFavoritesList);

            adapter.notifyDataSetChanged();

            if (combinedFavoritesList.isEmpty()) {
                Toast.makeText(FavoritesActivity.this, "No items found in favorites.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(FavoritesActivity.this, "Loaded " + combinedFavoritesList.size() + " favorites.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}