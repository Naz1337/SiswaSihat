package com.mad.satu_c.group_satu.siswasihat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log; // Import Log
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast; // Import Toast

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class QuizResultActivity extends AppCompatActivity {

    private static final String TAG = "QuizResultActivity";
    private TextView textViewScore, textViewInterpretation;
    private Button buttonRetakeQuiz, buttonBackToDashboard;
    private String username; // To store the logged-in username
    private FirebaseFirestore db;
    private int score; // Declare score as a class-level variable

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_result);

        initViews();
        initListeners();
        // Get username from intent
        username = getIntent().getStringExtra("USERNAME");
        if (username == null) {
            // Handle case where username is not passed (e.g., show error and finish)
            Toast.makeText(this, "Error: Username not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        db = FirebaseFirestore.getInstance(); // Initialize Firestore
        displayResults();
        saveQuizResult(score, username); // Save the quiz result to Firestore
    }

    private void initViews() {
        textViewScore = findViewById(R.id.textViewScore);
        textViewInterpretation = findViewById(R.id.textViewInterpretation);
        buttonRetakeQuiz = findViewById(R.id.buttonRetakeQuiz);
        buttonBackToDashboard = findViewById(R.id.buttonBackToDashboard);
    }

    private void initListeners() {
        buttonRetakeQuiz.setOnClickListener(v -> {
            Intent intent = new Intent(QuizResultActivity.this, AnxietyQuizActivity.class);
            // Pass username if needed for retake
            startActivity(intent);
            finish();
        });

        buttonBackToDashboard.setOnClickListener(v -> {
            Intent intent = new Intent(QuizResultActivity.this, DashboardActivity.class);
            intent.putExtra("USERNAME", username); // Pass the username
            // Clear activity stack to prevent going back to quiz results
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void displayResults() {
        Intent intent = getIntent();
        this.score = intent.getIntExtra("SCORE", 0); // Assign to class-level score
        String interpretation = intent.getStringExtra("INTERPRETATION");

        textViewScore.setText(String.valueOf(score));
        textViewInterpretation.setText(interpretation);
    }

    private void saveQuizResult(int score, String userId) {
        Map<String, Object> quizResult = new HashMap<>();
        quizResult.put("userId", userId);
        quizResult.put("score", score);
        quizResult.put("timestamp", new Date()); // Current timestamp

        db.collection("quiz_results")
                .add(quizResult)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "Quiz result saved with ID: " + documentReference.getId());
                        Toast.makeText(QuizResultActivity.this, "Quiz result saved!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error saving quiz result: " + e.getMessage(), e);
                        Toast.makeText(QuizResultActivity.this, "Error saving quiz result.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
