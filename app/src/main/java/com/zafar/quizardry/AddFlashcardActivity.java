package com.zafar.quizardry;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AddFlashcardActivity extends AppCompatActivity {
    private EditText etQuestion, etAnswer;
    private Button btnSave;
    private AppDatabase db;
    private boolean isEditing = false;
    private int existingFlashcardId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_flashcard);

        // Enable back button in action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        db = AppDatabase.getInstance(this);

        etQuestion = findViewById(R.id.etQuestion);
        etAnswer = findViewById(R.id.etAnswer);
        btnSave = findViewById(R.id.btnSave);

        // Check if we're editing an existing flashcard
        if (getIntent().hasExtra("flashcard_id")) {
            isEditing = true;
            existingFlashcardId = getIntent().getIntExtra("flashcard_id", -1);
            String question = getIntent().getStringExtra("question");
            String answer = getIntent().getStringExtra("answer");

            etQuestion.setText(question);
            etAnswer.setText(answer);
            setTitle("Edit Flashcard");
        } else {
            setTitle("Add New Flashcard");
        }

        btnSave.setOnClickListener(v -> saveFlashcard());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle back button click
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveFlashcard() {
        String question = etQuestion.getText().toString().trim();
        String answer = etAnswer.getText().toString().trim();

        if (question.isEmpty() || answer.isEmpty()) {
            Toast.makeText(this, "Please enter both question and answer", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            if (isEditing) {
                // Update existing flashcard
                Flashcard flashcard = db.flashcardDao().getFlashcardById(existingFlashcardId);
                if (flashcard != null) {
                    flashcard.setQuestion(question);
                    flashcard.setAnswer(answer);
                    db.flashcardDao().update(flashcard);
                    runOnUiThread(() -> {
                        Toast.makeText(AddFlashcardActivity.this, "Flashcard updated!", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }
            } else {
                // Insert new flashcard
                Flashcard flashcard = new Flashcard(question, answer);
                db.flashcardDao().insert(flashcard);
                runOnUiThread(() -> {
                    Toast.makeText(AddFlashcardActivity.this, "Flashcard saved!", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        }).start();
    }
}