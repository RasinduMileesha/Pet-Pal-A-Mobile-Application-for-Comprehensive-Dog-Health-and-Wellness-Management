package com.example.petdoc_app;

public class ChatMessage {
    private String userName;
    private String messageText;
    private String timestamp; // Optional timestamp
    private String userType; // Add userType field

    // Constructor with userType
    public ChatMessage(String userName, String messageText, String userType) {
        this.userName = userName;
        this.messageText = messageText;
        this.userType = userType;
    }

    public String getUserName() {
        return userName;
    }

    public String getMessageText() {
        return messageText;
    }

    public String getUserType() {
        return userType;
    }
}
