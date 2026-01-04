package com.develophub.roomfinder;

import java.util.ArrayList;
import java.util.List;

public class RoomModel {

    // ⭐ FIX 1: Null Safety के लिए Rent को Integer में बदलें
    private Integer rent;

    // ⭐ FIX 2: Contact फ़ील्ड को Object में बदलें क्योंकि यह Firebase में Long (संख्या) के रूप में सेव है।
    private String id, owner, location, roomType;
    private Object contact; // Long, String दोनों को स्वीकार करने के लिए Object

    private String mapLink;

    private List<String> images = new ArrayList<>();

    public RoomModel() {
        // empty constructor for Firebase
    }

    // UPDATED CONSTRUCTOR
    public RoomModel(String id, String owner, String location, Integer rent,
                     Object contact, String roomType, String mapLink, List<String> images) {
        this.id = id;
        this.owner = owner;
        this.location = location;
        this.rent = rent;
        this.contact = contact;
        this.roomType = roomType;
        this.mapLink = mapLink;
        this.images = (images != null) ? images : new ArrayList<>();
    }

    // Getters
    public String getId() { return id; }
    public String getOwner() { return owner; }
    public String getLocation() { return location; }

    // ⭐ FIX 3: getRent() अब Integer लौटाएगा (Null Safety)
    public Integer getRent() { return rent; }

    // ⭐ FIX 4: getContact() अब Object लौटाएगा
    public Object getContact() { return contact; }
    public String getRoomType() { return roomType; }
    public List<String> getImages() { return images; }
    public String getMapLink() { return mapLink; }

    // Helper method to safely get contact as String (optional but useful for UI)
    public String getContactString() {
        if (contact == null) return null;
        return contact.toString();
    }


    // Setters
    public void setId(String id) { this.id = id; }
    public void setOwner(String owner) { this.owner = owner; }
    public void setLocation(String location) { this.location = location; }

    // ⭐ FIX 5: setRent() अब Integer पैरामीटर स्वीकार करेगा
    public void setRent(Integer rent) { this.rent = rent; }

    // ⭐ FIX 6: setContact() अब Object पैरामीटर स्वीकार करेगा
    public void setContact(Object contact) { this.contact = contact; }

    public void setRoomType(String roomType) { this.roomType = roomType; }
    public void setMapLink(String mapLink) { this.mapLink = mapLink; }
    public void setImages(List<String> images) { this.images = images; }
}