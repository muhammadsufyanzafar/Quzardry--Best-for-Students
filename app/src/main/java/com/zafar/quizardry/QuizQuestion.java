package com.zafar.quizardry;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "quiz_questions")
public class QuizQuestion {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String question;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private int correctIndex; // 0..3

    public QuizQuestion(String question, String a, String b, String c, String d, int correctIndex) {
        this.question = question; this.optionA = a; this.optionB = b;
        this.optionC = c; this.optionD = d; this.correctIndex = correctIndex;
    }

    public QuizQuestion() {

    }

    // getters/setters...
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getQuestion() { return question; }
    public String getOptionA() { return optionA; }
    public String getOptionB() { return optionB; }
    public String getOptionC() { return optionC; }
    public String getOptionD() { return optionD; }
    public int getCorrectIndex() { return correctIndex; }
    public void setQuestion(String q) { this.question = q; }
    public void setOptionA(String s) { this.optionA = s; }
    public void setOptionB(String s) { this.optionB = s; }
    public void setOptionC(String s) { this.optionC = s; }
    public void setOptionD(String s) { this.optionD = s; }
    public void setCorrectIndex(int i) { this.correctIndex = i; }
}
