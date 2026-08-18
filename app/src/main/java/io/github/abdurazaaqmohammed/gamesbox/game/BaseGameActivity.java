package io.github.abdurazaaqmohammed.gamesbox.game;

import android.app.Activity;
import android.widget.Toast;

public abstract class BaseGameActivity extends Activity {

    private static final long EXIT_WINDOW_MS = 2000;

    private long lastBackPress;

    @Override
    public void onBackPressed() {
        long now = System.currentTimeMillis();
        if (now - lastBackPress <= EXIT_WINDOW_MS) {
            super.onBackPressed();
            return;
        }
        lastBackPress = now;
        Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();
    }
}