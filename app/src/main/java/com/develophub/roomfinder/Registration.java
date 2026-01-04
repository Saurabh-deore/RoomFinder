package com.develophub.roomfinder;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class Registration extends AppCompatActivity {

    // ⭐ NEW: etPhone को जोड़ा गया
    private EditText etFullName, etEmail, etPhone, etPassword, etConfirmPassword;
    private Button btnRegister;
    private TextView tvLogin;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        // Firebase
        mAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference("Users");

        // Init Views
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        // ⭐ FIX 1: etPhone को findViewById किया गया
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);

        // Register button click
        btnRegister.setOnClickListener(v -> registerUser());

        // Go to Login page directly
        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(Registration.this, SignupActivity.class));
            finish();
        });
    }

    private void registerUser() {
        String name = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        // ⭐ FIX 2: फोन नंबर प्राप्त किया गया
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Validations
        if (TextUtils.isEmpty(name)) {
            etFullName.setError("Enter name");
            return;
        }
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Enter email");
            return;
        }
        // ⭐ FIX 3: फोन नंबर के लिए Validation
        if (TextUtils.isEmpty(phone) || phone.length() < 10) {
            etPhone.setError("Enter a valid 10-digit phone number");
            return;
        }
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            return;
        }
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            return;
        }

        // Firebase Registration
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String uid = firebaseUser.getUid();

                            // Store user data in Firebase Realtime DB
                            HashMap<String, String> userMap = new HashMap<>();
                            userMap.put("name", name);
                            userMap.put("email", email);
                            // ⭐ FIX 4: फोन नंबर HashMap में जोड़ा गया
                            userMap.put("phone", phone);

                            userRef.child(uid).setValue(userMap)
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            Toast.makeText(Registration.this, "Registration Successful! Please Login.", Toast.LENGTH_SHORT).show();

                                            // Logout user after registration so login compulsory ho
                                            mAuth.signOut();

                                            // Go to Login page (SignupActivity)
                                            Intent intent = new Intent(Registration.this, SignupActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            Toast.makeText(Registration.this, "Database Error: " + task1.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(Registration.this, "Failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}