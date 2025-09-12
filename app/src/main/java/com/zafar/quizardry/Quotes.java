package com.zafar.quizardry;

import java.util.Random;

public class Quotes {
    private static final String[] Q = new String[] {
            "Success is not final, failure is not fatal: It is the courage to continue that counts. — Winston Churchill",
            "Small progress is still progress. Keep going.",
            "Discipline is choosing what you want most over what you want now.",
            "You don’t have to be great to start, but you have to start to be great.",
            "Focus on consistency, and results will follow."
    };

    public static String randomQuote() {
        return Q[new Random().nextInt(Q.length)];
    }
}
