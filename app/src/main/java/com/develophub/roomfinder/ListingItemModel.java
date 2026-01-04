package com.develophub.roomfinder;

import java.util.List;
import java.util.ArrayList;

public class ListingItemModel {

    // Firebase को डेटा लोड करने के लिए Default/Empty Constructor ज़रूरी है
    public ListingItemModel() {
        // Required for Firebase DataSnapshot.getValue(ListingItemModel.class)
    }

    // --- COMMON FIELDS ---
    private String id; // Listing ID (push().getKey())
    private String ownerId; // My Listings और Deletion के लिए सबसे ज़रूरी
    private String name; // PG Name, Room Title, या Roommate Name
    private String contact;
    private String location; // Address या Roommate Location

    // --- ROOM/PG SPECIFIC FIELDS ---
    private String address; // (Room/PG Specific, Roommate के लिए null रहेगा)
    private String occupancy;
    private int rent = 0;
    private List<String> images = new ArrayList<>();
    private List<String> facilities = new ArrayList<>();
    private int securityDeposit = 0;
    private String description;

    // FIX: MyListingsActivity में Deletion के लिए नया फ़ील्ड जोड़ा गया
    private String nodeType; // "Rooms", "pgs" या "Roommates" स्टोर करेगा

    // ⭐ NEW: ROOMMATE SPECIFIC FIELDS (RoommateModel से) ⭐
    private String age;
    private String gender;
    private String budget; // Roommate के लिए String बजट
    private String about;
    private String imageBase64; // Roommate का सिंगल प्रोफाइल फोटो (Images लिस्ट का उपयोग नहीं करेगा)


    // --- Getters और Setters (Firebase के लिए अनिवार्य) ---

    // Getters
    public String getId() { return id; }
    public String getOwnerId() { return ownerId; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public String getOccupancy() { return occupancy; }
    public int getRent() { return rent; }
    public String getContact() { return contact; }
    public List<String> getImages() { return images; }
    public List<String> getFacilities() { return facilities; }
    public int getSecurityDeposit() { return securityDeposit; }
    public String getDescription() { return description; }
    public String getNodeType() { return nodeType; }

    // ⭐ NEW ROOMMATE GETTERS
    public String getAge() { return age; }
    public String getGender() { return gender; }
    public String getBudget() { return budget; }
    public String getAbout() { return about; }
    public String getImageBase64() { return imageBase64; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
    public void setName(String name) { this.name = name; }
    public void setAddress(String address) { this.address = address; }
    public void setOccupancy(String occupancy) { this.occupancy = occupancy; }
    public void setRent(int rent) { this.rent = rent; }
    public void setContact(String contact) { this.contact = contact; }
    public void setImages(List<String> images) { this.images = images; }
    public void setFacilities(List<String> facilities) { this.facilities = facilities; }
    public void setSecurityDeposit(int securityDeposit) { this.securityDeposit = securityDeposit; }
    public void setDescription(String description) { this.description = description; }
    public void setNodeType(String nodeType) { this.nodeType = nodeType; }

    // ⭐ NEW ROOMMATE SETTERS
    public void setAge(String age) { this.age = age; }
    public void setGender(String gender) { this.gender = gender; }
    public void setBudget(String budget) { this.budget = budget; }
    public void setAbout(String about) { this.about = about; }
    public void setImageBase64(String imageBase64) { this.imageBase64 = imageBase64; }

    // नोट: मैंने सुविधा के लिए आपका पुराना कंस्ट्रक्टर हटा दिया है क्योंकि Firebase ऑटोमैपिंग Getters/Setters पर निर्भर करती है।
}