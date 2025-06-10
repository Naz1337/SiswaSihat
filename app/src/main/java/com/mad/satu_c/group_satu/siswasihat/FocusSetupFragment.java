package com.mad.satu_c.group_satu.siswasihat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class FocusSetupFragment extends Fragment {

    private int selectedDuration = 0; // in minutes

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_focus_setup, container, false);

        Button button15Min = view.findViewById(R.id.button15Min);
        Button button30Min = view.findViewById(R.id.button30Min);
        Button button60Min = view.findViewById(R.id.button60Min);
        Button buttonStartFocus = view.findViewById(R.id.buttonStartFocus);

        View.OnClickListener durationClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.button15Min) {
                    selectedDuration = 15;
                } else if (v.getId() == R.id.button30Min) {
                    selectedDuration = 30;
                } else if (v.getId() == R.id.button60Min) {
                    selectedDuration = 60;
                }
                Toast.makeText(getContext(), "Selected: " + selectedDuration + " minutes", Toast.LENGTH_SHORT).show();
            }
        };

        button15Min.setOnClickListener(durationClickListener);
        button30Min.setOnClickListener(durationClickListener);
        button60Min.setOnClickListener(durationClickListener);

        buttonStartFocus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedDuration > 0) {
                    if (getActivity() instanceof FocusZoneActivity) {
                        ((FocusZoneActivity) getActivity()).startInstructions(selectedDuration);
                    }
                } else {
                    Toast.makeText(getContext(), "Please select a duration", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }
}
