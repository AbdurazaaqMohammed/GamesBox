package io.github.abdurazaaqmohammed.gamesbox.game;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import io.github.abdurazaaqmohammed.gamesbox.HiScores;
import io.github.abdurazaaqmohammed.gamesbox.ui.UI;

public class SnakeActivity extends BaseGameActivity {

    private static final int ACCENT = UI.MINT;
    private static final int ACCENT_D = UI.MINT_D;
    private static final String HS_KEY = "snake";

    private LinearLayout content;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private UI.Toggle speedToggle;
    private SnakeView view;
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
        content = UI.screen(this, "Snake", "Eat, grow, don't crash", ACCENT);
        showSetup();
    }

    private void showSetup() {
        running = false;
        handler.removeCallbacksAndMessages(null);
        content.removeAllViews();

        TextView rules = UI.text(this,
                "Swipe to steer the snake. Eat the food to grow one longer and score a point - every bite makes you a little faster. Hit the wall or your own tail and the run is over.",
                UI.INK_SOFT, 14, false);
        rules.setPadding(0, 0, 0, UI.dp(this, 16));
        content.addView(rules);

        speedToggle = new UI.Toggle(this, "Speed", new String[]{"Easy", "Normal", "Fast"}, ACCENT, 1);
        content.addView(speedToggle.row);

        content.addView(UI.space(this, 16));

        TextView start = UI.button(this, "Start Game", ACCENT, ACCENT_D, 17, 16);
        content.addView(start, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        start.setOnClickListener(v -> startGame(speedToggle.get()));
    }

    private void startGame(int speed) {
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

        view = new SnakeView(this, speed);
        view.setOnEnd(this::endGame);
        view.setOnScore(() -> {
            if (scoreText != null) scoreText.setText("Score " + view.getScore());
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
        handler.removeCallbacksAndMessages(null);
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
        again.setOnClickListener(v -> startGame(speedToggle.get()));

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
        handler.removeCallbacksAndMessages(null);
        if (view != null) view.stop();
    }

    private static final class SnakeView extends View {

        private static final int COLS = 15;
        private static final int MAX_CELLS = COLS * 30;

        private final Paint boardPaint;
        private final Paint linePaint;
        private final Paint snakePaint;
        private final Paint headPaint;
        private final Paint foodPaint;
        private final Handler h = new Handler(Looper.getMainLooper());
        private final RectF rect = new RectF();

        private int rows;
        private float cell;
        private float left;
        private float top;

        private final int[] sx = new int[MAX_CELLS];
        private final int[] sy = new int[MAX_CELLS];
        private int len;
        private int dx = 1;
        private int dy = 0;
        private int qdx = 1;
        private int qdy = 0;
        private int fx;
        private int fy;
        private long tickMs;
private boolean running;
        private boolean sized;
        private int score;
        private Runnable onEnd;
        private Runnable onScore;
        private final GestureDetector gd;

        SnakeView(Context c, int speed) {
            super(c);
            tickMs = speed == 0 ? 240 : speed == 1 ? 170 : 110;
            boardPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            boardPaint.setColor(UI.PAPER_D);
            linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            linePaint.setColor(UI.withAlpha(UI.INK, 14));
            snakePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            snakePaint.setColor(ACCENT);
            headPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            headPaint.setColor(ACCENT_D);
            foodPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            foodPaint.setColor(UI.RED);
            gd = new GestureDetector(c, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onFling(MotionEvent e1, MotionEvent e2, float vx, float vy) {
                    turn(e2, e1);
                    return true;
                }
            });
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
            if (w <= 0 || h <= 0) return;
            float cw = w / (float) COLS;
            rows = Math.max(10, Math.min(26, Math.round(h / cw)));
            cell = Math.min(w / (float) COLS, h / (float) rows);
            left = (w - cell * COLS) / 2f;
            top = (h - cell * rows) / 2f;
            if (!sized) {
                sized = true;
                if (running) reset();
            }
        }

        void start() {
            if (running) return;
            running = true;
            if (sized) {
                reset();
            }
            h.removeCallbacks(tick);
            h.postDelayed(tick, tickMs);
            invalidate();
        }

        void stop() {
            running = false;
            h.removeCallbacks(tick);
        }

        private void reset() {
            score = 0;
            len = 3;
            int cy = rows / 2;
            int cx = COLS / 2;
            for (int i = 0; i < len; i++) {
                sx[i] = cx - i;
                sy[i] = cy;
            }
            dx = 1;
            dy = 0;
            qdx = 1;
            qdy = 0;
            spawnFood();
        }

        private void spawnFood() {
            if (len >= COLS * rows) {
                gameOver();
                return;
            }
            for (int tries = 0; tries < 500; tries++) {
                int x = UI.RND.nextInt(COLS);
                int y = UI.RND.nextInt(rows);
                if (!onSnake(x, y)) {
                    fx = x;
                    fy = y;
                    return;
                }
            }
            gameOver();
        }

        private boolean onSnake(int x, int y) {
            for (int i = 0; i < len; i++) {
                if (sx[i] == x && sy[i] == y) return true;
            }
            return false;
        }

        private void turn(MotionEvent e2, MotionEvent e1) {
            float gx = e2.getX() - e1.getX();
            float gy = e2.getY() - e1.getY();
            if (Math.abs(gx) < Math.abs(gy)) {
                if (gy < 0 && dy != 1) {
                    qdx = 0;
                    qdy = -1;
                } else if (gy > 0 && dy != -1) {
                    qdx = 0;
                    qdy = 1;
                }
            } else {
                if (gx > 0 && dx != -1) {
                    qdx = 1;
                    qdy = 0;
                } else if (gx < 0 && dx != 1) {
                    qdx = -1;
                    qdy = 0;
                }
            }
        }

        private final Runnable tick = new Runnable() {
            public void run() {
                if (!running) return;
                if (!sized) {
                    h.postDelayed(this, tickMs);
                    invalidate();
                    return;
                }
                step();
                invalidate();
                if (running) h.postDelayed(this, tickMs);
            }
        };

        private void step() {
            dx = qdx;
            dy = qdy;
            int nx = sx[0] + dx;
            int ny = sy[0] + dy;
            if (nx < 0 || ny < 0 || nx >= COLS || ny >= rows) {
                gameOver();
                return;
            }
            boolean eat = (nx == fx && ny == fy);
            int limit = eat ? len : len - 1;
            for (int i = 0; i < limit; i++) {
                if (sx[i] == nx && sy[i] == ny) {
                    gameOver();
                    return;
                }
            }
            for (int i = len; i > 0; i--) {
                sx[i] = sx[i - 1];
                sy[i] = sy[i - 1];
            }
            sx[0] = nx;
            sy[0] = ny;
            if (eat) {
                len++;
                score++;
                tickMs = Math.max(60, tickMs - 5);
                spawnFood();
                if (onScore != null) onScore.run();
            }
        }

        private void gameOver() {
            if (!running) return;
            running = false;
            h.removeCallbacks(tick);
            if (onEnd != null) onEnd.run();
        }

        @Override
        public boolean onTouchEvent(MotionEvent ev) {
            if (!running) return true;
            gd.onTouchEvent(ev);
            return true;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            if (cell <= 0) return;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                canvas.drawRoundRect(left, top, left + cell * COLS, top + cell * rows,
                        UI.dp(getContext(), 10), UI.dp(getContext(), 10), boardPaint);
            } else canvas.drawRect(left, top, left + cell * COLS, top + cell * rows, boardPaint);

            for (int i = 1; i < COLS; i++) {
                float x = left + cell * i;
                canvas.drawLine(x, top, x, top + cell * rows, linePaint);
            }
            for (int j = 1; j < rows; j++) {
                float y = top + cell * j;
                canvas.drawLine(left, y, left + cell * COLS, y, linePaint);
            }

            float inset = cell * 0.08f;
            rect.set(left + fx * cell + inset, top + fy * cell + inset,
                    left + (fx + 1) * cell - inset, top + (fy + 1) * cell - inset);
            canvas.drawOval(rect, foodPaint);

            for (int i = len - 1; i >= 0; i--) {
                float pad = i == 0 ? cell * 0.10f : cell * 0.16f;
                rect.set(left + sx[i] * cell + pad, top + sy[i] * cell + pad,
                        left + (sx[i] + 1) * cell - pad, top + (sy[i] + 1) * cell - pad);
                canvas.drawRoundRect(rect, cell * 0.30f, cell * 0.30f,
                        i == 0 ? headPaint : snakePaint);
            }
        }
    }
}
