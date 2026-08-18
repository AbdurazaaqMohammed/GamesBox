package io.github.abdurazaaqmohammed.gamesbox.game;

import android.content.Context;
import android.graphics.Canvas;
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

public class DartsActivity extends BaseGameActivity {

    private static final int ACCENT = UI.RED;
    private static final int ACCENT_D = UI.RED_D;
    private static final int DARTS_PER_TURN = 3;
    private static final int[] PALETTE = {
            UI.RED, UI.BLUE, UI.GOLD, UI.MINT, UI.PURPLE, UI.ORANGE, UI.CYAN, UI.PINK
    };
    private static final String[] COLOR_NAMES = {
            "Red", "Blue", "Gold", "Mint", "Purple", "Orange", "Cyan", "Pink"
    };

    private final Handler h = new Handler(Looper.getMainLooper());

    private LinearLayout content;
    private UI.Stepper playersStepper;
    private int players;
    private int[] totals;
    private int[] playerColors;
    private int currentPlayer;

    private DartboardView board;
    private int thrown;
    private int turnPoints;
    private TextView status;
    private TextView tally;

    private String playerName(int i) {
        return PlayerNames.get(this, i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        content = UI.screen(this, "Darts",
                "Three darts, highest score wins", ACCENT);
        showSetup();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        h.removeCallbacksAndMessages(null);
    }

    private void showSetup() {
        content.removeAllViews();

        TextView rules = UI.text(this,
                "Everyone throws three darts at the same board. Swipe from the dart at the bottom toward your target and release - the dart follows your aim, but a shaky hand lands a little off. Each player has their own dart color, and every dart stays on the board. Highest total wins.",
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
            totals = new int[players];
            playerColors = new int[players];
            for (int i = 0; i < players; i++) {
                playerColors[i] = PALETTE[i % PALETTE.length];
            }
            currentPlayer = 0;
            thrown = 0;
            turnPoints = 0;
            showTurn();
        });
    }

    private void showTurn() {
        content.removeAllViews();

        LinearLayout whoRow = new LinearLayout(this);
        whoRow.setGravity(Gravity.CENTER);
        whoRow.setOrientation(LinearLayout.HORIZONTAL);
        whoRow.setPadding(0, UI.dp(this, 2), 0, UI.dp(this, 6));

        TextView dot = UI.text(this, "\u25CF",
                playerColors[currentPlayer], 20, true);
        dot.setPadding(0, 0, UI.dp(this, 8), 0);
        whoRow.addView(dot);

        TextView who = UI.text(this, playerName(currentPlayer) + " ("
                + COLOR_NAMES[currentPlayer % COLOR_NAMES.length] + ") - your turn",
                UI.INK, 16, true);
        whoRow.addView(who);
        content.addView(whoRow);

        status = UI.text(this, "Throw 1 of 3 - swipe to throw!",
                ACCENT, 16, true);
        status.setGravity(Gravity.CENTER);
        status.setPadding(0, 0, 0, UI.dp(this, 8));
        content.addView(status);

        board = new DartboardView(this);
        board.setNextColor(playerColors[currentPlayer]);
        board.setOnThrow(this::recordThrow);
        content.addView(board, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, UI.dp(this, 340)));

        tally = UI.text(this, "This turn: 0 pts", UI.INK_SOFT, 14, true);
        tally.setGravity(Gravity.CENTER);
        tally.setPadding(0, UI.dp(this, 8), 0, UI.dp(this, 10));
        content.addView(tally);

