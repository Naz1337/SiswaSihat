package com.mad.satu_c.group_satu.siswasihat;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MoodTrackerActivity extends AppCompatActivity {

    private static final String TAG = "MoodTrackerActivity";

    private SeekBar moodSlider;
    private SeekBar stressSlider;
    private EditText journalField;
    private Button saveButton;
    private Button buttonViewHistory; // New button for history
    private TextView moodEmoji;
    private TextView stressValueText;

    private FirebaseFirestore db;
    private String username; // To store the logged-in username

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood_tracker);

        // Get username from intent
        username = getIntent().getStringExtra("USERNAME");
        if (username == null) {
            Log.e(TAG, "Username not received from intent.");
            Toast.makeText(this, "Error: User not identified.", Toast.LENGTH_SHORT).show();
            finish(); // Close activity if username is not available
            return;
        }

        db = FirebaseFirestore.getInstance();

        initViews();
        initListeners();
    }

    /**
     * Initializes all UI views from the layout.
     */
    private void initViews() {
        moodSlider = findViewById(R.id.moodSlider);
        stressSlider = findViewById(R.id.stressSlider);
        journalField = findViewById(R.id.journalField);
        saveButton = findViewById(R.id.saveButton);
        buttonViewHistory = findViewById(R.id.buttonViewHistory); // Initialize new button
        moodEmoji = findViewById(R.id.moodEmoji);
        stressValueText = findViewById(R.id.stressValueText);

        // Set initial emoji and stress value
        updateMoodEmoji(moodSlider.getProgress());
        updateStressValueText(stressSlider.getProgress());
    }

    /**
     * Initializes all listeners for UI interactions.
     */
    private void initListeners() {
        moodSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateMoodEmoji(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Not used
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Not used
            }
        });

        stressSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateStressValueText(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Not used
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Not used
            }
        });

        saveButton.setOnClickListener(v -> saveMoodLog());

        buttonViewHistory.setOnClickListener(v -> {
            Intent intent = new Intent(MoodTrackerActivity.this, MoodHistoryActivity.class);
            intent.putExtra("USERNAME", username);
            startActivity(intent);
        });
    }

    /**
     * Updates the mood emoji based on the slider progress.
     * @param progress The current progress of the mood slider (0-4).
     */
    private void updateMoodEmoji(int progress) {
        String emoji;
        switch (progress) {
            case 0: emoji = "😞"; break; // Very Sad
            case 1: emoji = "🙁"; break; // Sad
            case 2: emoji = "😐"; break; // Neutral
            case 3: emoji = "🙂"; break; // Happy
            case 4: emoji = "😊"; break; // Very Happy
            default: emoji = "😐"; break;
        }
        moodEmoji.setText(emoji);
    }

    /**
     * Updates the stress value text based on the slider progress.
     * @param progress The current progress of the stress slider (0-9).
     */
    private void updateStressValueText(int progress) {
        // Stress slider is 0-9, representing 1-10
        stressValueText.setText(String.valueOf(progress + 1));
    }

    /**
     * Saves the current mood log to Firebase Firestore.
     */
    private void saveMoodLog() {
        int mood = moodSlider.getProgress() + 1; // 1-5
        int stress = stressSlider.getProgress() + 1; // 1-10
        String journal = journalField.getText().toString().trim();

        Map<String, Object> moodLog = new HashMap<>();
        moodLog.put("userId", username);
        moodLog.put("mood", mood);
        moodLog.put("stress", stress);
        moodLog.put("journal", journal);
        moodLog.put("timestamp", FieldValue.serverTimestamp());

        db.collection("mood_logs")
                .add(moodLog)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Mood log added with ID: " + documentReference.getId());
                    Toast.makeText(MoodTrackerActivity.this, "Mood log saved!", Toast.LENGTH_SHORT).show();
                    finish(); // Optionally close activity after saving
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding mood log", e);
                    Toast.makeText(MoodTrackerActivity.this, "Error saving mood log: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
