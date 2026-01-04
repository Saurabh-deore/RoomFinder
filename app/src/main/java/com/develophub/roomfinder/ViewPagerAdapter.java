package com.develophub.roomfinder; // आप इसे अपने adapters पैकेज में रख सकते हैं

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ViewPagerAdapter extends FragmentStateAdapter {

    // ViewPagerAdapter constructor जो FragmentActivity को लेता है
    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // यह मेथड निर्धारित करता है कि किस पोजीशन पर कौन सा Fragment लोड होगा
        switch (position) {
            case 0:
                // पहला टैब (पोजीशन 0) Rooms के लिए
                return new MyRoomListingsFragment();
            case 1:
                // दूसरा टैब (पोजीशन 1) PGs के लिए
                return new MyPgListingsFragment();
            case 2:
                // तीसरा टैब (पोजीशन 2) Roommates के लिए
                return new MyRoommateListingsFragment();
            default:
                // Default के रूप में Rooms को वापस करें
                return new MyRoomListingsFragment();
        }
    }

    @Override
    public int getItemCount() {
        // कुल टैब की संख्या (Rooms, PGs, Roommates)
        return 3;
    }
}