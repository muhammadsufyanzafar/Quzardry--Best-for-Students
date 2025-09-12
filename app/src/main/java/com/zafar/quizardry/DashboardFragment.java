package com.zafar.quizardry;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.List;

public class DashboardFragment extends Fragment {

    private AppDatabase db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_dashboard_fragment, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getContext() != null) StreakManager.touchToday(getContext());
        bindData();
    }

    private void bindData() {
        if (getView() == null) return;
        TextView tvStreak = getView().findViewById(R.id.tvStreak);
        TextView tvTimeToday = getView().findViewById(R.id.tvTimeToday);
        TextView tvProgress = getView().findViewById(R.id.tvProgress);
        TextView tvFlashcards = getView().findViewById(R.id.tvFlashcards);
        TextView tvQuizzes = getView().findViewById(R.id.tvQuizzes);
        TextView tvQuote = getView().findViewById(R.id.tvQuote);

        tvStreak.setText(StreakManager.getStreak(requireContext()) + " Day Streak");
        tvQuote.setText(Quotes.randomQuote());

        db = AppDatabase.getInstance(requireContext());
        new Thread(() -> {
            List<Flashcard> cards = db.flashcardDao().getAllFlashcards();
            List<QuizQuestion> qs = db.quizDao().getAll();

            requireActivity().runOnUiThread(() -> {
                // Simple placeholders for now
                tvTimeToday.setText("0h 7m"); // Hook up to real tracking later
                tvProgress.setText("0%");
                tvFlashcards.setText(String.valueOf(cards.size()));
                tvQuizzes.setText(String.valueOf(qs.size()));
            });
        }).start();

        // Quick actions
        getView().findViewById(R.id.btnGoCards).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), MainActivity.class)));
        getView().findViewById(R.id.btnGoQuizzes).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), QuizActivity.class)));
    }
}
