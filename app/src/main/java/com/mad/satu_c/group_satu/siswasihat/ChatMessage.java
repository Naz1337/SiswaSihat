package com.mad.satu_c.group_satu.siswasihat;

import com.google.firebase.Timestamp;

public class ChatMessage {
    private String message;
    private Timestamp timestamp;

    public ChatMessage() {
        // Required no-argument constructor for Firestore
    }

    public ChatMessage(String message, Timestamp timestamp) {
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }
}
