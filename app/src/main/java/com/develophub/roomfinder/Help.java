package com.develophub.roomfinder;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

public class Help extends Fragment {

    public Help() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_help, container, false);

        // XML mein jo MaterialButton hai uski ID yahan use karein
        // Agar ID 'btnContactSupport' rakhi hai toh:

        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) MaterialButton btnSupport = view.findViewById(R.id.btnContactSupport2);

        if (btnSupport != null) {
            btnSupport.setOnClickListener(v -> openEmailSupport());
        }

        return view;
    }

    // Email kholne ka logic
    private void openEmailSupport() {
        String emailAddress = "mvprsmsaurabh2023@gmail.com"; // Apni email yahan likhein
        String subject = "Support Request: Room Finder App";
        String body = "Hello Support Team,\n\nI need help with...";

        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // Sirf email apps hi khulenge
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{emailAddress});
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, body);

        try {
            startActivity(Intent.createChooser(intent, "Choose Email App..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(getContext(), "No email app installed!", Toast.LENGTH_SHORT).show();
        }
    }
}