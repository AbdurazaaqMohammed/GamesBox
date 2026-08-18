package io.github.abdurazaaqmohammed.gamesbox.game;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import io.github.abdurazaaqmohammed.gamesbox.PlayerNames;
import io.github.abdurazaaqmohammed.gamesbox.ui.UI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TabooActivity extends BaseGameActivity {

    private static final int ACCENT = UI.LIME;
    private static final int ACCENT_D = UI.LIME_D;

    private LinearLayout content;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private UI.Stepper playersStepper;
    private UI.Stepper secondsStepper;
    private int players;
    private int seconds;
    private int[] points;
    private int describer;
    private boolean running;
    private int remaining;
    private TextView timerText;
    private final List<Integer> order = new ArrayList<>();
    private int cursor;

    private String playerName(int i) {
        return PlayerNames.get(this, i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        content = UI.screen(this, "Taboo",
                "Describe without the banned words", ACCENT);
        for (int i = 0; i < Data.TABOO_WORDS.length; i++) order.add(i);
        Collections.shuffle(order, UI.RND);
        showSetup();
    }

    private void showSetup() {
        running = false;
        content.removeAllViews();

        TextView rules = UI.text(this,
                "One player describes the word without saying it or either banned word. Teammates shout guesses. Tap \"Guessed!\" for a point or \"Skip\" to move on. When the timer runs out, the next player describes.",
                UI.INK_SOFT, 14, false);
        rules.setPadding(0, 0, 0, UI.dp(this, 16));
        content.addView(rules);

        playersStepper = new UI.Stepper(this, "Players", 3, 10, 4, 1);
        content.addView(playersStepper.row);

        secondsStepper = new UI.Stepper(this, "Seconds per turn", 30, 90, 60, 10);
        content.addView(secondsStepper.row);

        content.addView(UI.space(this, 16));

        TextView start = UI.button(this, "Start Game", ACCENT, ACCENT_D, 17, 16);
        content.addView(start, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        start.setOnClickListener(v -> {
            players = playersStepper.get();
            seconds = secondsStepper.get();
            points = new int[players];
            describer = 0;
            newTurn();
        });
    }

    private void newTurn() {
        running = false;
        content.removeAllViews();

        TextView who = UI.text(this, playerName(describer) + " describes",
                UI.INK, 18, true);
        who.setGravity(Gravity.CENTER);
        who.setPadding(0, UI.dp(this, 4), 0, UI.dp(this, 12));
        content.addView(who);

        String[] entry = Data.TABOO_WORDS[order.get(cursor)];
        TextView wordCard = UI.card(this, entry[0], UI.INK, 26);
        content.addView(wordCard, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView dont = UI.text(this, "Don't say:", UI.RED, 13, true);
        dont.setGravity(Gravity.CENTER);
        dont.setPadding(0, UI.dp(this, 14), 0, UI.dp(this, 6));
        content.addView(dont);

        TextView banned = UI.text(this, entry[1] + "  •  " + entry[2], UI.RED, 16, true);
        banned.setGravity(Gravity.CENTER);
        banned.setPadding(0, 0, 0, UI.dp(this, 18));
        content.addView(banned);

        TextView go = UI.button(this, "Start the timer", ACCENT, ACCENT_D, 17, 16);
        content.addView(go, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        go.setOnClickListener(v -> startTiming());
    }

    private void startTiming() {
        running = true;
        remaining = seconds;
        handler.removeCallbacks(tick);
        handler.postDelayed(tick, 1000);
        renderTiming();
    }

    private void renderTiming() {
        content.removeAllViews();

        String[] entry = Data.TABOO_WORDS[order.get(cursor)];

        TextView who = UI.text(this, playerName(describer) + " describes",
                UI.INK_SOFT, 13, true);
        who.setGravity(Gravity.CENTER);
        who.setPadding(0, 0, 0, UI.dp(this, 8));
        content.addView(who);

        TextView wordCard = UI.card(this, entry[0], UI.INK, 24);
        content.addView(wordCard, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView banned = UI.text(this, "Don't say: " + entry[1] + " or " + entry[2],
                UI.RED, 14, true);
        banned.setGravity(Gravity.CENTER);
        banned.setPadding(0, UI.dp(this, 10), 0, UI.dp(this, 6));
        content.addView(banned);

        timerText = UI.text(this, String.valueOf(remaining), UI.INK, 56, true);
        timerText.setGravity(Gravity.CENTER);
        content.addView(timerText);

        LinearLayout row = UI.row(this);
        row.setGravity(Gravity.CENTER_HORIZONTAL);
        TextView skip = UI.ghost(this, "Skip", 15, 14);
        TextView got = UI.button(this, "Guessed! (+1)", UI.MINT, UI.MINT_D, 15, 14);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        lp.setMargins(UI.dp(this, 3), 0, UI.dp(this, 3), 0);
        row.addView(skip, lp);
        row.addView(got, lp);
        skip.setOnClickListener(v -> nextWord());
        got.setOnClickListener(v -> {
            points[describer]++;
            nextWord();
        });
        content.addView(row);

        TextView tally = UI.text(this, tally(), UI.INK_SOFT, 12, false);
        tally.setGravity(Gravity.CENTER);
        tally.setPadding(0, UI.dp(this, 10), 0, 0);
        content.addView(tally);
    }

    private void nextWord() {
        cursor++;
        if (cursor >= order.size()) {
            Collections.shuffle(order, UI.RND);
            cursor = 0;
        }
        renderTiming();
    }

    private final Runnable tick = new Runnable() {
        public void run() {
            if (!running) return;
            remaining--;
            if (timerText != null) {
                timerText.setText(String.valueOf(remaining));
                if (remaining <= 5) timerText.setTextColor(UI.RED);
            }
            if (remaining <= 0) {
                running = false;
                timeUp();
                return;
            }
            handler.postDelayed(this, 1000);
        }
    };

    private void timeUp() {
        handler.removeCallbacks(tick);
        content.removeAllViews();

        TextView up = UI.text(this, "Time's up", UI.RED, 26, true);
        up.setGravity(Gravity.CENTER);
        content.addView(up);

        String[] entry = Data.TABOO_WORDS[order.get(cursor)];
        TextView wordCard = UI.card(this, entry[0], UI.INK, 20);
        content.addView(wordCard, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView tallyT = UI.text(this, tally(), UI.GOLD, 14, true);
        tallyT.setGravity(Gravity.CENTER);
        tallyT.setPadding(0, UI.dp(this, 14), 0, UI.dp(this, 16));
        content.addView(tallyT);

        TextView next = UI.button(this, "Next Player", ACCENT, ACCENT_D, 16, 15);
        content.addView(next, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        next.setOnClickListener(v -> {
            cursor++;
            if (cursor >= order.size()) {
                Collections.shuffle(order, UI.RND);
                cursor = 0;
            }
            describer = (describer + 1) % players;
            newTurn();
        });
    }

    private String tally() {
        StringBuilder sb = new StringBuilder("Scores  ");
        for (int i = 0; i < players; i++) {
            sb.append("P").append(i + 1).append(": ").append(points[i]).append("  ");
        }
        return sb.toString();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        running = false;
        handler.removeCallbacks(tick);
    }
}