        thrown = 0;
        turnPoints = 0;
    }

    private void recordThrow(int points) {
        thrown++;
        turnPoints += points;
        tally.setText("This turn: " + turnPoints + " pts");

        if (thrown >= DARTS_PER_TURN) {
            board.setLocked(true);
            totals[currentPlayer] += turnPoints;
            status.setText(playerName(currentPlayer) + " scored "
                    + turnPoints + " pts - nice!");
            h.removeCallbacks(advance);
            h.postDelayed(advance, 1100);
        } else {
            status.setText("Throw " + (thrown + 1) + " of " + DARTS_PER_TURN
                    + " - " + points + " points!");
        }
    }

    private final Runnable advance = new Runnable() {
        public void run() {
            currentPlayer = (currentPlayer + 1) % players;
            if (currentPlayer == 0) {
                showStandings();
            } else {
                showTurn();
            }
        }
    };

    private void showStandings() {
        content.removeAllViews();

        TextView title = UI.text(this, "Bullseye champions!", UI.INK, 24, true);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, UI.dp(this, 4), 0, UI.dp(this, 14));
        content.addView(title);

        List<Integer> rank = new ArrayList<>();
        for (int i = 0; i < players; i++) rank.add(i);
        java.util.Collections.sort(rank, (a, b) -> totals[b] - totals[a]);

        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < rank.size(); r++) {
            int p = rank.get(r);
            sb.append(r + 1).append(". ").append(playerName(p))
                    .append(" - ").append(totals[p]).append(" pts");
            if (r == 0) sb.append("  \uD83C\uDFAF");
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
            for (int i = 0; i < players; i++) {
                totals[i] = 0;
            }
            currentPlayer = 0;
            thrown = 0;
            turnPoints = 0;
            showTurn();
        });

        TextView setup = UI.ghost(this, "Back to Setup", 15, 14);
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        slp.topMargin = UI.dp(this, 10);
        content.addView(setup, slp);
        setup.setOnClickListener(v -> showSetup());
    }

    private static final class DartboardView extends View {

        interface OnThrow {
            void onThrow(int points);
        }

        private final Paint ringA;
        private final Paint ringB;
        private final Paint bull;
        private final Paint inner;
        private final Paint dartPaint;
        private final Paint labelPaint;
        private final List<float[]> darts = new ArrayList<>();
        private final List<Integer> dartColors = new ArrayList<>();
        private OnThrow onThrow;
        private boolean enabled = true;
        private boolean dragging;
        private float sx, sy, cpx, cpy;
        private int nextColor = UI.INK;

        DartboardView(Context c) {
            super(c);
            ringA = new Paint(Paint.ANTI_ALIAS_FLAG);
            ringA.setColor(0xFFE4E0D6);
            ringB = new Paint(Paint.ANTI_ALIAS_FLAG);
            ringB.setColor(0xFFCFC9BC);
            bull = new Paint(Paint.ANTI_ALIAS_FLAG);
            bull.setColor(UI.RED);
            inner = new Paint(Paint.ANTI_ALIAS_FLAG);
            inner.setColor(UI.GOLD);

            dartPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            dartPaint.setColor(UI.INK);
            dartPaint.setStrokeWidth(UI.dp(c, 3));
            dartPaint.setStrokeCap(Paint.Cap.ROUND);

            labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            labelPaint.setColor(UI.WHITE);
            labelPaint.setTextSize(UI.dp(c, 13));
            labelPaint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
            labelPaint.setTextAlign(Paint.Align.CENTER);
        }

        void setOnThrow(OnThrow cb) {
            onThrow = cb;
        }

        void setLocked(boolean e) {
            enabled = !e;
        }

        void setNextColor(int color) {
            nextColor = color;
            dartPaint.setColor(color);
            invalidate();
        }

        @Override
        public boolean onTouchEvent(MotionEvent ev) {
            if (!enabled) return true;
            switch (ev.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    dragging = true;
                    sx = ev.getX();
                    sy = ev.getY();
                    cpx = sx;
                    cpy = sy;
                    invalidate();
                    return true;
                case MotionEvent.ACTION_MOVE:
                    if (dragging) {
                        cpx = ev.getX();
                        cpy = ev.getY();
                        invalidate();
                    }
                    return true;
                case MotionEvent.ACTION_UP:
                    if (dragging) {
                        dragging = false;
                        if (Math.abs(ev.getX() - sx) + Math.abs(ev.getY() - sy) >= UI.dp(getContext(), 16)) {
                            throwDart();
                        } else {
                            invalidate();
                        }
                    }
                    return true;
            }
            return true;
        }

        private float throwX() {
            return getWidth() / 2f;
        }

        private float throwY() {
            return getHeight() - UI.dp(getContext(), 14);
        }

        private float boardR() {
            return Math.min(getWidth(), getHeight()) * 0.32f;
        }

        private float maxFlick() {
            return Math.min(getWidth(), getHeight()) * 0.60f;
        }

        private float[] aimTarget() {
            float dx = cpx - sx;
            float dy = cpy - sy;
            float dl = (float) Math.sqrt(dx * dx + dy * dy);
            if (dl < 0.1f) return new float[]{0f, 0f};
            float t = Math.min(1f, dl / maxFlick());
            float nx = dx / dl * t;
            float ny = dy / dl * t;
            float r = (float) Math.sqrt(nx * nx + ny * ny);
            if (r > 1f) {
                nx /= r;
                ny /= r;
            }
            return new float[]{nx, ny};
        }

        private void throwDart() {
            float[] a = aimTarget();
            float nx = a[0];
            float ny = a[1];
            nx += (UI.RND.nextFloat() - 0.5f) * 0.10f;
            ny += (UI.RND.nextFloat() - 0.5f) * 0.10f;
            float r = (float) Math.sqrt(nx * nx + ny * ny);
            if (r > 1f) {
                nx /= r;
                ny /= r;
            }
            darts.add(new float[]{nx, ny});
            dartColors.add(nextColor);
            invalidate();
            if (onThrow != null) onThrow.onThrow(scoreOf(nx, ny));
        }

        private int scoreOf(float nx, float ny) {
            float r = (float) Math.sqrt(nx * nx + ny * ny);
            if (r <= 0.10f) return 50;
            if (r <= 0.22f) return 25;
            if (r <= 0.40f) return 10;
            if (r <= 0.62f) return 5;
            if (r <= 0.84f) return 1;
            return 0;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            float cx = getWidth() / 2f;
            float cy = getHeight() / 2f;
            float R = boardR();

            float[] bounds = {1.0f, 0.84f, 0.62f, 0.40f, 0.22f, 0.10f};
            for (int i = 0; i < bounds.length - 1; i++) {
                boolean even = (i % 2 == 0);
                canvas.drawCircle(cx, cy, R * bounds[i], even ? ringA : ringB);
            }
            canvas.drawCircle(cx, cy, R * 0.10f, bull);
            canvas.drawCircle(cx, cy, R * 0.10f, inner);
            canvas.drawCircle(cx, cy, R * 0.10f * 0.45f, bull);

            labelPaint.setColor(UI.WHITE);
            canvas.drawText("50", cx, cy + UI.dp(getContext(), 4), labelPaint);
            labelPaint.setColor(UI.INK);
            canvas.drawText("25", cx, cy - R * 0.16f, labelPaint);

            for (int i = 0; i < darts.size(); i++) {
                float[] d = darts.get(i);
                float x = cx + d[0] * R;
                float y = cy + d[1] * R;
                dartPaint.setColor(dartColors.get(i));
                canvas.drawLine(x, y - UI.dp(getContext(), 12), x, y, dartPaint);
                canvas.drawCircle(x, y, UI.dp(getContext(), 3), dartPaint);
                canvas.drawLine(x, y - UI.dp(getContext(), 12),
                        x + UI.dp(getContext(), 4), y - UI.dp(getContext(), 18), dartPaint);
                canvas.drawLine(x, y - UI.dp(getContext(), 12),
                        x - UI.dp(getContext(), 4), y - UI.dp(getContext(), 18), dartPaint);
            }

            float aimAngle = (float) (-Math.PI / 2);
            float aimStrength = 0f;
            if (dragging) {
                float dx = cpx - sx;
                float dy = cpy - sy;
                float dl = (float) Math.sqrt(dx * dx + dy * dy);
                if (dl > 0.1f) {
                    aimAngle = (float) Math.atan2(dy, dx);
                    float[] a = aimTarget();
                    aimStrength = (float) Math.sqrt(a[0] * a[0] + a[1] * a[1]);
                }
            }

            if (enabled) {
                drawDart(canvas, throwX(), throwY(), aimAngle, nextColor, aimStrength);
            }
        }

        private void drawDart(Canvas canvas, float x, float y, float angle, int color, float strength) {
            float len = UI.dp(getContext(), 34);
            float tail = len * 0.55f;
            float fin = UI.dp(getContext(), 7);
            canvas.save();
            canvas.translate(x, y);
            canvas.rotate((float) Math.toDegrees(angle));
            dartPaint.setColor(color);
            dartPaint.setStrokeWidth(UI.dp(getContext(), 3));
            canvas.drawLine(-tail + UI.dp(getContext(), 4), -fin, -tail + UI.dp(getContext(), 4), fin, dartPaint);
            canvas.drawLine(-tail + UI.dp(getContext(), 4), -fin, -tail, 0, dartPaint);
            canvas.drawLine(-tail + UI.dp(getContext(), 4), fin, -tail, 0, dartPaint);
            canvas.drawLine(-tail, 0, len * 0.5f, 0, dartPaint);
            android.graphics.Path p = new android.graphics.Path();
            p.moveTo(len, 0);
            p.lineTo(len * 0.5f, -UI.dp(getContext(), 4));
            p.lineTo(len * 0.5f, UI.dp(getContext(), 4));
            p.close();
            canvas.drawPath(p, dartPaint);

            if (strength > 0.05f) {
                float gl = UI.dp(getContext(), 12) + strength * UI.dp(getContext(), 18);
                float tip = len + gl;
                float ah = UI.dp(getContext(), 9);
                dartPaint.setColor(UI.withAlpha(color, 150));
                dartPaint.setStrokeWidth(UI.dp(getContext(), 4));
                canvas.drawLine(len, 0, tip, 0, dartPaint);
                canvas.drawLine(tip, 0, tip - ah, -ah * 0.5f, dartPaint);
                canvas.drawLine(tip, 0, tip - ah, ah * 0.5f, dartPaint);
            }
            canvas.restore();
        }
    }
}
