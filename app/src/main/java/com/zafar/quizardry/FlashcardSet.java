package com.zafar.quizardry;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "flashcard_sets")
public class FlashcardSet {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String name; // e.g., "World War 1 — Flashcards (2025-09-15 10:05)"

    public FlashcardSet(String name) {
        this.name = name;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
}
