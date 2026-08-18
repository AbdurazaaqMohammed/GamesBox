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

public class FiveSecondActivity extends BaseGameActivity {

    private static final int ACCENT = UI.CYAN;
    private static final int ACCENT_D = UI.CYAN_D;

    private LinearLayout content;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private UI.Stepper playersStepper;
    private int players;
    private int[] points;
    private int currentPlayer;
    private int cursor;
    private String category;
    private boolean running;

    @Override
    protected boolean isGameInProgress() {
        return running;
    }
    private int remaining;
    private TextView timerText;

    private String playerName(int i) {
        return PlayerNames.get(this, i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        content = UI.screen(this, "5 Second Rule",
                "Name 3 things - fast!", ACCENT);
        showSetup();
    }

    private void showSetup() {
        running = false;
        content.removeAllViews();

        TextView rules = UI.text(this,
                "A category appears. The player has 5 seconds to name 3 things that fit - for example 3 breakfast foods. If they pull it off, they score a point. Then the phone passes on.",
                UI.INK_SOFT, 14, false);
        rules.setPadding(0, 0, 0, UI.dp(this, 16));
        content.addView(rules);

        playersStepper = new UI.Stepper(this, "Players", 2, 10, 4, 1);
        content.addView(playersStepper.row);

        content.addView(UI.space(this, 16));

        TextView start = UI.button(this, "Start Game", ACCENT, ACCENT_D, 17, 16);
        content.addView(start, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        start.setOnClickListener(v -> {
            players = playersStepper.get();
            points = new int[players];
            currentPlayer = 0;
            cursor = 0;
            newRound();
        });
    }

    private void newRound() {
        running = false;
        category = Data.FIVE_SECOND[cursor];
        cursor++;
        if (cursor >= Data.FIVE_SECOND.length) cursor = 0;
        content.removeAllViews();

        TextView who = UI.text(this, playerName(currentPlayer) + " goes",
                UI.INK, 16, true);
        who.setGravity(Gravity.CENTER);
        who.setPadding(0, UI.dp(this, 4), 0, UI.dp(this, 10));
        content.addView(who);

        TextView cat = UI.card(this, "Name 3 " + category, UI.INK, 18);
        content.addView(cat, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView note = UI.text(this,
                "They get 5 seconds. When they're ready, start the countdown.",
                UI.INK_SOFT, 13, false);
        note.setGravity(Gravity.CENTER);
        note.setPadding(0, UI.dp(this, 12), 0, UI.dp(this, 16));
        content.addView(note);

        TextView go = UI.button(this, "Start - 5 seconds", ACCENT, ACCENT_D, 17, 16);
        content.addView(go, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        go.setOnClickListener(v -> countdown());
    }

    private void countdown() {
        content.removeAllViews();
        running = true;
        remaining = 5;

        TextView cat = UI.chip(this, "Name 3 " + category, ACCENT, 14);
        cat.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        content.addView(cat, clp);

        timerText = UI.text(this, String.valueOf(remaining), UI.INK, 96, true);
        timerText.setGravity(Gravity.CENTER);
        content.addView(timerText);

        TextView note = UI.text(this, playerName(currentPlayer)
                + ": name 3 things fast!", UI.INK_SOFT, 14, false);
        note.setGravity(Gravity.CENTER);
        content.addView(note);

        handler.removeCallbacks(tick);
        handler.postDelayed(tick, 1000);
    }

    private final Runnable tick = new Runnable() {
        public void run() {
            if (!running) return;
            remaining--;
            if (timerText != null) {
                timerText.setText(String.valueOf(remaining));
                if (remaining <= 2) timerText.setTextColor(UI.RED);
            }
            if (remaining <= 0) {
                running = false;
                showResult();
                return;
            }
            handler.postDelayed(this, 1000);
        }
    };

    private void showResult() {
        content.removeAllViews();

        TextView up = UI.text(this, "Time's up!", UI.RED, 28, true);
        up.setGravity(Gravity.CENTER);
        content.addView(up);

        TextView ask = UI.text(this, "Did " + playerName(currentPlayer)
                + " name 3 " + category + "?", UI.INK, 17, true);
        ask.setGravity(Gravity.CENTER);
        ask.setPadding(0, UI.dp(this, 10), 0, UI.dp(this, 16));
        content.addView(ask);

        LinearLayout row = UI.row(this);
        row.setGravity(Gravity.CENTER_HORIZONTAL);
        TextView yes = UI.button(this, "Yes! (+1)", UI.MINT, UI.MINT_D, 15, 14);
        TextView no = UI.button(this, "No", UI.RED, UI.RED_D, 15, 14);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        lp.setMargins(UI.dp(this, 3), 0, UI.dp(this, 3), 0);
        row.addView(yes, lp);
        row.addView(no, lp);
        yes.setOnClickListener(v -> {
            points[currentPlayer]++;
            advance();
        });
        no.setOnClickListener(v -> advance());
        content.addView(row);

        TextView tally = UI.text(this, tally(), UI.GOLD, 14, true);
        tally.setGravity(Gravity.CENTER);
        tally.setPadding(0, UI.dp(this, 12), 0, 0);
        content.addView(tally);
    }

    private void advance() {
        currentPlayer = (currentPlayer + 1) % players;
        newRound();
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
