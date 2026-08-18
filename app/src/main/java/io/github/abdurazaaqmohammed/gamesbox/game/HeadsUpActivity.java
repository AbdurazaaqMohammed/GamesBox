package io.github.abdurazaaqmohammed.gamesbox.game;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import io.github.abdurazaaqmohammed.gamesbox.ui.UI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HeadsUpActivity extends BaseGameActivity implements SensorEventListener {

    private static final int ACCENT = UI.ORANGE;
    private static final int ACCENT_D = UI.ORANGE_D;
    private static final float TILT_DEG = 22f;
    private static final float NEUTRAL_DEG = 12f;
    private static final long CALIB_MS = 600L;

    private LinearLayout content;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final List<TextView> packChips = new ArrayList<>();
    private int packIndex = 0;
    private UI.Stepper secondsStepper;
    private int seconds = 60;

    private SensorManager sensors;
    private boolean calibrated;
    private float basePitch;
    private boolean armed;
    private long calibrateStart;
    private final List<Float> samples = new ArrayList<>();

    private final List<Integer> order = new ArrayList<>();
    private final Set<String> used = new HashSet<>();
    private String word;
    private int score;
    private int skipped;
    private int remaining;
    private boolean running;

    @Override
    protected boolean isGameInProgress() {
        return running;
    }
    private TextView timerText;
    private TextView wordText;
    private TextView scoreBadge;
    private TextView countText;
    private int countVal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        sensors = (SensorManager) getSystemService(SENSOR_SERVICE);
        content = UI.screen(this, "Heads Up",
                "Guess the word on your forehead", ACCENT);
        showSetup();
    }

    private void showSetup() {
        running = false;
        calibrated = false;
        content.removeAllViews();

        TextView rules = UI.text(this,
                "One player holds the phone to their forehead, screen facing the group. "
                        + "Everyone shouts clues, sings, or acts out the word on screen. "
                        + "Tilt the phone down when they get it right, tilt it up to skip.",
                UI.INK_SOFT, 14, false);
        rules.setPadding(0, 0, 0, UI.dp(this, 14));
        content.addView(rules);

        TextView packLabel = UI.text(this, "Pack", UI.INK_SOFT, 12, true);
        content.addView(packLabel);

        LinearLayout chipsRow = UI.row(this);
        chipsRow.setGravity(Gravity.CENTER_HORIZONTAL);
        for (int i = 0; i < Data.PACK_NAMES.length; i++) {
            final int idx = i;
            TextView chip = UI.chipGhost(this, Data.PACK_NAMES[i], UI.INK, 13);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            lp.setMargins(UI.dp(this, 2), UI.dp(this, 3), UI.dp(this, 2), UI.dp(this, 3));
            chipsRow.addView(chip, lp);
            if (i % 2 == 1) {
                content.addView(chipsRow);
                chipsRow = UI.row(this);
                chipsRow.setGravity(Gravity.CENTER_HORIZONTAL);
            }
            chip.setOnClickListener(v -> {
                packIndex = idx;
                refreshChips();
            });
            packChips.add(chip);
        }
        content.addView(chipsRow);
        refreshChips();

        secondsStepper = new UI.Stepper(this, "Seconds", 30, 120, 60, 10);
        content.addView(secondsStepper.row);

        content.addView(UI.space(this, 16));

        TextView start = UI.button(this, "Start Round", ACCENT, ACCENT_D, 17, 16);
        content.addView(start, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        start.setOnClickListener(v -> {
            seconds = secondsStepper.get();
            startRound();
        });
    }

    private void refreshChips() {
        for (int i = 0; i < packChips.size(); i++) {
            packChips.get(i).setTextColor(i == packIndex ? UI.INK : UI.withAlpha(UI.INK, 120));
            packChips.get(i).setBackgroundDrawable(UI.stroke(
                    i == packIndex ? ACCENT : UI.withAlpha(UI.INK, 60), 40, this));
            packChips.get(i).setText("★ " + Data.PACK_NAMES[i]);
        }
    }

    private void startRound() {
        score = 0;
        skipped = 0;
        used.clear();
        nextWord();
        showGetReady();
    }

    private void showGetReady() {
        content.removeAllViews();
        running = false;
        calibrated = false;
        samples.clear();
        countVal = 3;

        TextView head = UI.text(this, "Heads up!", ACCENT, 26, true);
        head.setGravity(Gravity.CENTER);
        content.addView(head);

        TextView pack = UI.text(this, Data.PACK_NAMES[packIndex],
                UI.INK_SOFT, 14, true);
        pack.setGravity(Gravity.CENTER);
        pack.setPadding(0, UI.dp(this, 4), 0, 0);
        content.addView(pack);

        TextView how = UI.text(this, "Hold the phone to your forehead.\n"
                        + "Friends shout clues — you guess the word on screen.",
                UI.INK_SOFT, 15, false);
        how.setGravity(Gravity.CENTER);
        how.setPadding(0, UI.dp(this, 10), 0, UI.dp(this, 18));
        content.addView(how);

        countText = UI.text(this, "3", UI.INK, 84, true);
        countText.setGravity(Gravity.CENTER);
        content.addView(countText);

        TextView hint = UI.text(this, "Tilt down when they get it right, tilt up to skip",
                UI.INK_SOFT, 13, false);
        hint.setGravity(Gravity.CENTER);
        hint.setPadding(0, UI.dp(this, 16), 0, 0);
        content.addView(hint);

        handler.postDelayed(countdown, 1000);
    }

    private final Runnable countdown = new Runnable() {
        public void run() {
            countVal--;
            if (countVal <= 0) {
                startPlay();
                return;
            }
            countText.setText(String.valueOf(countVal));
            handler.postDelayed(this, 1000);
        }
    };

    private void startPlay() {
        running = true;
        remaining = seconds;
        content.removeAllViews();

        LinearLayout top = UI.row(this);
        top.setPadding(0, 0, 0, UI.dp(this, 10));
        timerText = UI.text(this, String.valueOf(remaining), UI.INK, 34, true);
        top.addView(timerText, new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        scoreBadge = UI.chip(this, "Score 0", ACCENT, 14);
        top.addView(scoreBadge);
        content.addView(top);

        wordText = UI.card(this, word, UI.INK, 36);
        content.addView(wordText, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView hints = UI.text(this, "↓ Tilt down: got it     ↑ Tilt up: skip",
                UI.INK_SOFT, 13, false);
        hints.setGravity(Gravity.CENTER);
        hints.setPadding(0, UI.dp(this, 14), 0, UI.dp(this, 10));
        content.addView(hints);

        LinearLayout btns = UI.row(this);
        LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        blp.setMargins(UI.dp(this, 2), 0, UI.dp(this, 2), 0);
        TextView ok = UI.ghost(this, "Got it", 14, 11);
        btns.addView(ok, blp);
        ok.setOnClickListener(v -> onCorrect());
        TextView sk = UI.ghost(this, "Skip", 14, 11);
        btns.addView(sk, blp);
        sk.setOnClickListener(v -> onSkip());
        content.addView(btns);

        UI.popIn(wordText, 220);

        calibrated = false;
        armed = true;
        samples.clear();
        calibrateStart = System.currentTimeMillis();
        handler.postDelayed(calibrateDone, CALIB_MS);
        handler.postDelayed(tick, 1000);
    }

    private final Runnable calibrateDone = new Runnable() {
        public void run() {
            basePitch = mean();
            calibrated = true;
            armed = true;
        }
    };

    private final Runnable tick = new Runnable() {
        public void run() {
            if (!running) return;
            remaining--;
            if (timerText != null) {
                timerText.setText(String.valueOf(remaining));
                timerText.setTextColor(remaining <= 5 ? UI.RED : UI.INK);
            }
            if (remaining <= 0) {
                timeUp();
                return;
            }
            handler.postDelayed(this, 1000);
        }
    };

    private void onCorrect() {
        if (!running) return;
        score++;
        nextWord();
        updateWord();
    }

    private void onSkip() {
        if (!running) return;
        skipped++;
        nextWord();
        updateWord();
    }

    private void updateWord() {
        wordText.setText(word);
        UI.popIn(wordText, 180);
        scoreBadge.setText("Score " + score);
    }

    private void timeUp() {
        running = false;
        buzz(600);
        content.removeAllViews();

        TextView up = UI.text(this, "Time's up!", ACCENT, 30, true);
        up.setGravity(Gravity.CENTER);
        content.addView(up);

        TextView scoreBig = UI.text(this, String.valueOf(score), UI.INK, 76, true);
        scoreBig.setGravity(Gravity.CENTER);
        scoreBig.setPadding(0, UI.dp(this, 6), 0, 0);
        content.addView(scoreBig);

        TextView detail = UI.text(this, "Correct: " + score + "    Skipped: " + skipped,
                UI.INK_SOFT, 16, false);
        detail.setGravity(Gravity.CENTER);
        detail.setPadding(0, UI.dp(this, 6), 0, UI.dp(this, 20));
        content.addView(detail);

        TextView again = UI.button(this, "Play Again", ACCENT, ACCENT_D, 17, 16);
        content.addView(again, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        again.setOnClickListener(v -> startRound());

        TextView change = UI.ghost(this, "Change Pack", 15, 13);
        LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        clp.topMargin = UI.dp(this, 10);
        content.addView(change, clp);
        change.setOnClickListener(v -> showSetup());
    }

    private void nextWord() {
        String[] pack = Data.IMPOSTER_PACKS[packIndex];
        order.clear();
        for (int i = 0; i < pack.length; i++) {
            if (!used.contains(pack[i])) order.add(i);
        }
        if (order.isEmpty()) {
            used.clear();
            for (int i = 0; i < pack.length; i++) order.add(i);
        }
        Collections.shuffle(order, UI.RND);
        word = pack[order.get(0)];
        used.add(word);
    }

    private float mean() {
        if (samples.isEmpty()) return 0;
        float s = 0;
        for (float f : samples) s += f;
        return s / samples.size();
    }

    private void buzz(long ms) {
        Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        if (v != null) {
            v.vibrate(ms);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sensors != null) {
            sensors.registerListener(this,
                    sensors.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                    SensorManager.SENSOR_DELAY_GAME);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sensors != null) {
            sensors.unregisterListener(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        running = false;
        handler.removeCallbacks(tick);
        handler.removeCallbacks(countdown);
        handler.removeCallbacks(calibrateDone);
        if (sensors != null) {
            sensors.unregisterListener(this);
        }
    }

    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) return;
        float gy = event.values[1];
        float gz = event.values[2];
        float pitch = (float) Math.toDegrees(Math.atan2(gz, -gy));
        if (!calibrated) {
            if (System.currentTimeMillis() - calibrateStart < CALIB_MS) {
                samples.add(pitch);
            }
            return;
        }
        if (!running) return;
        float delta = pitch - basePitch;
        if (delta > -NEUTRAL_DEG && delta < NEUTRAL_DEG) {
            armed = true;
        } else if (delta > TILT_DEG && armed) {
            armed = false;
            onCorrect();
        } else if (delta < -TILT_DEG && armed) {
            armed = false;
            onSkip();
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
