package io.github.abdurazaaqmohammed.gamesbox.game;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
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
import java.util.Iterator;
import java.util.List;

public class FruitNinjaActivity extends BaseGameActivity {

    private static final int ACCENT = UI.RED;
    private static final int ACCENT_D = UI.RED_D;
    private static final String HS_KEY = "fruitninja";

    private LinearLayout content;
    private FruitNinjaView view;
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
        content = UI.screen(this, "Fruit Ninja", "Swipe to slice, dodge the bombs", ACCENT);
        showSetup();
    }

    private void showSetup() {
        running = false;
        if (view != null) view.stop();
        content.removeAllViews();

        TextView rules = UI.text(this,
                "Fruits fly up from the bottom - swipe across them to slice and score. Miss 3 fruits and the game ends. Watch out for the bombs - slicing one ends the run immediately.",
                UI.INK_SOFT, 14, false);
        rules.setPadding(0, 0, 0, UI.dp(this, 16));
        content.addView(rules);

        content.addView(UI.space(this, 16));

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
        bestText = UI.chip(this, "Best " + HiScores.get(this, HS_KEY), UI.GOLD, 14);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        lp.setMargins(UI.dp(this, 4), 0, UI.dp(this, 4), 0);
        status.addView(scoreText, lp);
        status.addView(bestText, lp);

        view = new FruitNinjaView(this);
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

        TextView title = UI.text(this, finalScore + " slices", UI.INK, 28, true);
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

    private static final class FruitNinjaView extends View {

        private static final int MAX_TRAIL = 25;

        private final int[] FRUIT_COLORS = {
                0xFFFF6B6B, 0xFF51CF66, 0xFF339AF0, 0xFFFFD43B,
                0xFFCC5DE8, 0xFFFF922B, 0xFF20C997, 0xFFE599F7
        };
        private final Paint fruitPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint bombPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint bombSpkPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint trailPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint juicePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint missPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Handler h = new Handler(Looper.getMainLooper());

        private float vw, vh;
        private final List<Fruit> fruits = new ArrayList<>();
        private final List<Juice> juices = new ArrayList<>();
        private final List<float[]> trail = new ArrayList<>();

        private boolean running;
        private int score;
        private int misses;
        private float spawnTimer;

        private Runnable onEnd;
        private Runnable onScore;

        private static final class Fruit {
            float x, y, vx, vy;
            int color;
            boolean bomb;
            boolean sliced;
            float rotation, rotSpeed;
            float size;
        }

        private static final class Juice {
            float x, y;
            int color;
            float alpha;
            float vx, vy;
        }

        FruitNinjaView(Context c) {
            super(c);
            bombPaint.setColor(0xFF2A2A2A);
            bombSpkPaint.setColor(0xFFBBBBBB);
            bombSpkPaint.setStrokeWidth(UI.dp(c, 2.5f));
            bombSpkPaint.setStrokeCap(Paint.Cap.ROUND);
            trailPaint.setColor(0xFFFFFFFF);
            trailPaint.setStyle(Paint.Style.STROKE);
            trailPaint.setStrokeWidth(UI.dp(c, 3));
            trailPaint.setStrokeCap(Paint.Cap.ROUND);
            juicePaint.setStyle(Paint.Style.FILL);
            missPaint.setColor(UI.RED);
            missPaint.setTextSize(UI.dp(c, 28));
            missPaint.setTextAlign(Paint.Align.CENTER);
            missPaint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        }

        void setOnEnd(Runnable r) { onEnd = r; }
        void setOnScore(Runnable r) { onScore = r; }
        int getScore() { return score; }

        void start() {
            running = true;
            score = 0;
            misses = 0;
            spawnTimer = 0.3f;
            fruits.clear();
            juices.clear();
            trail.clear();
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
        }

        private void spawnFruit() {
            Fruit f = new Fruit();
            f.x = vw * 0.12f + UI.RND.nextFloat() * vw * 0.76f;
            f.y = vh + UI.dp(getContext(), 30);
            f.vx = (UI.RND.nextFloat() - 0.5f) * vw * 0.35f;
            f.vy = -(vh * 1.5f + UI.RND.nextFloat() * vh * 0.5f);
            f.color = FRUIT_COLORS[UI.RND.nextInt(FRUIT_COLORS.length)];
            f.bomb = UI.RND.nextFloat() < 0.12f;
            f.size = 1f + UI.RND.nextFloat() * 1.2f;
            f.rotation = UI.RND.nextFloat() * 360;
            f.rotSpeed = (UI.RND.nextFloat() - 0.5f) * 8f;
            fruits.add(f);
        }

        private final Runnable loop = new Runnable() {
            public void run() {
                if (!running) return;
                if (vh <= 0) {
                    h.postDelayed(this, 16);
                    return;
                }
                float dt = 0.016f;

                spawnTimer -= dt;
                if (spawnTimer <= 0) {
                    spawnFruit();
                    spawnTimer = 0.7f + UI.RND.nextFloat() * 0.5f;
                }

                float gravity = vh * 2.0f;
                Iterator<Fruit> it = fruits.iterator();
                while (it.hasNext()) {
                    Fruit f = it.next();
                    f.x += f.vx * dt;
                    f.vy += gravity * dt;
                    f.y += f.vy * dt;
                    f.rotation += f.rotSpeed;
                    if (f.y > vh + UI.dp(getContext(), 60) && f.vy > 0 && !f.sliced) {
                        if (!f.bomb) {
                            misses++;
                        }
                        it.remove();
                    } else if (f.y > vh + UI.dp(getContext(), 120)) {
                        it.remove();
                    }
                }

                for (int i = trail.size() - 1; i >= 0; i--) {
                    float[] t = trail.get(i);
                    t[2] -= dt * 6f;
                    if (t[2] <= 0) trail.remove(i);
                }

                Iterator<Juice> jit = juices.iterator();
                while (jit.hasNext()) {
                    Juice j = jit.next();
                    j.x += j.vx * dt;
                    j.y += j.vy * dt;
                    j.vy += gravity * 0.5f * dt;
                    j.alpha -= dt * 2f;
                    if (j.alpha <= 0) jit.remove();
                }

                if (misses >= 3) {
                    gameOver();
                    return;
                }

                invalidate();
                h.postDelayed(this, 16);
            }
        };

        private void sliceCheck(float x1, float y1, float x2, float y2) {
            float baseR = UI.dp(getContext(), 22);
            for (Fruit f : fruits) {
                if (f.sliced) continue;
                float dx = f.x - (x1 + x2) / 2f;
                float dy = f.y - (y1 + y2) / 2f;
                float hitR = baseR * f.size + UI.dp(getContext(), 12);
                if (dx * dx + dy * dy < hitR * hitR) {
                    f.sliced = true;
                    if (f.bomb) {
                        gameOver();
                        return;
                    }
                    score += 10;
                    if (onScore != null) onScore.run();
                    for (int i = 0; i < 6; i++) {
                        Juice j = new Juice();
                        j.x = f.x;
                        j.y = f.y;
                        j.color = f.color;
                        j.alpha = 1f;
                        j.vx = (UI.RND.nextFloat() - 0.5f) * vw * 0.5f;
                        j.vy = (UI.RND.nextFloat() - 0.8f) * vh * 0.5f;
                        juices.add(j);
                    }
                }
            }
        }

        private void gameOver() {
            running = false;
            h.removeCallbacks(loop);
            invalidate();
            if (onEnd != null) onEnd.run();
        }

        @Override
        public boolean onTouchEvent(MotionEvent ev) {
            if (!running) return true;
            float x = ev.getX();
            float y = ev.getY();
            int action = ev.getActionMasked();
            if (action == MotionEvent.ACTION_DOWN) {
                trail.clear();
                trail.add(new float[]{x, y, 1f});
            } else if (action == MotionEvent.ACTION_MOVE) {
                if (trail.size() > 0) {
                    float[] last = trail.get(trail.size() - 1);
                    float ddx = x - last[0];
                    float ddy = y - last[1];
                    if (ddx * ddx + ddy * ddy > UI.dp(getContext(), 8) * UI.dp(getContext(), 8)) {
                        trail.add(new float[]{x, y, 1f});
                        if (trail.size() > MAX_TRAIL) trail.remove(0);
                        if (trail.size() >= 2) {
                            float[] p = trail.get(trail.size() - 2);
                            sliceCheck(p[0], p[1], x, y);
                        }
                    }
                }
            }
            return true;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            if (vh <= 0) return;
            canvas.drawColor(UI.PAPER);

            for (Juice j : juices) {
                juicePaint.setColor(UI.withAlpha(j.color, (int) (j.alpha * 180)));
                canvas.drawCircle(j.x, j.y, UI.dp(getContext(), 6) * j.alpha, juicePaint);
            }

            if (trail.size() >= 2) {
                Path path = new Path();
                path.moveTo(trail.get(0)[0], trail.get(0)[1]);
                for (int i = 1; i < trail.size(); i++) {
                    path.lineTo(trail.get(i)[0], trail.get(i)[1]);
                }
                for (int i = trail.size() - 1; i >= 0; i--) {
                    float a = trail.get(i)[2];
                    trailPaint.setColor(UI.withAlpha(0xFFFFFF, (int) (a * 220)));
                    trailPaint.setStrokeWidth(UI.dp(getContext(), 2 + 2 * a));
                }
                canvas.drawPath(path, trailPaint);
            }

            float baseR = UI.dp(getContext(), 22);
            for (Fruit f : fruits) {
                float r = baseR * f.size;
                canvas.save();
                canvas.rotate(f.rotation, f.x, f.y);
                if (f.bomb) {
                    canvas.drawCircle(f.x, f.y, r, bombPaint);
                    canvas.drawCircle(f.x, f.y, r * 0.6f, bombSpkPaint);
                    canvas.drawLine(f.x, f.y - r, f.x + UI.dp(getContext(), 6),
                            f.y - r - UI.dp(getContext(), 10), bombSpkPaint);
                } else {
                    if (f.sliced) {
                        fruitPaint.setColor(f.color);
                        canvas.save();
                        canvas.clipRect(f.x - r, f.y - r, f.x - UI.dp(getContext(), 2), f.y + r);
                        canvas.drawCircle(f.x - UI.dp(getContext(), 2), f.y, r, fruitPaint);
                        canvas.restore();
                        canvas.save();
                        canvas.clipRect(f.x + UI.dp(getContext(), 2), f.y - r, f.x + r, f.y + r);
                        canvas.drawCircle(f.x + UI.dp(getContext(), 2), f.y, r, fruitPaint);
                        canvas.restore();
                    } else {
                        fruitPaint.setColor(f.color);
                        canvas.drawCircle(f.x, f.y, r, fruitPaint);
                        fruitPaint.setColor(0xFF228B22);
                        canvas.drawRect(f.x - UI.dp(getContext(), 2), f.y - r - UI.dp(getContext(), 4),
                                f.x + UI.dp(getContext(), 2), f.y - r + UI.dp(getContext(), 2), fruitPaint);
                    }
                }
                canvas.restore();
            }

            for (int i = 0; i < 3; i++) {
                float mx = vw - UI.dp(getContext(), 30) - i * UI.dp(getContext(), 28);
                missPaint.setColor(i < 3 - misses ? UI.RED : 0xFF555555);
                canvas.drawCircle(mx, UI.dp(getContext(), 30), UI.dp(getContext(), 10), missPaint);
            }

            canvas.drawText(String.valueOf(score), vw / 2f, UI.dp(getContext(), 40), missPaint);
        }
    }
}
