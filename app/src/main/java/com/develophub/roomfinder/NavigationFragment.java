package com.develophub.yourapp; // आपके दिए गए पैकेज नाम का उपयोग किया गया

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.develophub.roomfinder.MessageAdapter;
import com.develophub.roomfinder.MessageModel;
import com.develophub.roomfinder.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

// MessageAdapter के दोनों इंटरफेस को लागू किया गया
public class NavigationFragment extends Fragment implements
        MessageAdapter.OnContactClickListener,
        MessageAdapter.OnMessageDeleteListener {

    private static final String TAG = "NavigationFragment";
    private RecyclerView rvMessages;
    private MessageAdapter adapter;
    private List<MessageModel> messageList = new ArrayList<>();
    private DatabaseReference messagesRef;
    private DatabaseReference usersRef;
    private DatabaseReference currentUserUnreadRef; // ⭐ NEW: Unread Status Reference

    private String currentUserId = "";

    public NavigationFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_navigation, container, false);

        // STEP 1: Get Current User ID
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
        } else {
            Log.e(TAG, "User not logged in. Cannot get currentUserId.");
            currentUserId = "DEFAULT_USER_ID";
        }

        // STEP 2: RecyclerView Initialization and Adapter Setup
        rvMessages = view.findViewById(R.id.rvMessages);

        // FIX 2: MessageAdapter को चारों आर्गुमेंट के साथ initialize किया गया
        adapter = new MessageAdapter(messageList, currentUserId, this, this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        rvMessages.setLayoutManager(layoutManager);
        rvMessages.setAdapter(adapter);

        // STEP 3: Firebase Reference Setup
        messagesRef = FirebaseDatabase.getInstance().getReference("Messages");
        usersRef = FirebaseDatabase.getInstance().getReference("Users"); // Users reference

        // ⭐ NEW: Unread Status Reference Setup
        if (!currentUserId.equals("DEFAULT_USER_ID")) {
            currentUserUnreadRef = FirebaseDatabase.getInstance().getReference("Users")
                    .child(currentUserId).child("hasUnreadMessages");
        }

        loadMessages();

        return view;
    }

    // ⭐ NEW METHOD: Fragment विजिबल होने पर Unread Status को Reset करें
    @Override
    public void onResume() {
        super.onResume();

        // यदि यूजर इस मैसेज लिस्ट को खोलता है, तो अनरीड डॉट को हटा दें।
        if (currentUserUnreadRef != null) {
            currentUserUnreadRef.setValue(false)
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to reset unread status: " + e.getMessage()));
        }
    }


    private void loadMessages() {
        if (messagesRef == null) {
            return;
        }

        messagesRef.orderByChild("timestamp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    MessageModel msg = ds.getValue(MessageModel.class);
                    // महत्वपूर्ण: सुनिश्चित करें कि MessageId सेव हो रहा है
                    if (msg != null && msg.getSenderId() != null) {
                        // Firebase Key को MessageModel में सेट करें ताकि हम इसे डिलीट कर सकें
                        msg.setMessageId(ds.getKey());
                        messageList.add(msg);
                    } else {
                        Log.w(TAG, "Skipping message due to null fields: " + ds.getKey());
                    }
                }

                adapter.notifyDataSetChanged();
                if (messageList.size() > 0) {
                    rvMessages.scrollToPosition(messageList.size() - 1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load messages: " + error.getMessage());
                Toast.makeText(getContext(), "Failed to load messages.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // OnContactClickListener का कार्यान्वयन (अपरिवर्तित)
    @Override
    public void onContactClick(String senderId) {
        if (usersRef == null || senderId == null || getContext() == null) {
            Toast.makeText(getContext(), "Error: Sender data missing.", Toast.LENGTH_SHORT).show();
            return;
        }

        usersRef.child(senderId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.hasChild("phone")) {

                    String phoneNumber = snapshot.child("phone").getValue(String.class);

                    if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
                        Intent intent = new Intent(Intent.ACTION_DIAL);
                        intent.setData(Uri.parse("tel:" + phoneNumber));
                        startActivity(intent);
                    } else {
                        Toast.makeText(getContext(), "Phone number not available for this user.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "User data structure error or profile not found for ID: " + senderId);
                    Toast.makeText(getContext(), "User profile data not found or missing phone number.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to fetch phone number: " + error.getMessage());
                Toast.makeText(getContext(), "Error fetching contact details.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // OnMessageDeleteListener का कार्यान्वयन (मैसेज डिलीट)
    @Override
    public void onDeleteClick(MessageModel message, int position) {
        if (messagesRef == null || message.getMessageId() == null) {
            Toast.makeText(getContext(), "Cannot delete: Message ID missing.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Firebase से मैसेज हटाएँ
        messagesRef.child(message.getMessageId()).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Message deleted.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to delete message: " + e.getMessage());
                    Toast.makeText(getContext(), "Failed to delete message.", Toast.LENGTH_SHORT).show();
                });
    }
}