package com.develophub.roomfinder;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FavoritesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "FavoritesAdapter";
    private static final int VIEW_TYPE_ROOM = 1;
    private static final int VIEW_TYPE_ROOMMATE = 2;
    private static final int VIEW_TYPE_PG = 3;

    private Context context;
    private ArrayList<Object> favoritesList;

    public FavoritesAdapter(Context context, ArrayList<Object> favoritesList) {
        this.context = context;
        this.favoritesList = (favoritesList != null) ? favoritesList : new ArrayList<>();
    }

    @Override
    public int getItemViewType(int position) {
        Object item = favoritesList.get(position);
        if (item instanceof RoomModel2) {
            return VIEW_TYPE_ROOM;
        } else if (item instanceof RoommateModel) {
            return VIEW_TYPE_ROOMMATE;
        } else if (item instanceof PgModel) {
            return VIEW_TYPE_PG;
        }
        return super.getItemViewType(position);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);

        if (viewType == VIEW_TYPE_ROOM) {
            View view = inflater.inflate(R.layout.item_room, parent, false);
            return new RoomViewHolder(view);
        } else if (viewType == VIEW_TYPE_ROOMMATE) {
            View view = inflater.inflate(R.layout.item_roommate, parent, false);
            return new RoommateViewHolder(view);
        } else if (viewType == VIEW_TYPE_PG) {
            // ⭐ PG के लिए नया लेआउट
            View view = inflater.inflate(R.layout.item_pg_card, parent, false);
            return new PgViewHolder(view);
        }

        return new RoomViewHolder(inflater.inflate(R.layout.item_room, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object item = favoritesList.get(position);

        if (holder.getItemViewType() == VIEW_TYPE_ROOMMATE && item instanceof RoommateModel) {
            bindRoommateData((RoommateViewHolder) holder, (RoommateModel) item);

        } else if (holder.getItemViewType() == VIEW_TYPE_ROOM && item instanceof RoomModel2) {
            bindRoomData((RoomViewHolder) holder, (RoomModel2) item);

        } else if (holder.getItemViewType() == VIEW_TYPE_PG && item instanceof PgModel) {
            bindPgData((PgViewHolder) holder, (PgModel) item);

        } else {
            Log.w(TAG, "Unmatched View Type/Model at position " + position +
                    ". ViewType: " + holder.getItemViewType() +
                    ", ModelType: " + item.getClass().getSimpleName());
        }
    }

    private void bindRoomData(RoomViewHolder holder, RoomModel2 room) {
        holder.tvOwner.setText(room.getOwner() != null ? room.getOwner() : "Unknown Listing");
        String rentString = room.getRent() != null ? room.getRent() : "N/A";
        holder.tvRent.setText("₹" + rentString + "/month");
        holder.tvLocation.setText(room.getLocation() != null ? room.getLocation() : "Location N/A");
        loadImage(holder.ivRoomImage, room.getImage());

        holder.btnContact.setOnClickListener(v -> {
            Toast.makeText(context, "Opening Room Details for " + room.getOwner(), Toast.LENGTH_SHORT).show();
        });

        holder.btnDeleteListing.setVisibility(View.GONE);
        holder.itemView.setOnClickListener(v -> holder.btnContact.performClick());
    }

    private void bindRoommateData(RoommateViewHolder holder, RoommateModel roommate) {
        String name = roommate.getName() != null ? roommate.getName() : "Unknown Roommate";
        String gender = roommate.getGender() != null ? roommate.getGender() : "";

        holder.tvName.setText(name);

        holder.tvLocationDetail.setText("Location: " + (roommate.getLocation() != null ? roommate.getLocation() : "N/A"));

        if (gender.equalsIgnoreCase("Male")) {
            holder.rbMale.setChecked(true);
            holder.rbFemale.setChecked(false);
        } else if (gender.equalsIgnoreCase("Female")) {
            holder.rbMale.setChecked(false);
            holder.rbFemale.setChecked(true);
        } else {
            holder.rgGender.clearCheck();
        }

        Object budgetObj = roommate.getBudget();
        String budgetText = "Budget: N/A";

        if (budgetObj != null) {
            try {
                if (budgetObj instanceof Number) {
                    budgetText = String.format(Locale.getDefault(), "Budget: ₹ %,d /month", ((Number) budgetObj).intValue());
                } else if (budgetObj instanceof String) {
                    budgetText = "Budget: ₹ " + (String) budgetObj + " /month";
                } else {
                    budgetText = "Budget: ₹ " + String.valueOf(budgetObj) + " /month";
                }
            } catch (Exception e) {
                Log.e(TAG, "Roommate Budget Casting Error: " + e.getMessage());
                budgetText = "Budget: N/A (Data Error)";
            }
        }
        holder.tvBudget.setText(budgetText);

        holder.tvDescription.setText(roommate.getAbout() != null ? roommate.getAbout() : "No description provided.");

        List<String> images = roommate.getImages();
        String imageBase64 = (images != null && !images.isEmpty()) ? images.get(0) : roommate.getFirstImageUrl();
        loadImage(holder.ivProfile, imageBase64);

        holder.itemView.setOnClickListener(v -> {
            Toast.makeText(context, "Opening Roommate Details for " + roommate.getName(), Toast.LENGTH_SHORT).show();
            // Intent logic to open RoommateDetailActivity
        });
    }

    private void bindPgData(PgViewHolder holder, PgModel pg) {

        // --- Data Extraction ---
        String pgName = pg.getName() != null ? pg.getName() : "Unknown PG Listing";

        // PG Location: fullAddress को प्राथमिकता दें, फिर locationText, अन्यथा N/A
        String pgLocation = pg.getFullAddress() != null ? pg.getFullAddress() :
                (pg.getLocationText() != null ? pg.getLocationText() : "Location N/A");

        // Rent Handling (Object/Integer to String)
        String pgRent = "N/A";
        Object monthlyRentObj = pg.getMonthlyRent();
        if (monthlyRentObj != null) {
            try {
                int rent = ((Number) monthlyRentObj).intValue();
                pgRent = String.format(Locale.getDefault(), "₹ %,d/Month", rent);
            } catch (Exception e) {
                pgRent = "₹ " + String.valueOf(monthlyRentObj) + "/Month";
            }
        }

        String occupancy = pg.getOccupancyType() != null ? pg.getOccupancyType() : "Unspecified";

        // --- BINDING TO NEW XML IDs ---

        holder.tvPgName.setText(pgName);
        holder.tvPgLocation.setText(pgLocation);
        holder.tvPgRent.setText(pgRent);
        holder.tvOccupancyType.setText(occupancy);

        // Image Loading
        String firstImageBase64 = pg.getFirstImageUrl();
        loadImage(holder.imgPgPhoto, firstImageBase64);

        // Click Listeners

        // 1. View Details Button
        holder.btnViewDetails.setOnClickListener(v -> {
            if (pg.getPgId() != null) {
                Intent intent = new Intent(context, PgDetailsActivity.class);
                intent.putExtra("pg_id", pg.getPgId());
                context.startActivity(intent);
            } else {
                Toast.makeText(context, "PG ID not available for details.", Toast.LENGTH_SHORT).show();
            }
        });

        // 2. Contact/Call Owner Button (Assuming btnContactOwner ID is used for contact)
        holder.btnContactOwner.setOnClickListener(v -> {
            // यहां आप मालिक को कॉल करने या चैट करने का Intent लॉजिक जोड़ सकते हैं
            Toast.makeText(context, "Call Owner Clicked for " + pg.getName(), Toast.LENGTH_SHORT).show();
            // Example: Intent callIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + pg.getContactNumber())); context.startActivity(callIntent);
        });

        // 3. Delete button (केवल Owner के लिए)
        holder.btnDeleteImage.setVisibility(View.GONE); // सामान्य उपयोगकर्ता के लिए छुपा हुआ

        // Card Click Listener (वैकल्पिक रूप से View Details पर भी जा सकता है)
        holder.itemView.setOnClickListener(v -> holder.btnViewDetails.performClick());
    }

    // --- Common Image Loading Utility ---
    private void loadImage(ImageView imageView, String imageBase64) {
        if (imageBase64 != null && !imageBase64.isEmpty()) {
            try {
                byte[] decoded = Base64.decode(imageBase64, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                } else {
                    imageView.setImageResource(R.drawable.user);
                }
            } catch (Exception e) {
                Log.e(TAG, "Image Decoding Error: " + e.getMessage());
                imageView.setImageResource(R.drawable.user);
            }
        } else {
            imageView.setImageResource(R.drawable.user);
        }
    }


    @Override
    public int getItemCount() {
        return (favoritesList != null) ? favoritesList.size() : 0;
    }


    // --- ViewHolder Classes ---

    static class RoomViewHolder extends RecyclerView.ViewHolder {
        ImageView ivRoomImage;
        TextView tvOwner, tvRent, tvLocation;
        Button btnContact, btnDeleteListing;

        public RoomViewHolder(@NonNull View itemView) {
            super(itemView);
            ivRoomImage = itemView.findViewById(R.id.ivRoomImage);
            tvOwner = itemView.findViewById(R.id.tvOwner);
            tvRent = itemView.findViewById(R.id.tvRent);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            btnContact = itemView.findViewById(R.id.btnContact);
            btnDeleteListing = itemView.findViewById(R.id.btnDeleteListing);
        }
    }

    static class RoommateViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProfile;
        TextView tvName, tvLocationDetail, tvBudget, tvDescription;
        RadioGroup rgGender;
        RadioButton rbMale, rbFemale;

        public RoommateViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfile = itemView.findViewById(R.id.ivProfile);
            tvName = itemView.findViewById(R.id.tvName);
            tvLocationDetail = itemView.findViewById(R.id.tvLocationDetail);
            tvBudget = itemView.findViewById(R.id.tvBudget);
            tvDescription = itemView.findViewById(R.id.tvDescription);

            rgGender = itemView.findViewById(R.id.rgGender);
            rbMale = itemView.findViewById(R.id.rbMale);
            rbFemale = itemView.findViewById(R.id.rbFemale);

            rbMale.setEnabled(false);
            rbFemale.setEnabled(false);
        }
    }

    // ⭐ PG Card के लिए ViewHolder
    static class PgViewHolder extends RecyclerView.ViewHolder {
        // XML IDs के अनुसार
        ImageView imgPgPhoto;
        TextView tvPgName, tvPgLocation, tvPgRent, tvOccupancyType;
        Button btnViewDetails, btnContactOwner, btnDeleteImage;

        public PgViewHolder(@NonNull View itemView) {
            super(itemView);
            // item_pg_card.xml से IDs
            imgPgPhoto = itemView.findViewById(R.id.imgPgPhoto);
            tvPgName = itemView.findViewById(R.id.tvPgName);
            tvPgLocation = itemView.findViewById(R.id.tvPgLocation);
            tvPgRent = itemView.findViewById(R.id.tvPgRent);
            tvOccupancyType = itemView.findViewById(R.id.tvOccupancyType);

            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
            btnContactOwner = itemView.findViewById(R.id.btnContactOwner);
            btnDeleteImage = itemView.findViewById(R.id.btnDeleteImage); // यह DELETE बटन है, इसे DeleteListing भी कहा जा सकता है
        }
    }
}