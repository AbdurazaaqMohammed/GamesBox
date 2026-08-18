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

public class KnifeHitActivity extends BaseGameActivity {

    private static final int ACCENT = UI.AMBER;
    private static final int ACCENT_D = UI.AMBER_D;

    private LinearLayout content;
    private UI.Stepper playersStepper;
    private UI.Stepper bladesStepper;
    private int players;
    private int blades;
    private int[] scores;
    private int currentPlayer;
    private boolean[] doneThisRound;

    private KnifeHitView arena;
    private boolean running;
    private TextView status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        content = UI.screen(this, "Knife Hit",
                "Throw all blades, miss the stuck ones", ACCENT);
        showSetup();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (arena != null) arena.stop();
    }

    private void showSetup() {
        content.removeAllViews();

        TextView rules = UI.text(this,
                "Tap anywhere to throw a knife into the spinning target. Each knife that lands speeds the target up - throw all of your blades without hitting a knife that's already stuck in. Hitting a stuck knife ends your turn. Most knives in wins.",
                UI.INK_SOFT, 14, false);
        rules.setPadding(0, 0, 0, UI.dp(this, 16));
        content.addView(rules);

        playersStepper = new UI.Stepper(this, "Players", 2, 8, 4, 1);
        content.addView(playersStepper.row);

        bladesStepper = new UI.Stepper(this, "Knives per player", 4, 12, 6, 1);
        content.addView(bladesStepper.row);

        content.addView(UI.space(this, 16));

        TextView start = UI.button(this, "Start Game", ACCENT, ACCENT_D, 17, 16);
        content.addView(start, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        start.setOnClickListener(v -> {
            players = playersStepper.get();
            blades = bladesStepper.get();
            scores = new int[players];
            doneThisRound = new boolean[players];
            currentPlayer = 0;
            showReady();
        });
    }

    private void showReady() {
        running = false;
        content.removeAllViews();

        TextView who = UI.text(this, playerName(currentPlayer),
                UI.INK, 18, true);
        who.setGravity(Gravity.CENTER);
        who.setPadding(0, UI.dp(this, 4), 0, UI.dp(this, 10));
        content.addView(who);

        TextView hint = UI.text(this,
                "Throw " + blades + " knives into the spinning target. Tap anywhere to throw - don't hit a knife that's already stuck!",
                UI.INK_SOFT, 14, false);
        hint.setGravity(Gravity.CENTER);
        hint.setLineSpacing(UI.dp(this, 4), 1f);
        hint.setPadding(0, 0, 0, UI.dp(this, 18));
        content.addView(hint);

        TextView go = UI.button(this, "Let's go!", ACCENT, ACCENT_D, 17, 16);
        content.addView(go, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        go.setOnClickListener(v -> runTurn());
    }

    private void runTurn() {
        running = true;
        content.removeAllViews();

        status = UI.text(this, playerName(currentPlayer) + " - tap to throw",
                ACCENT, 16, true);
        status.setGravity(Gravity.CENTER);
        status.setPadding(0, 0, 0, UI.dp(this, 8));
        content.addView(status);

        arena = new KnifeHitView(this);
        arena.setOnEnd(this::endTurn);
        content.addView(arena, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, UI.dp(this, 420)));
        arena.start(blades);
    }

    private void endTurn(boolean completed, int embedded) {
        if (!running) return;
        running = false;
        if (arena != null) arena.stop();

        scores[currentPlayer] = embedded;
        doneThisRound[currentPlayer] = true;

        content.removeAllViews();

        TextView title = UI.text(this, completed
                ? "All " + blades + " knives in!"
                : "Hit a stuck knife!", UI.INK, 26, true);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, UI.dp(this, 8), 0, UI.dp(this, 6));
        content.addView(title);

        TextView tally = UI.text(this, playerName(currentPlayer)
                + " got " + embedded + " knife"
                + (embedded == 1 ? "" : "s") + " in",
                UI.GOLD, 15, true);
        tally.setGravity(Gravity.CENTER);
        tally.setPadding(0, 0, 0, UI.dp(this, 16));
        content.addView(tally);

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
                showReady();
            }
        });
    }

    private void showStandings() {
        content.removeAllViews();

        TextView title = UI.text(this, "Knife throwers!", UI.INK, 24, true);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, UI.dp(this, 4), 0, UI.dp(this, 14));
        content.addView(title);

        List<Integer> rank = new ArrayList<>();
        for (int i = 0; i < players; i++) rank.add(i);
        java.util.Collections.sort(rank, (a, b) -> scores[b] - scores[a]);

        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < rank.size(); r++) {
            int p = rank.get(r);
            sb.append(r + 1).append(". ").append(playerName(p)).append(" - ")
                    .append(scores[p]).append(" knives");
            if (r == 0) sb.append("  \uD83D\uDD2A");
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
                scores[i] = 0;
                doneThisRound[i] = false;
            }
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

    private boolean allPlayersDone() {
        for (boolean d : doneThisRound) {
            if (!d) return false;
        }
        return true;
    }

    private String playerName(int i) {
        return PlayerNames.get(this, i);
    }

    private static final class KnifeHitView extends View {

        interface OnEnd {
            void onEnd(boolean completed, int embedded);
        }

        private final Paint rimPaint;
        private final Paint discPaint;
        private final Paint spokePaint;
        private final Paint pivotPaint;
        private final Paint bladePaint;
        private final Paint headPaint;
        private final Handler h = new Handler(Looper.getMainLooper());
        private final float[] stuck = new float[24];
        private OnEnd onEnd;
        private boolean running;
        private boolean flying;
        private int blades;
        private int embeddedCount;
        private double theta;
        private float flightT;
        private float vw, vh, cx, cy, R;

        KnifeHitView(Context c) {
            super(c);
            rimPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            rimPaint.setColor(0xFF6B4D2E);
            discPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            discPaint.setColor(0xFF8A6642);
            spokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            spokePaint.setColor(0xFF6B4D2E);
            spokePaint.setStyle(Paint.Style.STROKE);
            spokePaint.setStrokeWidth(UI.dp(c, 3));
            pivotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            pivotPaint.setColor(0xFF4A3624);
            bladePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            bladePaint.setColor(UI.INK);
            bladePaint.setStrokeWidth(UI.dp(c, 3));
            bladePaint.setStrokeCap(Paint.Cap.ROUND);
            headPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            headPaint.setColor(0xFFC9C4B8);
            headPaint.setStyle(Paint.Style.FILL);
        }

        void setOnEnd(OnEnd cb) {
            onEnd = cb;
        }

        void start(int bladeCount) {
            blades = bladeCount;
            embeddedCount = 0;
            theta = 0;
            flying = false;
            running = true;
            h.removeCallbacks(loop);
            h.postDelayed(loop, 16);
        }

        void stop() {
            running = false;
            h.removeCallbacks(loop);
        }

        @Override
        protected void onSizeChanged(int w, int h, int ow, int oh) {
            super.onSizeChanged(w, h, ow, oh);
            vw = w;
            vh = h;
            cx = w / 2f;
            cy = h * 0.40f;
            R = Math.min(w, h) * 0.20f;
            R = Math.max(UI.dp(getContext(), 52), Math.min(R, UI.dp(getContext(), 80)));
        }

        private float speed() {
            return 1.2f + embeddedCount * 0.35f;
        }

        private final Runnable loop = new Runnable() {
            public void run() {
                if (!running) return;
                theta += speed() * 0.016f;
                if (flying) {
                    flightT += 0.016f / 0.18f;
                    if (flightT >= 1f) {
                        flying = false;
                        embedKnife();
                    }
                }
                invalidate();
                h.postDelayed(this, 16);
            }
        };

        private void embedKnife() {
            float a = (float) ((-theta) % (Math.PI * 2));
            if (a < 0) a += Math.PI * 2;
            float threshold = (2f * UI.dp(getContext(), 7)) / R;
            for (int i = 0; i < embeddedCount; i++) {
                if (angleDiff(a, stuck[i]) < threshold) {
                    gameOver();
                    return;
                }
            }
            stuck[embeddedCount++] = a;
            if (embeddedCount >= blades) {
                completed();
            }
        }

        private float angleDiff(float a, float b) {
            float d = a - b;
            while (d > Math.PI) d -= Math.PI * 2;
            while (d < -Math.PI) d += Math.PI * 2;
            return Math.abs(d);
        }

        private void gameOver() {
            running = false;
            h.removeCallbacks(loop);
            if (onEnd != null) onEnd.onEnd(false, embeddedCount);
        }

        private void completed() {
            running = false;
            h.removeCallbacks(loop);
            if (onEnd != null) onEnd.onEnd(true, blades);
        }

        @Override
        public boolean onTouchEvent(MotionEvent ev) {
            if (!running || flying) return true;
            if (ev.getActionMasked() == MotionEvent.ACTION_DOWN) {
                flying = true;
                flightT = 0;
                return true;
            }
            return true;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            float discR = R * 0.92f;
            canvas.drawCircle(cx, cy, R, rimPaint);
            canvas.drawCircle(cx, cy, discR, discPaint);

            canvas.save();
            canvas.rotate((float) Math.toDegrees(theta), cx, cy);
            for (int i = 0; i < 4; i++) {
                float ang = (float) (i * Math.PI / 2);
                canvas.drawLine(cx, cy, cx + (float) Math.sin(ang) * discR,
                        cy + (float) Math.cos(ang) * discR, spokePaint);
            }
            canvas.restore();

            for (int i = 0; i < embeddedCount; i++) {
                drawStuckKnife(canvas, theta + stuck[i]);
            }

            canvas.drawCircle(cx, cy, UI.dp(getContext(), 8), pivotPaint);

            if (flying) {
                float ty = cy + R;
                float by = vh - UI.dp(getContext(), 14);
                float y = by + (ty - by) * flightT;
                drawLaunchedKnife(canvas, cx, y);
            }

            drawDisplay(canvas);
        }

        private void drawStuckKnife(Canvas canvas, double worldAngle) {
            float s = (float) Math.sin(worldAngle);
            float c = (float) Math.cos(worldAngle);
            float outer = R - UI.dp(getContext(), 6);
            float tipX = cx + s * outer;
            float tipY = cy + c * outer;
            float len = UI.dp(getContext(), 26);
            float endX = cx + s * (outer + len);
            float endY = cy + c * (outer + len);
            canvas.drawLine(tipX, tipY, endX, endY, bladePaint);
            canvas.drawCircle(endX, endY, UI.dp(getContext(), 3.5f), headPaint);
            canvas.drawCircle(endX, endY, UI.dp(getContext(), 3.5f), bladePaint);
        }

        private void drawLaunchedKnife(Canvas canvas, float x, float y) {
            float len = UI.dp(getContext(), 26);
            canvas.drawLine(x, y + len * 0.6f, x, y - len * 0.6f, bladePaint);
            canvas.drawCircle(x, y - len * 0.6f, UI.dp(getContext(), 3.5f), headPaint);
            canvas.drawCircle(x, y - len * 0.6f, UI.dp(getContext(), 3.5f), bladePaint);
        }

        private void drawDisplay(Canvas canvas) {
            int left = blades - embeddedCount - (flying ? 1 : 0);
            if (left < 0) left = 0;
            float spacing = UI.dp(getContext(), 14);
            float y = vh - UI.dp(getContext(), 34);
            float total = spacing * (left - 1);
            for (int i = 0; i < left; i++) {
                float x = cx - total / 2f + i * spacing;
                canvas.drawLine(x, y - UI.dp(getContext(), 6), x, y + UI.dp(getContext(), 6), bladePaint);
                canvas.drawCircle(x, y - UI.dp(getContext(), 6), UI.dp(getContext(), 3), headPaint);
            }
        }
    }
}
