package com.develophub.roomfinder;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

// ध्यान दें: आपकी क्लास का नाम Java कन्वेंशन के अनुसार PgSectionActivity होना चाहिए
// लेकिन चूंकि आपने activity_pg_section नाम का उपयोग किया है, मैं उसे ही रख रहा हूँ।
public class activity_pg_section extends AppCompatActivity {

    private CardView cardOwnerSide;
    private CardView cardUserSide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pg_section);

        // Views Initialize करें
        cardOwnerSide = findViewById(R.id.cardOwnerSide);
        cardUserSide = findViewById(R.id.cardUserSide);

        // --- 1. Owner Side Click Listener ---
        cardOwnerSide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 'AddPgActivity' को लॉन्च करने के लिए Intent
                // यह Activity PG मालिक को विवरण जोड़ने देती है।
                Intent ownerIntent = new Intent(activity_pg_section.this, AddPgActivity.class);
                startActivity(ownerIntent);

                Toast.makeText(activity_pg_section.this, "Opening Add PG Details...", Toast.LENGTH_SHORT).show();
            }
        });

        // --- 2. User Side Click Listener ---

        cardUserSide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 'PgSearchActivity' को लॉन्च करने के लिए Intent
                // यह Activity उपयोगकर्ताओं को PG सर्च और लिस्टिंग दिखाती है।
                Intent userIntent = new Intent(activity_pg_section.this, PgSearchActivity.class);
                startActivity(userIntent);

                Toast.makeText(activity_pg_section.this, "Opening PG Search...", Toast.LENGTH_SHORT).show();
            }
        });
    }
}