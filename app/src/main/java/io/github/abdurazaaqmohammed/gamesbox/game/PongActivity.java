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

public class PongActivity extends BaseGameActivity {

    private static final int ACCENT = UI.SLATE;
    private static final int ACCENT_D = UI.SLATE_D;
    private static final String HS_KEY = "pong";

    private LinearLayout content;
    private PongView view;
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
        content = UI.screen(this, "Pong", "2 players, first to 7 wins", ACCENT);
        showSetup();
    }

    private void showSetup() {
        running = false;
        if (view != null) view.stop();
        content.removeAllViews();

        TextView rules = UI.text(this,
                "Player 1 controls the left paddle, Player 2 controls the right. Each player drags their finger up and down to move. Bounce the ball past your opponent to score. First to 7 wins.",
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

        scoreText = UI.chip(this, "Score 0 - 0", ACCENT, 14);
        bestText = UI.chip(this, "Best " + HiScores.get(this, HS_KEY), UI.GOLD, 14);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        lp.setMargins(UI.dp(this, 4), 0, UI.dp(this, 4), 0);
        status.addView(scoreText, lp);
        status.addView(bestText, lp);

        view = new PongView(this);
        view.setOnEnd(this::endGame);
        view.setOnScore(() -> {
            if (scoreText != null)
                scoreText.setText("Score " + view.getP1Score() + " - " + view.getP2Score());
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
        int s1 = view == null ? 0 : view.getP1Score();
        int s2 = view == null ? 0 : view.getP2Score();
        boolean p1Won = s1 > s2;
        int best = HiScores.best(this, HS_KEY, p1Won ? 1 : 0);
        content.removeAllViews();

        TextView title = UI.text(this, p1Won ? "Player 1 wins!" : "Player 2 wins!",
                UI.INK, 28, true);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, UI.dp(this, 8), 0, UI.dp(this, 6));
        content.addView(title);

        TextView sub = UI.text(this, s1 + " - " + s2 + "    Best wins: " + best, UI.GOLD, 14, true);
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

    private static final class PongView extends View {

        interface OnEnd { void onEnd(); }
        interface OnScore { void onScore(); }

        private final Paint bgPaint;
        private final Paint p1Paint;
        private final Paint p2Paint;
        private final Paint ballPaint;
        private final Paint linePaint;
        private final Paint textPaint;
        private final Handler h = new Handler(Looper.getMainLooper());

        private OnEnd onEnd;
        private OnScore onScore;
        private boolean running;

        private float vw, vh;
        private float p1Y, p2Y;
        private float p1TouchY, p2TouchY;
        private boolean p1Touching, p2Touching;
        private float ballX, ballY;
        private float ballVX, ballVY;
        private float paddleW, paddleH, ballR;
        private int p1Score, p2Score;
        private final int WIN_SCORE = 7;

        PongView(Context c) {
            super(c);
            bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            bgPaint.setColor(UI.PAPER_D);
            p1Paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            p1Paint.setColor(UI.SLATE);
            p2Paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            p2Paint.setColor(UI.CORAL);
            ballPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            ballPaint.setColor(UI.INK);
            linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            linePaint.setColor(UI.withAlpha(UI.INK, 20));
            linePaint.setStyle(Paint.Style.STROKE);
            linePaint.setStrokeWidth(UI.dp(c, 2));
            textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            textPaint.setColor(UI.withAlpha(UI.INK, 30));
            textPaint.setTextSize(UI.dp(c, 40));
            textPaint.setTextAlign(Paint.Align.CENTER);
            textPaint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        }

        void setOnEnd(OnEnd r) { onEnd = r; }
        void setOnScore(OnScore r) { onScore = r; }
        int getP1Score() { return p1Score; }
        int getP2Score() { return p2Score; }

        void start() {
            running = true;
            p1Score = 0;
            p2Score = 0;
            resetBall();
            h.removeCallbacks(loop);
            h.postDelayed(loop, 16);
        }

        void stop() {
            running = false;
            h.removeCallbacks(loop);
        }

        private void resetBall() {
            ballX = vw / 2f;
            ballY = vh / 2f;
            float angle = (float) (Math.PI / 4 + UI.RND.nextFloat() * Math.PI / 2);
            float spd = vw * 1.3f;
            ballVX = (UI.RND.nextBoolean() ? 1 : -1) * (float) Math.cos(angle) * spd;
            ballVY = (UI.RND.nextBoolean() ? 1 : -1) * (float) Math.sin(angle) * spd;
        }

        @Override
        protected void onSizeChanged(int w, int h, int ow, int oh) {
            super.onSizeChanged(w, h, ow, oh);
            vw = w;
            vh = h;
            paddleW = vw * 0.028f;
            paddleH = vh * 0.16f;
            ballR = vw * 0.022f;
            p1Y = vh / 2f;
            p2Y = vh / 2f;
            if (running && ballX == 0 && ballY == 0) {
                resetBall();
            }
        }

        private final Runnable loop = new Runnable() {
            public void run() {
                if (!running) return;
                if (vh <= 0) {
                    h.postDelayed(this, 16);
                    return;
                }
                float dt = 0.016f;

                p1Y += (p1TouchY - p1Y) * 8f * dt;
                p1Y = Math.max(paddleH / 2, Math.min(vh - paddleH / 2, p1Y));

                p2Y += (p2TouchY - p2Y) * 8f * dt;
                p2Y = Math.max(paddleH / 2, Math.min(vh - paddleH / 2, p2Y));

                ballX += ballVX * dt;
                ballY += ballVY * dt;

                if (ballY - ballR < 0) {
                    ballY = ballR;
                    ballVY = Math.abs(ballVY);
                }
                if (ballY + ballR > vh) {
                    ballY = vh - ballR;
                    ballVY = -Math.abs(ballVY);
                }

                float p1Left = vw * 0.05f;
                float p1Right = p1Left + paddleW;
                if (ballVX < 0 && ballX - ballR <= p1Right && ballX + ballR >= p1Left) {
                    float py1 = p1Y - paddleH / 2;
                    float py2 = p1Y + paddleH / 2;
                    if (ballY + ballR >= py1 && ballY - ballR <= py2) {
                        ballX = p1Right + ballR;
                        float hitPos = (ballY - p1Y) / (paddleH / 2f);
                        float angle = hitPos * (float) Math.PI / 3.5f;
                        float speed = (float) Math.sqrt(ballVX * ballVX + ballVY * ballVY);
                        speed = Math.min(speed * 1.05f, vw * 1.8f);
                        ballVX = (float) Math.cos(angle) * speed;
                        ballVY = (float) Math.sin(angle) * speed;
                    }
                }

                float p2Left = vw - vw * 0.05f - paddleW;
                float p2Right = p2Left + paddleW;
                if (ballVX > 0 && ballX + ballR >= p2Left && ballX - ballR <= p2Right) {
                    float ay1 = p2Y - paddleH / 2;
                    float ay2 = p2Y + paddleH / 2;
                    if (ballY + ballR >= ay1 && ballY - ballR <= ay2) {
                        ballX = p2Left - ballR;
                        float hitPos = (ballY - p2Y) / (paddleH / 2f);
                        float angle = (float) Math.PI - hitPos * (float) Math.PI / 3.5f;
                        float speed = (float) Math.sqrt(ballVX * ballVX + ballVY * ballVY);
                        speed = Math.min(speed * 1.05f, vw * 1.8f);
                        ballVX = (float) Math.cos(angle) * speed;
                        ballVY = (float) Math.sin(angle) * speed;
                    }
                }

                if (ballX + ballR < 0) {
                    p2Score++;
                    if (onScore != null) onScore.onScore();
                    if (p2Score >= WIN_SCORE) { gameOver(); return; }
                    resetBall();
                }
                if (ballX - ballR > vw) {
                    p1Score++;
                    if (onScore != null) onScore.onScore();
                    if (p1Score >= WIN_SCORE) { gameOver(); return; }
                    resetBall();
                }

                invalidate();
                h.postDelayed(this, 16);
            }
        };

        private void gameOver() {
            running = false;
            h.removeCallbacks(loop);
            invalidate();
            if (onEnd != null) onEnd.onEnd();
        }

        @Override
        public boolean onTouchEvent(MotionEvent ev) {
            if (!running) return true;
            int actionMasked = ev.getActionMasked();
            int actionIndex = ev.getActionIndex();

            if (actionMasked == MotionEvent.ACTION_DOWN || actionMasked == MotionEvent.ACTION_POINTER_DOWN) {
                float tx = ev.getX(actionIndex);
                float ty = ev.getY(actionIndex);
                int ptrId = ev.getPointerId(actionIndex);
                if (tx < vw / 2f) {
                    p1TouchY = ty;
                    p1Touching = true;
                } else {
                    p2TouchY = ty;
                    p2Touching = true;
                }
            } else if (actionMasked == MotionEvent.ACTION_MOVE) {
                for (int i = 0; i < ev.getPointerCount(); i++) {
                    float tx = ev.getX(i);
                    float ty = ev.getY(i);
                    if (tx < vw / 2f) {
                        p1TouchY = ty;
                    } else {
                        p2TouchY = ty;
                    }
                }
            } else if (actionMasked == MotionEvent.ACTION_UP || actionMasked == MotionEvent.ACTION_CANCEL) {
                p1Touching = false;
                p2Touching = false;
            } else if (actionMasked == MotionEvent.ACTION_POINTER_UP) {
                float tx = ev.getX(actionIndex);
                if (tx < vw / 2f) {
                    p1Touching = false;
                } else {
                    p2Touching = false;
                }
            }
            return true;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            if (vh <= 0) return;
            canvas.drawRect(0, 0, vw, vh, bgPaint);

            float cx = vw / 2f;
            for (float y = 0; y < vh; y += UI.dp(getContext(), 18)) {
                canvas.drawLine(cx, y, cx, y + UI.dp(getContext(), 10), linePaint);
            }

            canvas.drawText(String.valueOf(p1Score), vw * 0.22f, vh * 0.18f, textPaint);
            canvas.drawText(String.valueOf(p2Score), vw * 0.78f, vh * 0.18f, textPaint);

            float p1x = vw * 0.05f;
            canvas.drawRoundRect(new RectF(p1x, p1Y - paddleH / 2, p1x + paddleW, p1Y + paddleH / 2),
                    paddleW / 2, paddleW / 2, p1Paint);

            float p2x = vw - vw * 0.05f - paddleW;
            canvas.drawRoundRect(new RectF(p2x, p2Y - paddleH / 2, p2x + paddleW, p2Y + paddleH / 2),
                    paddleW / 2, paddleW / 2, p2Paint);

            canvas.drawCircle(ballX, ballY, ballR, ballPaint);
        }
    }
}
