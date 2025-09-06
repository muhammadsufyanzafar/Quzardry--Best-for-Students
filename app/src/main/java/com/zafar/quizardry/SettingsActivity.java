package com.zafar.quizardry;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class SettingsActivity extends AppCompatActivity {
    private static final String PREFS = "quizardry_prefs";
    private static final String KEY_THEME = "theme";
    private static final String KEY_NOTIF = "notifications";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Switch sw = findViewById(R.id.switchNotifications);
        RadioGroup rg = findViewById(R.id.rgTheme);
        RadioButton rbSystem = findViewById(R.id.rbSystem);
        RadioButton rbLight = findViewById(R.id.rbLight);
        RadioButton rbDark = findViewById(R.id.rbDark);
        TextView tvVersion = findViewById(R.id.tvVersion);
        TextView tvContact = findViewById(R.id.tvContact);
        TextView tvPrivacy = findViewById(R.id.tvPrivacy);
        TextView tvTerms = findViewById(R.id.tvTerms);
        AdView ad = findViewById(R.id.adViewSettings);
        ad.loadAd(new AdRequest.Builder().build());

        // Version
        TextView versionText = findViewById(R.id.tvVersion);
        versionText.setText(getVersionNameSafe());

        android.content.SharedPreferences sp = getSharedPreferences(PREFS, MODE_PRIVATE);
        sw.setChecked(sp.getBoolean(KEY_NOTIF, false));

        int theme = sp.getInt(KEY_THEME, 0);
        if (theme == 0) rbSystem.setChecked(true);
        else if (theme == 1) rbLight.setChecked(true);
        else rbDark.setChecked(true);

        sw.setOnCheckedChangeListener((b, checked) -> {
            sp.edit().putBoolean(KEY_NOTIF, checked).apply();
            // Hook to scheduling logic later if desired
        });

        rg.setOnCheckedChangeListener((group, id) -> {
            int mode;
            int save;
            if (id == rbLight.getId()) { mode = AppCompatDelegate.MODE_NIGHT_NO; save = 1; }
            else if (id == rbDark.getId()) { mode = AppCompatDelegate.MODE_NIGHT_YES; save = 2; }
            else { mode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM; save = 0; }
            sp.edit().putInt(KEY_THEME, save).apply();
            AppCompatDelegate.setDefaultNightMode(mode);
            recreate();
        });

        tvContact.setOnClickListener(v -> open("https://onlineshoppingdealofficial.blogspot.com/p/ichatais-terms-conditions.html"));
        tvPrivacy.setOnClickListener(v -> open("https://onlineshoppingdealofficial.blogspot.com/p/ichatais-privacy-policy.html"));
        tvTerms.setOnClickListener(v -> open("https://muhammadsufyanzafar.github.io/portfolio/#contact"));
    }

    private void open(String url) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }

    private String getVersionNameSafe() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (Exception e) {
            return "—";
        }
    }

    @Override
    public boolean onSupportNavigateUp() { onBackPressed(); return true; }
}
