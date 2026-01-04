package com.develophub.roomfinder;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etEmail;
    private Button btnReset;
    private TextView tvBack;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        etEmail = findViewById(R.id.etForgotEmail);
        btnReset = findViewById(R.id.btnReset);
        tvBack = findViewById(R.id.tvBackLogin);
        mAuth = FirebaseAuth.getInstance();

        btnReset.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                etEmail.setError("Email is required");
                return;
            }

            mAuth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(ForgotPasswordActivity.this, "Check your email for reset link!", Toast.LENGTH_LONG).show();
                    finish(); // Email bhejte hi wapas Login screen par bhej dega
                } else {
                    Toast.makeText(ForgotPasswordActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        tvBack.setOnClickListener(v -> finish());
    }
}