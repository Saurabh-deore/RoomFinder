package com.develophub.roomfinder;

public class MessageModel {

    // ⭐ NEW: मैसेज को Firebase से हटाने के लिए आवश्यक ID
    private String messageId;

    // ⭐ EXISTING: मैसेज भेजने वाले उपयोगकर्ता की ID
    private String senderId;

    // ⭐ Private Fields (Encapsulation)
    private String senderName;
    private String messageText;
    private long timestamp;

    public MessageModel() {
        // Firebase Ke Liye Zaroori Default Constructor
    }

    // Constructor with all fields (senderId जोड़ा गया)
    // NOTE: आम तौर पर messageId को Constructor में शामिल नहीं किया जाता क्योंकि यह Firebase push key से आता है।
    public MessageModel(String senderId, String senderName, String messageText, long timestamp){
        this.senderId = senderId;
        this.senderName = senderName;
        this.messageText = messageText;
        this.timestamp = timestamp;
    }

    // ---------------------------------------------------------------------
    // ⭐ Public Getters (Firebase data retrieval ke liye)
    // ---------------------------------------------------------------------

    // ⭐ NEW Getter for messageId
    public String getMessageId() {
        return messageId;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getMessageText() {
        return messageText;
    }

    public long getTimestamp() {
        return timestamp;
    }

    // ---------------------------------------------------------------------
    // ⭐ Public Setters (Firebase data writing/updating ke liye)
    // ---------------------------------------------------------------------

    // ⭐ NEW Setter for messageId
    // NavigationFragment में ds.getKey() से प्राप्त मान सेट करने के लिए इसका उपयोग किया जाएगा।
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}