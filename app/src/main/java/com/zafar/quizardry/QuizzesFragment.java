package com.zafar.quizardry;

import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.List;

public class QuizzesFragment extends Fragment {

    private AppDatabase db;
    private TextView tvQuizCount, tvBestScore, tvAttempts;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_quizzes_fragment, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        bindData();
    }

    private void bindData() {
        if (getView() == null) return;
        tvQuizCount = getView().findViewById(R.id.tvQuizCount);
        tvBestScore = getView().findViewById(R.id.tvBestScore);
        tvAttempts = getView().findViewById(R.id.tvAttempts);

        db = AppDatabase.getInstance(requireContext());
        new Thread(() -> {
            List<QuizQuestion> qs = db.quizDao().getAll();
            requireActivity().runOnUiThread(() -> {
                tvQuizCount.setText(qs.isEmpty() ? "No quizzes" : "1 quiz • " + qs.size() + " questions");
                tvBestScore.setText("Best: 0%");
                tvAttempts.setText("Attempts: 0");
            });
        }).start();

        LinearLayout row = getView().findViewById(R.id.rowAllQuizzes);
        row.setOnClickListener(v -> startActivity(new Intent(requireContext(), QuizActivity.class)));

        getView().findViewById(R.id.btnCreateQuiz).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), AddQuizQuestionActivity.class)));
    }
}
