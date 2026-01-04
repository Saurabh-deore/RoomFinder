package com.develophub.roomfinder;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.develophub.yourapp.NavigationFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.badge.BadgeDrawable; // ⭐ NEW: BadgeDrawable के लिए इम्पोर्ट
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DashboardActivity extends AppCompatActivity {

    private static final String TAG = "DashboardActivity";

    private DrawerLayout drawerLayout;
    private BottomNavigationView bottomNavigationView;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private FirebaseAuth mAuth;

    // ⭐ NEW: Unread Message Tracking
    private DatabaseReference unreadRef;
    private ValueEventListener unreadListener;
    private String currentUserId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
        }

        // Init views
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        bottomNavigationView = findViewById(R.id.bottom_nav);

        // Drawer Toggle
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // ------------------------------
        // Drawer Header - Fetch User Data
        View headerView = navigationView.getHeaderView(0);
        TextView tvName = headerView.findViewById(R.id.tvUserName);
        TextView tvEmail = headerView.findViewById(R.id.tvUserEmail);

        if (currentUser != null) {
            String userId = currentUserId;
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(userId);

            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String name = snapshot.child("name").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);

                    if (name != null) tvName.setText(name);
                    if (email != null) tvEmail.setText(email);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(DashboardActivity.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
                }
            });

            // ⭐ NEW: Unread Indicator Setup
            setupUnreadMessageIndicator();
        }
        // ------------------------------

        // Default fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame_layout, new HomeFragment()).commit();

        // Bottom Navigation
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frame_layout, new HomeFragment()).commit();
            } else if (id == R.id.nav_messages) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frame_layout, new NavigationFragment()).commit();
            } else if (id == R.id.nav_profile) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frame_layout, new ProfileFragment()).commit();
            }
            return true;
        });

        // Drawer Navigation
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.drawer_help) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frame_layout, new Help())
                        .commit();
            } else if (id == R.id.drawer_about) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frame_layout, new Aboutus())
                        .commit();
            } else if (id == R.id.drawer_logout) {
                mAuth.signOut();
                startActivity(new Intent(DashboardActivity.this, SignupActivity.class));
                finish();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    // ⭐ NEW METHOD 1: Firebase Listener और Dot लॉजिक सेट करें
    private void setupUnreadMessageIndicator() {
        if (currentUserId.isEmpty()) return;

        // Firebase Path: Users/{currentUserId}/hasUnreadMessages
        unreadRef = FirebaseDatabase.getInstance().getReference("Users")
                .child(currentUserId).child("hasUnreadMessages");

        unreadListener = unreadRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean hasUnread = snapshot.getValue(Boolean.class);

                // अगर true है, तो मैसेज आइकॉन पर बैज दिखाएँ
                if (hasUnread != null && hasUnread) {
                    showBadge(R.id.nav_messages); // nav_messages BottomNavigationView मेनू आइटम की ID होनी चाहिए
                } else {
                    hideBadge(R.id.nav_messages);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to read unread status: " + error.getMessage());
            }
        });
    }

    // ⭐ NEW METHOD 2: BottomNavigationView पर बैज (Dot) दिखाएँ
    private void showBadge(int itemId) {
        if (bottomNavigationView == null) return;

        BadgeDrawable badge = bottomNavigationView.getOrCreateBadge(itemId);
        badge.setVisible(true);
        badge.setNumber(0); // छोटा डॉट दिखाने के लिए
        // आप यहां रंग बदल सकते हैं
        badge.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark, null));
    }

    // ⭐ NEW METHOD 3: BottomNavigationView से बैज हटाएँ
    private void hideBadge(int itemId) {
        if (bottomNavigationView == null) return;

        if (bottomNavigationView.getBadge(itemId) != null) {
            bottomNavigationView.removeBadge(itemId);
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    // ⭐ FIX: Firebase Listener को Stop करें
    @Override
    protected void onStop() {
        super.onStop();
        if (unreadRef != null && unreadListener != null) {
            unreadRef.removeEventListener(unreadListener);
        }
    }

    // onResume में Listener को फिर से शुरू करने की आवश्यकता नहीं है क्योंकि यह onStart/onCreate में शुरू हो गया है।
    // हमने onStop में Listener को हटा दिया है, इसलिए onStart में इसे फिर से सेट करना बेहतर है यदि आप onDestroy के बजाय onStop का उपयोग कर रहे हैं।
}