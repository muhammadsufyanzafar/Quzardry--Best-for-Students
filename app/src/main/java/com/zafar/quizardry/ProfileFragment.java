package com.zafar.quizardry;

import android.os.Bundle;
import android.view.*;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import java.util.List;

public class ProfileFragment extends Fragment {

    private AppDatabase db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_profile_fragment, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        bindData();
    }

    private void bindData() {
        if (getView() == null) return;

        TextView tvStudyTime = getView().findViewById(R.id.tvTotalStudy);
        TextView tvSets = getView().findViewById(R.id.tvSets);
        TextView tvTotalCards = getView().findViewById(R.id.tvTotalCards);
        TextView tvCardsMastered = getView().findViewById(R.id.tvCardsMastered);
        TextView tvQuizzesCompleted = getView().findViewById(R.id.tvQuizzesCompleted);
        TextView tvAvgScore = getView().findViewById(R.id.tvAvgScore);
        TextView tvStreak = getView().findViewById(R.id.tvCurrentStreak);
        TextView tvBadges = getView().findViewById(R.id.tvBadges);

        db = AppDatabase.getInstance(requireContext());
        new Thread(() -> {
            List<Flashcard> cards = db.flashcardDao().getAllFlashcards();
            List<QuizQuestion> qs = db.quizDao().getAll();
            requireActivity().runOnUiThread(() -> {
                tvStudyTime.setText("0h 8m"); // Placeholder
                tvSets.setText("1");          // Placeholder for sets count
                tvTotalCards.setText(String.valueOf(cards.size()));
                tvCardsMastered.setText("0");
                tvQuizzesCompleted.setText("0");
                tvAvgScore.setText("0%");
                tvStreak.setText(StreakManager.getStreak(requireContext()) + " day");
                tvBadges.setText("0/16");
            });
        }).start();
    }
}
