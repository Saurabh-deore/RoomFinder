package com.develophub.roomfinder;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

public class Aboutus extends Fragment {

    public Aboutus() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_aboutus, container, false);

        // 1. Instagram Layout Logic
        LinearLayout layoutInstagram = view.findViewById(R.id.layoutInstagram);
        if (layoutInstagram != null) {
            layoutInstagram.setOnClickListener(v -> openInstagram("room_finderr"));
        }

        // 2. Email Button Logic
        MaterialButton btnSupport = view.findViewById(R.id.btnContactSupport);
        if (btnSupport != null) {
            btnSupport.setOnClickListener(v -> openEmailSupport());
        }

        return view;
    }

    // Instagram kholne ka logic
    private void openInstagram(String username) {
        Uri uri = Uri.parse("http://instagram.com/_u/" + username);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);

        // Sirf Instagram app ko target karein
        intent.setPackage("com.instagram.android");

        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            // Agar App nahi hai toh Browser mein kholein
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://www.instagram.com/room_finderr?igsh=czJsa2V1b3Q4MmN6" + username)));
        }
    }

    // Email kholne ka logic
    private void openEmailSupport() {
        String emailAddress = "Mvprsmsaurabh2023@gmail.com";
        String subject = "Support Request: Room Finder App";

        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // Sirf email apps respond karengi
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{emailAddress});
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);

        try {
            startActivity(Intent.createChooser(intent, "Send Email via..."));
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(getContext(), "No email app found!", Toast.LENGTH_SHORT).show();
        }
    }
}