package com.zafar.quizardry;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface FlashcardDao {
    @Insert
    void insert(Flashcard flashcard);

    @Insert
    void insertAll(List<Flashcard> flashcards);  // New method for bulk insert

    @Update
    void update(Flashcard flashcard);

    @Delete
    void delete(Flashcard flashcard);

    @Query("SELECT * FROM flashcards ORDER BY id DESC")
    List<Flashcard> getAllFlashcards();

    @Query("SELECT * FROM flashcards WHERE id = :id")
    Flashcard getFlashcardById(int id);
}