package com.develophub.roomfinder;

// ⭐ Rent के लिए java.lang.Object या Long को Import करें
// यदि RoommateModel में भी समस्या है, तो आपको RoommateModel में भी इसी तरह से Budget/Age को ठीक करना होगा।

public class RoomModel2 {
    private String owner, location, image;
    // ⭐ FIX 1: Rent को Long (या Object) में बदलें
    private Object rent;

    public RoomModel2() { }

    // ⭐ FIX 2: कंस्ट्रक्टर को Object/Long वैल्यू स्वीकार करने के लिए अपडेट करें
    public RoomModel2(String owner, String location, Object rent, String image) {
        this.owner = owner;
        this.location = location;
        this.rent = rent;
        this.image = image;
    }

    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    // ⭐ FIX 3: Getter को सुरक्षित रूप से String रिटर्न करने के लिए अपडेट करें
    public String getRent() {
        if (rent instanceof Number) {
            // Number को String में बदलें, आवश्यक हो तो फॉर्मेट करें
            return String.valueOf(((Number) rent).intValue());
        } else if (rent != null) {
            // यदि किसी कारण से यह String या कोई अन्य Object है
            return String.valueOf(rent);
        }
        return null;
    }

    // ⭐ FIX 4: Setter को Object/Long स्वीकार करने के लिए अपडेट करें
    public void setRent(Object rent) { this.rent = rent; }

    // Firebase Deserialization के लिए Object getRent() आवश्यक हो सकता है
    public Object getRentObject() {
        return rent;
    }


    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
}