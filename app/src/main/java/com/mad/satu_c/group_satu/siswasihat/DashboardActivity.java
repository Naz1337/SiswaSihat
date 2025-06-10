package com.mad.satu_c.group_satu.siswasihat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class DashboardActivity extends AppCompatActivity {
    private static final String TAG = "DashboardActivity";
    private RecyclerView recyclerView;
    private Button buttonLogout;
    private String username; // To store the logged-in username

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Get username from intent
        username = getIntent().getStringExtra("USERNAME");
        if (username == null) {
            Toast.makeText(this, "Error: Username not found.", Toast.LENGTH_SHORT).show();
            finish(); // Close activity if no username
            return;
        }

        initViews();
        initListeners();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2)); // 2 columns
        int spacing = getResources().getDimensionPixelSize(R.dimen.grid_spacing); // 16dp
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, spacing, true));
        String[] features = getResources().getStringArray(R.array.dashboard_features);
        DashboardAdapter adapter = new DashboardAdapter(this, features, username); // Pass username to adapter
        recyclerView.setAdapter(adapter);

        buttonLogout = findViewById(R.id.buttonLogout);
    }

    private void initListeners() {
        // Listeners are handled within the DashboardAdapter now
        buttonLogout.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

}
