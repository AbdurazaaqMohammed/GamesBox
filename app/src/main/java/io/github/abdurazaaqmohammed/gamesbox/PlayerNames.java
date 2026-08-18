package io.github.abdurazaaqmohammed.gamesbox;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.text.TextUtils;

public final class PlayerNames {

    public static final int MAX = 12;

    private static final String PREFS = "GamesBox_names";
    private static final String PREFIX = "name_";

    private PlayerNames() {
    }

    private static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static String raw(Context c, int index) {
        return prefs(c).getString(PREFIX + index, "");
    }

    public static String get(Context c, int index) {
        String n = raw(c, index);
        if (TextUtils.isEmpty(n.trim())) return "Player " + (index + 1);
        return n;
    }

    public static void set(Context c, int index, String name) {
        SharedPreferences.Editor e = prefs(c).edit();
        e.putString(PREFIX + index, name == null ? "" : name.trim());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            e.apply();
        } else e.commit();
    }

    public static void clear(Context c) {
        SharedPreferences.Editor e = prefs(c).edit();
        for (int i = 0; i < MAX; i++) e.remove(PREFIX + i);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            e.apply();
        } else e.commit();
    }
}
