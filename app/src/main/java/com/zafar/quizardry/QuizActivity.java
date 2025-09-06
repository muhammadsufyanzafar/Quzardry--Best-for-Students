package com.zafar.quizardry;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.ads.AdRequest;
import java.util.List;

public class QuizActivity extends AppCompatActivity {
    private AppDatabase db;
    private List<QuizQuestion> questions;
    private int index = 0;
    private int[] answers; // -1 means unanswered
    private TextView tvQ;
    private RadioGroup rg;
    private RadioButton rbA, rbB, rbC, rbD;

    // Interstitial setup
    private com.google.android.gms.ads.interstitial.InterstitialAd interstitialAd;
    private void loadInterstitial() {
        AdRequest req = new AdRequest.Builder().build();
        com.google.android.gms.ads.interstitial.InterstitialAd.load(this,
                getString(R.string.admob_interstitial_quiz_end),
                req, new com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback() {
                    @Override public void onAdLoaded(@NonNull com.google.android.gms.ads.interstitial.InterstitialAd ad) { interstitialAd = ad; }
                });
    }
    private void maybeShowInterstitialAfterQuiz() {
        if (interstitialAd != null) { interstitialAd.show(this); interstitialAd = null; }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        db = AppDatabase.getInstance(this);

        tvQ = findViewById(R.id.tvQuizQuestion);
        rg = findViewById(R.id.rgOptions);
        rbA = findViewById(R.id.rbA); rbB = findViewById(R.id.rbB);
        rbC = findViewById(R.id.rbC); rbD = findViewById(R.id.rbD);

        Button prev = findViewById(R.id.btnPrevQ);
        Button next = findViewById(R.id.btnNextQ);
        Button submit = findViewById(R.id.btnSubmitQuiz);
        Button add = findViewById(R.id.btnAddQuiz);

        new Thread(() -> {
            questions = db.quizDao().getAll();
            answers = new int[questions.size()];
            for (int i = 0; i < answers.length; i++) answers[i] = -1;
            runOnUiThread(this::showQuestion);
        }).start();

        prev.setOnClickListener(v -> { saveSelection(); if (index > 0) { index--; showQuestion(); }});
        next.setOnClickListener(v -> { saveSelection(); if (index < questions.size()-1) { index++; showQuestion(); }});
        submit.setOnClickListener(v -> { saveSelection(); showScore(); });
        add.setOnClickListener(v -> startActivity(new Intent(this, AddQuizQuestionActivity.class)));

        loadInterstitial();
    }

    private void showQuestion() {
        if (questions == null || questions.isEmpty()) { tvQ.setText("No questions. Add some!"); return; }
        QuizQuestion q = questions.get(index);
        tvQ.setText(q.getQuestion());
        rbA.setText(q.getOptionA()); rbB.setText(q.getOptionB());
        rbC.setText(q.getOptionC()); rbD.setText(q.getOptionD());
        rg.clearCheck();
        if (answers[index] >= 0) {
            ((RadioButton) rg.getChildAt(answers[index])).setChecked(true);
        }
    }

    private void saveSelection() {
        int checkedId = rg.getCheckedRadioButtonId();
        int sel = -1;
        if (checkedId == rbA.getId()) sel = 0;
        else if (checkedId == rbB.getId()) sel = 1;
        else if (checkedId == rbC.getId()) sel = 2;
        else if (checkedId == rbD.getId()) sel = 3;
        answers[index] = sel;
    }

    private void showScore() {
        int score = 0;
        for (int i = 0; i < questions.size(); i++) {
            if (answers[i] == questions.get(i).getCorrectIndex()) score++;
        }
        new AlertDialog.Builder(this)
                .setTitle("Great job! 🎉")
                .setMessage("You scored " + score + " out of " + questions.size())
                .setPositiveButton("OK", (d,w) -> {
                    maybeShowInterstitialAfterQuiz();
                    d.dismiss();
                    finish();
                }).show();
    }

    @Override
    public boolean onSupportNavigateUp() { onBackPressed(); return true; }
}
