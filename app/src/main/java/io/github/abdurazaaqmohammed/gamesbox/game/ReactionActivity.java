package io.github.abdurazaaqmohammed.gamesbox.game;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import io.github.abdurazaaqmohammed.gamesbox.PlayerNames;
import io.github.abdurazaaqmohammed.gamesbox.ui.UI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReactionActivity extends BaseGameActivity {

    private static final int ACCENT = UI.LIME;
    private static final int ACCENT_D = UI.LIME_D;

    private LinearLayout content;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private UI.Stepper playersStepper;
    private int players;
    private int[] bestMs;
    private int currentPlayer;
    private boolean green;
    private long greenAt;
    private boolean[] doneThisRound;
    private LinearLayout root;
    private TextView prompt;
    private long tooSoonAt;

    private String playerName(int i) {
        return PlayerNames.get(this, i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        content = UI.screen(this, "Reaction",
                "Tap when it turns green", ACCENT);
        showSetup();
    }

    private void showSetup() {
        green = false;
        content.removeAllViews();

        TextView rules = UI.text(this,
                "The screen stays red... then turns green at a completely random moment. Tap as fast as you can. Tap too early and you must redo it. Fastest finger of the round wins.",
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
            bestMs = new int[players];
            for (int i = 0; i < players; i++) bestMs[i] = -1;
            doneThisRound = new boolean[players];
            currentPlayer = 0;
            showReady();
        });
    }

    private void showReady() {
        green = false;
        handler.removeCallbacks(goGreen);
        handler.removeCallbacks(tooSlow);
        content.removeAllViews();

        TextView who = UI.text(this, playerName(currentPlayer) + " goes",
                UI.INK, 16, true);
        who.setGravity(Gravity.CENTER);
        who.setPadding(0, UI.dp(this, 4), 0, UI.dp(this, 10));
        content.addView(who);

        prompt = UI.text(this, "Rest your thumb on the screen.\nWhen it turns green, TAP!",
                UI.INK, 18, true);
        prompt.setGravity(Gravity.CENTER);
        prompt.setLineSpacing(UI.dp(this, 5), 1f);
        prompt.setPadding(0, UI.dp(this, 6), 0, UI.dp(this, 18));
        content.addView(prompt);

        TextView go = UI.button(this, "Ready - go red", UI.RED, UI.RED_D, 17, 16);
        content.addView(go, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        go.setOnClickListener(v -> runRound());
    }

    private void runRound() {
        content.removeAllViews();
        green = false;

        root = UI.col(this);
        root.setGravity(Gravity.CENTER);
        root.setBackgroundDrawable(UI.fill(UI.RED, 0, this));
        LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, UI.dp(this, 300));
        content.addView(root, rlp);

        TextView w = UI.text(this, "WAIT...", UI.WHITE, 30, true);
        w.setGravity(Gravity.CENTER);
        root.addView(w);

        TextView t = UI.text(this, playerName(currentPlayer)
                + " - tap the moment it turns green", UI.WHITE_SOFT, 13, false);
        t.setGravity(Gravity.CENTER);
        t.setPadding(0, UI.dp(this, 8), 0, 0);
        root.addView(t);

        root.setOnClickListener(v -> onTap());

        long delay = 2000 + UI.RND.nextInt(4001);
        handler.removeCallbacks(goGreen);
        handler.removeCallbacks(tooSlow);
        handler.postDelayed(goGreen, delay);
    }

    private final Runnable goGreen = new Runnable() {
        public void run() {
            if (green) return;
            green = true;
            greenAt = SystemClock.elapsedRealtime();
            root.setBackgroundDrawable(UI.fill(UI.MINT, 0, ReactionActivity.this));
            ((TextView) root.getChildAt(0)).setText("TAP!");
            buzz();
            handler.removeCallbacks(tooSlow);
            handler.postDelayed(tooSlow, 3000);
        }
    };

    private final Runnable tooSlow = new Runnable() {
        public void run() {
            if (!green) return;
            green = false;
            content.removeAllViews();
            showResult(-1, true);
        }
    };

    private void onTap() {
        if (!green) {
            long now = SystemClock.elapsedRealtime();
            if (now - tooSoonAt < 600) return;
            tooSoonAt = now;
            UI.shake(root);
            runRound();
            return;
        }
        handler.removeCallbacks(tooSlow);
        green = false;
        long ms = SystemClock.elapsedRealtime() - greenAt;
        content.removeAllViews();
        showResult(ms, false);
    }

    private void showResult(long ms, boolean slow) {
        doneThisRound[currentPlayer] = true;

        TextView title;
        String rating;
        if (slow) {
            title = UI.text(this, "Too slow!", UI.RED, 30, true);
            rating = "You nodded off - no time recorded.";
        } else {
            boolean best = bestMs[currentPlayer] < 0 || ms < bestMs[currentPlayer];
            if (best) bestMs[currentPlayer] = (int) ms;
            title = UI.text(this, ms + " ms", UI.INK, 44, true);
            if (ms < 180) rating = "Lightning fast! ⚡";
            else if (ms < 250) rating = "Quick!";
            else if (ms < 350) rating = "Decent.";
            else rating = "Sleepy...";
        }

        content.removeAllViews();
        title.setGravity(Gravity.CENTER);
        content.addView(title);

        TextView r = UI.text(this, rating + (slow ? "" : "   " + playerName(currentPlayer)),
                UI.INK_SOFT, 15, false);
        r.setGravity(Gravity.CENTER);
        r.setPadding(0, UI.dp(this, 8), 0, UI.dp(this, 14));
        content.addView(r);

        TextView tally = UI.text(this, tally(), UI.GOLD, 14, true);
        tally.setGravity(Gravity.CENTER);
        tally.setPadding(0, 0, 0, UI.dp(this, 16));
        content.addView(tally);

        final boolean roundOver = allDone();

        TextView next = UI.button(this, roundOver ? "See standings" : "Next Player",
                ACCENT, ACCENT_D, 16, 15);
        content.addView(next, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        next.setOnClickListener(v -> {
            if (roundOver) {
                showStandings();
            } else {
                currentPlayer = (currentPlayer + 1) % players;
                showReady();
            }
        });
    }

    private void showStandings() {
        content.removeAllViews();

        TextView title = UI.text(this, "Round complete!", UI.INK, 24, true);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, UI.dp(this, 4), 0, UI.dp(this, 14));
        content.addView(title);

        List<Integer> rank = new ArrayList<>();
        for (int i = 0; i < players; i++) rank.add(i);
        Collections.sort(rank, (a, b) -> bestMs[a] - bestMs[b]);

        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < rank.size(); r++) {
            int p = rank.get(r);
            sb.append(r + 1).append(". ").append(playerName(p)).append(" - ");
            if (bestMs[p] < 0) {
                sb.append("no time");
            } else {
                sb.append(bestMs[p]).append(" ms");
            }
            if (r == 0 && bestMs[rank.get(0)] >= 0) sb.append("  ⚡");
            if (r < rank.size() - 1) sb.append("\n");
        }
        TextView list = UI.text(this, sb.toString(), UI.INK, 16, true);
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
            for (int i = 0; i < players; i++) doneThisRound[i] = false;
            currentPlayer = 0;
            showReady();
        });

        TextView setup = UI.ghost(this, "Back to Setup", 15, 14);
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        slp.topMargin = UI.dp(this, 10);
        content.addView(setup, slp);
        setup.setOnClickListener(v -> showSetup());
    }

    private String tally() {
        StringBuilder sb = new StringBuilder("Best   ");
        for (int i = 0; i < players; i++) {
            sb.append("P").append(i + 1).append(": ");
            sb.append(bestMs[i] < 0 ? "-" : bestMs[i] + "ms").append("  ");
        }
        return sb.toString();
    }

    private boolean allDone() {
        for (boolean d : doneThisRound) {
            if (!d) return false;
        }
        return true;
    }

    private void buzz() {
        try {
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (v != null) {
                v.vibrate(40);
            }
        } catch (Throwable t) {
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(goGreen);
        handler.removeCallbacks(tooSlow);
    }
}
