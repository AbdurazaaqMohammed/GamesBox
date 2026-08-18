package io.github.abdurazaaqmohammed.gamesbox.game;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import io.github.abdurazaaqmohammed.gamesbox.HiScores;
import io.github.abdurazaaqmohammed.gamesbox.ui.UI;

import java.util.ArrayList;
import java.util.List;

public class FlappyActivity extends BaseGameActivity {

    private static final int ACCENT = UI.SKY;
    private static final int ACCENT_D = UI.SKY_D;
    private static final String HS_KEY = "flappy";

    private LinearLayout content;
    private UI.Toggle diffToggle;
    private FlappyView view;
    private TextView scoreText;
    private TextView bestText;
    private boolean running;

    @Override
    protected boolean isGameInProgress() {
        return running;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        content = UI.screen(this, "Flappy Bird", "Tap to flap, dodge the pipes", ACCENT);
        showSetup();
    }

    private void showSetup() {
        running = false;
        if (view != null) view.stop();
        content.removeAllViews();

        TextView rules = UI.text(this,
                "Tap to flap your bird up, let go and it drops. Fly through the gaps in the pipes - each pipe you pass is a point. Hit a pipe or the ground and the run is over.",
                UI.INK_SOFT, 14, false);
        rules.setPadding(0, 0, 0, UI.dp(this, 16));
        content.addView(rules);

        diffToggle = new UI.Toggle(this, "Difficulty", new String[]{"Easy", "Normal", "Hard"}, ACCENT, 1);
        content.addView(diffToggle.row);

        content.addView(UI.space(this, 16));

        TextView start = UI.button(this, "Start Game", ACCENT, ACCENT_D, 17, 16);
        content.addView(start, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        start.setOnClickListener(v -> startGame(diffToggle.get()));
    }

    private void startGame(int diff) {
        running = true;
        content.removeAllViews();

        LinearLayout status = UI.row(this);
        status.setGravity(Gravity.CENTER_HORIZONTAL);
        content.addView(status, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        scoreText = UI.chip(this, "Score 0", ACCENT, 14);
        bestText = UI.chip(this, "Best " + HiScores.get(this, HS_KEY), UI.GOLD, 14);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        lp.setMargins(UI.dp(this, 4), 0, UI.dp(this, 4), 0);
        status.addView(scoreText, lp);
        status.addView(bestText, lp);

        view = new FlappyView(this, diff);
        view.setOnEnd(this::endGame);
        view.setOnScore(() -> {
            if (scoreText != null) scoreText.setText("Score " + view.getScore());
        });
        LinearLayout.LayoutParams vp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, UI.dp(this, 460));
        vp.topMargin = UI.dp(this, 10);
        content.addView(view, vp);

        content.addView(UI.space(this, 10));

        TextView hint = UI.ghost(this, "Give up", 14, 13);
        LinearLayout.LayoutParams hp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        content.addView(hint, hp);
        hint.setOnClickListener(v -> endGame());

        view.start();
    }

    private void endGame() {
        if (!running) return;
        running = false;
        if (view != null) view.stop();
        int finalScore = view == null ? 0 : view.getScore();
        int best = HiScores.best(this, HS_KEY, finalScore);
        content.removeAllViews();

        TextView title = UI.text(this, finalScore + " pipes", UI.INK, 28, true);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, UI.dp(this, 8), 0, UI.dp(this, 6));
        content.addView(title);

        TextView sub = UI.text(this, "Best " + best, UI.GOLD, 14, true);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 0, 0, UI.dp(this, 16));
        content.addView(sub);

        TextView again = UI.button(this, "Play Again", ACCENT, ACCENT_D, 16, 15);
        content.addView(again, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        again.setOnClickListener(v -> startGame(diffToggle.get()));

        TextView setup = UI.ghost(this, "Back to Setup", 15, 14);
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        slp.topMargin = UI.dp(this, 10);
        content.addView(setup, slp);
        setup.setOnClickListener(v -> showSetup());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        running = false;
        if (view != null) view.stop();
    }

    private static final class FlappyView extends View {

        private final Paint skyPaint;
        private final Paint groundPaint;
        private final Paint pipePaint;
        private final Paint birdPaint;
        private final Paint eyePaint;
        private final Paint overPaint;
        private final RectF rect = new RectF();
        private final Handler h = new Handler(Looper.getMainLooper());

        private final float gapMul;
        private final float speedMul;

        private float vw;
        private float vh;
        private float bx;
        private float by;
        private float vy;
        private final float radius;
private boolean running;
        private boolean over;
        private int score;
        private float pipeW;
        private float gapH;
        private float groundH;
        private float speed;
        private float pipeTimer;
        private Runnable onEnd;
        private Runnable onScore;
        private final List<Pipe> pipes = new ArrayList<>();

        private static final class Pipe {
            float x;
            float gapY;
            boolean passed;
        }

