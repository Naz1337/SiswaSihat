package com.mad.satu_c.group_satu.siswasihat;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class BreathingExerciseActivity extends AppCompatActivity {

    private static final String TAG = "BreathingExerciseActivity";
    private TextView tvBreathingStatus;
    private Button btnStartExercise;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_breathing_exercise);
        db = FirebaseFirestore.getInstance();
        initViews();
        initListeners();
    }

    private void initViews() {
        tvBreathingStatus = findViewById(R.id.tvBreathingStatus);
        btnStartExercise = findViewById(R.id.btnStartExercise);
    }

    private void initListeners() {
        btnStartExercise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startBreathingExercise();
            }
        });
    }

    private void startBreathingExercise() {
        tvBreathingStatus.setText(R.string.breathing_in);
        // Simulate breathing exercise steps
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(4000); // Breathe in for 4 seconds
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvBreathingStatus.setText(R.string.breathing_hold);
                        }
                    });
                    Thread.sleep(4000); // Hold for 4 seconds
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvBreathingStatus.setText(R.string.breathing_out);
                        }
                    });
                    Thread.sleep(4000); // Breathe out for 4 seconds
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvBreathingStatus.setText(R.string.breathing_complete);
                            logBreathingExercise();
                        }
                    });
                } catch (InterruptedException e) {
                    Log.e(TAG, "Breathing exercise interrupted", e);
                }
            }
        }).start();
    }

    private void logBreathingExercise() {
        Map<String, Object> exerciseLog = new HashMap<>();
        exerciseLog.put("userId", getIntent().getStringExtra("USERNAME"));
        exerciseLog.put("timestamp", System.currentTimeMillis());

        db.collection("breathing_exercises")
                .add(exerciseLog)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(BreathingExerciseActivity.this, R.string.exercise_logged, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error logging breathing exercise", e);
                    Toast.makeText(BreathingExerciseActivity.this, R.string.error_logging_exercise, Toast.LENGTH_SHORT).show();
                });
    }
}