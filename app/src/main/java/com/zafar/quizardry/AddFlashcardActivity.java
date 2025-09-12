package com.zafar.quizardry;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class AddFlashcardActivity extends AppCompatActivity {

    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_flashcard);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Add Flashcard");

        db = AppDatabase.getInstance(this);

        EditText etQ = findViewById(R.id.etQuestion);
        EditText etA = findViewById(R.id.etAnswer);
        Button btnSave = findViewById(R.id.btnSave);

        // Pre-fill if editing
        if (getIntent().hasExtra("flashcard_id")) {
            setTitle("Edit Flashcard");
            etQ.setText(getIntent().getStringExtra("question"));
            etA.setText(getIntent().getStringExtra("answer"));
        }

        btnSave.setOnClickListener(v -> {
            String q = etQ.getText().toString().trim();
            String a = etA.getText().toString().trim();
            if (q.isEmpty() || a.isEmpty()) {
                Toast.makeText(this, "Please fill question and answer", Toast.LENGTH_SHORT).show();
                return;
            }

            new Thread(() -> {
                if (getIntent().hasExtra("flashcard_id")) {
                    int id = getIntent().getIntExtra("flashcard_id", -1);
                    Flashcard existing = db.flashcardDao().getFlashcardById(id);
                    if (existing != null) {
                        existing.setQuestion(q);
                        existing.setAnswer(a);
                        db.flashcardDao().update(existing);
                    }
                } else {
                    db.flashcardDao().insert(new Flashcard(q, a));
                }
                runOnUiThread(() -> {
                    Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }).start();
        });
    }

    @Override
    public boolean onSupportNavigateUp() { onBackPressed(); return true; }
}
