package com.mad.satu_c.group_satu.siswasihat;

import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.os.CountDownTimer;
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
    private TextView tvTimer;
    private Button btnStartExercise;
    private TextView textViewLearnMore; // Added
    private FirebaseFirestore db;
    private CountDownTimer countDownTimer;

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
        tvTimer = findViewById(R.id.tvTimer);
        btnStartExercise = findViewById(R.id.btnStartExercise);
        textViewLearnMore = findViewById(R.id.textViewLearnMore); // Added
    }

    private void initListeners() {
        btnStartExercise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startBreathingExercise();
            }
        });

        textViewLearnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://www.youtube.com/watch?v=LiUnFJ8P4gM";
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            }
        });
    }

    private void startBreathingExercise() {
        tvBreathingStatus.setText(R.string.breathing_in);
        startTimer(4000, R.string.breathing_in);
    }

    private void startTimer(long millisInFuture, final int statusText) {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        countDownTimer = new CountDownTimer(millisInFuture, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tvTimer.setText(String.valueOf(millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                tvBreathingStatus.setText(statusText);
                if (statusText == R.string.breathing_in) {
                    startTimer(4000, R.string.breathing_hold);
                } else if (statusText == R.string.breathing_hold) {
                    startTimer(4000, R.string.breathing_out);
                } else if (statusText == R.string.breathing_out) {
                    tvBreathingStatus.setText(R.string.breathing_complete);
                    logBreathingExercise();
                }
            }
        }.start();
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
