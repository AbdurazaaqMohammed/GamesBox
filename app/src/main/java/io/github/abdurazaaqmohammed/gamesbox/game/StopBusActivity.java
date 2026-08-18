package io.github.abdurazaaqmohammed.gamesbox.game;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import io.github.abdurazaaqmohammed.gamesbox.ui.UI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StopBusActivity extends BaseGameActivity {

    private static final int ACCENT = UI.TEAL;

    private LinearLayout content;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private UI.Stepper playersStepper;
    private UI.Stepper timerStepper;
    private int players = 4;
    private int seconds = 60;
    private char letter;
    private final List<Integer> cats = new ArrayList<>();
    private int[] points;
    private boolean running;
    private int remaining;
    private TextView timerText;
    private List<TextView> catViews = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        content = UI.screen(this, "Stop the Bus",
                "Scattergories-style word race", ACCENT);
        showSetup();
    }

    private void showSetup() {
        running = false;
        content.removeAllViews();

        TextView rules = UI.text(this,
                "A letter and 3 categories appear. Every player writes a word for each starting with that letter. First to finish shouts \"stop the bus\".",
                UI.INK_SOFT, 14, false);
        rules.setPadding(0, 0, 0, UI.dp(this, 16));
        content.addView(rules);

        playersStepper = new UI.Stepper(this, "Players", 2, 10, 4, 1);
        content.addView(playersStepper.row);

        timerStepper = new UI.Stepper(this, "Seconds", 30, 120, 60, 10);
        content.addView(timerStepper.row);

        content.addView(UI.space(this, 20));

        TextView start = UI.button(this, "Start Round", UI.TEAL, UI.TEAL_D, 17, 16);
        content.addView(start, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        start.setOnClickListener(v -> {
            players = playersStepper.get();
            seconds = timerStepper.get();
            points = new int[players];
            newRound();
        });
    }

    private void newRound() {
        letter = (char) ('A' + UI.RND.nextInt(26));
        cats.clear();
        List<Integer> pool = new ArrayList<>();
        for (int i = 0; i < Data.BUS_CATEGORIES.length; i++) pool.add(i);
        Collections.shuffle(pool, UI.RND);
        for (int i = 0; i < 3; i++) cats.add(pool.get(i));
        showLetter();
    }

    private void showLetter() {
        running = false;
        content.removeAllViews();

        TextView l = UI.text(this, "Letter\n" + letter, UI.INK, 46, true);
        l.setGravity(Gravity.CENTER);
        content.addView(l);

        TextView note = UI.text(this, "Your words must start with " + letter,
                UI.INK_SOFT, 15, false);
        note.setGravity(Gravity.CENTER);
        note.setPadding(0, UI.dp(this, 8), 0, UI.dp(this, 16));
        content.addView(note);

        for (int c : cats) {
            TextView chip = UI.chip(this, Data.BUS_CATEGORIES[c], UI.TEAL, 14);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, UI.dp(this, 3), 0, UI.dp(this, 3));
            content.addView(chip, lp);
        }

        content.addView(UI.space(this, 18));

        TextView go = UI.button(this, "Go!", UI.TEAL, UI.TEAL_D, 17, 16);
        content.addView(go, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        go.setOnClickListener(v -> showTiming());
    }

    private void showTiming() {
        content.removeAllViews();
        running = true;
        remaining = seconds;

        TextView head = UI.text(this, "Write your words.\nFirst one done shouts \"stop the bus\"",
                UI.INK, 18, true);
        head.setGravity(Gravity.CENTER);
        head.setPadding(0, 0, 0, UI.dp(this, 16));
        content.addView(head);

        timerText = UI.text(this, String.valueOf(remaining), UI.INK, 72, true);
        timerText.setGravity(Gravity.CENTER);
        content.addView(timerText);

        TextView stop = UI.button(this, "Stop the bus", UI.RED, UI.RED_D, 18, 20);
        content.addView(stop, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        stop.setOnClickListener(v -> {
            running = false;
            showStopped();
        });

        handler.postDelayed(tick, 1000);
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
                showStopped();
                return;
            }
            handler.postDelayed(this, 1000);
        }
    };

    private void showStopped() {
        content.removeAllViews();

        TextView head = UI.text(this, "Time's up", UI.GOLD, 20, true);
        head.setGravity(Gravity.CENTER);
        content.addView(head);

        TextView instr = UI.text(this, "Read your answers out loud. Tap each player who had a valid unique word (+1).",
                UI.INK_SOFT, 13, false);
        instr.setGravity(Gravity.CENTER);
        instr.setPadding(0, UI.dp(this, 8), 0, UI.dp(this, 12));
        content.addView(instr);

        TextView letterT = UI.text(this, "Letter: " + letter, UI.INK, 20, true);
        letterT.setGravity(Gravity.CENTER);
        letterT.setPadding(0, 0, 0, UI.dp(this, 10));
        content.addView(letterT);

        catViews.clear();
        for (int c : cats) {
            TextView chip = UI.chip(this, Data.BUS_CATEGORIES[c], UI.TEAL, 14);
            LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            clp.setMargins(0, UI.dp(this, 2), 0, UI.dp(this, 8));
            content.addView(chip, clp);

            LinearLayout row = UI.row(this);
            row.setGravity(Gravity.CENTER_HORIZONTAL);
            int perRow = players <= 5 ? players : 5;
            for (int p = 0; p < players; p++) {
                final int pidx = p;
                TextView pchip = UI.ghost(this, "P" + (p + 1), 12, 10);
                LinearLayout.LayoutParams plp = new LinearLayout.LayoutParams(
                        0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                plp.setMargins(UI.dp(this, 2), UI.dp(this, 2), UI.dp(this, 2), UI.dp(this, 2));
                row.addView(pchip, plp);
                pchip.setOnClickListener(v -> {
                    points[pidx]++;
                    UI.popIn(v, 200);
                });
            }
            content.addView(row);
        }

        TextView tally = UI.text(this, tally(), UI.GOLD, 14, true);
        tally.setGravity(Gravity.CENTER);
        tally.setPadding(0, UI.dp(this, 12), 0, UI.dp(this, 10));
        content.addView(tally);

        TextView next = UI.button(this, "Next Round", UI.TEAL, UI.TEAL_D, 16, 15);
        content.addView(next, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        next.setOnClickListener(v -> newRound());
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
    }
}
