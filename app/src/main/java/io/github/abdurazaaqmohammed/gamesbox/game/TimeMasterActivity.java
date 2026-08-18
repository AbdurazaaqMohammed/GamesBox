package io.github.abdurazaaqmohammed.gamesbox.game;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;

import io.github.abdurazaaqmohammed.gamesbox.PlayerNames;
import io.github.abdurazaaqmohammed.gamesbox.ui.UI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class TimeMasterActivity extends BaseGameActivity {

    private static final int ACCENT = UI.TEAL;
    private static final int ACCENT_D = UI.TEAL_D;

    private LinearLayout content;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private UI.Stepper playersStepper;
    private int players;
    private int[] bestDeviation;
    private int currentPlayer;
    private boolean running;

    @Override
    protected boolean isGameInProgress() {
        return running;
    }
    private long startAt;
    private int targetSeconds;
    private long stoppedAt;
    private TextView pulse;
    private boolean[] doneThisRound;

    private String playerName(int i) {
        return PlayerNames.get(this, i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        content = UI.screen(this, "Time Master",
                "Stop at the target time", ACCENT);
        showSetup();
    }

    private void showSetup() {
        running = false;
        content.removeAllViews();

        TextView rules = UI.text(this,
                "The phone shows a target time - say 10 seconds. The player holds the phone, starts the clock, and stops it when they THINK the target is up. No numbers shown, just feel it. Closest stop wins the round.",
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
            bestDeviation = new int[players];
            for (int i = 0; i < players; i++) bestDeviation[i] = -1;
            doneThisRound = new boolean[players];
            currentPlayer = 0;
            showReady();
        });
    }

    private void showReady() {
        running = false;
        content.removeAllViews();

        TextView who = UI.text(this, playerName(currentPlayer) + " goes",
                UI.INK, 16, true);
        who.setGravity(Gravity.CENTER);
        who.setPadding(0, UI.dp(this, 4), 0, UI.dp(this, 10));
        content.addView(who);

        targetSeconds = 5 + UI.RND.nextInt(16);

        TextView label = UI.text(this, "Stop when you think", UI.INK_SOFT, 14, false);
        label.setGravity(Gravity.CENTER);
        label.setPadding(0, UI.dp(this, 6), 0, 0);
        content.addView(label);

        TextView target = UI.text(this, targetSeconds + " seconds",
                UI.INK, 46, true);
        target.setGravity(Gravity.CENTER);
        content.addView(target);

        TextView note = UI.text(this,
                "When you're ready, hit start. No clock on screen - trust your gut.",
                UI.INK_SOFT, 13, false);
        note.setGravity(Gravity.CENTER);
        note.setPadding(0, UI.dp(this, 10), 0, UI.dp(this, 16));
        content.addView(note);

        TextView go = UI.button(this, "Start the clock", ACCENT, ACCENT_D, 17, 16);
        content.addView(go, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        go.setOnClickListener(v -> countdownRun());
    }

    private void countdownRun() {
        running = true;
        content.removeAllViews();

        TextView label = UI.text(this, "Feeling it...", UI.INK_SOFT, 13, true);
        label.setGravity(Gravity.CENTER);
        content.addView(label);

        pulse = UI.text(this, "•", UI.INK, 80, false);
        pulse.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams plp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, UI.dp(this, 120));
        content.addView(pulse, plp);

        TextView hint = UI.text(this, "Tap the button when you think "
                + targetSeconds + " seconds have passed.", UI.INK_SOFT, 13, false);
        hint.setGravity(Gravity.CENTER);
        hint.setPadding(0, UI.dp(this, 14), 0, UI.dp(this, 18));
        content.addView(hint);

        TextView stop = UI.button(this, "STOP", UI.RED, UI.RED_D, 20, 20);
        content.addView(stop, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        stop.setOnClickListener(v -> stopClock());

        startAt = SystemClock.elapsedRealtime();
        handler.removeCallbacks(pulseRun);
        handler.postDelayed(pulseRun, 350);
    }

    private final Runnable pulseRun = new Runnable() {
        public void run() {
            if (!running || pulse == null) return;
            long elapsed = SystemClock.elapsedRealtime() - startAt;
            float p = Math.min(1f, (float) elapsed / (targetSeconds * 1000f));
            float max = 1f + 0.5f * p;
            ScaleAnimation s = new ScaleAnimation(1f, max, 1f, max,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            s.setDuration(320);
            s.setRepeatMode(Animation.REVERSE);
            s.setRepeatCount(Animation.INFINITE);
            pulse.startAnimation(s);
            handler.postDelayed(this, 380);
        }
    };

    private void stopClock() {
        if (!running) return;
        running = false;
        stoppedAt = SystemClock.elapsedRealtime();
        long elapsedMs = stoppedAt - startAt;
        double elapsed = elapsedMs / 1000.0;
        double deviation = Math.abs(elapsed - targetSeconds);

        boolean bullseye = deviation <= 0.5;
        int deviMillis = (int) Math.round(deviation * 1000);

        if (bestDeviation[currentPlayer] < 0 || deviMillis < bestDeviation[currentPlayer]) {
            bestDeviation[currentPlayer] = deviMillis;
        }
        doneThisRound[currentPlayer] = true;

        content.removeAllViews();

        TextView title = UI.text(this, bullseye ? "Bullseye!" : "Stopped!",
                bullseye ? UI.MINT : UI.INK, 30, true);
        title.setGravity(Gravity.CENTER);
        content.addView(title);

        TextView result = UI.text(this,
                playerName(currentPlayer) + " stopped at "
                        + String.format(Locale.US, "%.1f", elapsed) + "s\nTarget was "
                        + targetSeconds + "s - off by " + String.format(Locale.US, "%.1f", deviation) + "s",
                UI.INK, 18, true);
        result.setGravity(Gravity.CENTER);
        result.setLineSpacing(UI.dp(this, 5), 1f);
        result.setPadding(0, UI.dp(this, 8), 0, UI.dp(this, 14));
        content.addView(result);

        if (bullseye) bigVibrate();

        TextView tally = UI.text(this, tally(), UI.GOLD, 14, true);
        tally.setGravity(Gravity.CENTER);
        tally.setPadding(0, 0, 0, UI.dp(this, 16));
        content.addView(tally);

        final boolean roundOver = allDone();

        TextView next;
        if (roundOver) {
            next = UI.button(this, "See standings", ACCENT, ACCENT_D, 16, 15);
        } else {
            next = UI.button(this, "Next Player", ACCENT, ACCENT_D, 16, 15);
        }
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
        Collections.sort(rank, (a, b) -> bestDeviation[a] - bestDeviation[b]);

        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < rank.size(); r++) {
            int p = rank.get(r);
            sb.append(r + 1).append(". ").append(playerName(p)).append(" - ")
                    .append(String.format(Locale.US, "%.2f", bestDeviation[p] / 1000.0))
                    .append("s off");
            if (r == 0) sb.append("  🏆");
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
        StringBuilder sb = new StringBuilder("Best off by   ");        for (int i = 0; i < players; i++) {
            sb.append("P").append(i + 1).append(": ");
            if (bestDeviation[i] < 0) {
                sb.append("-  ");
            } else {
                sb.append(String.format(Locale.US, "%.1f", bestDeviation[i] / 1000.0)).append("s  ");
            }
        }
        return sb.toString();
    }

    private boolean allDone() {
        for (boolean d : doneThisRound) {
            if (!d) return false;
        }
        return true;
    }

    private void bigVibrate() {
        try {
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (v != null) {
                v.vibrate(new long[]{0, 200, 80, 200}, -1);
            }
        } catch (Throwable t) {
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        running = false;
        handler.removeCallbacks(pulseRun);
    }
}
