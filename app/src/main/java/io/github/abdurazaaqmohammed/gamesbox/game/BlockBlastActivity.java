package io.github.abdurazaaqmohammed.gamesbox.game;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import io.github.abdurazaaqmohammed.gamesbox.HiScores;
import io.github.abdurazaaqmohammed.gamesbox.ui.UI;

public class BlockBlastActivity extends BaseGameActivity {

    private static final int ACCENT = UI.MINT;
    private static final int ACCENT_D = UI.MINT_D;
    private static final String HS_KEY = "blockblast";

    private LinearLayout content;
    private UI.Toggle sizeToggle;
    private BlockBlastView view;
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
        content = UI.screen(this, "Block Blast", "Fill rows and columns to blast them", ACCENT);
        showSetup();
    }

    private void showSetup() {
        running = false;
        content.removeAllViews();

        TextView rules = UI.text(this,
                "You get three block pieces at a time. Pick a piece, then tap a spot on the board to drop it. Complete a full row or column to blast it away and score. When no piece fits anywhere, the game is over.",
                UI.INK_SOFT, 14, false);
        rules.setPadding(0, 0, 0, UI.dp(this, 16));
        content.addView(rules);

        sizeToggle = new UI.Toggle(this, "Board", new String[]{"8x8", "10x10"}, ACCENT, 0);
        content.addView(sizeToggle.row);

        content.addView(UI.space(this, 16));

        TextView start = UI.button(this, "Start Game", ACCENT, ACCENT_D, 17, 16);
        content.addView(start, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        start.setOnClickListener(v -> startGame(sizeToggle.get() == 0 ? 8 : 10));
    }

    private void startGame(int size) {
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

        view = new BlockBlastView(this, size);
        view.setOnEnd(this::endGame);
        view.setOnScore(() -> {
            if (scoreText != null) scoreText.setText("Score " + view.getScore());
        });
        LinearLayout.LayoutParams vp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, UI.dp(this, 500));
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
        again.setOnClickListener(v -> startGame(sizeToggle.get() == 0 ? 8 : 10));

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

    private static final class BlockBlastView extends View {

        private static final int SLOTS = 3;
        private static final int[][][] SHAPES = {
                {{0, 0}},
                {{0, 0}, {1, 0}},
                {{0, 0}, {0, 1}},
                {{0, 0}, {1, 0}, {2, 0}},
                {{0, 0}, {0, 1}, {0, 2}},
                {{0, 0}, {1, 0}, {1, 1}},
                {{0, 0}, {0, 1}, {1, 1}},
                {{0, 0}, {1, 0}, {0, 1}, {1, 1}},
                {{0, 0}, {1, 0}, {2, 0}, {3, 0}},
                {{0, 0}, {0, 1}, {0, 2}, {0, 3}},
                {{0, 0}, {1, 0}, {2, 0}, {0, 1}, {1, 1}, {2, 1}},
                {{0, 0}, {1, 0}, {2, 0}, {2, 1}},
                {{0, 0}, {0, 1}, {0, 2}, {1, 2}},
        };

        private final Paint bgPaint;
        private final Paint linePaint;
        private final Paint fillPaint;
        private final Paint ghostPaint;
        private final Paint badPaint;
        private final Paint slotPaint;
        private final Paint selPaint;
        private final Paint overPaint;
        private final RectF rect = new RectF();

        private final int size;
        private int[][] board;
        private final int[][][] pieces = new int[SLOTS][][];
        private int selected = -1;
        private int hx = -1;
        private int hy = -1;
        private int score;
private boolean running;
        private boolean over;
        private Runnable onEnd;
        private Runnable onScore;

        private float cell;
        private float boardLeft;
        private float boardTop;
        private float trayTop;
        private float trayH;
        private float slotW;

        BlockBlastView(Context c, int size) {
            super(c);
            this.size = size;
            bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            bgPaint.setColor(UI.PAPER_D);
            linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            linePaint.setColor(UI.withAlpha(UI.INK, 14));
            linePaint.setStrokeWidth(1f);
            fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            fillPaint.setColor(ACCENT);
            ghostPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            ghostPaint.setColor(UI.withAlpha(ACCENT, 90));
            badPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            badPaint.setColor(UI.withAlpha(UI.RED, 80));
            slotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            slotPaint.setColor(UI.withAlpha(UI.INK, 7));
            selPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            selPaint.setColor(ACCENT);
            selPaint.setStyle(Paint.Style.STROKE);
            selPaint.setStrokeWidth(UI.dp(c, 2));
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
            if (w <= 0 || h <= 0) return;
            cell = Math.min(w / (float) size, (h - UI.dp(getContext(), 18)) / (float) size);
            trayH = cell * 2.6f;
            float side = cell * size;
            boardLeft = (w - side) / 2f;
            boardTop = (h - side - trayH) / 2f + UI.dp(getContext(), 6);
            trayTop = boardTop + side;
            slotW = w / (float) SLOTS;
        }

        void start() {
            if (running) return;
            running = true;
            over = false;
            score = 0;
            board = new int[size][size];
            selected = -1;
            for (int i = 0; i < SLOTS; i++) {
                pieces[i] = randomShape();
            }
            invalidate();
        }

        private int[][] randomShape() {
            return SHAPES[UI.RND.nextInt(SHAPES.length)];
        }

        private boolean fits(int piece, int bx, int by) {
            int[][] cells = pieces[piece];
            for (int[] c : cells) {
                int x = bx + c[0];
                int y = by + c[1];
                if (x < 0 || y < 0 || x >= size || y >= size) return false;
                if (board[y][x] != 0) return false;
            }
            return true;
        }

        private void place(int piece, int bx, int by) {
            int[][] cells = pieces[piece];
            for (int[] c : cells) {
                board[by + c[1]][bx + c[0]] = 1;
            }
            int cleared = clearLines();
            if (cleared > 0) {
                score += cleared * 10;
                if (onScore != null) onScore.run();
            }
            pieces[piece] = randomShape();
        }

        private int clearLines() {
            int cleared = 0;
            for (int y = 0; y < size; y++) {
                boolean full = true;
                for (int x = 0; x < size; x++) {
                    if (board[y][x] == 0) {
                        full = false;
                        break;
                    }
                }
                if (full) {
                    for (int x = 0; x < size; x++) board[y][x] = 0;
                    cleared++;
                }
            }
            for (int x = 0; x < size; x++) {
                boolean full = true;
                for (int y = 0; y < size; y++) {
                    if (board[y][x] == 0) {
                        full = false;
                        break;
                    }
                }
                if (full) {
                    for (int y = 0; y < size; y++) board[y][x] = 0;
                    cleared++;
                }
            }
            return cleared;
        }

        private boolean anyCanPlace() {
            for (int p = 0; p < SLOTS; p++) {
                for (int y = 0; y < size; y++) {
                    for (int x = 0; x < size; x++) {
                        if (fits(p, x, y)) return true;
                    }
                }
            }
            return false;
        }

        private void gameOver() {
            if (!running) return;
            running = false;
            over = true;
            invalidate();
            if (onEnd != null) onEnd.run();
        }

        private int slotAt(float x) {
            int s = (int) (x / slotW);
            if (s < 0) s = 0;
            if (s >= SLOTS) s = SLOTS - 1;
            return s;
        }

        private void hoverAt(float x, float y) {
            if (y >= boardTop && y <= boardTop + cell * size) {
                int cxx = (int) ((x - boardLeft) / cell);
                int cyy = (int) ((y - boardTop) / cell);
                hx = cxx >= 0 && cxx < size ? cxx : -1;
                hy = cyy >= 0 && cyy < size ? cyy : -1;
            } else {
                hx = -1;
                hy = -1;
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent ev) {
            if (!running || over) return true;
            float x = ev.getX();
            float y = ev.getY();
            switch (ev.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    if (y >= trayTop) {
                        int s = slotAt(x);
                        selected = (selected == s) ? -1 : s;
                    } else {
                        hoverAt(x, y);
                    }
                    invalidate();
                    return true;
                case MotionEvent.ACTION_MOVE:
                    hoverAt(x, y);
                    invalidate();
                    return true;
                case MotionEvent.ACTION_UP:
                    if (selected >= 0 && hx >= 0 && hy >= 0) {
                        if (fits(selected, hx, hy)) {
                            place(selected, hx, hy);
                            if (!anyCanPlace()) {
                                selected = -1;
                                gameOver();
                                return true;
                            }
                        }
                        selected = -1;
                    }
                    hx = -1;
                    hy = -1;
                    invalidate();
                    return true;
                case MotionEvent.ACTION_CANCEL:
                    hx = -1;
                    hy = -1;
                    selected = -1;
                    invalidate();
                    return true;
            }
            return true;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            if (cell <= 0) return;
            float side = cell * size;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                canvas.drawRoundRect(boardLeft, boardTop, boardLeft + side, boardTop + side,
                        UI.dp(getContext(), 12), UI.dp(getContext(), 12), bgPaint);
            } else canvas.drawRect(boardLeft, boardTop, boardLeft + side, boardTop + side, bgPaint);

            for (int i = 1; i < size; i++) {
                float x = boardLeft + cell * i;
                canvas.drawLine(x, boardTop, x, boardTop + side, linePaint);
                float y = boardTop + cell * i;
                canvas.drawLine(boardLeft, y, boardLeft + side, y, linePaint);
            }

            float pad = cell * 0.08f;
            for (int y = 0; y < size; y++) {
                for (int x = 0; x < size; x++) {
                    if (board[y][x] != 0) {
                        rect.set(boardLeft + x * cell + pad, boardTop + y * cell + pad,
                                boardLeft + (x + 1) * cell - pad, boardTop + (y + 1) * cell - pad);
                        canvas.drawRoundRect(rect, cell * 0.22f, cell * 0.22f, fillPaint);
                    }
                }
            }

            if (selected >= 0 && hx >= 0 && hy >= 0) {
                boolean ok = fits(selected, hx, hy);
                int[][] cells = pieces[selected];
                for (int[] c : cells) {
                    int x = hx + c[0];
                    int y = hy + c[1];
                    if (x < 0 || y < 0 || x >= size || y >= size) continue;
                    rect.set(boardLeft + x * cell + pad, boardTop + y * cell + pad,
                            boardLeft + (x + 1) * cell - pad, boardTop + (y + 1) * cell - pad);
                    canvas.drawRoundRect(rect, cell * 0.22f, cell * 0.22f,
                            ok ? ghostPaint : badPaint);
                }
            }

            for (int i = 0; i < SLOTS; i++) {
                float x0 = i * slotW + UI.dp(getContext(), 6);
                float x1 = (i + 1) * slotW - UI.dp(getContext(), 6);
                float y0 = trayTop + UI.dp(getContext(), 6);
                float y1 = getHeight() - UI.dp(getContext(), 6);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    canvas.drawRoundRect(x0, y0, x1, y1, UI.dp(getContext(), 12),
                            UI.dp(getContext(), 12), slotPaint);
                } else canvas.drawRect(x0, y0, x1, y1, slotPaint);
                if (i == selected) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        canvas.drawRoundRect(x0, y0, x1, y1, UI.dp(getContext(), 12),
                                UI.dp(getContext(), 12), selPaint);
                    } else canvas.drawRect(x0, y0, x1, y1, selPaint);
                }
                drawPiece(canvas, pieces[i], (x0 + x1) / 2f, (y0 + y1) / 2f,
                        x1 - x0, y1 - y0);
            }

            if (over) {
                overPaint.setColor(UI.RED);
                float cx = boardLeft + side / 2f;
                float cy = boardTop + side / 2f;
                canvas.drawText("Game Over", cx, cy, overPaint);
            }
        }

        private void drawPiece(Canvas canvas, int[][] cells, float cx, float cy, float slotWp, float slotHp) {
            if (cells == null) return;
            int pw = 0;
            int ph = 0;
            for (int[] c : cells) {
                if (c[0] + 1 > pw) pw = c[0] + 1;
                if (c[1] + 1 > ph) ph = c[1] + 1;
            }
            float pc = Math.min(slotWp / pw, slotHp / ph);
            pc = Math.min(pc, cell * 0.82f);
            float ox = cx - pw * pc / 2f;
            float oy = cy - ph * pc / 2f;
            float pad = pc * 0.12f;
            for (int[] c : cells) {
                rect.set(ox + c[0] * pc + pad, oy + c[1] * pc + pad,
                        ox + (c[0] + 1) * pc - pad, oy + (c[1] + 1) * pc - pad);
                canvas.drawRoundRect(rect, pc * 0.24f, pc * 0.24f, fillPaint);
            }
        }
    }
}
