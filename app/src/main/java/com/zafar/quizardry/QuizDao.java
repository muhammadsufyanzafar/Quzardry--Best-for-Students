package com.zafar.quizardry;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface QuizDao {
    @Insert void insert(QuizQuestion q);
    @Insert void insertAll(List<QuizQuestion> qs);
    @Update void update(QuizQuestion q);
    @Delete void delete(QuizQuestion q);
    @Query("SELECT * FROM quiz_questions ORDER BY id DESC")
    List<QuizQuestion> getAll();
    @Query("SELECT * FROM quiz_questions WHERE id = :id")
    QuizQuestion getById(int id);
}
