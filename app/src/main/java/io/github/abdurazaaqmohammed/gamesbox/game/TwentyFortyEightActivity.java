package io.github.abdurazaaqmohammed.gamesbox.game;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
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

public class TwentyFortyEightActivity extends BaseGameActivity {

    private static final int ACCENT = UI.GOLD;
    private static final int ACCENT_D = UI.GOLD_D;
    private static final String HS_KEY = "2048";

    private LinearLayout content;
    private UI.Toggle gridToggle;
    private TwentyView view;
    private TextView scoreText;
    private TextView bestText;
    private int score;
    private int best;
    private boolean running;

    @Override
    protected boolean isGameInProgress() {
        return running;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        content = UI.screen(this, "2048", "Slide and merge the tiles", ACCENT);
        showSetup();
    }

    private void showSetup() {
        running = false;
        content.removeAllViews();

        TextView rules = UI.text(this,
                "Swipe to slide every tile. Tiles with the same number merge into one bigger tile. Chain merges to reach 2048 and beyond. The game ends when the board is full and nothing can merge.",
                UI.INK_SOFT, 14, false);
        rules.setPadding(0, 0, 0, UI.dp(this, 16));
        content.addView(rules);

        gridToggle = new UI.Toggle(this, "Board", new String[]{"4x4", "5x5"}, ACCENT, 0);
        content.addView(gridToggle.row);

        content.addView(UI.space(this, 16));

        TextView start = UI.button(this, "Start Game", ACCENT, ACCENT_D, 17, 16);
        content.addView(start, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        start.setOnClickListener(v -> startGame(gridToggle.get() == 0 ? 4 : 5));
    }

    private void startGame(int size) {
        score = 0;
        best = HiScores.get(this, HS_KEY);
        running = true;
        content.removeAllViews();

        LinearLayout status = UI.row(this);
        status.setGravity(Gravity.CENTER_HORIZONTAL);
        content.addView(status, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        scoreText = UI.chip(this, "Score 0", ACCENT, 14);
        bestText = UI.chip(this, "Best " + best, UI.GOLD, 14);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        lp.setMargins(UI.dp(this, 4), 0, UI.dp(this, 4), 0);
        status.addView(scoreText, lp);
        status.addView(bestText, lp);

        view = new TwentyView(this, size);
        view.setOnEnd(this::endGame);
        LinearLayout.LayoutParams vp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, UI.dp(this, 430));
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
        score = view == null ? score : view.getScore();
        best = HiScores.best(this, HS_KEY, score);
        content.removeAllViews();

        TextView title = UI.text(this, score + " points", UI.INK, 28, true);
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
        again.setOnClickListener(v -> startGame(gridToggle.get() == 0 ? 4 : 5));

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
    }

    private static final class TwentyView extends View {

        private final Paint bgPaint;
        private final Paint tilePaint;
        private final Paint textPaint;
        private final int size;
        private int[][] board;
        private int score;
private boolean running;
        private boolean over;
        private Runnable onEnd;
        private float cell;
        private float left;
        private float top;
        private final GestureDetector gd;
        private final List<int[]> empties = new ArrayList<>();

        TwentyView(Context c, int size) {
            super(c);
            this.size = size;
            bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            bgPaint.setColor(UI.PAPER_D);
            tilePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            textPaint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
            textPaint.setTextAlign(Paint.Align.CENTER);
            gd = new GestureDetector(c, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onFling(MotionEvent e1, MotionEvent e2, float vx, float vy) {
                    if (!running) return true;
                    int dir;
                    if (Math.abs(vx) > Math.abs(vy)) {
                        dir = vx > 0 ? 0 : 1;
                    } else {
                        dir = vy > 0 ? 2 : 3;
                    }
                    move(dir);
                    return true;
                }
            });
        }

        void setOnEnd(Runnable r) {
            onEnd = r;
        }

        int getScore() {
            return score;
        }

        @Override
        protected void onSizeChanged(int w, int h, int ow, int oh) {
            super.onSizeChanged(w, h, ow, oh);
            if (w <= 0 || h <= 0) return;
            cell = Math.min(w, h) / (float) size;
            left = (w - cell * size) / 2f;
            top = (h - cell * size) / 2f;
        }

        void start() {
            if (running) return;
            running = true;
            over = false;
            score = 0;
            board = new int[size][size];
            spawn();
            spawn();
            invalidate();
        }

        private void spawn() {
            empties.clear();
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    if (board[i][j] == 0) empties.add(new int[]{i, j});
                }
            }
            if (empties.isEmpty()) return;
            int[] spot = empties.get(UI.RND.nextInt(empties.size()));
            board[spot[0]][spot[1]] = UI.RND.nextInt(10) == 0 ? 4 : 2;
        }

        private void move(int dir) {
            boolean moved = false;
            int gained = 0;
            for (int i = 0; i < size; i++) {
                int[] line = read(i, dir);
                int[] before = line.clone();
                int[] merged = merge(line);
                gained += merged[size];
                int[] res = new int[size];
                System.arraycopy(merged, 0, res, 0, size);
                for (int j = 0; j < size; j++) {
                    write(i, j, dir, res[j]);
                    if (res[j] != before[j]) moved = true;
                }
            }
            if (!moved) return;
            score += gained;
            spawn();
            if (!canMove()) {
                over = true;
                running = false;
                invalidate();
                if (onEnd != null) onEnd.run();
                return;
            }
            invalidate();
        }

        private int[] read(int i, int dir) {
            int[] out = new int[size];
            for (int j = 0; j < size; j++) {
                out[j] = cellAt(i, j, dir);
            }
            return out;
        }

        private void write(int i, int j, int dir, int v) {
            switch (dir) {
                case 0: board[i][j] = v; break;
                case 1: board[i][size - 1 - j] = v; break;
                case 2: board[j][i] = v; break;
                case 3: board[size - 1 - j][i] = v; break;
            }
        }

        private int cellAt(int i, int j, int dir) {
            switch (dir) {
                case 0: return board[i][j];
                case 1: return board[i][size - 1 - j];
                case 2: return board[j][i];
                default: return board[size - 1 - j][i];
            }
        }

        private int[] merge(int[] line) {
            int[] res = new int[size + 1];
            int p = 0;
            for (int i = 0; i < size; i++) {
                if (line[i] == 0) continue;
                if (p > 0 && res[p - 1] == line[i]) {
                    res[p - 1] *= 2;
                    res[size] += res[p - 1];
                } else {
                    res[p++] = line[i];
                }
            }
            return res;
        }

        private boolean canMove() {
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    if (board[i][j] == 0) return true;
                    if (j + 1 < size && board[i][j] == board[i][j + 1]) return true;
                    if (i + 1 < size && board[i][j] == board[i + 1][j]) return true;
                }
            }
            return false;
        }

        @Override
        public boolean onTouchEvent(MotionEvent ev) {
            gd.onTouchEvent(ev);
            return true;
        }

        private int tileColor(int v) {
            switch (v) {
                case 2: return 0xFFEEDDBB;
                case 4: return 0xFFEBD7A5;
                case 8: return 0xFFF2B179;
                case 16: return 0xFFF59563;
                case 32: return 0xFFF67C5F;
                case 64: return 0xFFF65E3B;
                case 128: return 0xFFEDCF72;
                case 256: return 0xFFEDCC61;
                case 512: return 0xFFEDC850;
                case 1024: return 0xFFEDC53F;
                case 2048: return 0xFFEDC22E;
                default: return 0xFF3C3A32;
            }
        }

        @Override
        protected void onDraw(Canvas canvas) {
            if (cell <= 0) return;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                canvas.drawRoundRect(left, top, left + cell * size, top + cell * size,
                        UI.dp(getContext(), 14), UI.dp(getContext(), 14), bgPaint);
            } else canvas.drawRect(left, top, left + cell * size, top + cell * size, bgPaint);

            float pad = cell * 0.06f;
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    int v = board[i][j];
                    float x = left + j * cell;
                    float y = top + i * cell;
                    tilePaint.setColor(UI.withAlpha(UI.INK, 8));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        canvas.drawRoundRect(x + pad, y + pad, x + cell - pad, y + cell - pad,
                                cell * 0.12f, cell * 0.12f, tilePaint);
                    } else canvas.drawRect(x + pad, y + pad, x + cell - pad, y + cell - pad, tilePaint);
                    if (v != 0) {
                        tilePaint.setColor(tileColor(v));
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            canvas.drawRoundRect(x + pad, y + pad, x + cell - pad, y + cell - pad,
                                    cell * 0.12f, cell * 0.12f, tilePaint);
                        } else canvas.drawRect(x + pad, y + pad, x + cell - pad, y + cell - pad, tilePaint);
                        textPaint.setColor(v < 8 ? 0xFF776E65 : 0xFFF9F6F2);
                        textPaint.setTextSize(cell * 0.44f);
                        float ty = y + cell / 2f - (textPaint.ascent() + textPaint.descent()) / 2f;
                        canvas.drawText(String.valueOf(v), x + cell / 2f, ty, textPaint);
                    }
                }
            }

            if (over) {
                textPaint.setColor(UI.RED);
                textPaint.setTextSize(cell * 0.9f);
                float cx = left + cell * size / 2f;
                float cy = top + cell * size / 2f;
                textPaint.setStyle(Paint.Style.FILL);
                canvas.drawText("Game Over", cx, cy, textPaint);
            }
        }
    }
}
