package com.zafar.quizardry;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class AddQuizQuestionActivity extends AppCompatActivity {
    private AppDatabase db;
    private boolean isEditing = false;
    private int editId = -1;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_add_quiz_question);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        db = AppDatabase.getInstance(this);

        EditText etQ = findViewById(R.id.etQ), etA = findViewById(R.id.etA), etB = findViewById(R.id.etB),
                etC = findViewById(R.id.etC), etD = findViewById(R.id.etD);
        RadioGroup rg = findViewById(R.id.rgCorrect);
        RadioButton rbCA = findViewById(R.id.rbCA), rbCB = findViewById(R.id.rbCB),
                rbCC = findViewById(R.id.rbCC), rbCD = findViewById(R.id.rbCD);
        Button btn = findViewById(R.id.btnSaveQ);

        if (getIntent().hasExtra("quiz_id")) {
            isEditing = true;
            editId = getIntent().getIntExtra("quiz_id", -1);
            new Thread(() -> {
                QuizQuestion q = db.quizDao().getById(editId);
                runOnUiThread(() -> {
                    if (q != null) {
                        etQ.setText(q.getQuestion());
                        etA.setText(q.getOptionA());
                        etB.setText(q.getOptionB());
                        etC.setText(q.getOptionC());
                        etD.setText(q.getOptionD());
                        (new RadioButton[] {rbCA, rbCB, rbCC, rbCD}[q.getCorrectIndex()]).setChecked(true);
                        setTitle("Edit Question");
                    }
                });
            }).start();
        } else setTitle("Add Question");

        btn.setOnClickListener(v -> {
            String qs = etQ.getText().toString().trim();
            String a = etA.getText().toString().trim();
            String b2 = etB.getText().toString().trim();
            String c = etC.getText().toString().trim();
            String d = etD.getText().toString().trim();
            int checked = rg.getCheckedRadioButtonId();
            int ci = checked == rbCA.getId() ? 0 : checked == rbCB.getId() ? 1 : checked == rbCC.getId() ? 2 : checked == rbCD.getId() ? 3 : -1;

            if (qs.isEmpty() || a.isEmpty() || b2.isEmpty() || c.isEmpty() || d.isEmpty() || ci == -1) {
                Toast.makeText(this, "Please fill all fields and select correct answer", Toast.LENGTH_SHORT).show();
                return;
            }

            new Thread(() -> {
                if (isEditing) {
                    QuizQuestion q = db.quizDao().getById(editId);
                    if (q != null) {
                        q.setQuestion(qs); q.setOptionA(a); q.setOptionB(b2); q.setOptionC(c); q.setOptionD(d); q.setCorrectIndex(ci);
                        db.quizDao().update(q);
                    }
                } else {
                    db.quizDao().insert(new QuizQuestion(qs, a, b2, c, d, ci));
                }
                runOnUiThread(() -> { Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show(); finish(); });
            }).start();
        });
    }

    @Override
    public boolean onSupportNavigateUp() { onBackPressed(); return true; }
}
