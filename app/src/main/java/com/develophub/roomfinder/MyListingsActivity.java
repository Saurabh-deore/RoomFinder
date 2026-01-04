package com.develophub.roomfinder;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2; // ⭐ NEW IMPORT

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout; // ⭐ NEW IMPORT
import com.google.android.material.tabs.TabLayoutMediator; // ⭐ NEW IMPORT
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

// पुराने Imports: RecyclerView, ProgressBar, Query, ValueEventListener हटा दिए गए हैं क्योंकि लॉजिक Fragments में चला गया है।

public class MyListingsActivity extends AppCompatActivity {

    // ⭐ VIEWS UPDATED: अब हमें TabLayout और ViewPager2 की आवश्यकता है
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    // पुराने RecyclerView, TextView, ProgressBar अब Fragment के लेआउट में हैं।

    private FirebaseAuth mAuth;
    private String currentUserId; // यह अभी भी Fragment को पास करने के लिए उपयोगी हो सकता है (लेकिन यहां सीधे उपयोग नहीं हो रहा है)

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // ⭐ Layout को update किया गया माना जा रहा है जिसमें TabLayout और ViewPager2 हैं।
        setContentView(R.layout.activity_my_listings);

        // 1. Firebase और User ID Initialization
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "Login required to view listings.", Toast.LENGTH_LONG).show();
            // User को Login स्क्रीन पर भेजने का लॉजिक यहाँ जोड़ें
            finish();
            return;
        }
        currentUserId = user.getUid(); // User ID अभी भी यहाँ से प्राप्त की जा सकती है

        // 2. Views Initialization
        // सुनिश्चित करें कि activity_my_listings.xml में ये IDs मौजूद हैं
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        // 3. ViewPager Adapter Setup
        // ViewPagerAdapter को Fragments का पता होना चाहिए
        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(adapter);

        // 4. TabLayout और ViewPager को लिंक करें
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText("Rooms");
                            break;
                        case 1:
                            tab.setText("PGs");
                            break;
                        case 2:
                            tab.setText("Roommates");
                            break;
                    }
                }
        ).attach();

        // ⭐ पुराना listing fetching और deletion logic हटा दिया गया है
        // fetchMyListings(), fetchListingsFromNode(), checkIfAllQueriesCompleted(),
        // और deleteListingFromDatabase() मेथड्स को हटा दिया गया है
        // क्योंकि यह सब काम Fragments करेंगे।
    }
}