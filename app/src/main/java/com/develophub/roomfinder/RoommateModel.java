package com.develophub.roomfinder;

import com.google.firebase.database.IgnoreExtraProperties;
import java.util.ArrayList;
import java.util.List;

@IgnoreExtraProperties
public class RoommateModel {

    // ---------------------------------------------
    // 1. Core Roommate Fields
    // ---------------------------------------------
    private String name;
    private Object age;
    private String gender;
    private String about;
    private String phone; // ⭐ NEW FIELD: Added for mobile number

    // Past Roommate Feedback List
    private List<RoommateFeedbackEntry> pastRoommateFeedback = new ArrayList<>();

    // ---------------------------------------------
    // 2. Universal Listing Fields
    // ---------------------------------------------
    private String id;
    private String ownerId;
    private String listingType;
    private String location;
    private Object contact;
    private Object budget;
    private String locationText;
    private String address;

    // Media Fields
    private String imageBase64;
    private List<String> images = new ArrayList<>();
    private List<String> imageUrls = new ArrayList<>();

    // Financials
    private String rent;
    private Object monthlyRent;
    private Object securityDeposit;

    // Map & Status
    private String mapLink;
    private Object latitude;
    private Object longitude;
    private Object active;

    // ---------------------------------------------
    // 3. PG/Room Specific Fields
    // ---------------------------------------------
    private String pgId;
    private String roomType;
    private String description;
    private String occupancyType;
    private List<String> facilities = new ArrayList<>();

    private String listingDate;
    private Object contactNumber;
    private String fullAddress;

    // ---------------------------------------------------------------------
    // CONSTRUCTOR (REQUIRED)
    // ---------------------------------------------------------------------

    public RoommateModel() {
        // Required empty constructor for Firebase
    }

    // ---------------------------------------------------------------------
    // GETTERS AND SETTERS
    // ---------------------------------------------------------------------

    // Core Roommate Fields
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Object getAge() { return age; }
    public void setAge(Object age) { this.age = age; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getAbout() { return about; }
    public void setAbout(String about) { this.about = about; }

    // ⭐ NEW: Phone Getter and Setter
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    // Roommate Feedback GETTER AND SETTER
    public List<RoommateFeedbackEntry> getPastRoommateFeedback() {
        return pastRoommateFeedback;
    }
    public void setPastRoommateFeedback(List<RoommateFeedbackEntry> pastRoommateFeedback) {
        this.pastRoommateFeedback = (pastRoommateFeedback != null) ? pastRoommateFeedback : new ArrayList<>();
    }

    // Universal Listing Fields
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
    public String getListingType() { return listingType; }
    public void setListingType(String listingType) { this.listingType = listingType; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public Object getContact() { return contact; }
    public void setContact(Object contact) { this.contact = contact; }
    public Object getBudget() { return budget; }
    public void setBudget(Object budget) { this.budget = budget; }
    public String getLocationText() { return locationText; }
    public void setLocationText(String locationText) { this.locationText = locationText; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getImageBase64() { return imageBase64; }
    public void setImageBase64(String imageBase64) { this.imageBase64 = imageBase64; }

    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = (images != null) ? images : new ArrayList<>(); }

    public String getRent() { return rent; }
    public void setRent(String rent) { this.rent = rent; }
    public String getMapLink() { return mapLink; }
    public void setMapLink(String mapLink) { this.mapLink = mapLink; }
    public Object getLatitude() { return latitude; }
    public void setLatitude(Object latitude) { this.latitude = latitude; }
    public Object getLongitude() { return longitude; }
    public void setLongitude(Object longitude) { this.longitude = longitude; }
    public Object getActive() { return active; }
    public void setActive(Object active) { this.active = active; }

    // PG/Room Specific Fields
    public String getRoomType() { return roomType; }
    public void setRoomType(String roomType) { this.roomType = roomType; }
    public String getListingDate() { return listingDate; }
    public void setListingDate(String listingDate) { this.listingDate = listingDate; }
    public String getPgId() { return pgId; }
    public void setPgId(String pgId) { this.pgId = pgId; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Object getSecurityDeposit() { return securityDeposit; }
    public void setSecurityDeposit(Object securityDeposit) { this.securityDeposit = securityDeposit; }
    public Object getMonthlyRent() { return monthlyRent; }
    public void setMonthlyRent(Object monthlyRent) { this.monthlyRent = monthlyRent; }
    public String getOccupancyType() { return occupancyType; }
    public void setOccupancyType(String occupancyType) { this.occupancyType = occupancyType; }

    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = (imageUrls != null) ? imageUrls : new ArrayList<>(); }

    public Object getContactNumber() { return contactNumber; }
    public void setContactNumber(Object contactNumber) { this.contactNumber = contactNumber; }
    public String getFullAddress() { return fullAddress; }
    public void setFullAddress(String fullAddress) { this.fullAddress = fullAddress; }

    public List<String> getFacilities() { return facilities; }
    public void setFacilities(List<String> facilities) { this.facilities = (facilities != null) ? facilities : new ArrayList<>(); }

    public String getFirstImageUrl() {
        if (imageUrls != null && !imageUrls.isEmpty()) {
            return imageUrls.get(0);
        }
        if (images != null && !images.isEmpty()) {
            return images.get(0);
        }
        return imageBase64;
    }
}