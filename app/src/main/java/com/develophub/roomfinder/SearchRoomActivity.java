package com.develophub.roomfinder;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar; // ✅ Standard ProgressBar
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Locale;
import java.util.stream.Collectors;

public class SearchRoomActivity extends AppCompatActivity implements View.OnClickListener {

    RecyclerView recyclerView;
    EditText etSearch;
    TextView tvAll, tv1BHK, tv2BHK, tv3BHK, tvPG, tvBudget;
    ProgressBar progressBar; // ✅ Animation ki jagah ProgressBar

    private ArrayList<RoomModel> fullRoomList;
    ArrayList<RoomModel> roomList;

    com.develophub.roomfinder.SearchRoomAdapter adapter;
    DatabaseReference dbRef;

    private String currentFilter = "All";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_room);

        // 1. View Binding
        recyclerView = findViewById(R.id.rvRooms);
        etSearch = findViewById(R.id.etSearch);
        tvAll = findViewById(R.id.tvAll);
        tv1BHK = findViewById(R.id.tv1BHK);
        tv2BHK = findViewById(R.id.tv2BHK);
        tv3BHK = findViewById(R.id.tv3BHK);
        tvPG = findViewById(R.id.tvPG);
        tvBudget = findViewById(R.id.tvBudget);
        progressBar = findViewById(R.id.progressBar); // ✅ ProgressBar Binding

        // 2. RecyclerView Setup
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        fullRoomList = new ArrayList<>();
        roomList = new ArrayList<>();
        adapter = new com.develophub.roomfinder.SearchRoomAdapter(roomList, this);
        recyclerView.setAdapter(adapter);

        // 3. Listener Setup
        tvAll.setOnClickListener(this);
        tv1BHK.setOnClickListener(this);
        tv2BHK.setOnClickListener(this);
        tv3BHK.setOnClickListener(this);
        tvPG.setOnClickListener(this);
        tvBudget.setOnClickListener(this);

        // 4. Search Bar Listener
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterRooms(currentFilter, s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // 5. Firebase Fetch
        dbRef = FirebaseDatabase.getInstance().getReference("Rooms");
        fetchRoomsFromFirebase();
    }

    private void fetchRoomsFromFirebase() {
        // ✅ Loading Start
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                fullRoomList.clear();
                for(DataSnapshot ds : snapshot.getChildren()){
                    try {
                        String id = ds.child("id").getValue(String.class);
                        String owner = ds.child("owner").getValue(String.class);
                        String location = ds.child("locationText").getValue(String.class);

                        Long rentLong = ds.child("rent").getValue(Long.class);
                        int rentValue = rentLong != null ? rentLong.intValue() : 0;

                        String contact = ds.child("contact").getValue(String.class);
                        String roomType = ds.child("roomType").getValue(String.class);
                        String mapLink = ds.child("mapLink").getValue(String.class);
                        if (mapLink == null) mapLink = "";

                        ArrayList<String> images = new ArrayList<>();
                        if(ds.child("images").exists()){
                            for(DataSnapshot imgSnap : ds.child("images").getChildren()){
                                String base64 = imgSnap.getValue(String.class);
                                images.add(base64);
                            }
                        }

                        RoomModel room = new RoomModel(id, owner, location, rentValue, contact, roomType, mapLink, images);
                        fullRoomList.add(room);

                    } catch (Exception e) {
                        Log.e("FirebaseFetch", "Error: " + e.getMessage());
                    }
                }

                // ✅ Loading Complete
                progressBar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);

                filterRooms(currentFilter, etSearch.getText().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(SearchRoomActivity.this, "Error: "+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClick(View v) {
        String filterType = "";

        if (v.getId() == R.id.tvAll) {
            filterType = "All";
        } else if (v.getId() == R.id.tv1BHK) {
            filterType = "1 BHK";
        } else if (v.getId() == R.id.tv2BHK) {
            filterType = "2 BHK";
        } else if (v.getId() == R.id.tv3BHK) {
            filterType = "3 BHK";
        } else if (v.getId() == R.id.tvPG) {
            filterType = "PG";
        } else if (v.getId() == R.id.tvBudget) {
            Toast.makeText(this, "Budget filter functionality pending.", Toast.LENGTH_SHORT).show();
            return;
        }

        currentFilter = filterType;
        updateFilterUI(v);
        filterRooms(currentFilter, etSearch.getText().toString());
    }

    private void filterRooms(String typeFilter, String query) {
        roomList.clear();
        String lowerQuery = query.toLowerCase(Locale.getDefault()).trim();

        ArrayList<RoomModel> typeFilteredList;

        if (typeFilter.equals("All")) {
            typeFilteredList = new ArrayList<>(fullRoomList);
        } else {
            typeFilteredList = fullRoomList.stream()
                    .filter(room -> room.getRoomType() != null && room.getRoomType().equalsIgnoreCase(typeFilter))
                    .collect(Collectors.toCollection(ArrayList::new));
        }

        if (lowerQuery.isEmpty()) {
            roomList.addAll(typeFilteredList);
        } else {
            for (RoomModel room : typeFilteredList) {
                if (room.getLocation() != null && room.getLocation().toLowerCase(Locale.getDefault()).contains(lowerQuery)) {
                    roomList.add(room);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void updateFilterUI(View clickedView) {
        TextView[] filterButtons = {tvAll, tv1BHK, tv2BHK, tv3BHK, tvPG, tvBudget};
        for (TextView button : filterButtons) {
            if (button == clickedView) {
                button.setBackgroundResource(R.drawable.rounded_bg_blue);
                button.setTextColor(getResources().getColor(android.R.color.white));
            } else {
                button.setBackgroundResource(R.drawable.rounded_bg_light_gray);
                button.setTextColor(getResources().getColor(android.R.color.black));
            }
        }
    }
}