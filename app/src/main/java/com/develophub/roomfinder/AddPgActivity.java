package com.develophub.roomfinder;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AddPgActivity extends AppCompatActivity {

    private static final String TAG = "AddPgActivity";
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PERMISSION_REQUEST_CODE = 100;

    // Input Fields
    private TextInputEditText etPgName, etAddress, etRent, etDeposit, etDescription, etContactNumber;
    private Spinner spinnerOccupancy;
    private CheckBox cbWifi, cbFood, cbLaundry, cbAC;

    private Button btnUploadImages, btnSubmitPg;

    private List<Uri> selectedImageUris = new ArrayList<>();

    // Firebase Instances
    private DatabaseReference databaseRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_pg);

        FirebaseApp.initializeApp(this);

        // 1. Firebase Initialization
        // यहां हम सीधे 'pgs' नोड को टारगेट कर रहे हैं
        databaseRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        // 2. Initialize Views
        initializeViews();

        // 3. Set Listener
        btnSubmitPg.setOnClickListener(v -> validateAndSubmitPgDetails());

        btnUploadImages.setOnClickListener(v -> handleImageUpload());
    }

    private void initializeViews() {
        etPgName = findViewById(R.id.etPgName);
        etAddress = findViewById(R.id.etAddress);
        etRent = findViewById(R.id.etRent);
        etDeposit = findViewById(R.id.etDeposit);
        etDescription = findViewById(R.id.etDescription);
        etContactNumber = findViewById(R.id.etContactNumber);

        spinnerOccupancy = findViewById(R.id.spinnerOccupancy);

        cbWifi = findViewById(R.id.cbWifi);
        cbFood = findViewById(R.id.cbFood);
        cbLaundry = findViewById(R.id.cbLaundry);
        cbAC = findViewById(R.id.cbAC);

        btnUploadImages = findViewById(R.id.btnUploadImages);
        btnSubmitPg = findViewById(R.id.btnSubmitPg);
    }

    // ----------------------------------------------------
    // GALLERY OPENING & PERMISSIONS LOGIC (unchanged)
    // ----------------------------------------------------

    private void handleImageUpload() {
        if (checkStoragePermission()) {
            openGallery();
        } else {
            requestStoragePermission();
        }
    }

    private boolean checkStoragePermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestStoragePermission() {
        String permission = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) ?
                Manifest.permission.READ_MEDIA_IMAGES :
                Manifest.permission.READ_EXTERNAL_STORAGE;

        ActivityCompat.requestPermissions(this,
                new String[]{permission},
                PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(this, "Storage permission denied. Cannot select images.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select PG Images"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedImageUris.clear();

            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    selectedImageUris.add(data.getClipData().getItemAt(i).getUri());
                }
            } else if (data.getData() != null) {
                selectedImageUris.add(data.getData());
            }

            Toast.makeText(this, selectedImageUris.size() + " images selected. Ready to submit!", Toast.LENGTH_SHORT).show();
        }
    }


    // ----------------------------------------------------
    // SUBMISSION LOGIC (Updated for int conversion and Base64)
    // ----------------------------------------------------

    private void validateAndSubmitPgDetails() {
        String name = etPgName.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String rentStr = etRent.getText().toString().trim();
        String depositStr = etDeposit.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String contact = etContactNumber.getText().toString().trim();
        String occupancy = spinnerOccupancy.getSelectedItem().toString();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(address) || TextUtils.isEmpty(rentStr) || TextUtils.isEmpty(contact) || contact.length() != 10) {
            Toast.makeText(this, "Please fill all required fields correctly and check contact number.", Toast.LENGTH_LONG).show();
            return;
        }

        int rentValue;
        int depositValue;

        try {
            // ✅ FIX 1: Rent String को Integer में बदलना
            rentValue = Integer.parseInt(rentStr);
            // ✅ FIX 2: Deposit String को Integer में बदलना (यदि खाली है तो 0)
            depositValue = depositStr.isEmpty() ? 0 : Integer.parseInt(depositStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "किराया और जमा राशि केवल अंकों में होनी चाहिए।", Toast.LENGTH_LONG).show();
            return;
        }

        if (selectedImageUris.isEmpty()) {
            // यदि कोई इमेज नहीं चुनी गई है, तो खाली लिस्ट के साथ सेव करें
            savePgDataToRealtimeDatabase(name, address, occupancy, rentValue, depositValue, description, contact, new ArrayList<>());
        } else {
            // Base64 कन्वर्जन और सेविंग शुरू करें
            // depositValue को rent की जगह send करें
            convertToBase64AndSave(name, address, occupancy, rentValue, depositValue, description, contact);
        }
    }

    // ✅ नया: convertToBase64AndSave मेथड को सुधार दिया गया
    private void convertToBase64AndSave(String name, String address, String occupancy, int rent, int deposit, String description, String contact) {

        List<String> base64Strings = new ArrayList<>();
        // Note: यह ऑपरेशन मेन थ्रेड को ब्लॉक करता है। बेहतर परफॉर्मेंस के लिए इसे AsyncTask/Coroutine में ले जाना चाहिए।

        try {
            for (Uri uri : selectedImageUris) {
                Bitmap bitmap = null;

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    bitmap = android.graphics.ImageDecoder.decodeBitmap(android.graphics.ImageDecoder.createSource(getContentResolver(), uri));
                } else {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                }

                if (bitmap != null) {
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    // कंप्रेसन क्वालिटी 70%
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream);

                    byte[] imageBytes = byteArrayOutputStream.toByteArray();
                    String base64Image = Base64.encodeToString(imageBytes, Base64.DEFAULT);
                    base64Strings.add(base64Image);
                    Log.d(TAG, "Encoded Base64 string length: " + base64Image.length());
                }
            }

            // 4. Base64 लिस्ट के साथ डेटा सेव करें
            savePgDataToRealtimeDatabase(name, address, occupancy, rent, deposit, description, contact, base64Strings);

        } catch (IOException e) {
            Toast.makeText(this, "Error processing images: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e(TAG, "Image Processing Error: " + e.getMessage());
            e.printStackTrace();
        }
    }


    // Realtime Database Saving Function
    private void savePgDataToRealtimeDatabase(String name, String address, String occupancy, int rent, int deposit, String description, String contact, List<String> base64ImageUrls) {

        List<String> finalFacilitiesList = new ArrayList<>();
        if (cbWifi.isChecked()) finalFacilitiesList.add("Wi-Fi");
        if (cbFood.isChecked()) finalFacilitiesList.add("Food/Meals");
        if (cbLaundry.isChecked()) finalFacilitiesList.add("Laundry");
        if (cbAC.isChecked()) finalFacilitiesList.add("A/C");

        String ownerId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "UNKNOWN_OWNER";

        String newPgId = databaseRef.child("pgs").push().getKey();

        // हमने PGModel/ListingItemModel में SecurityDeposit और Description के लिए setters का उपयोग किया
        // इसलिए, हम PgModel का उपयोग कर सकते हैं।
        PgModel newPg = new PgModel(
                newPgId,
                ownerId,
                name, address, occupancy, rent, contact,
                base64ImageUrls,
                finalFacilitiesList
        );
        newPg.setSecurityDeposit(deposit);
        newPg.setDescription(description);

        databaseRef.child("pgs")
                .child(newPgId)
                .setValue(newPg)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "✅ PG Listing Added and Saved!", Toast.LENGTH_LONG).show();
                    // PgSearchActivity में रीडायरेक्ट करें
                    Intent intent = new Intent(this, PgSearchActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "❌ Error saving PG data to Realtime DB: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}