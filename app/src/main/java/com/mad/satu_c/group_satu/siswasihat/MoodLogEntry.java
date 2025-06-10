package com.mad.satu_c.group_satu.siswasihat;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class MoodLogEntry {
    private String userId;
    private int mood;
    private int stress;
    private String journal;
    @ServerTimestamp
    private Date timestamp;

    public MoodLogEntry() {
        // Required no-argument constructor for Firestore
    }

    public MoodLogEntry(String userId, int mood, int stress, String journal, Date timestamp) {
        this.userId = userId;
        this.mood = mood;
        this.stress = stress;
        this.journal = journal;
        this.timestamp = timestamp;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getMood() {
        return mood;
    }

    public void setMood(int mood) {
        this.mood = mood;
    }

    public int getStress() {
        return stress;
    }

    public void setStress(int stress) {
        this.stress = stress;
    }

    public String getJournal() {
        return journal;
    }

    public void setJournal(String journal) {
        this.journal = journal;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
