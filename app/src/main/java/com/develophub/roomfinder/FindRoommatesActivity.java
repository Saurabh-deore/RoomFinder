package com.develophub.roomfinder;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View; // ✅ Added for View.VISIBLE/GONE
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar; // ✅ Added ProgressBar
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.*;

import java.util.ArrayList;

public class FindRoommatesActivity extends AppCompatActivity {

    private RecyclerView rvRoommates;
    private com.develophub.roomfinder.RoommateAdapter adapter;
    private ArrayList<RoommateModel> list = new ArrayList<>();

    private EditText etLocationFilter, etMinBudget, etMaxBudget;
    private Spinner spGender;
    private Button btnApply, btnReset;
    private ProgressBar progressBarRoommate; // ✅ Added ProgressBar variable

    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_roommates);

        // 1. View Binding
        rvRoommates = findViewById(R.id.rvRoommates);
        etLocationFilter = findViewById(R.id.etLocationFilter);
        etMinBudget = findViewById(R.id.etMinBudget);
        etMaxBudget = findViewById(R.id.etMaxBudget);
        spGender = findViewById(R.id.spGender);
        btnApply = findViewById(R.id.btnApply);
        btnReset = findViewById(R.id.btnReset);
        progressBarRoommate = findViewById(R.id.progressBarRoommate); // ✅ Binding ProgressBar

        // 2. Adapter setup
        adapter = new com.develophub.roomfinder.RoommateAdapter(this, list);
        rvRoommates.setLayoutManager(new LinearLayoutManager(this));
        rvRoommates.setAdapter(adapter);

        // 3. FAB logic
        findViewById(R.id.fabAdd).setOnClickListener(v ->
                startActivity(new Intent(this, AddRoommateActivity.class)));

        // 4. Gender spinner setup
        String[] genders = new String[]{"Any", "Male", "Female"};
        ArrayAdapter<String> spAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, genders);
        spAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spGender.setAdapter(spAdapter);

        // 5. Firebase Data Loading with Loader logic
        dbRef = FirebaseDatabase.getInstance().getReference("Roommates");

        // Data load hone se pehle loader dikhao
        progressBarRoommate.setVisibility(View.VISIBLE);

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {
                list.clear();
                if (snap.exists()) {
                    for (DataSnapshot ds : snap.getChildren()) {
                        try {
                            RoommateModel m = ds.getValue(RoommateModel.class);
                            if (m != null) {
                                list.add(m);
                            }
                        } catch (Exception e) {
                            Log.e("FirebaseData", "Error parsing roommate: " + e.getMessage());
                        }
                    }

                    adapter.updateOriginalList(list);
                    adapter.resetFilters();
                } else {
                    Log.d("Firebase", "No data found");
                    adapter.notifyDataSetChanged();
                }

                // ✅ Data load hone ke baad loader chhupao
                progressBarRoommate.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // ✅ Error aane par bhi loader chhupao
                progressBarRoommate.setVisibility(View.GONE);
                Toast.makeText(FindRoommatesActivity.this, "Database Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // 6. Filter Apply Logic
        btnApply.setOnClickListener(v -> {
            String loc = etLocationFilter.getText().toString();
            String gender = spGender.getSelectedItem().toString();
            String minB = etMinBudget.getText().toString();
            String maxB = etMaxBudget.getText().toString();

            adapter.applyFilters(loc, gender, minB, maxB);
            Toast.makeText(this, "Filters Applied", Toast.LENGTH_SHORT).show();
        });

        // 7. Reset Filter Logic
        btnReset.setOnClickListener(v -> {
            etLocationFilter.setText("");
            etMinBudget.setText("");
            etMaxBudget.setText("");
            spGender.setSelection(0);
            adapter.resetFilters();
            Toast.makeText(this, "Filters Reset", Toast.LENGTH_SHORT).show();
        });
    }
}