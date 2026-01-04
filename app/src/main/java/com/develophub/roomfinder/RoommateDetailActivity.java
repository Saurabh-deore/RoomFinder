package com.develophub.roomfinder;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class RoommateDetailActivity extends AppCompatActivity {

    private static final String TAG = "RoommateDetailActivity";

    ImageView ivProfileDetail;
    TextView tvNameAgeDetail, tvGenderDetail, tvLocationDetail, tvBudgetDetail, tvAboutDetail, tvPhoneDetail;
    ImageButton btnAddFavorite;
    Button btnContact; // ⭐ Button for Dialing

    // FEEDBACK VIEWS
    private LinearLayout llFeedbackContainer;
    private TextView tvFeedbackTitle;

    private String roommateId;
    private boolean isFavorite = false;
    private DatabaseReference favoriteRef;

    private RoommateModel currentRoommateModel;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_roommate_detail);

        // --- Views Binding ---
        ivProfileDetail = findViewById(R.id.ivProfileDetail);
        tvNameAgeDetail = findViewById(R.id.tvNameAgeDetail);
        tvGenderDetail = findViewById(R.id.tvGenderDetail);
        tvLocationDetail = findViewById(R.id.tvLocationDetail);
        tvBudgetDetail = findViewById(R.id.tvBudgetDetail);
        tvAboutDetail = findViewById(R.id.tvAboutDetail);
        tvPhoneDetail = findViewById(R.id.tvPhoneDetail); // ⭐ New Phone TextView
        btnAddFavorite = findViewById(R.id.btnAddFavorite);
        btnContact = findViewById(R.id.btnContact); // ⭐ New Contact Button

        llFeedbackContainer = findViewById(R.id.llFeedbackContainer);
        tvFeedbackTitle = findViewById(R.id.tvFeedbackTitle);

        // --- Intent Data ---
        roommateId = getIntent().getStringExtra("roommateId");

        if (roommateId != null && FirebaseAuth.getInstance().getCurrentUser() != null) {
            loadRoommateDetails(roommateId);
            setupFavoriteReference();

            btnAddFavorite.setOnClickListener(v -> toggleRoommateFavoriteStatus());

            // ⭐ Contact Button Logic
            btnContact.setOnClickListener(v -> {
                if (currentRoommateModel != null && currentRoommateModel.getPhone() != null) {
                    openDialer(currentRoommateModel.getPhone());
                } else {
                    Toast.makeText(this, "Number not available", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            finish();
        }
    }

    private void loadRoommateDetails(String id) {
        DatabaseReference roommateRef = FirebaseDatabase.getInstance().getReference("Roommates").child(id);
        roommateRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    currentRoommateModel = snapshot.getValue(RoommateModel.class);
                    if (currentRoommateModel != null) {
                        displayCoreData(currentRoommateModel);
                        displayFeedback(currentRoommateModel.getPastRoommateFeedback());
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void displayCoreData(RoommateModel model) {
        tvNameAgeDetail.setText(model.getName() + " • " + model.getAge());
        tvGenderDetail.setText("Gender: " + model.getGender());
        tvLocationDetail.setText("Location: " + model.getLocation());
        tvAboutDetail.setText(model.getAbout());

        // ⭐ Display Phone
        if (model.getPhone() != null && !model.getPhone().isEmpty()) {
            tvPhoneDetail.setText("Phone: " + model.getPhone());
            tvPhoneDetail.setVisibility(View.VISIBLE);
        } else {
            tvPhoneDetail.setVisibility(View.GONE);
        }

        // Budget Handling
        Object budgetObj = model.getBudget();
        tvBudgetDetail.setText("Budget: ₹ " + String.valueOf(budgetObj));

        // Image Handling
        if (model.getImageBase64() != null && !model.getImageBase64().isEmpty()) {
            byte[] decoded = Base64.decode(model.getImageBase64(), Base64.DEFAULT);
            Bitmap bmp = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
            ivProfileDetail.setImageBitmap(bmp);
        }
    }

    // ⭐ Logic to Open Phone Dialer
    private void openDialer(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        startActivity(intent);
    }

    private void displayFeedback(List<RoommateFeedbackEntry> feedbackList) {
        if (feedbackList == null || feedbackList.isEmpty()) {
            tvFeedbackTitle.setVisibility(View.GONE);
            return;
        }
        tvFeedbackTitle.setVisibility(View.VISIBLE);
        llFeedbackContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (RoommateFeedbackEntry entry : feedbackList) {
            View v = inflater.inflate(R.layout.item_roommate_feedback_display, llFeedbackContainer, false);
            ((TextView) v.findViewById(R.id.tvFeedbackNameDisplay)).setText(entry.getName());

            ((CheckBox) v.findViewById(R.id.cbCleanlinessDisplay)).setChecked(entry.getCleanlinessRating() > 0.5f);
            ((CheckBox) v.findViewById(R.id.cbQuietDisplay)).setChecked(entry.getQuietRating() > 0.5f);
            ((CheckBox) v.findViewById(R.id.cbRespectfulDisplay)).setChecked(entry.getRespectfulRating() > 0.5f);
            ((CheckBox) v.findViewById(R.id.cbPatientDisplay)).setChecked(entry.getPatientRating() > 0.5f);
            ((CheckBox) v.findViewById(R.id.cbOrganizedDisplay)).setChecked(entry.getOrganizedRating() > 0.5f);

            ((TextView) v.findViewById(R.id.tvCommentDisplay)).setText(entry.getComment());
            llFeedbackContainer.addView(v);
        }
    }

    private void setupFavoriteReference() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        favoriteRef = FirebaseDatabase.getInstance().getReference("Favorites").child(userId).child("roommates").child(roommateId);
        favoriteRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                isFavorite = snapshot.exists();
                btnAddFavorite.setImageResource(isFavorite ? R.drawable.like : R.drawable.heart);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void toggleRoommateFavoriteStatus() {
        if (isFavorite) favoriteRef.removeValue();
        else favoriteRef.setValue(currentRoommateModel);
    }
}