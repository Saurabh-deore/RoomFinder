package com.develophub.roomfinder;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    GridView gridView;
    ViewPager2 imageSlider;
    RecyclerView recommendedRecyclerView;
    EditText etSearch;
    HomeGridAdapter gridAdapter;
    FloatingActionButton fabChatbot;

    private DatabaseReference rootReference;
    private List<RecommendationItem> recommendedData;
    private RecommendedAdapter recommendedAdapter;

    private Handler sliderHandler = new Handler();
    private Runnable sliderRunnable;
    private int sliderDataSize = 0;

    String[] FeatureTitle = {"List Your Room", "Search Room", "Find Roommate", "Favourites", "Send Notication", "PG Section", "My Listings"};
    int[] featureImages = {R.drawable.listroom2, R.drawable.searchroom2, R.drawable.findroomate, R.drawable.fav, R.drawable.notificatin, R.drawable.pg2, R.drawable.mylisting2};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if (getActivity() != null) {
            getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);

            getActivity().getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        rootReference = FirebaseDatabase.getInstance().getReference();
        gridView = view.findViewById(R.id.gridView);
        imageSlider = view.findViewById(R.id.imageSlider);
        recommendedRecyclerView = view.findViewById(R.id.recommendedRecyclerView);
        etSearch = view.findViewById(R.id.search_bar);
        fabChatbot = view.findViewById(R.id.fab_chatbot);

        gridAdapter = new HomeGridAdapter(getContext(), featureImages, FeatureTitle);
        gridView.setAdapter(gridAdapter);

        // Initial height set
        gridView.post(() -> setGridViewHeightBasedOnChildren(gridView, 2));

        setupImageSlider();
        setupRecommendedRecyclerView();

        // --- FIXED SEARCH LOGIC ---
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (gridAdapter != null) {
                    // Filter call with callback to ensure height updates AFTER data changes
                    gridAdapter.getFilter().filter(s.toString(), count1 -> {
                        setGridViewHeightBasedOnChildren(gridView, 2);
                    });
                }
            }

            @Override public void afterTextChanged(Editable s) {}
        });

        gridView.setOnItemClickListener((adapterView, view1, position, id) -> {
            Object item = gridAdapter.getItem(position);
            if (item != null) {
                handleGridClicks(item.toString());
            }
        });

        fabChatbot.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ChatActivity.class);
            startActivity(intent);
        });

        return view;
    }

    private void setupImageSlider() {
        List<HomeImageSliderAdapter.ImageSliderItem> sliderData = new ArrayList<>();
        sliderData.add(new HomeImageSliderAdapter.ImageSliderItem(R.drawable.p1, "", ""));
        sliderData.add(new HomeImageSliderAdapter.ImageSliderItem(R.drawable.p2, "", ""));
        sliderData.add(new HomeImageSliderAdapter.ImageSliderItem(R.drawable.p3, "", ""));

        sliderDataSize = sliderData.size();
        imageSlider.setAdapter(new HomeImageSliderAdapter(sliderData));

        sliderRunnable = new Runnable() {
            @Override
            public void run() {
                int currentItem = imageSlider.getCurrentItem();
                int nextItem = (currentItem + 1) % sliderDataSize;
                imageSlider.setCurrentItem(nextItem, true);
                sliderHandler.postDelayed(this, 3000);
            }
        };

        imageSlider.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                sliderHandler.removeCallbacks(sliderRunnable);
                sliderHandler.postDelayed(sliderRunnable, 3000);
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        sliderHandler.removeCallbacks(sliderRunnable);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (sliderRunnable != null) {
            sliderHandler.postDelayed(sliderRunnable, 3000);
        }
    }

    // GridView height reset logic
    private void setGridViewHeightBasedOnChildren(GridView gridView, int columns) {
        ListAdapter listAdapter = gridView.getAdapter();
        if (listAdapter == null) return;

        int totalHeight = 0;
        int items = listAdapter.getCount();
        int rows = (int) Math.ceil((double) items / columns);

        for (int i = 0; i < rows; i++) {
            View listItem = listAdapter.getView(i, null, gridView);
            listItem.measure(
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            );
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = gridView.getLayoutParams();
        float density = getResources().getDisplayMetrics().density;
        // Adjusting spacing based on rows
        params.height = totalHeight + (int)(40 * (rows + 1) * density);
        gridView.setLayoutParams(params);
    }

    private void handleGridClicks(String title) {
        if (title.equals("List Your Room")) startActivity(new Intent(getActivity(), Activity_list_room.class));
        else if (title.equals("Search Room")) startActivity(new Intent(getActivity(), SearchRoomActivity.class));
        else if (title.equals("Find Roommate")) startActivity(new Intent(getActivity(), FindRoommatesActivity.class));
        else if (title.equals("Favourites")) startActivity(new Intent(getActivity(), FavoritesActivity.class));
        else if (title.equals("Send Notication")) startActivity(new Intent(getActivity(), SendMessageActivity.class));
        else if (title.equals("PG Section")) startActivity(new Intent(getActivity(), activity_pg_section.class));
        else if (title.equals("My Listings")) startActivity(new Intent(getActivity(), MyListingsActivity.class));
    }

    private void setupRecommendedRecyclerView() {
        recommendedRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recommendedData = new ArrayList<>();
        recommendedAdapter = new RecommendedAdapter(getContext(), recommendedData);
        recommendedRecyclerView.setAdapter(recommendedAdapter);
        fetchDataFromAllNodes();
    }

    private void fetchDataFromAllNodes() {
        if (getContext() == null) return;
        rootReference.child("Rooms").limitToFirst(10).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                recommendedData.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    RoomDataModel roomData = ds.getValue(RoomDataModel.class);
                    if (roomData != null) {
                        RecommendationItem item = new RecommendationItem();
                        item.setTitle(roomData.getListingTitle());
                        item.setSubtitle(roomData.getOwner());
                        item.setRent(roomData.getRent());
                        item.setImages(roomData.getImages());
                        item.setListingId(ds.getKey());
                        recommendedData.add(item);
                    }
                }
                recommendedAdapter.notifyDataSetChanged();
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (getActivity() != null) {
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        }
    }
}