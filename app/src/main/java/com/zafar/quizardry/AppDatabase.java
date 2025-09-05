package com.zafar.quizardry;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;
import android.content.Context;
import android.util.Log;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;

@Database(entities = {Flashcard.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract FlashcardDao flashcardDao();

    private static AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "flashcard-db")
                    .addCallback(new RoomDatabase.Callback() {
                        @Override
                        public void onCreate(@NonNull SupportSQLiteDatabase db) {
                            super.onCreate(db);
                            Executors.newSingleThreadExecutor().execute(() -> {
                                prepopulateDatabase(INSTANCE.flashcardDao());
                            });
                        }
                    })
                    .allowMainThreadQueries()
                    .build();
        }
        return INSTANCE;
    }

    private static void prepopulateDatabase(FlashcardDao dao) {
        try {
            List<Flashcard> existing = dao.getAllFlashcards();
            Log.d("DB_DEBUG", "Current card count: " + existing.size());

            if (existing.isEmpty()) {
                List<Flashcard> defaultFlashcards = Arrays.asList(
                        new Flashcard("What is the capital of France?", "Paris"),
                        new Flashcard("Which is the longest river in the world??", "The Nile River"),
                        new Flashcard("Who painted the Mona Lisa?", "Leonardo da Vinci"),
                        new Flashcard("Who was the first person to walk on the moon?", "Neil Armstrong")
//                        new Flashcard("What is the largest ocean on Earth?", "Pacific Ocean"),
//                        new Flashcard("What is the hardest natural substance on Earth?", "Diamond"),
//                        new Flashcard("What process do plants use to convert sunlight into energy?", "Photosynthesis"),
//                        new Flashcard("What is the name of the first artificial satellite?", "Sputnik 1"),
//                        new Flashcard("Who is the author of the Harry Potter series?", "J.K. Rowling"),
//                        new Flashcard("Who built the Taj Mahal?", "Shah Jahan")
                );

                dao.insertAll(defaultFlashcards);
                Log.d("DB_DEBUG", "Inserted " + defaultFlashcards.size() + " cards");

                // Verify insertion
                List<Flashcard> afterInsert = dao.getAllFlashcards();
                Log.d("DB_DEBUG", "New card count: " + afterInsert.size());
            }
        } catch (Exception e) {
            Log.e("DB_DEBUG", "Prepopulation failed", e);
        }
    }
}