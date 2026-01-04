package com.develophub.roomfinder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.CheckBox;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AddRoommateActivity extends AppCompatActivity {

    private static final String TAG = "AddRoommateActivity";
    private static final int PICK_IMAGE = 101;
    private static final int MAX_FEEDBACK_ENTRIES = 3;

    private ShapeableImageView ivPickProfile;
    private FloatingActionButton fabPickImage;
    private Button btnSave, btnAddRoommateFeedback;
    private EditText etName, etAge, etLocation, etBudget, etAbout, etPhone;
    private AutoCompleteTextView spGender;
    private LinearLayout llPastRoommatesList;

    private String imageBase64 = "";
    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_add_roommate);

        // --- View Initialization ---
        ivPickProfile = findViewById(R.id.ivPickProfile);
        fabPickImage = findViewById(R.id.fabPickImage);
        btnSave = findViewById(R.id.btnSave);
        etName = findViewById(R.id.etName);
        etAge = findViewById(R.id.tAge);
        etLocation = findViewById(R.id.etLocation);
        etBudget = findViewById(R.id.etBudget);
        etAbout = findViewById(R.id.etAbout);
        etPhone = findViewById(R.id.etPhone);
        spGender = findViewById(R.id.spGender);
        btnAddRoommateFeedback = findViewById(R.id.btnAddRoommateFeedback);
        llPastRoommatesList = findViewById(R.id.llPastRoommatesList);

        // --- Gender Dropdown ---
        String[] genders = new String[]{"Male", "Female"};
        ArrayAdapter<String> spAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, genders);
        spGender.setAdapter(spAdapter);
        spGender.setOnClickListener(v -> spGender.showDropDown());

        dbRef = FirebaseDatabase.getInstance().getReference("Roommates");

        // --- Listeners ---
        fabPickImage.setOnClickListener(v -> {
            Intent pick = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(pick, PICK_IMAGE);
        });

        btnAddRoommateFeedback.setOnClickListener(v -> addRoommateFeedbackEntry());

        // ⭐ Save Button Click with Visual Feedback
        btnSave.setOnClickListener(v -> {
            // Visual Effect
            btnSave.setEnabled(false);
            btnSave.setText("Saving Profile...");
            btnSave.setAlpha(0.7f);

            saveProfile();
        });
    }

    private void addRoommateFeedbackEntry() {
        if (llPastRoommatesList.getChildCount() >= MAX_FEEDBACK_ENTRIES) {
            Toast.makeText(this, "Maximum 3 feedbacks allowed.", Toast.LENGTH_SHORT).show();
            return;
        }
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View feedbackView = inflater.inflate(R.layout.item_roommate_feedback, llPastRoommatesList, false);
        feedbackView.findViewById(R.id.btnRemoveFeedback).setOnClickListener(v -> llPastRoommatesList.removeView(feedbackView));
        llPastRoommatesList.addView(feedbackView);
    }

    private List<RoommateFeedbackEntry> collectFeedbackData() {
        List<RoommateFeedbackEntry> feedbackList = new ArrayList<>();
        for (int i = 0; i < llPastRoommatesList.getChildCount(); i++) {
            View v = llPastRoommatesList.getChildAt(i);
            TextInputEditText etFBName = v.findViewById(R.id.etFeedbackRoommateName);
            String name = etFBName.getText().toString().trim();
            if (!name.isEmpty()) {
                RoommateFeedbackEntry entry = new RoommateFeedbackEntry(
                        name,
                        ((CheckBox) v.findViewById(R.id.cbCleanliness)).isChecked() ? 1.0f : 0.0f,
                        ((CheckBox) v.findViewById(R.id.cbRespectful)).isChecked() ? 1.0f : 0.0f,
                        ((CheckBox) v.findViewById(R.id.cbQuiet)).isChecked() ? 1.0f : 0.0f,
                        ((CheckBox) v.findViewById(R.id.cbPatient)).isChecked() ? 1.0f : 0.0f,
                        ((CheckBox) v.findViewById(R.id.cbOrganized)).isChecked() ? 1.0f : 0.0f,
                        ((TextInputEditText) v.findViewById(R.id.etFeedbackComment)).getText().toString().trim()
                );
                feedbackList.add(entry);
            }
        }
        return feedbackList;
    }

    @Override
    protected void onActivityResult(int req, int res, @Nullable Intent data) {
        super.onActivityResult(req, res, data);
        if (req == PICK_IMAGE && res == Activity.RESULT_OK && data != null) {
            try {
                Bitmap bmp = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                ivPickProfile.setImageBitmap(bmp);
                imageBase64 = encodeToBase64(bmp);
            } catch (IOException e) { Log.e(TAG, e.getMessage()); }
        }
    }

    private String encodeToBase64(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 70, baos);
        return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
    }

    private void saveProfile() {
        String name = etName.getText().toString().trim();
        String ageStr = etAge.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String budgetStr = etBudget.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        if (name.isEmpty() || ageStr.isEmpty() || location.isEmpty() || budgetStr.isEmpty() || phone.isEmpty() || imageBase64.isEmpty()) {
            Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show();
            resetSaveButton(); // Reset if validation fails
            return;
        }

        if (phone.length() != 10) {
            etPhone.setError("Invalid 10-digit number");
            resetSaveButton();
            return;
        }

        try {
            String key = dbRef.push().getKey();
            RoommateModel model = new RoommateModel();
            model.setId(key);
            model.setName(name);
            model.setAge(Integer.parseInt(ageStr));
            model.setGender(spGender.getText().toString());
            model.setBudget(Integer.parseInt(budgetStr));
            model.setLocation(location);
            model.setPhone(phone);
            model.setAbout(etAbout.getText().toString().trim());
            model.setImageBase64(imageBase64);
            model.setPastRoommateFeedback(collectFeedbackData());
            model.setOwnerId(FirebaseAuth.getInstance().getUid());
            model.setListingType("Roommate_Need");

            if (key != null) {
                dbRef.child(key).setValue(model).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // ⭐ Success Flash
                        Toast.makeText(this, "Roommate Profile Saved! 🎉", Toast.LENGTH_LONG).show();

                        // Chota delay finish karne se pehle
                        new android.os.Handler().postDelayed(this::finish, 500);
                    } else {
                        Toast.makeText(this, "Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        resetSaveButton();
                    }
                });
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            resetSaveButton();
        }
    }

    // ⭐ Helper to reset button state
    private void resetSaveButton() {
        btnSave.setEnabled(true);
        btnSave.setText("Save Profile");
        btnSave.setAlpha(1.0f);
    }
}