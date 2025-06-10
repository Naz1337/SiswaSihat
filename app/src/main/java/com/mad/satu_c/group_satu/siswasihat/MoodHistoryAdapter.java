package com.mad.satu_c.group_satu.siswasihat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class MoodHistoryAdapter extends RecyclerView.Adapter<MoodHistoryAdapter.MoodLogViewHolder> {

    private List<MoodLogEntry> moodLogList;

    public MoodHistoryAdapter(List<MoodLogEntry> moodLogList) {
        this.moodLogList = moodLogList;
    }

    @NonNull
    @Override
    public MoodLogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mood_history, parent, false);
        return new MoodLogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MoodLogViewHolder holder, int position) {
        MoodLogEntry entry = moodLogList.get(position);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        holder.textViewDate.setText(sdf.format(entry.getTimestamp()));

        String emoji;
        switch (entry.getMood()) {
            case 1: emoji = "😞"; break;
            case 2: emoji = "🙁"; break;
            case 3: emoji = "😐"; break;
            case 4: emoji = "🙂"; break;
            case 5: emoji = "😊"; break;
            default: emoji = "😐"; break;
        }
        holder.textViewMoodEmoji.setText(emoji);
        holder.textViewStressLevel.setText("Stress: " + entry.getStress() + "/10");
        holder.textViewJournalPreview.setText(entry.getJournal());
    }

    @Override
    public int getItemCount() {
        return moodLogList.size();
    }

    public static class MoodLogViewHolder extends RecyclerView.ViewHolder {
        TextView textViewDate;
        TextView textViewMoodEmoji;
        TextView textViewStressLevel;
        TextView textViewJournalPreview;

        public MoodLogViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewDate = itemView.findViewById(R.id.textViewDate);
            textViewMoodEmoji = itemView.findViewById(R.id.textViewMoodEmoji);
            textViewStressLevel = itemView.findViewById(R.id.textViewStressLevel);
            textViewJournalPreview = itemView.findViewById(R.id.textViewJournalPreview);
        }
    }
}
