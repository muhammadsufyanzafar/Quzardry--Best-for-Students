package com.zafar.quizardry;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "flashcards")
public class Flashcard {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String question;
    private String answer;

    @ColumnInfo(name = "set_id")
    private int folderId;

    // Room’s constructor
    public Flashcard() { }

    // Convenience constructors (ignored by Room)
    @Ignore
    public Flashcard(String question, String answer) {
        this.question = question;
        this.answer = answer;
    }

    @Ignore
    public Flashcard(String question, String answer, int folderId) {
        this.question = question;
        this.answer = answer;
        this.folderId = folderId;
    }

    // Getters
    public int getId() { return id; }
    public String getQuestion() { return question; }
    public String getAnswer() { return answer; }
    public int getFolderId() { return folderId; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setQuestion(String question) { this.question = question; }
    public void setAnswer(String answer) { this.answer = answer; }
    public void setFolderId(int folderId) { this.folderId = folderId; }
}
