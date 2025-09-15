package com.zafar.quizardry;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
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
    private int correctIndex;

    @ColumnInfo(name = "set_id")
    private int folderId;

    // Room’s constructor
    public QuizQuestion() { }

    // Convenience constructor
    @Ignore
    public QuizQuestion(String question,
                        String a, String b, String c, String d,
                        int correctIndex) {
        this.question = question;
        this.optionA = a;
        this.optionB = b;
        this.optionC = c;
        this.optionD = d;
        this.correctIndex = correctIndex;
    }

    // Getters
    public int getId() { return id; }
    public String getQuestion() { return question; }
    public String getOptionA() { return optionA; }
    public String getOptionB() { return optionB; }
    public String getOptionC() { return optionC; }
    public String getOptionD() { return optionD; }
    public int getCorrectIndex() { return correctIndex; }
    public int getFolderId() { return folderId; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setQuestion(String q) { this.question = q; }
    public void setOptionA(String a) { this.optionA = a; }
    public void setOptionB(String b) { this.optionB = b; }
    public void setOptionC(String c) { this.optionC = c; }
    public void setOptionD(String d) { this.optionD = d; }
    public void setCorrectIndex(int idx) { this.correctIndex = idx; }
    public void setFolderId(int folderId) { this.folderId = folderId; }
}
