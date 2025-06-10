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

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvFeatureName;
        public TextView tvFeatureDescription;

        public ViewHolder(View view) {
            super(view);
            tvFeatureName = view.findViewById(R.id.tvFeatureName);
            tvFeatureDescription = view.findViewById(R.id.tvFeatureDescription);
        }
    }

    public DashboardAdapter(AppCompatActivity activity, String[] dataSet) {
        this.activity = activity;
        localDataSet = dataSet;
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

                // TODO: Implement explicit intents for each feature
                // Example:
                // if (featureName.equals(activity.getString(R.string.planner))) {
                //     activity.startActivity(new Intent(activity, PlannerActivity.class));
                // } else if (featureName.equals(activity.getString(R.string.mood_tracker))) {
                //     activity.startActivity(new Intent(activity, MoodTrackerActivity.class));
                // }
            }
        });
    }

    @Override
    public int getItemCount() {
        return localDataSet.length;
    }
}
