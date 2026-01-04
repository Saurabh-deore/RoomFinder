package com.develophub.roomfinder;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignupActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegister, tvForgotPassword;

    private FirebaseAuth mAuth;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Firebase Initialize
        mAuth = FirebaseAuth.getInstance();

        // Init Views
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnAction);
        tvRegister = findViewById(R.id.tvToggle);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        // --- Password Eye Icon Logic ---
        etPassword.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (etPassword.getCompoundDrawables()[DRAWABLE_RIGHT] != null) {
                    if (event.getRawX() >= (etPassword.getRight() - etPassword.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width() - 30)) {
                        togglePasswordVisibility();
                        return true;
                    }
                }
            }
            return false;
        });

        // Login button click
        btnLogin.setOnClickListener(v -> loginUser());

        // Forgot Password click
        tvForgotPassword.setOnClickListener(v -> resetPassword());

        // Go to Register
        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(SignupActivity.this, Registration.class));
            finish();
        });
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            // Hide Password
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            etPassword.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(this, R.drawable.closeeye), null);
            isPasswordVisible = false;
        } else {
            // Show Password
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            etPassword.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(this, R.drawable.view), null);
            isPasswordVisible = true;
        }
        etPassword.setSelection(etPassword.getText().length());
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Enter email");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Enter password");
            return;
        }

        // --- LOGIN START: COLOR CHANGE LOGIC ---
        btnLogin.setEnabled(false); // Disable button to prevent double clicks
        btnLogin.setAlpha(0.7f);    // Button ko thoda light/transparent karein
        btnLogin.setText("Please wait...");

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            Toast.makeText(SignupActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(SignupActivity.this, DashboardActivity.class));
                            finish();
                        }
                    } else {
                        // --- LOGIN FAILED: RESET COLOR & BUTTON ---
                        btnLogin.setEnabled(true);
                        btnLogin.setAlpha(1.0f); // Wapas original color
                        btnLogin.setText("Log In");

                        Toast.makeText(SignupActivity.this,
                                "Failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void resetPassword() {
        String email = etEmail.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Please enter The email");
            return;
        }

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(SignupActivity.this, "Reset link sent to your email!", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(SignupActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            startActivity(new Intent(SignupActivity.this, DashboardActivity.class));
            finish();
        }
    }
}