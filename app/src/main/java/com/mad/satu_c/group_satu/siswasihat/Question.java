package com.mad.satu_c.group_satu.siswasihat;

public class Question {
    private String questionText;
    private String[] options;

    public Question(String questionText, String[] options) {
        this.questionText = questionText;
        this.options = options;
    }

    public String getQuestionText() {
        return questionText;
    }

    public String[] getOptions() {
        return options;
    }
}
