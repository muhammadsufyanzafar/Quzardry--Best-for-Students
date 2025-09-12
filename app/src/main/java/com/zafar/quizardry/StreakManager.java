package com.zafar.quizardry;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class StreakManager {
    private static final String PREFS = "quizardry_prefs";
    private static final String KEY_STREAK = "streak_days";
    private static final String KEY_LAST_DATE = "streak_last_date";

    public static int getStreak(Context ctx) {
        return ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(KEY_STREAK, 0);
    }

    public static void touchToday(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        int streak = sp.getInt(KEY_STREAK, 0);
        String last = sp.getString(KEY_LAST_DATE, "");
        String today = today();

        if (today.equals(last)) return;

        if (isYesterday(last)) {
            streak += 1;
        } else {
            streak = 1;
        }
        sp.edit()
                .putInt(KEY_STREAK, streak)
                .putString(KEY_LAST_DATE, today)
                .apply();
    }

    private static String today() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Calendar.getInstance().getTime());
    }

    private static boolean isYesterday(String ymd) {
        if (ymd == null || ymd.isEmpty()) return false;
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -1);
        String yesterday = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.getTime());
        return yesterday.equals(ymd);
    }
}
