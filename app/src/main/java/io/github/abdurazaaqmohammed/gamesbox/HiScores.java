package io.github.abdurazaaqmohammed.gamesbox;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

public final class HiScores {

    private static final String PREFS = "GamesBox_hi";

    public static int get(Context c, String key) {
        return c.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(key, 0);
    }

    public static void put(Context c, String key, int value) {
        SharedPreferences.Editor editor = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putInt(key, value);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            editor.apply();
        } else editor.commit();
    }

    public static int best(Context c, String key, int score) {
        int b = Math.max(get(c, key), score);
        SharedPreferences.Editor editor = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putInt(key, b);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            editor.apply();
        } else editor.commit();
        return b;
    }
}
