package io.github.abdurazaaqmohammed.gamesbox.game;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
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

public class SpikeArenaActivity extends BaseGameActivity {

    private static final int ACCENT = UI.ORANGE;
    private static final int ACCENT_D = UI.ORANGE_D;
    private static final int ROUND_SECONDS = 30;

    private LinearLayout content;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private UI.Stepper playersStepper;
    private int players;
    private int[] bestScores;
    private int currentPlayer;
    private boolean[] doneThisRound;

    private SpikeArenaView arena;
    private int remaining;
    private boolean running;

    @Override
    protected boolean isGameInProgress() {
        return running;
    }

    private TextView timeText;
    private TextView scoreText;

    private String playerName(int i) {
        return PlayerNames.get(this, i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        content = UI.screen(this, "Don't Touch the Spikes",
                "Flap the bird, dodge the spikes", ACCENT);
        showSetup();
    }

    private void showSetup() {
        running = false;
        handler.removeCallbacks(tick);
        content.removeAllViews();

        TextView rules = UI.text(this,
                "The bird flies left and right between two spiked walls, bouncing off them. Tap to flap up, let go to drop. Hit a wall's spike and your run is over - the spikes move to a new spot every time. Survive as long as you can, up to " + ROUND_SECONDS + " seconds. Each player's best run counts.",
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
            bestScores = new int[players];
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
                "Tap to flap, keep the bird clear of the spikes! Survive as long as you can.",
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
        remaining = ROUND_SECONDS;
        content.removeAllViews();

        LinearLayout status = UI.row(this);
        status.setGravity(Gravity.CENTER_HORIZONTAL);
        content.addView(status, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        timeText = UI.chip(this, "Target " + remaining, UI.RED, 14);
        scoreText = UI.chip(this, "Time 0s", UI.MINT, 14);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        lp.setMargins(UI.dp(this, 4), 0, UI.dp(this, 4), 0);
        status.addView(timeText, lp);
        status.addView(scoreText, lp);

        arena = new SpikeArenaView(this);
        arena.setOnDeath(() -> endTurn(arena.getScore()));
        LinearLayout.LayoutParams alp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, UI.dp(this, 360));
        alp.topMargin = UI.dp(this, 10);
        content.addView(arena, alp);

        content.addView(UI.space(this, 10));

        TextView done = UI.ghost(this, "Give up early", 14, 13);
        LinearLayout.LayoutParams dlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        content.addView(done, dlp);
        done.setOnClickListener(v -> endTurn(arena.getScore()));

        arena.start();
        handler.removeCallbacks(tick);
        handler.postDelayed(tick, 1000);
    }

    private final Runnable tick = new Runnable() {
        public void run() {
            if (!running) return;
            remaining--;
            if (remaining > 0) {
                timeText.setText("Target " + remaining);
                scoreText.setText("Time " + (ROUND_SECONDS - remaining) + "s");
                if (remaining <= 5) timeText.setTextColor(UI.RED);
                handler.postDelayed(this, 1000);
            } else {
                endTurn(ROUND_SECONDS);
            }
        }
    };

    private void endTurn(int score) {
        if (!running) return;
        running = false;
        handler.removeCallbacks(tick);
        if (arena != null) arena.stop();

        if (score > bestScores[currentPlayer]) {
            bestScores[currentPlayer] = score;
        }
        doneThisRound[currentPlayer] = true;

        content.removeAllViews();

        TextView title = UI.text(this, score + " seconds",
                UI.INK, 28, true);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, UI.dp(this, 8), 0, UI.dp(this, 6));
        content.addView(title);

        TextView tally = UI.text(this, tally(), UI.GOLD, 14, true);
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

        TextView title = UI.text(this, "Spike dodgers!", UI.INK, 24, true);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, UI.dp(this, 4), 0, UI.dp(this, 14));
        content.addView(title);

        List<Integer> rank = new ArrayList<>();
        for (int i = 0; i < players; i++) rank.add(i);
        java.util.Collections.sort(rank, (a, b) -> bestScores[b] - bestScores[a]);

        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < rank.size(); r++) {
            int p = rank.get(r);
            sb.append(r + 1).append(". ").append(playerName(p)).append(" - ")
                    .append(bestScores[p]).append("s");
            if (r == 0) sb.append("  🐦");
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

    private boolean allPlayersDone() {
        for (boolean d : doneThisRound) {
            if (!d) return false;
        }
        return true;
    }

    private String tally() {
        StringBuilder sb = new StringBuilder("Best   ");
        for (int i = 0; i < players; i++) {
            sb.append("P").append(i + 1).append(": ").append(bestScores[i]).append("s  ");
        }
        return sb.toString();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        running = false;
        handler.removeCallbacks(tick);
        if (arena != null) arena.stop();
    }

    private static final class SpikeArenaView extends View {

        private static final int MAX_SPIKES_PER_WALL = 5;

        private final Paint skyPaint;
        private final Paint groundPaint;
        private final Paint wallPaint;
        private final Paint spikePaint;
        private final Paint birdPaint;
        private final Paint eyePaint;
        private final Handler h = new Handler(Looper.getMainLooper());
        private final float[] leftYs = new float[MAX_SPIKES_PER_WALL];
        private final float[] rightYs = new float[MAX_SPIKES_PER_WALL];
        private int leftCount;
        private int rightCount;
private boolean running;
        private boolean wallsInit;
        private boolean counting;
        private int count;
        private float bx, by, vx, vy;
        private double t;
        private int score;
        private Runnable onDeath;
        private float vw, vh;
        private final Paint countPaint;
        private final Paint countSubPaint;

        SpikeArenaView(Context c) {
            super(c);
            skyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            skyPaint.setColor(0xFFDCEAF7);
            groundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            groundPaint.setColor(0xFF8BB96B);
            wallPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            wallPaint.setColor(0xFF2E3440);
            spikePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            spikePaint.setColor(UI.RED);
            birdPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            birdPaint.setColor(UI.GOLD);
            eyePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            eyePaint.setColor(0xFF2E2320);
            countPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            countPaint.setColor(Color.BLACK);
            countPaint.setTextSize(UI.dp(c, 56));
            countPaint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
            countPaint.setTextAlign(Paint.Align.CENTER);
            countSubPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            countSubPaint.setColor(UI.INK_SOFT);
            countSubPaint.setTextSize(UI.dp(c, 15));
            countSubPaint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
            countSubPaint.setTextAlign(Paint.Align.CENTER);
        }

        void setOnDeath(Runnable r) {
            onDeath = r;
        }

        int getScore() {
            return score;
        }

        @Override
        protected void onSizeChanged(int w, int h, int ow, int oh) {
            super.onSizeChanged(w, h, ow, oh);
            vw = w;
            vh = h;
            if (running && !wallsInit && vh > 0) {
                bx = vw * 0.5f;
                by = vh * 0.5f;
                leftCount = randCount();
                rightCount = randCount();
                randomizeWall(leftYs, leftCount, by);
                randomizeWall(rightYs, rightCount, by);
                wallsInit = true;
            }
        }

        void start() {
            if (running) return;
            running = true;
            counting = true;
            count = 3;
            t = 0;
            score = 0;
            bx = vw * 0.5f;
            by = vh * 0.5f;
            vx = 1;
            vy = 0;
            wallsInit = false;
            h.removeCallbacks(loop);
            h.removeCallbacks(countdown);
            h.postDelayed(countdown, 1000);
            h.postDelayed(loop, 16);
        }

        void stop() {
            running = false;
            h.removeCallbacks(loop);
            h.removeCallbacks(countdown);
        }

        private final Runnable countdown = new Runnable() {
            public void run() {
                if (!running) return;
                count--;
                if (count > 0) {
                    h.postDelayed(this, 1000);
                } else {
                    counting = false;
                }
                invalidate();
            }
        };

        private float lx() {
            return vw * 0.10f;
        }

        private float rx() {
            return vw * 0.90f;
        }

        private float spikeLen() {
            return UI.dp(getContext(), 28);
        }

        private float thick() {
            return UI.dp(getContext(), 12);
        }

        private float spikeH() {
            return UI.dp(getContext(), 22);
        }

        private int randCount() {
            return 3 + UI.RND.nextInt(3);
        }

        private void randomizeWall(float[] ys, int count, float avoidY) {
            if (vh <= 0) return;
            float lo = UI.dp(getContext(), 34);
            float hi = vh - UI.dp(getContext(), 34);
            float gap = UI.dp(getContext(), 20);
            float birdMargin = UI.dp(getContext(), 26);
            for (int i = 0; i < count; i++) {
                float y;
                int tries = 0;
                do {
                    y = lo + UI.RND.nextFloat() * (hi - lo);
                    tries++;
                    if (tries > 200) break;
                } while (Math.abs(y - avoidY) < birdMargin || tooClose(ys, i, y, gap));
                ys[i] = y;
            }
        }

        private boolean tooClose(float[] ys, int upTo, float y, float gap) {
            for (int i = 0; i < upTo; i++) {
                if (Math.abs(ys[i] - y) < gap) return true;
            }
            return false;
        }

        private boolean spikeHits(float[] ys, int count, boolean pointingRight) {
            float wallX = pointingRight ? lx() : rx();
            float len = spikeLen();
            float th = thick();
            float x1 = pointingRight ? wallX : wallX - len;
            float x2 = pointingRight ? wallX + len : wallX;
            float rb = UI.dp(getContext(), 9);
            for (int i = 0; i < count; i++) {
                if (circleHitsRect(bx, by, rb, x1, x2, ys[i] - th / 2f, ys[i] + th / 2f)) {
                    return true;
                }
            }
            return false;
        }

        private final Runnable loop = new Runnable() {
            public void run() {
                if (!running) return;
                if (counting) {
                    invalidate();
                    h.postDelayed(this, 16);
                    return;
                }
                t += 0.016;
                score = (int) t;

                float dt = 0.016f;
                float rb = UI.dp(getContext(), 9);

                vy += vh * 3.0f * dt;
                by += vy * dt;

                boolean dead = false;
                float spikeH = spikeH();
                if (by - rb < spikeH) {
                    dead = true;
                } else if (by + rb > vh - spikeH) {
                    dead = true;
                }

                bx += vx * vw * 1.2f * dt;
                if (bx - rb < lx()) {
                    if (spikeHits(leftYs, leftCount, true)) {
                        dead = true;
                    } else {
                        bx = lx() + rb;
                        vx = 1;
                        leftCount = randCount();
                        randomizeWall(leftYs, leftCount, by);
                    }
                }
                if (bx + rb > rx()) {
                    if (spikeHits(rightYs, rightCount, false)) {
                        dead = true;
                    } else {
                        bx = rx() - rb;
                        vx = -1;
                        rightCount = randCount();
                        randomizeWall(rightYs, rightCount, by);
                    }
                }

                invalidate();
                if (dead) {
                    running = false;
                    if (onDeath != null) onDeath.run();
                    return;
                }
                h.postDelayed(this, 16);
            }
        };

        private float clamp(float v, float lo, float hi) {
            return Math.max(lo, Math.min(hi, v));
        }

        private boolean circleHitsRect(float cx, float cy, float r, float x1, float x2, float y1, float y2) {
            float nx = clamp(cx, x1, x2);
            float ny = clamp(cy, y1, y2);
            float dx = cx - nx;
            float dy = cy - ny;
            return dx * dx + dy * dy <= r * r;
        }

        @Override
        public boolean onTouchEvent(MotionEvent ev) {
            if (!running || counting) return true;
            if (ev.getActionMasked() == MotionEvent.ACTION_DOWN) {
                vy = -vh * 0.8f;
                invalidate();
                return true;
            }
            return true;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawRect(0, 0, vw, vh, skyPaint);

            float lx = lx();
            float rx = rx();
            float len = spikeLen();
            float th = thick();

            canvas.drawRect(0, 0, lx, vh, wallPaint);
            canvas.drawRect(rx, 0, vw, vh, wallPaint);

            for (int i = 0; i < leftCount; i++) {
                drawSpike(canvas, lx, leftYs[i], th, len, true);
            }
            for (int i = 0; i < rightCount; i++) {
                drawSpike(canvas, rx, rightYs[i], th, len, false);
            }

            drawSpikeRow(canvas, 0, true);
            drawSpikeRow(canvas, vh, false);

            float rb = UI.dp(getContext(), 9);
            canvas.drawCircle(bx, by, rb, birdPaint);
            canvas.drawCircle(bx + rb * 0.35f, by - rb * 0.2f, rb * 0.22f, eyePaint);
            float beak = vx >= 0 ? rb : -rb;
            android.graphics.Path bp = new android.graphics.Path();
            bp.moveTo(bx + beak, by);
            bp.lineTo(bx + beak * 0.2f, by - rb * 0.35f);
            bp.lineTo(bx + beak * 0.2f, by + rb * 0.35f);
            bp.close();
            canvas.drawPath(bp, eyePaint);

            if (counting) {
                canvas.drawText("Get ready", vw / 2f, vh / 2f - UI.dp(getContext(), 46), countSubPaint);
                canvas.drawText(String.valueOf(count), vw / 2f, vh / 2f + UI.dp(getContext(), 26), countPaint);
            }
        }

        private void drawSpikeRow(Canvas canvas, float y, boolean pointingDown) {
            float w = UI.dp(getContext(), 16);
            float h = spikeH();
            float half = w / 2f;
            for (float x = -w; x <= vw; x += w) {
                android.graphics.Path p = new android.graphics.Path();
                if (pointingDown) {
                    p.moveTo(x, y);
                    p.lineTo(x + half, y + h);
                    p.lineTo(x + w, y);
                } else {
                    p.moveTo(x, y);
                    p.lineTo(x + half, y - h);
                    p.lineTo(x + w, y);
                }
                p.close();
                canvas.drawPath(p, spikePaint);
            }
        }

        private void drawSpike(Canvas canvas, float wallX, float y, float thick, float len, boolean pointingRight) {
            float half = thick / 2f;
            if (pointingRight) {
                android.graphics.Path p = new android.graphics.Path();
                p.moveTo(wallX, y - half);
                p.lineTo(wallX + len, y);
                p.lineTo(wallX, y + half);
                p.close();
                canvas.drawPath(p, spikePaint);
            } else {
                android.graphics.Path p = new android.graphics.Path();
                p.moveTo(wallX, y - half);
                p.lineTo(wallX - len, y);
                p.lineTo(wallX, y + half);
                p.close();
                canvas.drawPath(p, spikePaint);
            }
        }
    }
}
