package com.develophub.roomfinder;

import java.util.ArrayList;
import java.util.List;

public class PgModel {

    // 1. Identification and Status
    private String pgId;
    private String ownerId;
    private Long listingDate;
    private Boolean isActive;

    // 2. Basic Details
    private String name;
    private String description;
    private String occupancyType;

    // 3. Location and Address
    private String fullAddress;
    private String area;
    private String city;

    // ⭐ FIX 1: Missing field added to resolve compile error in FavoritesAdapter
    private String locationText;

    private Double latitude;
    private Double longitude;

    // 4. Financials
    private Integer monthlyRent;
    private Integer securityDeposit;

    // 5. Features and Facilities
    private List<String> facilities = new ArrayList<>();

    // 6. Media
    private List<String> imageUrls = new ArrayList<>();

    // 7. Contact
    private String contactNumber;
    private String contactEmail;

    // ---------------------------------------------------------------------
    // CONSTRUCTOR
    // ---------------------------------------------------------------------

    public PgModel() {
        // Default constructor required for Firebase.
    }

    // Full Constructor (Updated to use Integer wrappers)
    public PgModel(String pgId, String ownerId, String name, String fullAddress, String occupancyType, Integer monthlyRent, String contactNumber, List<String> imageUrls, List<String> facilities) {
        this.pgId = pgId;
        this.ownerId = ownerId;
        this.name = name;
        this.fullAddress = fullAddress;
        this.occupancyType = occupancyType;
        this.monthlyRent = monthlyRent;
        this.contactNumber = contactNumber;

        this.imageUrls = (imageUrls != null) ? imageUrls : new ArrayList<>();
        this.facilities = (facilities != null) ? facilities : new ArrayList<>();

        this.isActive = true;
        this.listingDate = System.currentTimeMillis();
    }


    // ---------------------------------------------------------------------
    // GETTERS AND SETTERS
    // ---------------------------------------------------------------------

    public String getPgId() { return pgId; }
    public void setPgId(String pgId) { this.pgId = pgId; }

    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    public Long getListingDate() { return listingDate; }
    public void setListingDate(Long listingDate) { this.listingDate = listingDate; }

    public Boolean getActive() { return isActive; }
    public void setActive(Boolean active) { isActive = active; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getOccupancyType() { return occupancyType; }
    public void setOccupancyType(String occupancyType) { this.occupancyType = occupancyType; }

    public String getFullAddress() { return fullAddress; }
    public void setFullAddress(String fullAddress) { this.fullAddress = fullAddress; }

    public String getArea() { return area; }
    public String setArea(String area) { return area; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    // ⭐ FIX 2: Getter and Setter for locationText added
    public String getLocationText() { return locationText; }
    public void setLocationText(String locationText) { this.locationText = locationText; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public Integer getMonthlyRent() { return monthlyRent; }
    public void setMonthlyRent(Integer monthlyRent) { this.monthlyRent = monthlyRent; }

    public Integer getSecurityDeposit() { return securityDeposit; }
    public void setSecurityDeposit(Integer securityDeposit) { this.securityDeposit = securityDeposit; }

    public List<String> getFacilities() { return facilities; }
    public void setFacilities(List<String> facilities) {
        this.facilities = (facilities != null) ? facilities : new ArrayList<>();
    }

    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = (imageUrls != null) ? imageUrls : new ArrayList<>();
    }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }

    // First Image URL (Helper Getter)
    public String getFirstImageUrl() {
        if (imageUrls != null && !imageUrls.isEmpty()) {
            return imageUrls.get(0);
        }
        return null;
    }
}