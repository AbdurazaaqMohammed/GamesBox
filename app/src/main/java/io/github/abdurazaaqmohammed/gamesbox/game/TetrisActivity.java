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

import java.util.ArrayList;
import java.util.List;

public class TetrisActivity extends BaseGameActivity {

    private static final int ACCENT = UI.INDIGO;
    private static final int ACCENT_D = UI.INDIGO_D;
    private static final String HS_KEY = "tetris";

    private LinearLayout content;
    private UI.Toggle speedToggle;
    private TetrisView view;
    private TextView scoreText;
    private TextView levelText;
    private boolean running;

    @Override
    protected boolean isGameInProgress() {
        return running;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        content = UI.screen(this, "Tetris", "Stack the falling blocks", ACCENT);
        showSetup();
    }

    private void showSetup() {
        running = false;
        if (view != null) view.stop();
        content.removeAllViews();

        TextView rules = UI.text(this,
                "Swipe left and right to move, swipe up or tap to rotate, swipe down to drop the block. Complete full rows to clear them and score. The game speeds up as you clear more.",
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
        levelText = UI.chip(this, "Level 1", UI.GOLD, 14);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        lp.setMargins(UI.dp(this, 4), 0, UI.dp(this, 4), 0);
        status.addView(scoreText, lp);
        status.addView(levelText, lp);

        view = new TetrisView(this, speed);
        view.setOnEnd(this::endGame);
        view.setOnStats(() -> {
            if (scoreText != null) scoreText.setText("Score " + view.getScore());
            if (levelText != null) levelText.setText("Level " + view.getLevel());
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
        if (view != null) view.stop();
    }

    private static final class TetrisView extends View {

        private static final int W = 10;
        private static final int H = 18;

        private static final int[][][] BASE = {
                {{0, 0}, {1, 0}, {2, 0}, {3, 0}},
                {{0, 0}, {1, 0}, {0, 1}, {1, 1}},
                {{0, 0}, {1, 0}, {2, 0}, {1, 1}},
                {{1, 0}, {2, 0}, {0, 1}, {1, 1}},
                {{0, 0}, {1, 0}, {1, 1}, {2, 1}},
                {{0, 0}, {1, 0}, {2, 0}, {0, 1}},
                {{0, 0}, {1, 0}, {2, 0}, {2, 1}},
        };

        private static final int[] COLORS = {
                UI.CYAN, UI.GOLD, UI.PURPLE, UI.MINT, UI.RED, UI.BLUE, UI.ORANGE,
        };

        private static final int[][][][] ROTS = buildAllRotations();

        private final Paint bgPaint;
        private final Paint linePaint;
        private final Paint cellPaint;
        private final Paint ghostPaint;
        private final Paint overPaint;
        private final RectF rect = new RectF();
        private final Handler h = new Handler(Looper.getMainLooper());

        private final int[][] board = new int[H][W];
        private int curType;
        private int curRot;
        private int curX;
        private int curY;
        private int score;
        private int lines;
        private int level;
private boolean running;
        private boolean over;
        private long interval;
        private Runnable onEnd;
        private Runnable onStats;

        private float cell;
        private float left;
        private float top;

        private final GestureDetector gd;
        private float dragAccum;
        private int holdDir;
        private boolean holding;

        private final Runnable holdMove = new Runnable() {
            public void run() {
                if (!holding || holdDir == 0 || !running || over) return;
                move(holdDir);
                invalidate();
                h.postDelayed(this, 120);
            }
        };

        TetrisView(Context c, int speed) {
            super(c);
            long base = speed == 0 ? 900 : speed == 1 ? 650 : 430;
            interval = base;
            bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            bgPaint.setColor(UI.PAPER_D);
            linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            linePaint.setColor(UI.withAlpha(UI.INK, 12));
            linePaint.setStrokeWidth(1f);
            cellPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            ghostPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            ghostPaint.setColor(UI.withAlpha(UI.INK, 40));
            ghostPaint.setStyle(Paint.Style.STROKE);
            ghostPaint.setStrokeWidth(UI.dp(c, 2));
            overPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            overPaint.setColor(UI.RED);
            overPaint.setTextSize(UI.dp(c, 34));
            overPaint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
            overPaint.setTextAlign(Paint.Align.CENTER);
            gd = new GestureDetector(c, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onFling(MotionEvent e1, MotionEvent e2, float vx, float vy) {
                    if (!running || over) return true;
                    if (Math.abs(vx) > Math.abs(vy)) {
                        if (vx > 0) move(1);
                        else move(-1);
                    } else {
                        if (vy < 0) rotate();
                        else hardDrop();
                    }
                    invalidate();
                    return true;
                }

                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    if (running && !over) rotate();
                    invalidate();
                    return true;
                }

                @Override
                public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                    if (!running || over) return true;
                    dragAccum += -distanceX;
                    float step = cell * 1.0f;
                    if (step <= 0) step = UI.dp(getContext(), 12);
                    int dir = 0;
                    while (dragAccum >= step) {
                        dragAccum -= step;
                        dir = 1;
                    }
                    while (dragAccum <= -step) {
                        dragAccum += step;
                        dir = -1;
                    }
                    if (dir != 0) {
                        holdDir = dir;
                        if (!holding) {
                            holding = true;
                            h.post(holdMove);
                        }
                    }
                    return true;
                }
            });
        }

        private static int[][][][] buildAllRotations() {
            int[][][][] out = new int[BASE.length][][][];
            for (int i = 0; i < BASE.length; i++) {
                out[i] = rotationsFor(BASE[i]);
            }
            return out;
        }

        private static int[][][] rotationsFor(int[][] base) {
            int box = 0;
            for (int[] c : base) {
                box = Math.max(box, Math.max(c[0], c[1]) + 1);
            }
            List<int[][]> rots = new ArrayList<>();
            int[][] cur = base;
            while (true) {
                rots.add(cur);
                int[][] next = rotate(cur, box);
                if (sameShape(next, base)) break;
                cur = next;
            }
            return rots.toArray(new int[0][][]);
        }

        private static int[][] rotate(int[][] cells, int box) {
            int[][] out = new int[cells.length][2];
            int minX = Integer.MAX_VALUE;
            int minY = Integer.MAX_VALUE;
            for (int i = 0; i < cells.length; i++) {
                int nx = cells[i][1];
                int ny = box - 1 - cells[i][0];
                out[i][0] = nx;
                out[i][1] = ny;
                if (nx < minX) minX = nx;
                if (ny < minY) minY = ny;
            }
            for (int[] c : out) {
                c[0] -= minX;
                c[1] -= minY;
            }
            return out;
        }

        private static boolean sameShape(int[][] a, int[][] b) {
            if (a.length != b.length) return false;
            for (int[] ca : a) {
                boolean found = false;
                for (int[] cb : b) {
                    if (ca[0] == cb[0] && ca[1] == cb[1]) {
                        found = true;
                        break;
                    }
                }
                if (!found) return false;
            }
            return true;
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

        int getLevel() {
            return level;
        }

        @Override
        protected void onSizeChanged(int w, int h, int ow, int oh) {
            super.onSizeChanged(w, h, ow, oh);
            if (w <= 0 || h <= 0) return;
            cell = Math.min(w / (float) W, h / (float) H);
            left = (w - cell * W) / 2f;
            top = (h - cell * H) / 2f;
        }

        void start() {
            if (running) return;
            running = true;
            over = false;
            score = 0;
            lines = 0;
            level = 1;
            holding = false;
            holdDir = 0;
            for (int y = 0; y < H; y++) {
                for (int x = 0; x < W; x++) board[y][x] = 0;
            }
            spawn();
            h.removeCallbacks(holdMove);
            h.removeCallbacks(tick);
            h.postDelayed(tick, interval);
            invalidate();
        }

        void stop() {
            running = false;
            h.removeCallbacks(holdMove);
            h.removeCallbacks(tick);
        }

        private void spawn() {
            curType = UI.RND.nextInt(BASE.length);
            curRot = 0;
            curX = W / 2 - 2;
            curY = 0;
            if (!fits(ROTS[curType][curRot], curX, curY)) {
                gameOver();
            }
        }

        private int[][] curCells() {
            return ROTS[curType][curRot];
        }

        private boolean fits(int[][] cells, int x, int y) {
            for (int[] c : cells) {
                int cx = x + c[0];
                int cy = y + c[1];
                if (cx < 0 || cx >= W) return false;
                if (cy >= H) return false;
                if (cy >= 0 && board[cy][cx] != 0) return false;
            }
            return true;
        }

        private void move(int dx) {
            if (fits(curCells(), curX + dx, curY)) curX += dx;
        }

        private void rotate() {
            int nextRot = (curRot + 1) % ROTS[curType].length;
            int[][] cells = ROTS[curType][nextRot];
            for (int kick = 0; kick <= 2; kick++) {
                for (int s = -1; s <= 1; s += 2) {
                    int k = s * kick;
                    if (fits(cells, curX + k, curY)) {
                        curRot = nextRot;
                        curX += k;
                        return;
                    }
                }
            }
        }

        private void hardDrop() {
            int drop = 0;
            while (fits(curCells(), curX, curY + 1)) {
                curY++;
                drop++;
            }
            score += drop * 2;
            lock();
        }

        private int ghostY() {
            int y = curY;
            while (fits(curCells(), curX, y + 1)) y++;
            return y;
        }

        private final Runnable tick = new Runnable() {
            public void run() {
                if (!running || over) return;
                if (fits(curCells(), curX, curY + 1)) {
                    curY++;
                } else {
                    lock();
                }
                invalidate();
                if (running && !over) {
                    h.postDelayed(this, interval);
                }
            }
        };

        private void lock() {
            int[][] cells = curCells();
            boolean above = false;
            for (int[] c : cells) {
                int cy = curY + c[1];
                int cx = curX + c[0];
                if (cy < 0) {
                    above = true;
                    continue;
                }
                board[cy][cx] = curType + 1;
            }
            if (above) {
                gameOver();
                return;
            }
            int cleared = clearLines();
            if (cleared > 0) {
                int[] pts = {0, 100, 300, 500, 800};
                score += pts[Math.min(cleared, 4)];
                lines += cleared;
                level = lines / 10 + 1;
                interval = Math.max(90, interval - (cleared * 15));
                if (onStats != null) onStats.run();
            }
            spawn();
        }

        private int clearLines() {
            int cleared = 0;
            for (int y = H - 1; y >= 0; y--) {
                boolean full = true;
                for (int x = 0; x < W; x++) {
                    if (board[y][x] == 0) {
                        full = false;
                        break;
                    }
                }
                if (full) {
                    cleared++;
                    for (int yy = y; yy > 0; yy--) {
                        System.arraycopy(board[yy - 1], 0, board[yy], 0, W);
                    }
                    for (int x = 0; x < W; x++) board[0][x] = 0;
                    y++;
                }
            }
            return cleared;
        }

        private void gameOver() {
            if (!running) return;
            running = false;
            over = true;
            holding = false;
            holdDir = 0;
            h.removeCallbacks(holdMove);
            h.removeCallbacks(tick);
            invalidate();
            if (onEnd != null) onEnd.run();
        }

        @Override
        public boolean onTouchEvent(MotionEvent ev) {
            switch (ev.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    dragAccum = 0;
                    holding = false;
                    holdDir = 0;
                    h.removeCallbacks(holdMove);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    holding = false;
                    holdDir = 0;
                    h.removeCallbacks(holdMove);
                    break;
            }
            gd.onTouchEvent(ev);
            return true;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            if (cell <= 0) return;
            float bw = cell * W;
            float bh = cell * H;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                canvas.drawRoundRect(left, top, left + bw, top + bh,
                        UI.dp(getContext(), 12), UI.dp(getContext(), 12), bgPaint);
            } else canvas.drawRect(left, top, left + bw, top + bh, bgPaint);

            for (int i = 1; i < W; i++) {
                float x = left + cell * i;
                canvas.drawLine(x, top, x, top + bh, linePaint);
            }
            for (int j = 1; j < H; j++) {
                float y = top + cell * j;
                canvas.drawLine(left, y, left + bw, y, linePaint);
            }

            float pad = cell * 0.08f;
            for (int y = 0; y < H; y++) {
                for (int x = 0; x < W; x++) {
                    if (board[y][x] != 0) {
                        cellPaint.setColor(COLORS[board[y][x] - 1]);
                        rect.set(left + x * cell + pad, top + y * cell + pad,
                                left + (x + 1) * cell - pad, top + (y + 1) * cell - pad);
                        canvas.drawRoundRect(rect, cell * 0.2f, cell * 0.2f, cellPaint);
                    }
                }
            }

            if (running && !over) {
                int gy = ghostY();
                int[][] cells = curCells();
                for (int[] c : cells) {
                    rect.set(left + (curX + c[0]) * cell + pad, top + (gy + c[1]) * cell + pad,
                            left + (curX + c[0] + 1) * cell - pad, top + (gy + c[1] + 1) * cell - pad);
                    canvas.drawRoundRect(rect, cell * 0.2f, cell * 0.2f, ghostPaint);
                }
                cellPaint.setColor(COLORS[curType]);
                for (int[] c : cells) {
                    rect.set(left + (curX + c[0]) * cell + pad, top + (curY + c[1]) * cell + pad,
                            left + (curX + c[0] + 1) * cell - pad, top + (curY + c[1] + 1) * cell - pad);
                    canvas.drawRoundRect(rect, cell * 0.2f, cell * 0.2f, cellPaint);
                }
            }

            if (over) {
                float cx = left + bw / 2f;
                float cy = top + bh / 2f;
                canvas.drawText("Game Over", cx, cy, overPaint);
            }
        }
    }
}
