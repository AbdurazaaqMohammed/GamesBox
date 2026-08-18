package io.github.abdurazaaqmohammed.gamesbox.game;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import io.github.abdurazaaqmohammed.gamesbox.ui.UI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ScavengerHuntActivity extends BaseGameActivity {

    private static final int ACCENT = UI.MINT;
    private static final int ACCENT_D = UI.MINT_D;

    private LinearLayout content;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private UI.Stepper teamsStepper;
    private UI.Stepper itemsStepper;
    private UI.Toggle timeToggle;
    private int teams;
    private int[] points;
    private int currentTeam;
    private int itemsCount;
    private int roundSeconds;
    private int remaining;
    private boolean running;

    @Override
    protected boolean isGameInProgress() {
        return running;
    }
    private String[] items;
    private boolean[] found;
    private int itemsFound;
    private TextView timerText;
    private TextView startBtn;

    private static final String[] TIME_LABELS = {"30s", "60s", "90s"};
    private static final int[] TIME_VALUES = {30, 60, 90};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        content = UI.screen(this, "Scavenger Hunt",
                "Race to find the items", ACCENT);
        showSetup();
    }

    private void showSetup() {
        running = false;
        content.removeAllViews();

        TextView rules = UI.text(this,
                "A list of things to find appears - \"something green\", \"something that spins\"... Each team races to collect as many as they can before the clock runs out. Tap an item the moment your team finds it to score a point.",
                UI.INK_SOFT, 14, false);
        rules.setPadding(0, 0, 0, UI.dp(this, 16));
        content.addView(rules);

        teamsStepper = new UI.Stepper(this, "Teams", 2, 8, 3, 1);
        content.addView(teamsStepper.row);

        itemsStepper = new UI.Stepper(this, "Items per round", 3, 6, 5, 1);
        content.addView(itemsStepper.row);

        timeToggle = new UI.Toggle(this, "Time per round", TIME_LABELS, ACCENT, 1);
        content.addView(timeToggle.row);

        content.addView(UI.space(this, 16));

        TextView start = UI.button(this, "Start Game", ACCENT, ACCENT_D, 17, 16);
        content.addView(start, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        start.setOnClickListener(v -> {
            teams = teamsStepper.get();
            points = new int[teams];
            currentTeam = 0;
            roundSeconds = TIME_VALUES[timeToggle.get()];
            newRound();
        });
    }

    private void newRound() {
        running = false;
        handler.removeCallbacks(tick);
        content.removeAllViews();

        List<Integer> pick = new ArrayList<>();
        for (int i = 0; i < Data.SCAVENGER_ITEMS.length; i++) pick.add(i);
        Collections.shuffle(pick, UI.RND);
        itemsCount = itemsStepper.get();
        items = new String[itemsCount];
        found = new boolean[itemsCount];
        itemsFound = 0;
        for (int i = 0; i < itemsCount; i++) {
            items[i] = Data.SCAVENGER_ITEMS[pick.get(i)];
        }

        TextView who = UI.text(this, "Team " + (currentTeam + 1) + "'s turn",
                UI.INK, 18, true);
        who.setGravity(Gravity.CENTER);
        who.setPadding(0, UI.dp(this, 4), 0, UI.dp(this, 8));
        content.addView(who);

        TextView score = UI.text(this, tally(), UI.GOLD, 14, true);
        score.setGravity(Gravity.CENTER);
        score.setPadding(0, 0, 0, UI.dp(this, 10));
        content.addView(score);

        timerText = UI.text(this, "Ready - " + roundSeconds + " seconds",
                UI.INK, 20, true);
        timerText.setGravity(Gravity.CENTER);
        timerText.setPadding(0, UI.dp(this, 4), 0, UI.dp(this, 10));
        content.addView(timerText);

        LinearLayout list = UI.col(this);
        for (int i = 0; i < items.length; i++) {
            final int idx = i;
            final TextView chip = UI.text(this, items[i], UI.INK, 15, false);
            chip.setGravity(Gravity.CENTER);
            chip.setPadding(UI.dp(this, 14), UI.dp(this, 12), UI.dp(this, 14), UI.dp(this, 12));
            chip.setBackgroundDrawable(UI.cardBg(this));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.bottomMargin = UI.dp(this, 6);
            list.addView(chip, lp);
            chip.setOnClickListener(v -> {
                if (!running || found[idx]) return;
                found[idx] = true;
                itemsFound++;
                chip.setText(items[idx] + "  ✓");
                chip.setBackgroundDrawable(UI.fill(ACCENT, 12, ScavengerHuntActivity.this));
                chip.setTextColor(UI.WHITE);
            });
        }
        content.addView(list);

        TextView note = UI.text(this,
                "Tap each item the moment your team finds it. Start the clock and go!",
                UI.INK_SOFT, 13, false);
        note.setGravity(Gravity.CENTER);
        note.setPadding(0, UI.dp(this, 10), 0, UI.dp(this, 12));
        content.addView(note);

        startBtn = UI.button(this, "Start the clock!", ACCENT, ACCENT_D, 17, 16);
        content.addView(startBtn, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        startBtn.setOnClickListener(v -> startTimer());
    }

    private void startTimer() {
        running = true;
        remaining = roundSeconds;
        if (startBtn != null) startBtn.setVisibility(View.GONE);
        if (timerText != null) timerText.setText(String.valueOf(remaining));
        handler.removeCallbacks(tick);
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
                endRound();
                return;
            }
            handler.postDelayed(this, 1000);
        }
    };

    private void endRound() {
        bigVibrate();
        points[currentTeam] += itemsFound;
        content.removeAllViews();

        TextView up = UI.text(this, "Time's up!", UI.RED, 28, true);
        up.setGravity(Gravity.CENTER);
        content.addView(up);

        TextView got = UI.text(this, "Team " + (currentTeam + 1) + " found "
                + itemsFound + " of " + itemsCount + " items.", UI.INK, 16, true);
        got.setGravity(Gravity.CENTER);
        got.setPadding(0, UI.dp(this, 8), 0, UI.dp(this, 14));
        content.addView(got);

        TextView score = UI.text(this, tally(), UI.GOLD, 16, true);
        score.setGravity(Gravity.CENTER);
        score.setPadding(0, 0, 0, UI.dp(this, 16));
        content.addView(score);

        final boolean last = currentTeam + 1 >= teams;
        TextView next = UI.button(this,
                last ? "See final scores" : "Team " + (currentTeam + 2) + " - Go!",
                ACCENT, ACCENT_D, 16, 15);
        content.addView(next, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        next.setOnClickListener(v -> {
            currentTeam++;
            if (currentTeam >= teams) {
                showFinal();
            } else {
                newRound();
            }
        });
    }

    private void showFinal() {
        content.removeAllViews();

        TextView title = UI.text(this, "Final scores", UI.INK, 24, true);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, UI.dp(this, 4), 0, UI.dp(this, 14));
        content.addView(title);

        int best = 0;
        for (int i = 1; i < teams; i++) {
            if (points[i] > points[best]) best = i;
        }

        List<Integer> rank = new ArrayList<>();
        for (int i = 0; i < teams; i++) rank.add(i);
        Collections.sort(rank, (a, b) -> points[b] - points[a]);

        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < rank.size(); r++) {
            int t = rank.get(r);
            sb.append(r + 1).append(". Team ").append(t + 1)
                    .append(" - ").append(points[t]).append(points[t] == 1 ? " point" : " points");
            if (t == best) sb.append("  👑");
            if (r < rank.size() - 1) sb.append("\n");
        }
        TextView list = UI.text(this, sb.toString(), UI.INK, 17, true);
        list.setGravity(Gravity.CENTER);
        list.setLineSpacing(UI.dp(this, 5), 1f);
        list.setPadding(UI.dp(this, 16), UI.dp(this, 14), UI.dp(this, 16), UI.dp(this, 14));
        list.setBackgroundDrawable(UI.cardBg(this));
        content.addView(list, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        content.addView(UI.space(this, 16));

        TextView again = UI.button(this, "New Round", ACCENT, ACCENT_D, 16, 15);
        content.addView(again, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        again.setOnClickListener(v -> {
            for (int i = 0; i < teams; i++) points[i] = 0;
            currentTeam = 0;
            newRound();
        });

        TextView setup = UI.ghost(this, "Back to Setup", 15, 14);
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        slp.topMargin = UI.dp(this, 10);
        content.addView(setup, slp);
        setup.setOnClickListener(v -> showSetup());
    }

    private String tally() {
        StringBuilder sb = new StringBuilder("Scores  ");
        for (int i = 0; i < teams; i++) {
            sb.append("T").append(i + 1).append(": ").append(points[i]).append("  ");
        }
        return sb.toString();
    }

    private void bigVibrate() {
        try {
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (v != null) {
                v.vibrate(new long[]{0, 400, 120, 400}, -1);
            }
        } catch (Throwable t) {
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        running = false;
        handler.removeCallbacks(tick);
    }
}
