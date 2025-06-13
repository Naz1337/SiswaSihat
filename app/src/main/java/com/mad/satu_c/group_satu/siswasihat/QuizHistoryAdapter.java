package com.mad.satu_c.group_satu.siswasihat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class QuizHistoryAdapter extends RecyclerView.Adapter<QuizHistoryAdapter.QuizHistoryViewHolder> {

    private Context context;
    private List<QuizResult> quizResultsList;
    private OnItemLongClickListener onItemLongClickListener;

    public interface OnItemLongClickListener {
        void onItemLongClick(int position);
    }

    public QuizHistoryAdapter(Context context, List<QuizResult> quizResultsList, OnItemLongClickListener onItemLongClickListener) {
        this.context = context;
        this.quizResultsList = quizResultsList;
        this.onItemLongClickListener = onItemLongClickListener;
    }

    @NonNull
    @Override
    public QuizHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_quiz_history, parent, false);
        return new QuizHistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuizHistoryViewHolder holder, int position) {
        QuizResult currentResult = quizResultsList.get(position);

        holder.textViewQuizScore.setText(String.format(Locale.getDefault(), "Score: %d", currentResult.getScore()));

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        holder.textViewQuizDate.setText(String.format("Date: %s", sdf.format(currentResult.getTimestamp())));

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (onItemLongClickListener != null) {
                    onItemLongClickListener.onItemLongClick(holder.getAdapterPosition());
                    return true; // Consume the long click
                }
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return quizResultsList.size();
    }

    public static class QuizHistoryViewHolder extends RecyclerView.ViewHolder {
        TextView textViewQuizScore;
        TextView textViewQuizDate;

        public QuizHistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewQuizScore = itemView.findViewById(R.id.textViewQuizScore);
            textViewQuizDate = itemView.findViewById(R.id.textViewQuizDate);
        }
    }
}
