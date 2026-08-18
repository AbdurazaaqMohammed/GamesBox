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

import io.github.abdurazaaqmohammed.gamesbox.ui.UI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HotPotatoActivity extends BaseGameActivity {

    private static final int ACCENT = UI.RED;

    private LinearLayout content;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private UI.Stepper playersStepper;
    private boolean running;
    private String currentTopic;
    private TextView potato;
    private long startAt;
    private long totalMillis;
    private final List<Integer> topicOrder = new ArrayList<>();
    private int topicCursor;
    private final List<Integer> challengeOrder = new ArrayList<>();
    private int challengeCursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        content = UI.screen(this, "Hot Potato",
                "Say a word, pass the phone", ACCENT);
        showSetup();
    }

    private void showSetup() {
        running = false;
        content.removeAllViews();

        TextView rules = UI.text(this,
                "A topic appears on screen. Players take turns saying a word that fits the topic out loud, then quickly pass the phone to the next person.\n\nA hidden timer ends the round with a buzz - whoever is holding the phone when it goes off has to do a challenge.",
                UI.INK_SOFT, 14, false);
        rules.setPadding(0, 0, 0, UI.dp(this, 16));
        content.addView(rules);

        playersStepper = new UI.Stepper(this, "Players", 2, 10, 4, 1);
        content.addView(playersStepper.row);

        content.addView(UI.space(this, 16));

        TextView start = UI.button(this, "Start Game", UI.RED, UI.RED_D, 17, 16);
        content.addView(start, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        start.setOnClickListener(v -> startRound());
    }

    private void startRound() {
        running = false;
        handler.removeCallbacks(finish);
        content.removeAllViews();
        currentTopic = nextTopic();

        TextView label = UI.text(this, "Topic", UI.INK_SOFT, 13, true);
        label.setGravity(Gravity.CENTER);
        label.setPadding(0, UI.dp(this, 10), 0, UI.dp(this, 8));
        content.addView(label);

        TextView topic = UI.text(this, currentTopic, UI.INK, 34, true);
        topic.setGravity(Gravity.CENTER);
        topic.setPadding(0, 0, 0, UI.dp(this, 16));
        content.addView(topic);

        TextView instr = UI.text(this,
                "Say a word that fits the topic, then pass the phone to the next player.",
                UI.INK_SOFT, 14, false);
        instr.setGravity(Gravity.CENTER);
        instr.setPadding(0, 0, 0, UI.dp(this, 18));
        content.addView(instr);

        TextView go = UI.button(this, "Start the timer", UI.RED, UI.RED_D, 17, 16);
        content.addView(go, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        go.setOnClickListener(v -> play());
    }

    private String nextTopic() {
        if (topicOrder.isEmpty()) {
            for (int i = 0; i < Data.HOT_POTATO_TOPICS.length; i++) topicOrder.add(i);
            Collections.shuffle(topicOrder, UI.RND);
            topicCursor = 0;
        }
        String t = Data.HOT_POTATO_TOPICS[topicOrder.get(topicCursor)];
        topicCursor++;
        if (topicCursor >= topicOrder.size()) topicOrder.clear();
        return t;
    }

    private void play() {
        running = true;
        content.removeAllViews();

        TextView label = UI.text(this, "Topic", UI.INK_SOFT, 13, true);
        label.setGravity(Gravity.CENTER);
        content.addView(label);

        TextView topic = UI.text(this, currentTopic, UI.INK, 26, true);
        topic.setGravity(Gravity.CENTER);
        topic.setPadding(0, UI.dp(this, 4), 0, UI.dp(this, 6));
        content.addView(topic);

        potato = UI.text(this, "🥔", UI.INK, 110, false);
        potato.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams plp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, UI.dp(this, 150));
        content.addView(potato, plp);

        TextView hint = UI.text(this, "Say a fitting word, then pass the phone.", UI.INK_SOFT, 13, false);
        hint.setGravity(Gravity.CENTER);
        hint.setPadding(0, UI.dp(this, 14), 0, 0);
        content.addView(hint);

        totalMillis = 1500 + UI.RND.nextInt(16000);
        startAt = SystemClock.elapsedRealtime();
        startConstantVibration();
        handler.postDelayed(pulse, 400);
        handler.postDelayed(finish, totalMillis);
    }

    private final Runnable pulse = new Runnable() {
        public void run() {
            if (!running || potato == null) return;
            long elapsed = SystemClock.elapsedRealtime() - startAt;
            float p = Math.min(1f, (float) elapsed / (float) totalMillis);
            float max = 1.04f + 0.22f * p;
            ScaleAnimation s = new ScaleAnimation(1f, max, 1f, max,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            s.setDuration(420);
            s.setRepeatMode(Animation.REVERSE);
            s.setRepeatCount(Animation.INFINITE);
            potato.startAnimation(s);
            handler.postDelayed(this, 480);
        }
    };

    private final Runnable finish = new Runnable() {
        public void run() {
            if (!running) return;
            running = false;
            timeUp();
        }
    };

    private void timeUp() {
        handler.removeCallbacks(pulse);
        stopConstantVibration();
        bigVibrate();
        if (potato != null) potato.clearAnimation();
        content.removeAllViews();

        TextView up = UI.text(this, "Time's up", UI.INK, 30, true);
        up.setGravity(Gravity.CENTER);
        content.addView(up);

        TextView who = UI.text(this, "Whoever is holding the phone\ndoes the challenge:", UI.INK_SOFT, 15, false);
        who.setGravity(Gravity.CENTER);
        who.setPadding(0, UI.dp(this, 10), 0, UI.dp(this, 16));
        content.addView(who);

        TextView ch = UI.card(this, nextChallenge(), UI.INK, 18);
        content.addView(ch, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        content.addView(UI.space(this, 16));

        TextView next = UI.button(this, "Next Topic", UI.RED, UI.RED_D, 16, 15);
        content.addView(next, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        next.setOnClickListener(v -> startRound());
    }

    private String nextChallenge() {
        if (challengeOrder.isEmpty()) {
            for (int i = 0; i < Data.HOT_POTATO_CHALLENGES.length; i++) challengeOrder.add(i);
            Collections.shuffle(challengeOrder, UI.RND);
            challengeCursor = 0;
        }
        String c = Data.HOT_POTATO_CHALLENGES[challengeOrder.get(challengeCursor)];
        challengeCursor++;
        if (challengeCursor >= challengeOrder.size()) challengeOrder.clear();
        return c;
    }

    private void startConstantVibration() {
        try {
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (v != null) {
                v.vibrate(new long[]{0, 120, 380}, 0);
            }
        } catch (Throwable t) {
            // vibration not available - ignore
        }
    }

    private void stopConstantVibration() {
        try {
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (v != null) {
                v.cancel();
            }
        } catch (Throwable t) {
            // vibration not available - ignore
        }
    }

    private void bigVibrate() {
        try {
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (v != null) {
                v.vibrate(new long[]{0, 400, 120, 400, 120, 500}, -1);
            }
        } catch (Throwable t) {
            // vibration not available - ignore
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        running = false;
        handler.removeCallbacks(finish);
        handler.removeCallbacks(pulse);
        stopConstantVibration();
    }
}
