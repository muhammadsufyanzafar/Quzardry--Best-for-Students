package com.zafar.quizardry;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "FlashcardDebug";
    private TextView tvQuestion, tvAnswer;
    private Button btnShowAnswer, btnNext, btnPrevious, btnAddCard;
    private LinearLayout cardFront, cardBack;
    private List<Flashcard> flashcards;
    private int currentPosition = 0;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate started");

        try {
            setContentView(R.layout.activity_main);
            Log.d(TAG, "ContentView set successfully");

            // Initialize database with try-catch
            try {
                db = AppDatabase.getInstance(this);
                Log.d(TAG, "Database initialized");
            } catch (Exception e) {
                Log.e(TAG, "Database initialization failed", e);
                Toast.makeText(this, "Database error", Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            // Initialize views with null checks
            initializeViews();
            Log.d(TAG, "Views initialized");

            // Set click listeners
            setupClickListeners();
            Log.d(TAG, "Click listeners set");

            // Load flashcards
            loadFlashcards();
            Log.d(TAG, "Flashcards loading initiated");

        } catch (Exception e) {
            Log.e(TAG, "Critical error in onCreate", e);
            Toast.makeText(this, "App initialization failed", Toast.LENGTH_LONG).show();
            finish();
        }
    }


    private void initializeViews() {
        tvQuestion = findViewById(R.id.tvQuestion);
        tvAnswer = findViewById(R.id.tvAnswer);
        btnShowAnswer = findViewById(R.id.btnShowAnswer);
        btnNext = findViewById(R.id.btnNext);
        btnPrevious = findViewById(R.id.btnPrevious);
        btnAddCard = findViewById(R.id.btnAddCard);
        cardFront = findViewById(R.id.card_front);
        cardBack = findViewById(R.id.card_back);

        // Verify all critical views
        if (tvQuestion == null) Log.e(TAG, "tvQuestion is null");
        if (tvAnswer == null) Log.e(TAG, "tvAnswer is null");
        if (btnShowAnswer == null) Log.e(TAG, "btnShowAnswer is null");
        if (cardFront == null) Log.e(TAG, "cardFront is null");
        if (cardBack == null) Log.e(TAG, "cardBack is null");
    }

    private void setupClickListeners() {
        // Use safe click listeners
        if (btnShowAnswer != null) {
            btnShowAnswer.setOnClickListener(v -> {
                Log.d(TAG, "Show Answer clicked");
                toggleCard();
            });
        }

        View container = findViewById(R.id.flashcard_container);
        if (container != null) {
            container.setOnClickListener(v -> {
                Log.d(TAG, "Card container clicked");
                toggleCard();
            });
        }

        if (btnNext != null) {
            btnNext.setOnClickListener(v -> {
                Log.d(TAG, "Next clicked");
                showNextCard();
            });
        }

        if (btnPrevious != null) {
            btnPrevious.setOnClickListener(v -> {
                Log.d(TAG, "Previous clicked");
                showPreviousCard();
            });
        }

        if (btnAddCard != null) {
            btnAddCard.setOnClickListener(v -> {
                Log.d(TAG, "Add Card clicked");
                startActivity(new Intent(MainActivity.this, AddFlashcardActivity.class));
            });
        }
    }

    private void flipCard(boolean showBack) {
        int duration = getResources().getInteger(R.integer.card_flip_time_full);
        float scale = getResources().getDisplayMetrics().density;
        cardFront.setCameraDistance(8000 * scale);
        cardBack.setCameraDistance(8000 * scale);

        View visible = showBack ? cardBack : cardFront;
        View invisible = showBack ? cardFront : cardBack;

        invisible.setVisibility(View.VISIBLE);
        invisible.setRotationY(-180f);

        invisible.animate().rotationY(0f).setDuration(duration);
        visible.animate().rotationY(180f).setDuration(duration)
                .withEndAction(() -> {
                    visible.setVisibility(View.GONE);
                    visible.setRotationY(0f);
                });
    }

    private void toggleCard() {
        if (cardFront.getVisibility() == View.VISIBLE) {
            if (btnShowAnswer != null) btnShowAnswer.setText("Show Question");
            flipCard(true);
        } else {
            if (btnShowAnswer != null) btnShowAnswer.setText("Show Answer");
            flipCard(false);
        }
    }


    private void loadFlashcards() {
        new Thread(() -> {
            try {
                List<Flashcard> loadedCards = db.flashcardDao().getAllFlashcards();
                runOnUiThread(() -> {
                    flashcards = loadedCards;
                    if (flashcards == null || flashcards.isEmpty()) {
                        Log.d(TAG, "No flashcards found");
                        if (tvQuestion != null) {
                            tvQuestion.setText("No flashcards available. Add some!");
                        }
                        if (btnShowAnswer != null) {
                            btnShowAnswer.setEnabled(false);
                        }
                    } else {
                        Log.d(TAG, "Loaded " + flashcards.size() + " flashcards");
                        currentPosition = 0;
                        displayCurrentCard();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error loading flashcards", e);
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Error loading cards", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void displayCurrentCard() {
        try {
            if (flashcards == null || flashcards.isEmpty()) {
                Log.d(TAG, "No cards to display");
                return;
            }

            Flashcard current = flashcards.get(currentPosition);
            if (tvQuestion != null) tvQuestion.setText(current.getQuestion());
            if (tvAnswer != null) tvAnswer.setText(current.getAnswer());

            // Reset to front view
            if (cardFront != null) cardFront.setVisibility(View.VISIBLE);
            if (cardBack != null) cardBack.setVisibility(View.GONE);
            if (btnShowAnswer != null) {
                btnShowAnswer.setEnabled(true);
                btnShowAnswer.setText("Show Answer");
            }

            // Update navigation buttons
            if (btnPrevious != null) btnPrevious.setEnabled(currentPosition > 0);
            if (btnNext != null) btnNext.setEnabled(currentPosition < flashcards.size() - 1);

        } catch (Exception e) {
            Log.e(TAG, "Error displaying card", e);
        }
    }

    private void showNextCard() {
        if (flashcards != null && currentPosition < flashcards.size() - 1) {
            currentPosition++;
            displayCurrentCard();
        } else if (flashcards != null && !flashcards.isEmpty()) {
            Toast.makeText(this, "Nice! You reached the last card 🎉", Toast.LENGTH_SHORT).show();
        }
    }


    private void showPreviousCard() {
        try {
            if (flashcards != null && currentPosition > 0) {
                currentPosition--;
                displayCurrentCard();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error showing previous card", e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (flashcards == null || flashcards.isEmpty()) {
            return false;
        }

        Flashcard current = flashcards.get(currentPosition);
        int id = item.getItemId();

        if (id == R.id.action_edit) {
            // Edit flashcard
            Intent intent = new Intent(this, AddFlashcardActivity.class);
            intent.putExtra("flashcard_id", current.getId());
            intent.putExtra("question", current.getQuestion());
            intent.putExtra("answer", current.getAnswer());
            startActivity(intent);
            return true;
        } else if (id == R.id.action_delete) {
            // Delete flashcard
            new Thread(() -> {
                db.flashcardDao().delete(current);
                runOnUiThread(() -> {
                    loadFlashcards(); // Refresh the list
                    Toast.makeText(this, "Flashcard deleted", Toast.LENGTH_SHORT).show();
                });
            }).start();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called");
        loadFlashcards();
    }

}