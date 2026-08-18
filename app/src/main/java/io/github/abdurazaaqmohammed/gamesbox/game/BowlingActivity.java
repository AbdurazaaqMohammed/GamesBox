package io.github.abdurazaaqmohammed.gamesbox.game;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import io.github.abdurazaaqmohammed.gamesbox.PlayerNames;
import io.github.abdurazaaqmohammed.gamesbox.ui.UI;

import java.util.ArrayList;
import java.util.List;

public class BowlingActivity extends BaseGameActivity {

    private static final int ACCENT = UI.CYAN;
    private static final int ACCENT_D = UI.CYAN_D;

    private LinearLayout content;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private UI.Stepper playersStepper;
    private int players;
    private int[] totals;
    private int currentPlayer;
    private boolean[] doneThisRound;

    private BowlingLaneView lane;
    private int rollsUsed;
    private int turnPins;
    private TextView status;
    private boolean rolling;

    private String playerName(int i) {
        return PlayerNames.get(this, i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        content = UI.screen(this, "Bowling",
                "Roll two balls, knock down pins", ACCENT);
        showSetup();
    }

    private void showSetup() {
        content.removeAllViews();

        TextView rules = UI.text(this,
                "Swipe on the lane to aim, then release to roll - the guide line shows where the ball will go. Two rolls per player, knock down as many pins as you can. Highest total wins.",
                UI.INK_SOFT, 14, false);
        rules.setPadding(0, 0, 0, UI.dp(this, 16));
        content.addView(rules);

        playersStepper = new UI.Stepper(this, "Players", 2, 8, 4, 1);
        content.addView(playersStepper.row);

        content.addView(UI.space(this, 16));

        TextView start = UI.button(this, "Start Game", ACCENT, ACCENT_D, 17, 16);
        content.addView(start, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        start.setOnClickListener(v -> {
            players = playersStepper.get();
            totals = new int[players];
            doneThisRound = new boolean[players];
            currentPlayer = 0;
            showTurn();
        });
    }

    private void showTurn() {
        rollsUsed = 0;
        turnPins = 0;
        rolling = false;
        content.removeAllViews();

        TextView who = UI.text(this, playerName(currentPlayer) + " - your turn",
                UI.INK, 16, true);
        who.setGravity(Gravity.CENTER);
        who.setPadding(0, UI.dp(this, 2), 0, UI.dp(this, 8));
        content.addView(who);

        status = UI.text(this, "Roll 1 of 2 - swipe to aim, release to bowl!",
                ACCENT, 16, true);
        status.setGravity(Gravity.CENTER);
        status.setPadding(0, 0, 0, UI.dp(this, 8));
        content.addView(status);

        lane = new BowlingLaneView(this);
        lane.setOnRoll(this::rollDone);
        content.addView(lane, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, UI.dp(this, 360)));

        TextView hint = UI.text(this, "Drag left or right to aim, release to roll. A quick flick works too.",
                UI.INK_SOFT, 13, false);
        hint.setGravity(Gravity.CENTER);
        hint.setPadding(0, UI.dp(this, 10), 0, 0);
        content.addView(hint);
    }

    private void rollDone(int knocked) {
        rolling = false;
        rollsUsed++;
        turnPins += knocked;

        if (knocked == 10) {
            lane.setLocked(true);
            content.removeAllViews();
            showRollResult(true, true);
            return;
        }

        if (rollsUsed >= 2) {
            lane.setLocked(true);
            content.removeAllViews();
            showRollResult(true, false);
        } else {
            status.setText("You knocked " + knocked + " down! Roll 2 - swipe to bowl.");
        }
    }

    private void showRollResult(boolean done, boolean strike) {
        TextView title = UI.text(this,
                strike ? "STRIKE!" : "Rolls done!",
                strike ? UI.GOLD : UI.INK, 26, true);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, UI.dp(this, 8), 0, UI.dp(this, 6));
        content.addView(title);

        TextView result = UI.text(this,
                playerName(currentPlayer) + " knocked down " + turnPins + " pins",
                UI.INK, 18, true);
        result.setGravity(Gravity.CENTER);
        result.setPadding(0, UI.dp(this, 6), 0, UI.dp(this, 10));
        content.addView(result);

        totals[currentPlayer] += turnPins;
        doneThisRound[currentPlayer] = true;

        TextView score = UI.text(this, "Total: " + totals[currentPlayer], UI.GOLD, 15, true);
        score.setGravity(Gravity.CENTER);
        score.setPadding(0, 0, 0, UI.dp(this, 16));
        content.addView(score);

        final boolean allDone = allPlayersDone();
        TextView next = UI.button(this, allDone ? "See standings" : "Next Player",
                ACCENT, ACCENT_D, 16, 15);
        content.addView(next, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        next.setOnClickListener(v -> {
            if (allDone) {
                showStandings();
            } else {
                currentPlayer = (currentPlayer + 1) % players;
                showTurn();
            }
        });
    }

    private void showStandings() {
        content.removeAllViews();

        TextView title = UI.text(this, "Bowling night champions!", UI.INK, 24, true);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, UI.dp(this, 4), 0, UI.dp(this, 14));
        content.addView(title);

        List<Integer> rank = new ArrayList<>();
        for (int i = 0; i < players; i++) rank.add(i);
        java.util.Collections.sort(rank, (a, b) -> totals[b] - totals[a]);

        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < rank.size(); r++) {
            int p = rank.get(r);
            sb.append(r + 1).append(". ").append(playerName(p)).append(" - ")
                    .append(totals[p]).append(" pins");
            if (r == 0) sb.append("  🎳");
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
            for (int i = 0; i < players; i++) {
                doneThisRound[i] = false;
                totals[i] = 0;
            }
            currentPlayer = 0;
            showTurn();
        });

        TextView setup = UI.ghost(this, "Back to Setup", 15, 14);
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        slp.topMargin = UI.dp(this, 10);
        content.addView(setup, slp);
        setup.setOnClickListener(v -> showSetup());
    }

    private boolean allPlayersDone() {
        for (boolean d : doneThisRound) {
            if (!d) return false;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (lane != null) lane.stopAnim();
    }

    private static final class BowlingLaneView extends View {

        interface OnRoll {
            void onRollComplete(int knocked);
        }

        private final Paint lanePaint;
        private final Paint gutterPaint;
        private final Paint pinPaint;
        private final Paint pinDownPaint;
        private final Paint ballPaint;
        private final Paint guidePaint;
        private final Paint guideDotPaint;
        private final Handler h = new Handler(Looper.getMainLooper());
        private final boolean[] standing = new boolean[10];
        private final float[][] pins = new float[10][2];
        private boolean animating;
        private float ballX, ballY;
        private OnRoll onRoll;
        private boolean enabled = true;
        private boolean dragging;
        private float aimX;
        private float dragStartX, dragStartY;

        BowlingLaneView(Context c) {
            super(c);
            lanePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            lanePaint.setColor(0xFFD9B88C);
            gutterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            gutterPaint.setColor(0xFF2E3440);
            pinPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            pinPaint.setColor(0xFFFFFFFF);
            pinDownPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            pinDownPaint.setColor(0xFF2E3440);
            pinDownPaint.setStyle(Paint.Style.STROKE);
            pinDownPaint.setStrokeWidth(UI.dp(c, 2));
            ballPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            ballPaint.setColor(UI.INK);
            guidePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            guidePaint.setColor(0xFFFFFFFF);
            guidePaint.setStyle(Paint.Style.STROKE);
            guidePaint.setStrokeWidth(UI.dp(c, 3));
            guideDotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            guideDotPaint.setColor(0xFFFFFFFF);
            aimX = -1f;
        }

        void setOnRoll(OnRoll cb) {
            onRoll = cb;
        }

        void setLocked(boolean e) {
            enabled = !e;
        }

        void stopAnim() {
            animating = false;
            h.removeCallbacksAndMessages(null);
        }

        private void resetPins() {
            float w = getWidth();
            float topY = getHeight() * 0.10f;
            float pinGap = UI.dp(getContext(), 26);
            float rowGap = UI.dp(getContext(), 22);
            int idx = 0;
            for (int row = 0; row < 4; row++) {
                int count = 4 - row;
                float y = topY + row * rowGap;
                for (int i = 0; i < count; i++) {
                    float x = w / 2f + (i - (count - 1) / 2f) * pinGap;
                    pins[idx][0] = x;
                    pins[idx][1] = y;
                    standing[idx] = true;
                    idx++;
                }
            }
        }

        @Override
        protected void onSizeChanged(int w, int h, int ow, int oh) {
            super.onSizeChanged(w, h, ow, oh);
            resetPins();
            ballX = w / 2f;
            ballY = h - UI.dp(getContext(), 22);
        }

        @Override
        public boolean onTouchEvent(MotionEvent ev) {
            if (!enabled || animating) return true;
            switch (ev.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    dragging = true;
                    dragStartX = ev.getX();
                    dragStartY = ev.getY();
                    aimX = getWidth() / 2f;
                    invalidate();
                    return true;
                case MotionEvent.ACTION_MOVE:
                    if (dragging) {
                        float w = getWidth();
                        float maxAim = w * 0.34f;
                        float c = w / 2f;
                        aimX = c + Math.max(-maxAim, Math.min(maxAim, ev.getX() - c));
                        invalidate();
                    }
                    return true;
                case MotionEvent.ACTION_UP:
                    if (dragging) {
                        dragging = false;
                        float dist = Math.abs(ev.getX() - dragStartX) + Math.abs(ev.getY() - dragStartY);
                        if (dist >= UI.dp(getContext(), 16)) {
                            roll(aimX);
                        } else {
                            aimX = -1f;
                            invalidate();
                        }
                    }
                    return true;
            }
            return true;
        }

        private void roll(float releaseX) {
            animating = true;
            float w = getWidth();
            float vh = getHeight();
            float center = w / 2f;
            float maxAim = w * 0.34f;
            float target = center + Math.max(-maxAim, Math.min(maxAim, releaseX - center));
            float startX = center;
            float startY = vh - UI.dp(getContext(), 22);
            float endY = vh * 0.10f;

            final int frames = 26;
            final float dx = target - startX;
            final float dy = endY - startY;
            final float ballR = UI.dp(getContext(), 14);
            final float pinR = UI.dp(getContext(), 10);
            final int[] knocked = new int[]{0};
            h.postDelayed(new Runnable() {
                int f = 0;
                public void run() {
                    if (!animating) return;
                    f++;
                    float p = f / (float) frames;
                    ballX = startX + dx * p;
                    ballY = startY + dy * p;
                    for (int i = 0; i < 10; i++) {
                        if (standing[i] && ballY <= pins[i][1] + pinR) {
                            if (Math.abs(ballX - pins[i][0]) < ballR + pinR) {
                                standing[i] = false;
                                knocked[0]++;
                            }
                        }
                    }
                    invalidate();
                    if (f >= frames) {
                        animating = false;
                        ballX = startX;
                        ballY = startY;
                        if (onRoll != null) onRoll.onRollComplete(knocked[0]);
                    } else {
                        h.postDelayed(this, 16);
                    }
                }
            }, 16);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            float w = getWidth();
            float h = getHeight();
            float side = w * 0.06f;

            canvas.drawRect(0, 0, w, h, lanePaint);
            canvas.drawRect(0, 0, side, h, gutterPaint);
            canvas.drawRect(w - side, 0, w, h, gutterPaint);

            float pinR = UI.dp(getContext(), 10);
            for (int i = 0; i < 10; i++) {
                if (standing[i]) {
                    canvas.drawCircle(pins[i][0], pins[i][1], pinR, pinPaint);
                    canvas.drawCircle(pins[i][0], pins[i][1], pinR, pinDownPaint);
                }
            }

            float ballR = UI.dp(getContext(), 14);
            canvas.drawCircle(ballX, ballY, ballR, ballPaint);

            if (dragging && aimX >= 0) {
                float startX = w / 2f;
                float startY = h - UI.dp(getContext(), 22);
                float endY = h * 0.10f;
                float gdx = aimX - startX;
                float gdy = endY - startY;
                float gl = (float) Math.sqrt(gdx * gdx + gdy * gdy);
                if (gl > 0.1f) {
                    float len = UI.dp(getContext(), 56);
                    float tx = ballX + gdx / gl * len;
                    float ty = ballY + gdy / gl * len;
                    canvas.drawLine(ballX, ballY, tx, ty, guidePaint);
                    canvas.drawCircle(tx, ty, UI.dp(getContext(), 4), guideDotPaint);
                }
            }
        }
    }
}
