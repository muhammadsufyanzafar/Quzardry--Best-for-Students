package com.zafar.quizardry;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface QuizSetDao {
    @Insert
    long insert(QuizSet set);

    @Query("SELECT * FROM quiz_sets ORDER BY id DESC")
    List<QuizSet> getAll();

    @Query("SELECT COUNT(*) FROM quiz_sets")
    int count();
}
