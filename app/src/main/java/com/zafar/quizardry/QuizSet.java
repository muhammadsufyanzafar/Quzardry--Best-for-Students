package com.zafar.quizardry;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "quiz_sets")
public class QuizSet {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String name; // e.g., "World War 1 — Quiz (2025-09-15 10:05)"

    public QuizSet(String name) {
        this.name = name;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
}
