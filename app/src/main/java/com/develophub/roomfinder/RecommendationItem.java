package com.develophub.roomfinder;

import com.google.firebase.database.PropertyName;
import java.util.List;

public class RecommendationItem {

    // ⭐ 0. LISTING CARD FIELDS
    private String title;
    private String subtitle;
    private String type;
    private String listingId;

    // ⭐ 1. PG FIELDS
    private String firstImageUrl;
    private String monthlyRent;

    // ⭐ 2. ROOMS FIELDS (Keys are lower-camelCase in Java) ⭐
    private String owner;
    private String locationText;
    private Object rent; // Changed to Object to match map.put("rent", rentValue)
    private String contact;
    private String roomType;
    private List<String> images; // Key: 'images'
    private String fullAddress;
    private String name; // Key: 'name'

    // NEWLY ADDED FIELDS FROM DATA SAVING CODE (Activity_list_room)
    private String mapLink;
    private String id; // Key: 'id'
    private String ownerId; // Key: 'ownerId'

    // अन्य लिस्टिंग के लिए फ़ील्ड्स (Ignored by Firebase if no Setter/Property)
    private Boolean active;
    private Object listingDate;
    private Object pgId;
    private Object imageUrls;
    private Object facilities;
    private String description;
    private String securityDeposit;
    private String occupancyType;
    private String contactNumber;


    // ⭐ 3. Firebase के लिए आवश्यक: PUBLIC NO-ARGS CONSTRUCTOR ⭐
    public RecommendationItem() {
    }

    // ⭐ 4. GETTERS (Adapter/Details Activity uses these) ⭐

    // Image URL: Rooms aur PGs dono ke liye image dega
    public String getImageUrl() {
        if (firstImageUrl != null) return firstImageUrl;
        if (images != null && !images.isEmpty()) return images.get(0);
        return null;
    }

    public String getTitle() {
        if (name != null && !name.isEmpty()) {
            return name;
        }
        if (title != null && !title.isEmpty()) {
            return title;
        }
        if (roomType != null && locationText != null) {
            return roomType + " in " + locationText;
        }
        return "Listing";
    }

    public String getSubtitle() {
        if (owner != null && !owner.isEmpty()) {
            return "Owner: " + owner;
        }
        if (subtitle != null && !subtitle.isEmpty()) {
            return subtitle;
        }
        if (fullAddress != null && !fullAddress.isEmpty()) {
            return fullAddress;
        }
        return "Details N/A";
    }

    public String getPrice() {
        if (rent != null) {
            return "₹ " + rent.toString() + "/month";
        }
        if (monthlyRent != null && !monthlyRent.isEmpty()) {
            return "₹ " + monthlyRent + "/month";
        }
        return "Price N/A";
    }

    public String getListingId() {
        if (listingId != null) return listingId;
        return id; // id field ko listingId ke roop mein use karein
    }

    // ... Baaki ke Getters same hain ...
    public String getType() { return type; }
    public String getDescription() { return description; }
    public String getContactNumber() { if (contact != null) return contact; return contactNumber; }
    public String getFullAddress() { return fullAddress; }
    public String getName() { return name; }
    public String getOwnerId() { return ownerId; }
    public String getRoomType() { return roomType; }
    public String getMapLink() { return mapLink; }


    // ⭐ 5. SETTERS (Firebase Mapping ke liye) - @PropertyName ab bhi zaroori hai ⭐

    // --- PGs/Favorites Setters ---
    public void setFirstImageUrl(String firstImageUrl) { this.firstImageUrl = firstImageUrl; }
    public void setMonthlyRent(String monthlyRent) { this.monthlyRent = monthlyRent; }

    // --- Rooms Setters (Keys are exactly same as map.put() keys) ---
    @PropertyName("owner") public void setOwner(String owner) { this.owner = owner; }
    @PropertyName("locationText") public void setLocationText(String locationText) { this.locationText = locationText; }
    @PropertyName("rent") public void setRent(Object rent) { this.rent = rent; }
    @PropertyName("contact") public void setContact(String contact) { this.contact = contact; }
    @PropertyName("roomType") public void setRoomType(String roomType) { this.roomType = roomType; }
    @PropertyName("images") public void setImages(List<String> images) { this.images = images; }
    @PropertyName("address") public void setAddress(String address) { this.fullAddress = address; } // maps 'address' to fullAddress
    @PropertyName("name") public void setName(String name) { this.name = name; }

    // NEW SETTERS
    @PropertyName("mapLink") public void setMapLink(String mapLink) { this.mapLink = mapLink; }
    @PropertyName("id") public void setId(String id) { this.id = id; } // id ko seedha map karein
    @PropertyName("ownerId") public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    // --- Common Setters ---
    public void setTitle(String title) { this.title = title; }
    public void setSubtitle(String subtitle) { this.subtitle = subtitle; }
    public void setType(String type) { this.type = type; }
    public void setListingId(String listingId) { this.listingId = listingId; }

    // ... Baaki ke setters same hain ...
    public void setDescription(String description) { this.description = description; }
    public void setSecurityDeposit(String securityDeposit) { this.securityDeposit = securityDeposit; }
    public void setOccupancyType(String occupancyType) { this.occupancyType = occupancyType; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }
    public void setFullAddress(String fullAddress) { this.fullAddress = fullAddress; }
    public void setActive(Boolean active) { this.active = active; }
    public void setListingDate(Object listingDate) { this.listingDate = listingDate; }
    public void setPgId(Object pgId) { this.pgId = pgId; }
    public void setImageUrls(Object imageUrls) { this.imageUrls = imageUrls; }
    public void setFacilities(Object facilities) { this.facilities = facilities; }
}