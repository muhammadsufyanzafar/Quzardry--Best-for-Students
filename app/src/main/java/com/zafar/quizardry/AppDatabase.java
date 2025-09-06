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

@Database(entities = {Flashcard.class, QuizQuestion.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {
    public abstract FlashcardDao flashcardDao();
    public abstract QuizDao quizDao();

    private static AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "flashcard-db")
                    .addCallback(new Callback() {
                        @Override
                        public void onCreate(@NonNull SupportSQLiteDatabase db) {
                            super.onCreate(db);
                            Executors.newSingleThreadExecutor().execute(() -> {
                                prepopulateFlashcards(INSTANCE.flashcardDao());
                                prepopulateQuiz(INSTANCE.quizDao());
                            });
                        }
                    })
                    .fallbackToDestructiveMigration() // for dev; replace with Migration later
                    .build();
        }
        return INSTANCE;
    }

    private static void prepopulateFlashcards(FlashcardDao dao) {
        if (dao.getAllFlashcards().isEmpty()) {
            dao.insertAll(Arrays.asList(
                    new Flashcard("What is the capital of France?", "Paris"),
                    new Flashcard("Which is the longest river in the world?", "The Nile River"),
                    new Flashcard("Who painted the Mona Lisa?", "Leonardo da Vinci"),
                    new Flashcard("First person on the moon?", "Neil Armstrong"),
                    new Flashcard("Largest ocean?", "Pacific Ocean"),
                    new Flashcard("Hardest natural substance?", "Diamond"),
                    new Flashcard("Plant process using sunlight?", "Photosynthesis"),
                    new Flashcard("First artificial satellite?", "Sputnik 1"),
                    new Flashcard("Author of Harry Potter?", "J.K. Rowling"),
                    new Flashcard("Who built the Taj Mahal?", "Shah Jahan")
            ));
        }
    }

    private static void prepopulateQuiz(QuizDao dao) {
        if (dao.getAll().isEmpty()) {
            dao.insertAll(Arrays.asList(
                    new QuizQuestion("Capital of Germany?",
                            "Berlin","Munich","Hamburg","Frankfurt", 0),
                    new QuizQuestion("Largest planet?",
                            "Earth","Jupiter","Saturn","Neptune", 1),
                    new QuizQuestion("2 + 2 = ?",
                            "3","4","5","22", 1),
                    new QuizQuestion("Mona Lisa painter?",
                            "Michelangelo","Raphael","Leonardo da Vinci","Donatello", 2),
                    new QuizQuestion("Blue whale is a ...",
                            "Fish","Reptile","Bird","Mammal", 3),
                    new QuizQuestion("Speed of light approx?",
                            "3,000 km/s","30,000 km/s","300,000 km/s","3,000,000 km/s", 2),
                    new QuizQuestion("H2O is",
                            "Hydrogen","Oxygen","Water","Helium", 2),
                    new QuizQuestion("Photosynthesis uses",
                            "Sunlight","Moonlight","Starlight","No light", 0),
                    new QuizQuestion("Taj Mahal city?",
                            "Agra","Delhi","Jaipur","Mumbai", 0),
                    new QuizQuestion("Largest desert?",
                            "Sahara","Gobi","Arctic","Atacama", 0)
            ));
        }
    }
}
