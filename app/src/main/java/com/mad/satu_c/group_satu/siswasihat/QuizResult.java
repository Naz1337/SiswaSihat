package com.mad.satu_c.group_satu.siswasihat;

import java.util.Date;

public class QuizResult {
    private String userId;
    private int score;
    private Date timestamp;
    private String documentId; // To store the Firestore document ID for deletion

    public QuizResult() {
        // Required empty public constructor for Firestore
    }

    public QuizResult(String userId, int score, Date timestamp, String documentId) {
        this.userId = userId;
        this.score = score;
        this.timestamp = timestamp;
        this.documentId = documentId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }
}
