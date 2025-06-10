package com.mad.satu_c.group_satu.siswasihat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast; // Import Toast

import androidx.appcompat.app.AppCompatActivity;

public class QuizResultActivity extends AppCompatActivity {

    private TextView textViewScore, textViewInterpretation;
    private Button buttonRetakeQuiz, buttonBackToDashboard;
    private String username; // To store the logged-in username

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
        displayResults();
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
        int score = intent.getIntExtra("SCORE", 0);
        String interpretation = intent.getStringExtra("INTERPRETATION");

        textViewScore.setText(String.valueOf(score));
        textViewInterpretation.setText(interpretation);
    }
}
