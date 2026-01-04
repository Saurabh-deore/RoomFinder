package com.develophub.roomfinder;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

// ⭐ आवश्यक Imports
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot; // ⭐ NEW
import com.google.firebase.database.DatabaseError; // ⭐ NEW
import com.google.firebase.database.ValueEventListener; // ⭐ NEW
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SendMessageActivity extends AppCompatActivity {

    private static final String TAG = "SendMessageActivity";
    private EditText etMessage;
    private Button btnSend;
    private DatabaseReference messagesRef;
    private DatabaseReference usersRef; // ⭐ Users नोड के लिए

    private String currentUserId;
    private String currentUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_message);

        // Firebase reference
        messagesRef = FirebaseDatabase.getInstance().getReference("Messages");
        usersRef = FirebaseDatabase.getInstance().getReference("Users"); // Users नोड का संदर्भ

        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);

        // नाम लोड होने तक बटन अक्षम करें
        btnSend.setEnabled(false);

        // ⭐ STEP 1: Current User ID प्राप्त करें
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
            // ⭐ STEP 2: उपयोगकर्ता का नाम लोड करें
            loadCurrentUserName();
        } else {
            Toast.makeText(this, "Error: User not logged in.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText = etMessage.getText().toString().trim();
                sendMessage(messageText);
            }
        });
    }

    // ⭐ NEW METHOD: Firebase से उपयोगकर्ता का नाम लोड करें
    private void loadCurrentUserName() {
        usersRef.child(currentUserId).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    currentUserName = snapshot.getValue(String.class);
                    // नाम लोड होने के बाद बटन को सक्षम करें
                    btnSend.setEnabled(true);
                } else {
                    // यदि नाम नहीं मिला, तो एक डिफ़ॉल्ट नाम सेट करें
                    currentUserName = "Unknown User";
                    btnSend.setEnabled(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                currentUserName = "Error User";
                btnSend.setEnabled(true);
                Log.e(TAG, "Failed to load user name: " + error.getMessage());
            }
        });
    }

    private void sendMessage(String messageText) {
        if(TextUtils.isEmpty(messageText)){
            etMessage.setError("Message cannot be empty");
            return;
        }

        // नाम लोड हो गया है या नहीं, इसकी जाँच करें
        if (currentUserName == null) {
            Toast.makeText(this, "Please wait, user data is loading...", Toast.LENGTH_SHORT).show();
            return;
        }

        // Unique key generate
        String messageId = messagesRef.push().getKey();
        if(messageId != null){

            // ⭐ FIX: MessageModel कंस्ट्रक्टर में अब वास्तविक currentUserName जा रहा है
            MessageModel message = new MessageModel(
                    currentUserId,
                    currentUserName,   // 2. Sender Name (वास्तविक नाम)
                    messageText,
                    System.currentTimeMillis()
            );

            messagesRef.child(messageId).setValue(message)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(SendMessageActivity.this, "Message Sent", Toast.LENGTH_SHORT).show();
                                etMessage.setText("");
                            } else {
                                Log.e(TAG, "Failed to send message: " + task.getException());
                                Toast.makeText(SendMessageActivity.this, "Failed to send", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }
}