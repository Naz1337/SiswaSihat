package com.mad.satu_c.group_satu.siswasihat;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Locale;

public class FocusSessionFragment extends Fragment {

    private static final String ARG_DURATION = "duration";
    private int focusDurationMinutes;
    private TextView textViewTimer;
    private TextView textViewStatus;
    private CountDownTimer countDownTimer;
    private long timeLeftInMillis;
    private boolean timerRunning;

    public static FocusSessionFragment newInstance(int duration) {
        FocusSessionFragment fragment = new FocusSessionFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_DURATION, duration);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            focusDurationMinutes = getArguments().getInt(ARG_DURATION);
            timeLeftInMillis = focusDurationMinutes * 60 * 1000;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_focus_session, container, false);
        textViewTimer = view.findViewById(R.id.textViewTimer);
        textViewStatus = view.findViewById(R.id.textViewStatus);

        updateCountDownText();
        return view;
    }

    public void startTimer() {
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateCountDownText();
            }

            @Override
            public void onFinish() {
                timerRunning = false;
                if (textViewStatus != null && getView() != null) {
                    textViewStatus.setText("Session Complete!");
                }
                // TODO: Notify activity that session is complete
            }
        }.start();
        timerRunning = true;
        if (textViewStatus != null && getView() != null) {
            textViewStatus.setText("Session Active");
        }
    }

    public void pauseTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        timerRunning = false;
        if (textViewStatus != null && getView() != null) {
            textViewStatus.setText("Phone Picked Up!");
        }
    }

    public void resumeTimer() {
        startTimer();
    }

    private void updateCountDownText() {
        if (textViewTimer != null && getView() != null) {
            int minutes = (int) (timeLeftInMillis / 1000) / 60;
            int seconds = (int) (timeLeftInMillis / 1000) % 60;

            String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
            textViewTimer.setText(timeLeftFormatted);
        }
    }

    public boolean isTimerRunning() {
        return timerRunning;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
