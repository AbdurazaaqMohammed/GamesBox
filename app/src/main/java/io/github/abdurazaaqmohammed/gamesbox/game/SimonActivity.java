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

public class SimonActivity extends BaseGameActivity {

    private static final int ACCENT = UI.PURPLE;
    private static final int ACCENT_D = UI.PURPLE_D;
    private static final String HS_KEY = "simon";

    private LinearLayout content;
    private UI.Toggle speedToggle;
    private SimonView view;
    private TextView roundText;
    private TextView bestText;
    private boolean running;

    @Override
    protected boolean isGameInProgress() {
        return running;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        content = UI.screen(this, "Simon", "Follow the light pattern", ACCENT);
        showSetup();
    }

    private void showSetup() {
        running = false;
        if (view != null) view.stop();
        content.removeAllViews();

        TextView rules = UI.text(this,
                "Four pads light up in a pattern. Watch closely, then repeat the pattern by tapping the pads in the same order. Every round adds one more step - one wrong tap and it's over.",
                UI.INK_SOFT, 14, false);
        rules.setPadding(0, 0, 0, UI.dp(this, 16));
        content.addView(rules);

        speedToggle = new UI.Toggle(this, "Speed", new String[]{"Slow", "Normal", "Fast"}, ACCENT, 1);
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

        roundText = UI.chip(this, "Round 1", ACCENT, 14);
        bestText = UI.chip(this, "Best " + HiScores.get(this, HS_KEY), UI.GOLD, 14);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        lp.setMargins(UI.dp(this, 4), 0, UI.dp(this, 4), 0);
        status.addView(roundText, lp);
        status.addView(bestText, lp);

        view = new SimonView(this, speed);
        view.setOnEnd(this::endGame);
        view.setOnRound(() -> {
            if (roundText != null) roundText.setText("Round " + view.getRound());
        });
        LinearLayout.LayoutParams vp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, UI.dp(this, 420));
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

        TextView title = UI.text(this, "Round " + finalScore, UI.INK, 28, true);
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

    private static final class SimonView extends View {

        private static final int[] COLORS = {UI.RED, UI.MINT, UI.BLUE, UI.GOLD};

        private final Paint dimPaint;
        private final Paint litPaint;
        private final Paint ringPaint;
        private final RectF rect = new RectF();
        private final Handler h = new Handler(Looper.getMainLooper());
        private final List<Integer> seq = new ArrayList<>();

        private final long flashOn;
        private final long flashOff;

        private float vw;
        private float vh;
        private float cell;
        private float bx;
        private float by;
        private float gap;
        private int lit = -1;
private boolean running;
        private boolean over;
        private boolean accepting;
        private int round;
        private int playingStep;
        private int inputStep;
        private Runnable onEnd;
        private Runnable onRound;

        SimonView(Context c, int speed) {
            super(c);
            flashOn = speed == 0 ? 650 : speed == 1 ? 450 : 320;
            flashOff = speed == 0 ? 220 : 140;
            dimPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            litPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            ringPaint.setColor(UI.withAlpha(UI.INK, 40));
            ringPaint.setStyle(Paint.Style.STROKE);
            ringPaint.setStrokeWidth(UI.dp(c, 2));
        }

        void setOnEnd(Runnable r) {
            onEnd = r;
        }

        void setOnRound(Runnable r) {
            onRound = r;
        }

        int getScore() {
            return round;
        }

        int getRound() {
            return round;
        }

        @Override
        protected void onSizeChanged(int w, int h, int ow, int oh) {
            super.onSizeChanged(w, h, ow, oh);
            vw = w;
            vh = h;
            float dim = Math.min(w, h);
            float board = dim * 0.86f;
            cell = board / 2f;
            gap = UI.dp(getContext(), 5);
            bx = (w - board) / 2f;
            by = (h - board) / 2f;
        }

        void start() {
            if (running) return;
            running = true;
            over = false;
            round = 0;
            seq.clear();
            seq.add(UI.RND.nextInt(4));
            h.removeCallbacksAndMessages(null);
            if (onRound != null) onRound.run();
            h.postDelayed(() -> playStep(0), 600);
            invalidate();
        }

        void stop() {
            running = false;
            h.removeCallbacksAndMessages(null);
            lit = -1;
            invalidate();
        }

        private void playStep(int step) {
            if (!running) return;
            accepting = false;
            playingStep = step;
            lit = seq.get(step);
            invalidate();
            h.postDelayed(() -> {
                lit = -1;
                invalidate();
                if (step + 1 < seq.size()) {
                    h.postDelayed(() -> playStep(step + 1), flashOff);
                } else {
                    accepting = true;
                    inputStep = 0;
                }
            }, flashOn);
        }

        private void nextRound() {
            seq.add(UI.RND.nextInt(4));
            round++;
            if (onRound != null) onRound.run();
            h.postDelayed(() -> playStep(0), 500);
        }

        @Override
        public boolean onTouchEvent(MotionEvent ev) {
            if (!running || over) return true;
            if (ev.getActionMasked() != MotionEvent.ACTION_DOWN) return true;
            int q = quadrantAt(ev.getX(), ev.getY());
            if (q < 0) return true;
            if (!accepting) return true;
            if (q == seq.get(inputStep)) {
                inputStep++;
                flash(q);
                if (inputStep >= seq.size()) {
                    accepting = false;
                    nextRound();
                }
            } else {
                flash(q);
                gameOver();
            }
            return true;
        }

        private void flash(int q) {
            lit = q;
            invalidate();
            h.postDelayed(() -> {
                if (running) {
                    lit = -1;
                    invalidate();
                }
            }, 130);
        }

        private int quadrantAt(float x, float y) {
            if (x < bx || y < by || x > bx + cell * 2 || y > by + cell * 2) return -1;
            float hx = (x - bx) < cell ? 0 : 1;
            float hy = (y - by) < cell ? 0 : 1;
            return (int) hy * 2 + (int) hx;
        }

        private void gameOver() {
            if (!running) return;
            running = false;
            over = true;
            h.removeCallbacksAndMessages(null);
            lit = -1;
            invalidate();
            if (onEnd != null) onEnd.run();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            if (cell <= 0) return;
            for (int i = 0; i < 4; i++) {
                int col = i % 2;
                int row = i / 2;
                float x = bx + col * cell + gap / 2f;
                float y = by + row * cell + gap / 2f;
                float hw = cell - gap;
                boolean on = lit == i;
                dimPaint.setColor(UI.withAlpha(COLORS[i], on ? 255 : 110));
                rect.set(x, y, x + hw, y + hw);
                canvas.drawRoundRect(rect, UI.dp(getContext(), 18), UI.dp(getContext(), 18), dimPaint);
                canvas.drawRoundRect(rect, UI.dp(getContext(), 18), UI.dp(getContext(), 18), ringPaint);
            }
        }
    }
}
