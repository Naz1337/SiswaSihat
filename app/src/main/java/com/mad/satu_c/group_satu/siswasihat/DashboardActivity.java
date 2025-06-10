package com.mad.satu_c.group_satu.siswasihat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class DashboardActivity extends AppCompatActivity {
    private static final String TAG = "DashboardActivity";
    private GridView gridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        initViews();
        initListeners();
    }

    private void initViews() {
        gridView = findViewById(R.id.gridView);
        String[] features = getResources().getStringArray(R.array.dashboard_features);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.grid_item_dashboard, R.id.tvFeatureName, features);
        gridView.setAdapter(adapter);
    }

    private void initListeners() {
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String featureName = (String) parent.getItemAtPosition(position);
                Log.d(TAG, "Clicked on: " + featureName);
                Toast.makeText(DashboardActivity.this, "Opening " + featureName, Toast.LENGTH_SHORT).show();

                // TODO: Implement explicit intents for each feature
                // Example:
                // if (featureName.equals(getString(R.string.planner))) {
                //     startActivity(new Intent(DashboardActivity.this, PlannerActivity.class));
                // } else if (featureName.equals(getString(R.string.mood_tracker))) {
                //     startActivity(new Intent(DashboardActivity.this, MoodTrackerActivity.class));
                // }
            }
        });
    }
}
