package com.zafar.quizardry;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;
import android.content.Context;

import java.util.Arrays;
import java.util.concurrent.Executors;

@Database(
        entities = { Flashcard.class, QuizQuestion.class, FlashcardSet.class, QuizSet.class },
        version = 3 // bumped version
)
public abstract class AppDatabase extends RoomDatabase {
    public abstract FlashcardDao flashcardDao();
    public abstract QuizDao quizDao();
    public abstract FlashcardSetDao flashcardSetDao();
    public abstract QuizSetDao quizSetDao();

    private static AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "flashcard-db")
                    .addCallback(new Callback() {
                        @Override
                        public void onCreate(@NonNull SupportSQLiteDatabase db) {
                            super.onCreate(db);
                            // Optional: seed sets/items if you want
                            Executors.newSingleThreadExecutor().execute(() -> {
                                // No prepopulate to sets by default
                            });
                        }
                    })
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return INSTANCE;
    }
}
