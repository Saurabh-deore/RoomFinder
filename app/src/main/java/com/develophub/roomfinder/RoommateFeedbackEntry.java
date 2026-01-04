package com.develophub.roomfinder;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class RoommateFeedbackEntry {
    private String name;
    // Rating values are stored as Floats (1.0 for checked, 0.0 for unchecked)
    private Float cleanlinessRating;
    private Float respectfulRating;
    private Float quietRating;

    // ⭐ NEW FIELDS ADDED
    private Float patientRating;
    private Float organizedRating;
    // ⭐ END NEW FIELDS

    private String comment;

    public RoommateFeedbackEntry() {
        // Required empty constructor for Firebase
    }

    // Constructor with all fields
    public RoommateFeedbackEntry(String name, Float cleanlinessRating, Float respectfulRating, Float quietRating, Float patientRating, Float organizedRating, String comment) {
        this.name = name;
        this.cleanlinessRating = cleanlinessRating;
        this.respectfulRating = respectfulRating;
        this.quietRating = quietRating;
        this.patientRating = patientRating; // ⭐ UPDATED
        this.organizedRating = organizedRating; // ⭐ UPDATED
        this.comment = comment;
    }

    // ---------------------------------------------------------------------
    // Getters and Setters
    // ---------------------------------------------------------------------

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Float getCleanlinessRating() { return cleanlinessRating; }
    public void setCleanlinessRating(Float cleanlinessRating) { this.cleanlinessRating = cleanlinessRating; }

    public Float getRespectfulRating() { return respectfulRating; }
    public void setRespectfulRating(Float respectfulRating) { this.respectfulRating = respectfulRating; }

    public Float getQuietRating() { return quietRating; }
    public void setQuietRating(Float quietRating) { this.quietRating = quietRating; }

    // ⭐ NEW Getters and Setters
    public Float getPatientRating() { return patientRating; }
    public void setPatientRating(Float patientRating) { this.patientRating = patientRating; }

    public Float getOrganizedRating() { return organizedRating; }
    public void setOrganizedRating(Float organizedRating) { this.organizedRating = organizedRating; }
    // ⭐ END NEW Getters and Setters

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}