package com.develophub.roomfinder;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Activity_list_room extends AppCompatActivity {

    EditText etOwnerName, etLocation, etMapLink, etRent, etContact;
    Spinner spRoomType;
    Button btnChooseImages, btnSave;
    Toolbar toolbar;
    RecyclerView rvRoomImages;
    com.develophub.roomfinder.RoomImagesAdapter adapter;

    ArrayList<String> roomImagesBase64 = new ArrayList<>();
    private static final int PICK_IMAGE = 1;

    DatabaseReference dbRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_room);

        // Toolbar setup
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Views Initialization
        etOwnerName = findViewById(R.id.etOwnerName);
        etLocation = findViewById(R.id.etLocation);
        etMapLink = findViewById(R.id.etMapLink);
        etRent = findViewById(R.id.etRent);
        etContact = findViewById(R.id.etContact);
        spRoomType = findViewById(R.id.spRoomType);
        rvRoomImages = findViewById(R.id.rvRoomImages);
        btnChooseImages = findViewById(R.id.btnChooseImages);
        btnSave = findViewById(R.id.btnSave);

        // Spinner setup
        String[] roomTypes = {"1 BHK", "2 BHK", "3 BHK", "PG", "Shared"};
        ArrayAdapter<String> adapterSpinner = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roomTypes);
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spRoomType.setAdapter(adapterSpinner);

        // RecyclerView setup
        adapter = new com.develophub.roomfinder.RoomImagesAdapter(roomImagesBase64, this);
        rvRoomImages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvRoomImages.setAdapter(adapter);

        // Firebase initialization
        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference("Rooms");

        // Choose Images
        btnChooseImages.setOnClickListener(v -> {
            Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            startActivityForResult(pickIntent, PICK_IMAGE);
        });

        // Save button with Effect
        btnSave.setOnClickListener(v -> {
            // Option 1: Visual Click Effect (Flash/Disable)
            btnSave.setEnabled(false);
            btnSave.setText("Saving...");
            btnSave.setAlpha(0.7f);

            saveRoom();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    addImageAsBase64(imageUri);
                }
            } else if (data.getData() != null) {
                Uri imageUri = data.getData();
                addImageAsBase64(imageUri);
            }
            adapter.notifyDataSetChanged();
        }
    }

    private void addImageAsBase64(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            byte[] bytes = baos.toByteArray();
            String base64Image = Base64.encodeToString(bytes, Base64.DEFAULT);
            roomImagesBase64.add(base64Image);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveRoom() {
        String owner = etOwnerName.getText().toString().trim();
        String locationText = etLocation.getText().toString().trim();
        String mapLink = etMapLink.getText().toString().trim();
        String rentString = etRent.getText().toString().trim();
        String contact = etContact.getText().toString().trim();
        String roomType = spRoomType.getSelectedItem().toString();

        String currentUserId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "UNKNOWN_USER";

        if (owner.isEmpty() || locationText.isEmpty() || mapLink.isEmpty() || rentString.isEmpty() || contact.isEmpty() || roomImagesBase64.isEmpty() || currentUserId.equals("UNKNOWN_USER")) {
            Toast.makeText(this, "Fill all fields and select images.", Toast.LENGTH_SHORT).show();
            // Reset Button agar validation fail ho jaye
            btnSave.setEnabled(true);
            btnSave.setText("Save Listing");
            btnSave.setAlpha(1.0f);
            return;
        }

        int rentValue;
        try {
            rentValue = Integer.parseInt(rentString);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Rent must be a number.", Toast.LENGTH_LONG).show();
            btnSave.setEnabled(true);
            btnSave.setText("Save Listing");
            btnSave.setAlpha(1.0f);
            return;
        }

        String id = dbRef.push().getKey();
        HashMap<String, Object> map = new HashMap<>();

        map.put("ownerId", currentUserId);
        map.put("id", id);
        map.put("owner", owner);
        map.put("locationText", locationText);
        map.put("mapLink", mapLink);
        map.put("rent", rentValue);
        map.put("contact", contact);
        map.put("name", roomType + " (" + locationText + ")");
        map.put("address", locationText);
        map.put("roomType", roomType);
        map.put("images", roomImagesBase64);

        dbRef.child(id).setValue(map).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Option 2: Flash Success Message
                Toast.makeText(Activity_list_room.this, "Room Listed Successfully! 🎉", Toast.LENGTH_LONG).show();

                // Chota sa delay taaki user success dekh sake
                new android.os.Handler().postDelayed(() -> finish(), 500);
            } else {
                btnSave.setEnabled(true);
                btnSave.setText("Save Listing");
                btnSave.setAlpha(1.0f);
                Toast.makeText(Activity_list_room.this, "Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}