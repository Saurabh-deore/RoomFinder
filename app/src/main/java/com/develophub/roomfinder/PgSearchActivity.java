package com.develophub.roomfinder;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView; // FIX: Added CardView import
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// Firebase Imports
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PgSearchActivity extends AppCompatActivity {

    private static final String TAG = "PgSearchActivity";

    private EditText etSearchLocation;
    private CardView btnFilter; // FIX: ImageButton se change karke CardView kiya
    private RecyclerView recyclerViewPgs;

    // Data Source
    private List<PgModel> allPgsList;
    private PgAdapter pgAdapter;

    // Firebase Reference
    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pg_search);

        // 1. Firebase Initialization
        databaseRef = FirebaseDatabase.getInstance().getReference("pgs");

        // Views Initialize
        etSearchLocation = findViewById(R.id.etSearchLocation);
        btnFilter = findViewById(R.id.btnFilter); // Ab crash nahi hoga
        recyclerViewPgs = findViewById(R.id.recyclerViewPgs);

        // 2. Setup Data and Adapter
        allPgsList = new ArrayList<>();
        setupRecyclerView();
        loadAllPgs();

        // 3. Set Listeners
        btnFilter.setOnClickListener(v -> openFilterDialog());
        setupSearchListener();
    }

    // Realtime Database se data load karna
    private void loadAllPgs() {
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allPgsList.clear();

                if (snapshot.exists()) {
                    for (DataSnapshot pgSnapshot : snapshot.getChildren()) {
                        try {
                            PgModel pg = pgSnapshot.getValue(PgModel.class);
                            if (pg != null) {
                                // ID mapping check
                                if (pg.getPgId() == null || pg.getPgId().isEmpty()) {
                                    pg.setPgId(pgSnapshot.getKey());
                                }
                                allPgsList.add(pg);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error mapping PG data: " + e.getMessage());
                        }
                    }

                    // Adapter refresh logic
                    if (pgAdapter != null) {
                        pgAdapter.updateList(allPgsList);
                    }

                    Log.d(TAG, "Data loaded successfully: " + allPgsList.size());
                } else {
                    if (pgAdapter != null) {
                        pgAdapter.updateList(new ArrayList<>());
                    }
                    Toast.makeText(PgSearchActivity.this, "No PG listings found.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database Error: " + error.getMessage());
                Toast.makeText(PgSearchActivity.this, "Failed to load data.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupRecyclerView() {
        // Third parameter 'false' for Find PG screen
        pgAdapter = new PgAdapter(this, allPgsList, false);
        recyclerViewPgs.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewPgs.setAdapter(pgAdapter);
    }

    private void setupSearchListener() {
        etSearchLocation.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(pgAdapter != null) {
                    pgAdapter.getFilter().filter(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void openFilterDialog() {
        Toast.makeText(this, "Opening Advanced Filters...", Toast.LENGTH_SHORT).show();
    }
}