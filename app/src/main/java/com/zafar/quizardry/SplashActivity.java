package com.zafar.quizardry;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.gms.ads.MobileAds;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply saved theme before any view inflation for a seamless splash
        applySavedTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        MobileAds.initialize(this, initializationStatus -> {});

        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, HomeActivity.class));
            finish();
        }, 1500);
    }

    private void applySavedTheme() {
        final String PREFS = "quizardry_prefs";
        final String KEY_THEME = "theme";
        int saved = getSharedPreferences(PREFS, MODE_PRIVATE).getInt(KEY_THEME, 0);
        int mode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        if (saved == 1) mode = AppCompatDelegate.MODE_NIGHT_NO;
        else if (saved == 2) mode = AppCompatDelegate.MODE_NIGHT_YES;
        AppCompatDelegate.setDefaultNightMode(mode);
    }
}
