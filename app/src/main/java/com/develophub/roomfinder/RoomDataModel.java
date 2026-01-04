package com.develophub.roomfinder;

import com.google.firebase.database.PropertyName;
import java.util.List;

/**
 * Yeh Rooms node ke data structure ko seedhe map karne ke liye hai.
 * Isme sirf woh fields hain jo Activity_list_room se save hote hain.
 */
public class RoomDataModel {

    // ⭐ Rooms data keys (Must match map.put() keys from Activity_list_room) ⭐
    private String owner;
    private String locationText;
    private Object rent; // Integer/Long/String ko handle karne ke liye
    private String contact;
    private String roomType;
    private List<String> images; // Base64 List
    private String name; // e.g., "1 BHK (Nashik)"
    private String address; // Location/address
    private String mapLink;
    private String id; // Listing ID
    private String ownerId;

    // ⭐ New Field: Description ⭐
    private String description;

    // Public no-args constructor for Firebase (CRITICAL)
    public RoomDataModel() {
    }

    // ⭐ 1. GETTERS (HomeFragment aur ListingDetailsActivity mein data nikalne ke liye) ⭐

    public String getFirstImageBase64() {
        if (images != null && !images.isEmpty()) {
            return images.get(0);
        }
        return null;
    }

    public String getListingTitle() {
        if (name != null && !name.isEmpty()) {
            return name;
        }
        if (roomType != null && address != null) {
            return roomType + " in " + address;
        }
        return "Room Listing";
    }

    public String getListingPrice() {
        if (rent != null) {
            // Hum rent ko seedhe ListingDetailsActivity mein RoomDataModel se display kar rahe hain
            return "₹ " + rent.toString() + " / month";
        }
        return "Price N/A";
    }

    public String getListingId() {
        return id;
    }

    // Other Getters
    public String getOwner() { return owner; }
    public String getAddress() { return address; }
    public Object getRent() { return rent; }
    public List<String> getImages() { return images; }
    public String getContact() { return contact; }
    public String getRoomType() { return roomType; }

    // ⭐ New Getter for Description ⭐
    public String getDescription() { return description; }


    // ⭐ 2. SETTERS (Firebase mapping ke liye - @PropertyName ke saath) ⭐

    @PropertyName("owner") public void setOwner(String owner) { this.owner = owner; }
    @PropertyName("locationText") public void setLocationText(String locationText) { this.locationText = locationText; }
    @PropertyName("rent") public void setRent(Object rent) { this.rent = rent; }
    @PropertyName("contact") public void setContact(String contact) { this.contact = contact; }
    @PropertyName("roomType") public void setRoomType(String roomType) { this.roomType = roomType; }
    @PropertyName("images") public void setImages(List<String> images) { this.images = images; }
    @PropertyName("name") public void setName(String name) { this.name = name; }
    @PropertyName("address") public void setAddress(String address) { this.address = address; }
    @PropertyName("mapLink") public void setMapLink(String mapLink) { this.mapLink = mapLink; }
    @PropertyName("id") public void setId(String id) { this.id = id; }
    @PropertyName("ownerId") public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    // ⭐ New Setter for Description ⭐
    @PropertyName("description") public void setDescription(String description) { this.description = description; }
}