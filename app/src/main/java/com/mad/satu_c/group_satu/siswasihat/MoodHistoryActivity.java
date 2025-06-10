package com.mad.satu_c.group_satu.siswasihat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MoodHistoryActivity extends AppCompatActivity {

    private static final String TAG = "MoodHistoryActivity";

    private RecyclerView recyclerView;
    private MoodHistoryAdapter adapter;
    private List<MoodLogEntry> moodLogList;
    private FirebaseFirestore db;
    private String username;
    private Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood_history);

        username = getIntent().getStringExtra("USERNAME");
        if (username == null) {
            Log.e(TAG, "Username not received from intent.");
            Toast.makeText(this, "Error: User not identified.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db = FirebaseFirestore.getInstance();
        moodLogList = new ArrayList<>();

        initViews();
        initListeners();
        loadMoodHistory();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewMoodHistory);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MoodHistoryAdapter(moodLogList);
        recyclerView.setAdapter(adapter);

        backButton = findViewById(R.id.buttonBackToMoodTracker);
    }

    private void initListeners() {
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(MoodHistoryActivity.this, MoodTrackerActivity.class);
            intent.putExtra("USERNAME", username); // Pass username back if needed
            startActivity(intent);
            finish(); // Close MoodHistoryActivity
        });
    }

    private void loadMoodHistory() {
        db.collection("mood_logs")
                .whereEqualTo("userId", username)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        moodLogList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            MoodLogEntry entry = document.toObject(MoodLogEntry.class);
                            moodLogList.add(entry);
                        }
                        adapter.notifyDataSetChanged();
                        if (moodLogList.isEmpty()) {
                            Toast.makeText(MoodHistoryActivity.this, "No mood entries found.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, "Error getting mood logs: ", task.getException());
                        Toast.makeText(MoodHistoryActivity.this, "Error loading mood history.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
