package com.mad.satu_c.group_satu.siswasihat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnxietyQuizActivity extends AppCompatActivity implements QuestionFragment.OnAnswerSelectedListener {

    private static final String TAG = "AnxietyQuizActivity";

    private ViewPager viewPagerQuiz;
    private Button buttonPrevious, buttonNext, buttonSubmit;
    private TextView textViewQuizTitle;

    private QuizPagerAdapter quizPagerAdapter;
    private List<Question> questions;
    private List<Integer> answers; // Stores selected answer index for each question

    private String loggedInUsername;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anxiety_quiz);

        loggedInUsername = getIntent().getStringExtra("USERNAME");
        if (loggedInUsername == null) {
            Toast.makeText(this, "Error: Username not found. Please log in again.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        db = FirebaseFirestore.getInstance();

        initViews();
        initListeners();
        setupQuizQuestions();
    }

    private void initViews() {
        viewPagerQuiz = findViewById(R.id.viewPagerQuiz);
        buttonPrevious = findViewById(R.id.buttonPrevious);
        buttonNext = findViewById(R.id.buttonNext);
        buttonSubmit = findViewById(R.id.buttonSubmit);
        textViewQuizTitle = findViewById(R.id.textViewQuizTitle);
    }

    private void initListeners() {
        buttonPrevious.setOnClickListener(v -> {
            if (viewPagerQuiz.getCurrentItem() > 0) {
                viewPagerQuiz.setCurrentItem(viewPagerQuiz.getCurrentItem() - 1);
            }
        });

        buttonNext.setOnClickListener(v -> {
            if (viewPagerQuiz.getCurrentItem() < questions.size() - 1) {
                viewPagerQuiz.setCurrentItem(viewPagerQuiz.getCurrentItem() + 1);
            } else {
                // Last question, show submit button
                handleSubmit();
            }
        });

        buttonSubmit.setOnClickListener(v -> handleSubmit());

        viewPagerQuiz.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // Not used
            }

            @Override
            public void onPageSelected(int position) {
                updateNavigationButtons(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // Not used
            }
        });
    }

    private void setupQuizQuestions() {
        questions = new ArrayList<>();
        // GAD-7 Questions
        questions.add(new Question("Feeling nervous, anxious, or on edge?",
                new String[]{"Not at all", "Several days", "More than half the days", "Nearly every day"}));
        questions.add(new Question("Not being able to stop or control worrying?",
                new String[]{"Not at all", "Several days", "More than half the days", "Nearly every day"}));
        questions.add(new Question("Worrying too much about different things?",
                new String[]{"Not at all", "Several days", "More than half the days", "Nearly every day"}));
        questions.add(new Question("Trouble relaxing?",
                new String[]{"Not at all", "Several days", "More than half the days", "Nearly every day"}));
        questions.add(new Question("Being so restless that it's hard to sit still?",
                new String[]{"Not at all", "Several days", "More than half the days", "Nearly every day"}));
        questions.add(new Question("Becoming easily annoyed or irritable?",
                new String[]{"Not at all", "Several days", "More than half the days", "Nearly every day"}));
        questions.add(new Question("Feeling afraid as if something awful might happen?",
                new String[]{"Not at all", "Several days", "More than half the days", "Nearly every day"}));

        answers = new ArrayList<>(questions.size());
        for (int i = 0; i < questions.size(); i++) {
            answers.add(-1); // Initialize with -1 (no answer selected)
        }

        quizPagerAdapter = new QuizPagerAdapter(getSupportFragmentManager());
        viewPagerQuiz.setAdapter(quizPagerAdapter);
        viewPagerQuiz.setOffscreenPageLimit(questions.size()); // Keep all fragments in memory
        updateNavigationButtons(0);
    }

    private void updateNavigationButtons(int position) {
        buttonPrevious.setVisibility(position == 0 ? View.GONE : View.VISIBLE);
        buttonNext.setVisibility(position == questions.size() - 1 ? View.GONE : View.VISIBLE);
        buttonSubmit.setVisibility(position == questions.size() - 1 ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onAnswerSelected(int questionIndex, int answerIndex) {
        if (questionIndex >= 0 && questionIndex < answers.size()) {
            answers.set(questionIndex, answerIndex);
            Log.d(TAG, "Question " + questionIndex + " answered with: " + answerIndex);
        }
    }

    private void handleSubmit() {
        // Check if all questions are answered
        for (int i = 0; i < answers.size(); i++) {
            if (answers.get(i) == -1) {
                Toast.makeText(this, "Please answer all questions before submitting.", Toast.LENGTH_SHORT).show();
                viewPagerQuiz.setCurrentItem(i); // Go to the unanswered question
                return;
            }
        }

        int totalScore = calculateScore();
        String interpretation = getInterpretation(totalScore);

        saveQuizResult(totalScore, interpretation);

        Intent intent = new Intent(AnxietyQuizActivity.this, QuizResultActivity.class);
        intent.putExtra("SCORE", totalScore);
        intent.putExtra("INTERPRETATION", interpretation);
        intent.putExtra("USERNAME", loggedInUsername); // Pass the username
        startActivity(intent);
        finish();
    }

    private int calculateScore() {
        int score = 0;
        for (int answer : answers) {
            score += answer;
        }
        return score;
    }

    private String getInterpretation(int score) {
        if (score >= 15) {
            return "Severe anxiety";
        } else if (score >= 10) {
            return "Moderate anxiety";
        } else if (score >= 5) {
            return "Mild anxiety";
        } else {
            return "Minimal anxiety";
        }
    }

    private void saveQuizResult(int score, String interpretation) {
        Map<String, Object> quizResult = new HashMap<>();
        quizResult.put("userId", loggedInUsername);
        quizResult.put("score", score);
        quizResult.put("interpretation", interpretation);
        quizResult.put("timestamp", Timestamp.now());

        db.collection("quiz_results")
                .add(quizResult)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Quiz result saved with ID: " + documentReference.getId());
                    Toast.makeText(AnxietyQuizActivity.this, "Quiz result saved!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving quiz result", e);
                    Toast.makeText(AnxietyQuizActivity.this, "Error saving quiz result.", Toast.LENGTH_SHORT).show();
                });
    }

    private class QuizPagerAdapter extends FragmentStatePagerAdapter {

        public QuizPagerAdapter(@NonNull FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return QuestionFragment.newInstance(position, questions.get(position).getQuestionText(), questions.get(position).getOptions());
        }

        @Override
        public int getCount() {
            return questions.size();
        }
    }
}
