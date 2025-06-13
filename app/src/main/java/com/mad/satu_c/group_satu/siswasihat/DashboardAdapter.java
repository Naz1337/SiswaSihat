package com.mad.satu_c.group_satu.siswasihat;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

public class DashboardAdapter extends RecyclerView.Adapter<DashboardAdapter.ViewHolder> {

    private static final String TAG = "DashboardAdapter";
    private String[] localDataSet;
    private AppCompatActivity activity;
    private String username; // Add username field

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvFeatureName;
        public TextView tvFeatureDescription;

        public ViewHolder(View view) {
            super(view);
            tvFeatureName = view.findViewById(R.id.tvFeatureName);
            tvFeatureDescription = view.findViewById(R.id.tvFeatureDescription);
        }
    }

    public DashboardAdapter(AppCompatActivity activity, String[] dataSet, String username) { // Add username to constructor
        this.activity = activity;
        this.localDataSet = dataSet;
        this.username = username; // Initialize username
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.grid_item_dashboard, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        String featureName = localDataSet[position];
        viewHolder.tvFeatureName.setText(featureName);
        // Set description based on featureName if needed, or leave generic
        viewHolder.tvFeatureDescription.setText("Short description of the feature");

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Clicked on: " + featureName);
                Toast.makeText(activity, "Opening " + featureName, Toast.LENGTH_SHORT).show();

                if (featureName.equals(activity.getString(R.string.planner))) {
                    Intent intent = new Intent(activity, PlannerActivity.class);
                    intent.putExtra("USERNAME", username); // Pass the username
                    activity.startActivity(intent);
                } else if (featureName.equals(activity.getString(R.string.mood_tracker))) {
                    Intent intent = new Intent(activity, MoodTrackerActivity.class);
                    intent.putExtra("USERNAME", username); // Pass the username
                    activity.startActivity(intent);
                } else if (featureName.equals(activity.getString(R.string.mental_health_quiz))) {
                    Intent intent = new Intent(activity, AnxietyQuizActivity.class);
                    intent.putExtra("USERNAME", username); // Pass the username
                    activity.startActivity(intent);
                } else if (featureName.equals(activity.getString(R.string.focus_zone))) {
                    Intent intent = new Intent(activity, FocusZoneActivity.class);
                    intent.putExtra("USERNAME", username); // Pass the username
                    activity.startActivity(intent);
                }
                else if (featureName.equals(activity.getString(R.string.sos))) {
                    Intent intent = new Intent(activity, SosHelpActivity.class);
                    intent.putExtra("USERNAME", username); // Pass the username
                    activity.startActivity(intent);
                }
                else if (featureName.equals(activity.getString(R.string.breathing))) {
                    Intent intent = new Intent(activity, BreathingExerciseActivity.class);
                    intent.putExtra("USERNAME", username); // Pass the username
                    activity.startActivity(intent);
                }
                // TODO: Add else if for other features as they are implemented
            }
        });
    }

    @Override
    public int getItemCount() {
        return localDataSet.length;
    }
}
