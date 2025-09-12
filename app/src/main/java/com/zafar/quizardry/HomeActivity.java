package com.zafar.quizardry;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import com.zafar.quizardry.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class HomeActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_host);

        bottomNav = findViewById(R.id.bottomNavigation);

        //  Merged working navigation code
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId(); // Get the ID of the selected item

            if (itemId == R.id.nav_home) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, new DashboardFragment())
                        .commit();
                return true;
            } else if (itemId == R.id.nav_cards) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, new CardsFragment())
                        .commit();
                return true;
            } else if (itemId == R.id.nav_quizzes) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, new QuizzesFragment())
                        .commit();
                return true;
            } else if (itemId == R.id.nav_profile) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, new ProfileFragment())
                        .commit();
                return true;
            }
            return false; // Return false for unhandled item IDs
        });


        // Load default fragment on first launch
        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_home);
        }

        // Floating create button
        View fab = findViewById(R.id.fabCreate);
        if (fab != null) fab.setOnClickListener(v -> showCreateSheet());
    }

    private void showCreateSheet() {
        BottomSheetDialog sheet = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.sheet_create_new, null);
        sheet.setContentView(view);

        LinearLayout btnAI = view.findViewById(R.id.btnGenerateAI);
        LinearLayout btnImportCards = view.findViewById(R.id.btnImportCards);
        LinearLayout btnImportQuiz = view.findViewById(R.id.btnImportQuiz);
        LinearLayout btnQuizFromCards = view.findViewById(R.id.btnQuizFromCards);

        btnAI.setOnClickListener(v -> {
            sheet.dismiss();
            // TODO: Navigate to AI generation flow
        });
        btnImportCards.setOnClickListener(v -> {
            sheet.dismiss();
            // TODO: Implement flashcard import
        });
        btnImportQuiz.setOnClickListener(v -> {
            sheet.dismiss();
            // TODO: Implement quiz import
        });
        btnQuizFromCards.setOnClickListener(v -> {
            sheet.dismiss();
            // TODO: Generate quiz from cards
        });

        sheet.show();
    }
}
