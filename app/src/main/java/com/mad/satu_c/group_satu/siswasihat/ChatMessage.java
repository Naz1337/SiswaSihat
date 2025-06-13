package com.mad.satu_c.group_satu.siswasihat;

import com.google.firebase.Timestamp;

public class ChatMessage {
    private String messageId;
    private String username;
    private String message;
    private Timestamp timestamp;

    public ChatMessage() {
        // Required no-argument constructor for Firestore
    }

    public ChatMessage(String messageId, String username, String message, Timestamp timestamp) {
        this.messageId = messageId;
        this.username = username;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}