        FlappyView(Context c, int diff) {
            super(c);
            gapMul = diff == 0 ? 1.25f : diff == 1 ? 1f : 0.82f;
            speedMul = diff == 0 ? 0.85f : diff == 1 ? 1f : 1.18f;
            radius = UI.dp(c, 10);
            skyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            skyPaint.setColor(0xFFDCEAF7);
            groundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            groundPaint.setColor(0xFF8BB96B);
            pipePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            pipePaint.setColor(0xFF4E9B4A);
            birdPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            birdPaint.setColor(UI.GOLD);
            eyePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            eyePaint.setColor(0xFF2E2320);
            overPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            overPaint.setColor(UI.INK);
            overPaint.setTextSize(UI.dp(c, 34));
            overPaint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
            overPaint.setTextAlign(Paint.Align.CENTER);
        }

        void setOnEnd(Runnable r) {
            onEnd = r;
        }

        void setOnScore(Runnable r) {
            onScore = r;
        }

        int getScore() {
            return score;
        }

        @Override
        protected void onSizeChanged(int w, int h, int ow, int oh) {
            super.onSizeChanged(w, h, ow, oh);
            vw = w;
            vh = h;
            groundH = vh * 0.10f;
            pipeW = vw * 0.13f;
            gapH = vh * 0.24f * gapMul;
            speed = vw * 0.55f * speedMul;
            bx = vw * 0.30f;
            by = vh * 0.45f;
        }

        void start() {
            if (running) return;
            running = true;
            over = false;
            score = 0;
            pipes.clear();
            by = vh * 0.45f;
            vy = 0;
            pipeTimer = 0.4f;
            h.removeCallbacks(loop);
            h.postDelayed(loop, 16);
        }

        void stop() {
            running = false;
            h.removeCallbacks(loop);
        }

        private void spawnPipe() {
            Pipe p = new Pipe();
            p.x = vw;
            float margin = gapH * 0.6f + radius * 2;
            p.gapY = margin + UI.RND.nextFloat() * (vh - groundH - margin * 2);
            pipes.add(p);
        }

        private final Runnable loop = new Runnable() {
            public void run() {
                if (!running) return;
                if (vh <= 0) {
                    h.postDelayed(this, 16);
                    return;
                }
                float dt = 0.016f;

                vy += vh * 3.0f * dt;
                by += vy * dt;

                boolean dead = false;
                if (by - radius < 0 || by + radius > vh - groundH) {
                    dead = true;
                }

                for (int i = 0; i < pipes.size(); i++) {
                    Pipe p = pipes.get(i);
                    p.x -= speed * dt;
                    if (!p.passed && p.x + pipeW < bx) {
                        p.passed = true;
                        score++;
                        if (onScore != null) onScore.run();
                    }
                    if (circleHitsPipe(bx, by, radius, p)) {
                        dead = true;
                    }
                }

                for (int i = pipes.size() - 1; i >= 0; i--) {
                    if (pipes.get(i).x + pipeW < 0) pipes.remove(i);
                }

                pipeTimer -= dt;
                if (pipeTimer <= 0) {
                    spawnPipe();
                    pipeTimer = vw / (speed * 2.1f);
                }

                invalidate();
                if (dead) {
                    gameOver();
                    return;
                }
                h.postDelayed(this, 16);
            }
        };

        private boolean circleHitsPipe(float cx, float cy, float r, Pipe p) {
            float x1 = p.x;
            float x2 = p.x + pipeW;
            float topBottom = p.gapY - gapH / 2f;
            float bottomTop = p.gapY + gapH / 2f;
            return circleHitsRect(cx, cy, r, x1, x2, 0, topBottom)
                    || circleHitsRect(cx, cy, r, x1, x2, bottomTop, vh - groundH);
        }

        private boolean circleHitsRect(float cx, float cy, float r, float x1, float x2, float y1, float y2) {
            float nx = Math.max(x1, Math.min(x2, cx));
            float ny = Math.max(y1, Math.min(y2, cy));
            float dx = cx - nx;
            float dy = cy - ny;
            return dx * dx + dy * dy <= r * r;
        }

        private void gameOver() {
            if (!running) return;
            running = false;
            over = true;
            h.removeCallbacks(loop);
            invalidate();
            if (onEnd != null) onEnd.run();
        }

        @Override
        public boolean onTouchEvent(MotionEvent ev) {
            if (!running || over) return true;
            if (ev.getActionMasked() == MotionEvent.ACTION_DOWN) {
                vy = -vh * 0.82f;
            }
            return true;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            if (vh <= 0) return;
            canvas.drawRect(0, 0, vw, vh, skyPaint);

            float capH = UI.dp(getContext(), 12);
            for (Pipe p : pipes) {
                float topBottom = p.gapY - gapH / 2f;
                float bottomTop = p.gapY + gapH / 2f;
                canvas.drawRect(p.x, 0, p.x + pipeW, topBottom, pipePaint);
                canvas.drawRect(p.x, bottomTop, p.x + pipeW, vh - groundH, pipePaint);
                canvas.drawRect(p.x, topBottom - capH, p.x + pipeW, topBottom, pipePaint);
                canvas.drawRect(p.x, bottomTop, p.x + pipeW, bottomTop + capH, pipePaint);
            }

            canvas.drawRect(0, vh - groundH, vw, vh, groundPaint);

            canvas.drawCircle(bx, by, radius, birdPaint);
            canvas.drawCircle(bx + radius * 0.35f, by - radius * 0.2f, radius * 0.25f, eyePaint);

            if (over) {
                canvas.drawText("Game Over", vw / 2f, vh / 2f, overPaint);
            }
        }
    }
}
