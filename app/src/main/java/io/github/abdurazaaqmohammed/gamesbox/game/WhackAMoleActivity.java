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

public class WhackAMoleActivity extends BaseGameActivity {

    private static final int ACCENT = UI.OLIVE;
    private static final int ACCENT_D = UI.OLIVE_D;
    private static final int ROUND_SECONDS = 30;

    private static final int[] PALETTE = {UI.RED, UI.BLUE, UI.GOLD, UI.PURPLE, UI.MINT, UI.ORANGE};
    private static final String[] COLOR_NAMES = {"Red", "Blue", "Gold", "Purple", "Mint", "Orange"};

    private LinearLayout content;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private UI.Stepper playersStepper;
    private int players;
    private int[] colors;
    private int colorPickIdx;
    private int[] scores;

    private MoleGridView moles;
    private int remaining;
    private boolean running;
    private TextView timeText;
    private TextView[] scoreChips;

    private String playerName(int i) {
        return PlayerNames.get(this, i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        content = UI.screen(this, "Whack-a-Mole",
                "Everyone, whack your color", ACCENT);
        showSetup();
    }

    private void showSetup() {
        running = false;
        handler.removeCallbacks(tick);
        content.removeAllViews();

        TextView rules = UI.text(this,
                "Everyone plays at the SAME time on one big board. Each player picks a mole color, then moles of every color pop up - whack ONLY your color. Colors nobody picked are decoys, they're just a waste of time. Most points after " + ROUND_SECONDS + " seconds wins!",
                UI.INK_SOFT, 14, false);
        rules.setPadding(0, 0, 0, UI.dp(this, 16));
        content.addView(rules);

        playersStepper = new UI.Stepper(this, "Players", 2, 6, 4, 1);
        content.addView(playersStepper.row);

        content.addView(UI.space(this, 16));

        TextView start = UI.button(this, "Start Game", ACCENT, ACCENT_D, 17, 16);
        content.addView(start, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        start.setOnClickListener(v -> {
            players = playersStepper.get();
            colors = new int[players];
            colorPickIdx = 0;
            showColorPick();
        });
    }

    private void showColorPick() {
        running = false;
        content.removeAllViews();

        TextView who = UI.text(this, playerName(colorPickIdx) + " - pick your mole color",
                UI.INK, 18, true);
        who.setGravity(Gravity.CENTER);
        who.setPadding(0, UI.dp(this, 4), 0, UI.dp(this, 10));
        content.addView(who);

        TextView hint = UI.text(this,
                "You will whack moles of this color only. Tap a color to pick it.",
                UI.INK_SOFT, 14, false);
        hint.setGravity(Gravity.CENTER);
        hint.setLineSpacing(UI.dp(this, 4), 1f);
        hint.setPadding(0, 0, 0, UI.dp(this, 18));
        content.addView(hint);

        for (int r = 0; r < 2; r++) {
            LinearLayout row = UI.row(this);
            row.setGravity(Gravity.TOP);
            for (int c = 0; c < 3; c++) {
                final int idx = r * 3 + c;
                final boolean taken = alreadyTaken(idx);
                TextView swatch = UI.text(this, COLOR_NAMES[idx] + "  ●",
                        taken ? UI.INK_SOFT : PALETTE[idx], 15, true);
                swatch.setGravity(Gravity.CENTER);
                swatch.setPadding(0, UI.dp(this, 18), 0, UI.dp(this, 18));
                swatch.setBackgroundDrawable(UI.fill(
                        taken ? UI.withAlpha(UI.INK, 6) : UI.withAlpha(PALETTE[idx], 24), 16, this));
                if (!taken) {
                    swatch.setOnClickListener(v -> {
                        colors[colorPickIdx] = idx;
                        colorPickIdx++;
                        if (colorPickIdx >= players) {
                            showReady();
                        } else {
                            showColorPick();
                        }
                    });
                }
                row.addView(swatch, new LinearLayout.LayoutParams(
                        0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            }
            content.addView(row);
        }
    }

    private boolean alreadyTaken(int idx) {
        for (int i = 0; i < colorPickIdx; i++) {
            if (colors[i] == idx) return true;
        }
        return false;
    }

    private void showReady() {
        running = false;
        content.removeAllViews();

        TextView title = UI.text(this, "Everyone, get ready!",
                UI.INK, 20, true);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, UI.dp(this, 4), 0, UI.dp(this, 8));
        content.addView(title);

        for (int p = 0; p < players; p++) {
            TextView t = UI.text(this, playerName(p) + "  whacks  "
                    + COLOR_NAMES[colors[p]] + " ●",
                    PALETTE[colors[p]], 15, true);
            t.setGravity(Gravity.CENTER);
            t.setPadding(0, 0, 0, UI.dp(this, 4));
            content.addView(t);
        }

        TextView hint = UI.text(this,
                "One round, " + ROUND_SECONDS + " seconds, whack only your color. Both hands welcome!",
                UI.INK_SOFT, 14, false);
        hint.setGravity(Gravity.CENTER);
        hint.setLineSpacing(UI.dp(this, 4), 1f);
        hint.setPadding(0, UI.dp(this, 10), 0, UI.dp(this, 18));
        content.addView(hint);

        TextView go = UI.button(this, "Let's go!", ACCENT, ACCENT_D, 17, 16);
        content.addView(go, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        go.setOnClickListener(v -> runTurn());
    }

    private int gridRows() {
        int cells = Math.max(16, players * 4);
        return (int) Math.round(Math.sqrt(cells));
    }

    private int gridCols() {
        int cells = Math.max(16, players * 4);
        int rows = gridRows();
        return (int) Math.ceil(cells / (double) rows);
    }

    private void runTurn() {
        running = true;
        remaining = ROUND_SECONDS;
        scores = new int[players];
        content.removeAllViews();

        LinearLayout status = UI.row(this);
        status.setGravity(Gravity.CENTER_HORIZONTAL);
        content.addView(status, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        timeText = UI.chip(this, "Time " + remaining, UI.RED, 14);
        LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        tlp.setMargins(UI.dp(this, 4), 0, UI.dp(this, 4), 0);
        status.addView(timeText, tlp);

        scoreChips = new TextView[players];
        for (int p = 0; p < players; p++) {
            scoreChips[p] = UI.chip(this, "P" + (p + 1) + " 0",
                    PALETTE[colors[p]], 14);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            lp.setMargins(UI.dp(this, 4), 0, UI.dp(this, 4), 0);
            status.addView(scoreChips[p], lp);
        }

        moles = new MoleGridView(this);
        moles.setGrid(gridRows(), gridCols());
        moles.setOnWhack(colorIdx -> {
            int owner = ownerOf(colorIdx);
            if (owner >= 0) {
                scores[owner]++;
                scoreChips[owner].setText("P" + (owner + 1) + " " + scores[owner]);
            }
        });
        content.addView(moles, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, UI.dp(this, 360)));

        content.addView(UI.space(this, 10));

        TextView done = UI.ghost(this, "End early", 14, 13);
        content.addView(done, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        done.setOnClickListener(v -> endRound());

        moles.start();
        handler.removeCallbacks(tick);
        handler.postDelayed(tick, 1000);
    }

    private int ownerOf(int colorIdx) {
        for (int p = 0; p < players; p++) {
            if (colors[p] == colorIdx) return p;
        }
        return -1;
    }

    private final Runnable tick = new Runnable() {
        public void run() {
            if (!running) return;
            remaining--;
            if (remaining > 0) {
                timeText.setText("Time " + remaining);
                if (remaining <= 5) timeText.setTextColor(UI.RED);
                handler.postDelayed(this, 1000);
            } else {
                endRound();
            }
        }
    };

    private void endRound() {
        if (!running) return;
        running = false;
        handler.removeCallbacks(tick);
        if (moles != null) moles.stop();

        content.removeAllViews();

        TextView title = UI.text(this, "Time's up!", UI.INK, 28, true);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, UI.dp(this, 8), 0, UI.dp(this, 14));
        content.addView(title);

        List<Integer> rank = new ArrayList<>();
        for (int i = 0; i < players; i++) rank.add(i);
        java.util.Collections.sort(rank, (a, b) -> scores[b] - scores[a]);

        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < rank.size(); r++) {
            int p = rank.get(r);
            sb.append(r + 1).append(". ").append(playerName(p)).append("  ").append(COLOR_NAMES[colors[p]])
                    .append("  -  ").append(scores[p]).append(" pts");
            if (r == 0) sb.append("  🐹");
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

        TextView again = UI.button(this, "Play Again", ACCENT, ACCENT_D, 16, 15);
        content.addView(again, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        again.setOnClickListener(v -> showReady());

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
        handler.removeCallbacks(tick);
        if (moles != null) moles.stop();
    }

    private static final class MoleGridView extends View {

        interface OnWhack {
            void onWhack(int colorIdx);
        }

        private final Paint grassPaint;
        private final Paint holePaint;
        private final Paint eyePaint;
        private final Paint[] molePaints;
        private final Handler h = new Handler(Looper.getMainLooper());
        private final boolean[] active = new boolean[36];
        private final int[] ticksLeft = new int[36];
        private final int[] flash = new int[36];
        private final int[] moleColor = new int[36];
        private int rows = 4;
        private int cols = 4;
        private boolean running;
        private OnWhack onWhack;

        MoleGridView(Context c) {
            super(c);
            grassPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            grassPaint.setColor(0xFF8BB96B);
            holePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            holePaint.setColor(0xFF4E3B2E);
            eyePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            eyePaint.setColor(0xFF2E2320);
            molePaints = new Paint[6];
            for (int i = 0; i < molePaints.length; i++) {
                molePaints[i] = new Paint(Paint.ANTI_ALIAS_FLAG);
                molePaints[i].setColor(WhackAMoleActivity.PALETTE[i]);
            }
        }

        void setGrid(int r, int c) {
            rows = r;
            cols = c;
        }

        void setOnWhack(OnWhack cb) {
            onWhack = cb;
        }

        void start() {
            if (running) return;
            running = true;
            for (int i = 0; i < rows * cols; i++) {
                active[i] = false;
                ticksLeft[i] = 0;
                flash[i] = 0;
            }
            h.removeCallbacks(loop);
            h.postDelayed(loop, 100);
        }

        void stop() {
            running = false;
            h.removeCallbacks(loop);
        }

        private final Runnable loop = new Runnable() {
            public void run() {
                if (!running) return;
                int cells = rows * cols;
                int activeCount = 0;
                for (int i = 0; i < cells; i++) {
                    if (active[i]) {
                        ticksLeft[i]--;
                        if (ticksLeft[i] <= 0) active[i] = false;
                        else activeCount++;
                    }
                    if (flash[i] > 0) flash[i]--;
                }
                int maxActive = Math.max(3, cells / 4);
                if (activeCount < maxActive && UI.RND.nextFloat() < 0.30f) {
                    int idx = UI.RND.nextInt(cells);
                    if (!active[idx]) {
                        active[idx] = true;
                        ticksLeft[idx] = 8 + UI.RND.nextInt(8);
                        moleColor[idx] = UI.RND.nextInt(6);
                    }
                }
                invalidate();
                h.postDelayed(this, 100);
            }
        };

        @Override
        public boolean onTouchEvent(MotionEvent ev) {
            int action = ev.getActionMasked();
            if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
                int idx = action == MotionEvent.ACTION_DOWN ? 0 : ev.getActionIndex();
                whack(ev.getX(idx), ev.getY(idx));
                return true;
            }
            return true;
        }

        private void whack(float x, float y) {
            if (!running) return;
            int w = getWidth();
            int h = getHeight();
            int col = (int) (x / (w / (float) cols));
            int row = (int) (y / (h / (float) rows));
            if (col < 0 || col >= cols || row < 0 || row >= rows) return;
            int idx = row * cols + col;
            if (active[idx]) {
                active[idx] = false;
                ticksLeft[idx] = 0;
                flash[idx] = 3;
                if (onWhack != null) onWhack.onWhack(moleColor[idx]);
            }
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawRect(0, 0, getWidth(), getHeight(), grassPaint);

            float cw = getWidth() / (float) cols;
            float ch = getHeight() / (float) rows;
            float holeR = Math.min(cw, ch) * 0.28f;

            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    int idx = r * cols + c;
                    float cx = c * cw + cw / 2f;
                    float cy = r * ch + ch / 2f;
                    canvas.drawCircle(cx, cy, holeR, holePaint);

                    if (active[idx]) {
                        float moleR = holeR * 1.25f;
                        canvas.drawCircle(cx, cy - moleR * 0.3f, moleR * 0.9f, molePaints[moleColor[idx]]);
                        float earR = moleR * 0.28f;
                        canvas.drawCircle(cx - moleR * 0.5f, cy - moleR * 1.0f, earR, molePaints[moleColor[idx]]);
                        canvas.drawCircle(cx + moleR * 0.5f, cy - moleR * 1.0f, earR, molePaints[moleColor[idx]]);
                        float eyeR = moleR * 0.16f;
                        canvas.drawCircle(cx - moleR * 0.3f, cy - moleR * 0.45f, eyeR, eyePaint);
                        canvas.drawCircle(cx + moleR * 0.3f, cy - moleR * 0.45f, eyeR, eyePaint);
                    }

                    if (flash[idx] > 0) {
                        canvas.drawCircle(cx, cy - holeR * 0.3f, holeR * 1.3f, molePaints[moleColor[idx]]);
                    }
                }
            }
        }
    }
}
