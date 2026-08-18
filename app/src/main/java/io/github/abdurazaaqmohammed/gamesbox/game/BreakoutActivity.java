package io.github.abdurazaaqmohammed.gamesbox.game;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
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

public class BreakoutActivity extends BaseGameActivity {

    private static final int ACCENT = UI.CYAN;
    private static final int ACCENT_D = UI.CYAN_D;
    private static final String HS_KEY = "breakout";

    private LinearLayout content;
    private BreakoutView view;
    private TextView scoreText;
    private TextView livesText;
    private boolean running;

    @Override
    protected boolean isGameInProgress() {
        return running;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        content = UI.screen(this, "Breakout", "Smash every brick with the ball", ACCENT);
        showSetup();
    }

    private void showSetup() {
        running = false;
        if (view != null) view.stop();
        content.removeAllViews();

        TextView rules = UI.text(this,
                "Drag your finger to slide the paddle and keep the ball bouncing. Break every brick to move up a level - the ball gets faster each time. You have three lives, so don't let it fall.",
                UI.INK_SOFT, 14, false);
        rules.setPadding(0, 0, 0, UI.dp(this, 16));
        content.addView(rules);

        TextView start = UI.button(this, "Start Game", ACCENT, ACCENT_D, 17, 16);
        content.addView(start, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        start.setOnClickListener(v -> startGame());
    }

    private void startGame() {
        running = true;
        content.removeAllViews();

        LinearLayout status = UI.row(this);
        status.setGravity(Gravity.CENTER_HORIZONTAL);
        content.addView(status, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        scoreText = UI.chip(this, "Score 0", ACCENT, 14);
        livesText = UI.chip(this, "Lives 3", UI.RED, 14);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        lp.setMargins(UI.dp(this, 4), 0, UI.dp(this, 4), 0);
        status.addView(scoreText, lp);
        status.addView(livesText, lp);

        view = new BreakoutView(this);
        view.setOnEnd(this::endGame);
        view.setOnStats(() -> {
            if (scoreText != null) scoreText.setText("Score " + view.getScore());
            if (livesText != null) livesText.setText("Lives " + view.getLives());
        });
        LinearLayout.LayoutParams vp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, UI.dp(this, 470));
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

        TextView title = UI.text(this, finalScore + " points", UI.INK, 28, true);
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
        again.setOnClickListener(v -> startGame());

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

    private static final class BreakoutView extends View {

        private static final int COLS = 8;
        private static final int BASE_ROWS = 4;
        private static final int[] BRICK_COLORS = {UI.CORAL, UI.GOLD, UI.MINT, UI.SKY, UI.PURPLE, UI.ORANGE};

        private final Paint bgPaint;
        private final Paint brickPaint;
        private final Paint paddlePaint;
        private final Paint ballPaint;
        private final Paint overPaint;
        private final Paint titlePaint;
        private final RectF rect = new RectF();
        private final Handler h = new Handler(Looper.getMainLooper());

        private float vw;
        private float vh;
        private float brickRows;
        private float brickH;
        private float brickW;
        private float brickTop;

        private final boolean[][] bricks = new boolean[8][8];
        private float px;
        private float pw;
        private float ph;
        private float bx;
        private float by;
        private float br;
        private float vx;
        private float vy;
        private float ballSpeed;

        private int score;
        private int lives;
        private int level;
        private boolean serving;
private boolean running;
        private boolean over;
        private boolean won;
        private Runnable onEnd;
        private Runnable onStats;
        private float serveTimer;

        BreakoutView(Context c) {
            super(c);
            bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            bgPaint.setColor(UI.PAPER_D);
            brickPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paddlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paddlePaint.setColor(ACCENT);
            ballPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            ballPaint.setColor(UI.GOLD);
            overPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            overPaint.setColor(UI.RED);
            overPaint.setTextSize(UI.dp(c, 34));
            overPaint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
            overPaint.setTextAlign(Paint.Align.CENTER);
            titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            titlePaint.setColor(UI.INK);
            titlePaint.setTextSize(UI.dp(c, 26));
            titlePaint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
            titlePaint.setTextAlign(Paint.Align.CENTER);
        }

        void setOnEnd(Runnable r) {
            onEnd = r;
        }

        void setOnStats(Runnable r) {
            onStats = r;
        }

        int getScore() {
            return score;
        }

        int getLives() {
            return lives;
        }

        @Override
        protected void onSizeChanged(int w, int h, int ow, int oh) {
            super.onSizeChanged(w, h, ow, oh);
            vw = w;
            vh = h;
            brickW = vw / COLS;
            brickH = vw / COLS * 0.42f;
            brickTop = UI.dp(getContext(), 14);
            pw = vw * 0.24f;
            ph = UI.dp(getContext(), 13);
            br = UI.dp(getContext(), 7);
            ballSpeed = (vw * 1.3f) + (level - 1) * vw * 0.08f;
        }

        void start() {
            if (running) return;
            running = true;
            score = 0;
            lives = 3;
            level = 1;
            buildLevel();
            serving = true;
            serveTimer = 0.6f;
            px = vw / 2f - pw / 2f;
            h.removeCallbacks(loop);
            h.postDelayed(loop, 16);
        }

        void stop() {
            running = false;
            h.removeCallbacks(loop);
        }

        private void buildLevel() {
            brickRows = Math.min(BASE_ROWS + (level - 1), 7);
            for (int y = 0; y < 8; y++) {
                for (int x = 0; x < COLS; x++) {
                    bricks[y][x] = y < brickRows;
                }
            }
            ballSpeed = (vw * 1.3f) + (level - 1) * vw * 0.08f;
            resetBall();
        }

        private void resetBall() {
            bx = px + pw / 2f;
            by = vh - ph - br - 1;
            vx = 0;
            vy = 0;
            serving = true;
            serveTimer = 0.6f;
        }

        private void launch() {
            serving = false;
            vx = ballSpeed * (UI.RND.nextBoolean() ? 0.6f : -0.6f);
            vy = -ballSpeed;
        }

        private final Runnable loop = new Runnable() {
            public void run() {
                if (!running) return;
                float dt = 0.016f;

                if (serving) {
                    bx = px + pw / 2f;
                    by = vh - ph - br - 1;
                    serveTimer -= dt;
                    if (serveTimer <= 0) launch();
                    invalidate();
                    h.postDelayed(this, 16);
                    return;
                }

                bx += vx * dt;
                by += vy * dt;

                if (bx - br < 0) {
                    bx = br;
                    vx = Math.abs(vx);
                }
                if (bx + br > vw) {
                    bx = vw - br;
                    vx = -Math.abs(vx);
                }
                if (by - br < brickTop + brickH) {
                    if (by - br < 0) {
                        by = br;
                        vy = Math.abs(vy);
                    }
                }

                boolean dead = false;
                if (by + br >= vh) {
                    lives--;
                    if (onStats != null) onStats.run();
                    if (lives <= 0) {
                        dead = true;
                    } else {
                        resetBall();
                    }
                }

                if (by + br >= vh - ph && by - br <= vh - ph + br && bx >= px - br && bx <= px + pw + br) {
                    vy = -Math.abs(vy);
                    float hit = (bx - (px + pw / 2f)) / (pw / 2f);
                    vx = hit * ballSpeed * 0.85f;
                    by = vh - ph - br - 1;
                }

                if (hitBrick()) {
                    score += 10;
                    if (onStats != null) onStats.run();
                    if (bricksGone()) {
                        level++;
                        buildLevel();
                        if (onStats != null) onStats.run();
                    }
                }

                float max = ballSpeed * 1.2f;
                if (Math.abs(vx) > max) vx = Math.signum(vx) * max;
                if (Math.abs(vy) > max) vy = Math.signum(vy) * max;

                invalidate();
                if (dead) {
                    gameOver();
                    return;
                }
                h.postDelayed(this, 16);
            }
        };

        private boolean hitBrick() {
            for (int y = 0; y < 8; y++) {
                for (int x = 0; x < COLS; x++) {
                    if (!bricks[y][x]) continue;
                    float x1 = x * brickW;
                    float y1 = brickTop + y * brickH;
                    float x2 = x1 + brickW;
                    float y2 = y1 + brickH;
                    if (circleHitsRect(bx, by, br, x1, y1, x2, y2)) {
                        bricks[y][x] = false;
                        float nx = Math.max(x1, Math.min(x2, bx));
                        float ny = Math.max(y1, Math.min(y2, by));
                        float dx = bx - nx;
                        float dy = by - ny;
                        if (Math.abs(dx) > Math.abs(dy)) {
                            vx = Math.signum(dx) * Math.abs(vx);
                        } else {
                            vy = Math.signum(dy) * Math.abs(vy);
                        }
                        return true;
                    }
                }
            }
            return false;
        }

        private boolean bricksGone() {
            for (int y = 0; y < 8; y++) {
                for (int x = 0; x < COLS; x++) {
                    if (bricks[y][x]) return false;
                }
            }
            return true;
        }

        private boolean circleHitsRect(float cx, float cy, float r, float x1, float y1, float x2, float y2) {
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
            int action = ev.getActionMasked();
            if (action == MotionEvent.ACTION_DOWN
                    || action == MotionEvent.ACTION_MOVE) {
                px = ev.getX() - pw / 2f;
                if (px < 0) px = 0;
                if (px + pw > vw) px = vw - pw;
            }
            if (action == MotionEvent.ACTION_UP && serving) {
                launch();
            }
            return true;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            if (vh <= 0) return;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                canvas.drawRoundRect(0, 0, vw, vh, 0, 0, bgPaint);
            } else canvas.drawRect(0, 0, vw, vh, bgPaint);

            for (int y = 0; y < 8; y++) {
                for (int x = 0; x < COLS; x++) {
                    if (!bricks[y][x]) continue;
                    brickPaint.setColor(BRICK_COLORS[y % BRICK_COLORS.length]);
                    float pad = UI.dp(getContext(), 2);
                    rect.set(x * brickW + pad, brickTop + y * brickH + pad,
                            (x + 1) * brickW - pad, brickTop + (y + 1) * brickH - pad);
                    canvas.drawRoundRect(rect, UI.dp(getContext(), 4), UI.dp(getContext(), 4), brickPaint);
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                canvas.drawRoundRect(px, vh - ph, px + pw, vh, UI.dp(getContext(), 7),
                        UI.dp(getContext(), 7), paddlePaint);
            } else canvas.drawRect(px, vh - ph, px + pw, vh, paddlePaint);
            canvas.drawCircle(bx, by, br, ballPaint);

            if (over) {
                canvas.drawText("Game Over", vw / 2f, vh / 2f, overPaint);
            }
        }
    }
}
