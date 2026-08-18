package io.github.abdurazaaqmohammed.gamesbox.game;

import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import io.github.abdurazaaqmohammed.gamesbox.ui.UI;

public class HigherOrLowerActivity extends BaseGameActivity {

    private static final int ACCENT = UI.SKY;
    private static final int ACCENT_D = UI.SKY_D;

    private LinearLayout content;
    private UI.Toggle rangeToggle;
    private int range;
    private int current;
    private int streak;
    private int best;

    private static final String[] RANGE_LABELS = {"1-10", "1-50", "1-100"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        content = UI.screen(this, "Higher or Lower",
                "Guess the next number", ACCENT);
        showSetup();
    }

    private void showSetup() {
        content.removeAllViews();

        TextView rules = UI.text(this,
                "A number appears. Guess whether the NEXT number will be higher or lower, then pass the phone fast. Chain correct guesses to build a streak!",
                UI.INK_SOFT, 14, false);
        rules.setPadding(0, 0, 0, UI.dp(this, 16));
        content.addView(rules);

        rangeToggle = new UI.Toggle(this, "Number range", RANGE_LABELS, ACCENT, 1);
        content.addView(rangeToggle.row);

        content.addView(UI.space(this, 16));

        TextView start = UI.button(this, "Start Game", ACCENT, ACCENT_D, 17, 16);
        content.addView(start, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        start.setOnClickListener(v -> {
            range = rangeToggle.get() == 0 ? 10 : (rangeToggle.get() == 1 ? 50 : 100);
            streak = 0;
            best = 0;
            newValue();
        });
    }

    private void newValue() {
        content.removeAllViews();
        current = 1 + UI.RND.nextInt(range);

        TextView label = UI.text(this, "Current number", UI.INK_SOFT, 13, true);
        label.setGravity(Gravity.CENTER);
        label.setPadding(0, UI.dp(this, 10), 0, UI.dp(this, 8));
        content.addView(label);

        TextView n = UI.text(this, String.valueOf(current), UI.INK, 88, true);
        n.setGravity(Gravity.CENTER);
        n.setPadding(0, 0, 0, UI.dp(this, 6));
        content.addView(n);

        TextView q = UI.text(this, "Is the next number higher or lower?",
                UI.INK, 18, true);
        q.setGravity(Gravity.CENTER);
        q.setPadding(0, 0, 0, UI.dp(this, 16));
        content.addView(q);

        LinearLayout row = UI.row(this);
        row.setGravity(Gravity.CENTER_HORIZONTAL);
        TextView higher = UI.button(this, "Higher ▲", UI.MINT, UI.MINT_D, 16, 15);
        TextView lower = UI.button(this, "Lower ▼", UI.RED, UI.RED_D, 16, 15);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        lp.setMargins(UI.dp(this, 3), 0, UI.dp(this, 3), 0);
        row.addView(higher, lp);
        row.addView(lower, lp);
        higher.setOnClickListener(v -> guess(true));
        lower.setOnClickListener(v -> guess(false));
        content.addView(row);

        TextView streakT = UI.text(this, "Streak: " + streak + "   Best: " + best,
                UI.GOLD, 14, true);
        streakT.setGravity(Gravity.CENTER);
        streakT.setPadding(0, UI.dp(this, 12), 0, 0);
        content.addView(streakT);
    }

    private void guess(boolean higher) {
        int next;
        do {
            next = 1 + UI.RND.nextInt(range);
        } while (next == current);
        final int revealed = next;
        boolean correct = higher ? revealed > current : revealed < current;

        content.removeAllViews();

        TextView title = UI.text(this, correct ? "Yes!" : "No!",
                correct ? UI.MINT : UI.RED, 30, true);
        title.setGravity(Gravity.CENTER);
        content.addView(title);

        TextView reveal = UI.text(this, "It was " + revealed, UI.INK, 40, true);
        reveal.setGravity(Gravity.CENTER);
        reveal.setPadding(0, UI.dp(this, 6), 0, UI.dp(this, 14));
        content.addView(reveal);

        if (correct) {
            streak++;
            if (streak > best) best = streak;

            TextView s = UI.text(this, "Streak: " + streak + "   Best: " + best,
                    UI.GOLD, 16, true);
            s.setGravity(Gravity.CENTER);
            s.setPadding(0, 0, 0, UI.dp(this, 16));
            content.addView(s);

            TextView nxt = UI.button(this, "Next Number", ACCENT, ACCENT_D, 16, 15);
            content.addView(nxt, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            nxt.setOnClickListener(v -> {
                current = revealed;
                newValue();
            });
        } else {
            streak = 0;

            TextView s = UI.text(this, "Back to zero!   Best streak: " + best,
                    UI.GOLD, 16, true);
            s.setGravity(Gravity.CENTER);
            s.setPadding(0, 0, 0, UI.dp(this, 16));
            content.addView(s);

            TextView again = UI.button(this, "Play Again", ACCENT, ACCENT_D, 16, 15);
            content.addView(again, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            again.setOnClickListener(v -> {
                current = revealed;
                newValue();
            });
        }
    }
}
