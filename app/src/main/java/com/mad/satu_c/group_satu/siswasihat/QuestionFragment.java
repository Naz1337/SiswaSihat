package com.mad.satu_c.group_satu.siswasihat;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class QuestionFragment extends Fragment {

    private static final String ARG_QUESTION_INDEX = "question_index";
    private static final String ARG_QUESTION_TEXT = "question_text";
    private static final String ARG_OPTIONS = "options";

    private int questionIndex;
    private String questionText;
    private String[] options;

    public interface OnAnswerSelectedListener {
        void onAnswerSelected(int questionIndex, int answerIndex);
    }

    private OnAnswerSelectedListener listener;

    public static QuestionFragment newInstance(int questionIndex, String questionText, String[] options) {
        QuestionFragment fragment = new QuestionFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_QUESTION_INDEX, questionIndex);
        args.putString(ARG_QUESTION_TEXT, questionText);
        args.putStringArray(ARG_OPTIONS, options);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnAnswerSelectedListener) {
            listener = (OnAnswerSelectedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnAnswerSelectedListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            questionIndex = getArguments().getInt(ARG_QUESTION_INDEX);
            questionText = getArguments().getString(ARG_QUESTION_TEXT);
            options = getArguments().getStringArray(ARG_OPTIONS);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_question, container, false);

        TextView textViewQuestion = view.findViewById(R.id.textViewQuestion);
        RadioGroup radioGroupOptions = view.findViewById(R.id.radioGroupOptions);

        textViewQuestion.setText(questionText);

        // Clear existing radio buttons to prevent duplication on re-creation
        radioGroupOptions.removeAllViews();

        for (int i = 0; i < options.length; i++) {
            RadioButton radioButton = new RadioButton(getContext());
            radioButton.setId(View.generateViewId()); // Generate unique ID for each radio button
            radioButton.setText(options[i]);
            radioButton.setTextSize(getResources().getDimension(R.dimen.text_description) / getResources().getDisplayMetrics().density); // Convert px to sp
            radioButton.setPadding((int) getResources().getDimension(R.dimen.small_padding), 0, 0, 0); // Apply padding

            int finalI = i;
            radioButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    if (listener != null) {
                        listener.onAnswerSelected(questionIndex, finalI);
                    }
                }
            });
            radioGroupOptions.addView(radioButton);
        }

        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }
}
