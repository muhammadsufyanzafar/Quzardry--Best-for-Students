package com.zafar.quizardry;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class HomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        LinearLayout btnFlashcards = findViewById(R.id.btnFlashcards);
        LinearLayout btnQuiz = findViewById(R.id.btnQuiz);
        ImageButton btnSettings = findViewById(R.id.btnSettings);
        AdView adView = findViewById(R.id.adView);
        adView.loadAd(new AdRequest.Builder().build());

        btnFlashcards.setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));
        btnQuiz.setOnClickListener(v -> startActivity(new Intent(this, QuizActivity.class)));
        btnSettings.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
    }
}
