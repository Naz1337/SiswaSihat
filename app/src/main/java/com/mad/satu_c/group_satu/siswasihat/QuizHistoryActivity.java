package com.mad.satu_c.group_satu.siswasihat;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class QuizHistoryActivity extends AppCompatActivity {

    private static final String TAG = "QuizHistoryActivity";
    private RecyclerView recyclerViewQuizHistory;
    private QuizHistoryAdapter quizHistoryAdapter;
    private List<QuizResult> quizResultsList;
    private FirebaseFirestore db;
    private String loggedInUsername; // To store the logged-in user's username

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_history);

        // Retrieve the logged-in username from the Intent
        loggedInUsername = getIntent().getStringExtra("USERNAME");
        if (loggedInUsername == null) {
            // Handle case where username is not passed (e.g., redirect to login)
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show();
            finish(); // Close this activity
            return;
        }

        db = FirebaseFirestore.getInstance();
        quizResultsList = new ArrayList<>();

        initViews();
        initListeners();
        loadQuizHistory();
    }

    private void initViews() {
        TextView textViewQuizHistoryTitle = findViewById(R.id.textViewQuizHistoryTitle);
        textViewQuizHistoryTitle.setText(getString(R.string.quiz_history_title));

        recyclerViewQuizHistory = findViewById(R.id.recyclerViewQuizHistory);
        recyclerViewQuizHistory.setLayoutManager(new LinearLayoutManager(this));
        quizHistoryAdapter = new QuizHistoryAdapter(this, quizResultsList, new QuizHistoryAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(final int position) {
                showDeleteConfirmationDialog(position);
            }
        });
        recyclerViewQuizHistory.setAdapter(quizHistoryAdapter);
    }

    private void initListeners() {
        Button buttonStartNewQuiz = findViewById(R.id.buttonStartNewQuiz);
        buttonStartNewQuiz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the AnxietyQuizActivity
                Intent intent = new Intent(QuizHistoryActivity.this, AnxietyQuizActivity.class);
                intent.putExtra("USERNAME", loggedInUsername); // Pass username to the quiz
                startActivity(intent);
            }
        });
    }

    private void loadQuizHistory() {
        db.collection("quiz_results")
                .whereEqualTo("userId", loggedInUsername) // Filter by logged-in user
                .orderBy("timestamp", Query.Direction.DESCENDING) // Order by most recent
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        quizResultsList.clear();
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            String userId = document.getString("userId");
                            Long scoreLong = document.getLong("score");
                            Date timestamp = document.getDate("timestamp");
                            String documentId = document.getId(); // Get the document ID

                            if (userId != null && scoreLong != null && timestamp != null) {
                                QuizResult quizResult = new QuizResult(userId, scoreLong.intValue(), timestamp, documentId);
                                quizResultsList.add(quizResult);
                            }
                        }
                        quizHistoryAdapter.notifyDataSetChanged();
                        if (quizResultsList.isEmpty()) {
                            Toast.makeText(QuizHistoryActivity.this, "No quiz history found.", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error loading quiz history: " + e.getMessage(), e);
                        Toast.makeText(QuizHistoryActivity.this, "Error loading quiz history.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showDeleteConfirmationDialog(final int position) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.delete_quiz_record_title))
                .setMessage(getString(R.string.delete_quiz_record_message))
                .setPositiveButton(getString(R.string.delete_button_text), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteQuizRecord(position);
                    }
                })
                .setNegativeButton(getString(R.string.cancel_button_text), null)
                .show();
    }

    private void deleteQuizRecord(final int position) {
        String documentIdToDelete = quizResultsList.get(position).getDocumentId();
        if (documentIdToDelete == null) {
            Toast.makeText(this, "Error: Cannot delete record without ID.", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("quiz_results").document(documentIdToDelete)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(QuizHistoryActivity.this, "Quiz record deleted.", Toast.LENGTH_SHORT).show();
                        quizResultsList.remove(position);
                        quizHistoryAdapter.notifyItemRemoved(position);
                        quizHistoryAdapter.notifyItemRangeChanged(position, quizResultsList.size());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error deleting quiz record: " + e.getMessage(), e);
                        Toast.makeText(QuizHistoryActivity.this, "Error deleting quiz record.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
