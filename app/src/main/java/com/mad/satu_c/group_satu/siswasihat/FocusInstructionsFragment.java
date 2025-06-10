package com.mad.satu_c.group_satu.siswasihat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class FocusInstructionsFragment extends Fragment {

    private static final String ARG_DURATION = "duration";
    private int focusDuration;

    public static FocusInstructionsFragment newInstance(int duration) {
        FocusInstructionsFragment fragment = new FocusInstructionsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_DURATION, duration);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            focusDuration = getArguments().getInt(ARG_DURATION);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_focus_instructions, container, false);
        return view;
    }

    // You might want to add a method here to get the duration if needed by the parent activity
    public int getFocusDuration() {
        return focusDuration;
    }
}
