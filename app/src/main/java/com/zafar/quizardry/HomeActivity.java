package com.zafar.quizardry;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class HomeActivity extends AppCompatActivity {

    private LinearLayout btnFlashcards, btnQuiz;
    private AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Set title in the ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.app_name));
        }

        // Find views
        btnFlashcards = findViewById(R.id.btnFlashcards);
        btnQuiz = findViewById(R.id.btnQuiz);
        adView = findViewById(R.id.adView);

        // Load AdMob banner
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        // Click listeners
        btnFlashcards.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, MainActivity.class)));

        btnQuiz.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, QuizActivity.class)));
    }

    // Inflate the menu with the Settings button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu); // Use menu_home.xml for Home screen
        return true;
    }

    // Handle menu item clicks
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
