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

public class CardsFragment extends Fragment {

    private AppDatabase db;
    private TextView tvCount, tvMastery;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_cards_fragment, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        bindData();
    }

    private void bindData() {
        if (getView() == null) return;
        tvCount = getView().findViewById(R.id.tvCardCount);
        tvMastery = getView().findViewById(R.id.tvMastery);

        db = AppDatabase.getInstance(requireContext());
        new Thread(() -> {
            List<Flashcard> cards = db.flashcardDao().getAllFlashcards();
            requireActivity().runOnUiThread(() -> {
                tvCount.setText(cards.size() + " cards");
                tvMastery.setText("Mastered: 0%"); // Hook mastery model later
            });
        }).start();

        LinearLayout btnAll = getView().findViewById(R.id.rowAllCards);
        btnAll.setOnClickListener(v -> startActivity(new Intent(requireContext(), MainActivity.class)));

        getView().findViewById(R.id.btnImportCards).setOnClickListener(v -> {
            // TODO: implement import flow
        });
        getView().findViewById(R.id.btnAddCard).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), AddFlashcardActivity.class)));
    }
}
