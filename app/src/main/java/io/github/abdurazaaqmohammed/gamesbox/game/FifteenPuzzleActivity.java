package io.github.abdurazaaqmohammed.gamesbox.game;

import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import io.github.abdurazaaqmohammed.gamesbox.HiScores;
import io.github.abdurazaaqmohammed.gamesbox.ui.UI;

public class FifteenPuzzleActivity extends BaseGameActivity {

    private static final int ACCENT = UI.TEAL;
    private static final int ACCENT_D = UI.TEAL_D;
    private static final String HS_KEY = "fifteen";

    private LinearLayout content;
    private TextView[][] cells;
    private TextView movesText;
    private TextView bestText;
    private TextView statusText;
    private int[] board = new int[16];
    private int emptyIndex;
    private int moves;
    private int best;
    private boolean solved;
    private boolean inGame;

    @Override
    protected boolean isGameInProgress() {
        return inGame;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        content = UI.screen(this, "15 Puzzle", "Slide the tiles into order", ACCENT);
        showSetup();
    }

    private void showSetup() {
        inGame = false;
        content.removeAllViews();

        TextView rules = UI.text(this,
                "Slide the tiles by tapping the tile next to the empty space. Arrange the numbers 1 to 15 in order, left to right and top to bottom, to solve it. Fewer moves is better.",
                UI.INK_SOFT, 14, false);
        rules.setPadding(0, 0, 0, UI.dp(this, 16));
        content.addView(rules);

        TextView start = UI.button(this, "Start Game", ACCENT, ACCENT_D, 17, 16);
        content.addView(start, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        start.setOnClickListener(v -> startGame());
    }

    private void startGame() {
        best = HiScores.get(this, HS_KEY);
        inGame = true;
        content.removeAllViews();

        statusText = UI.text(this, "Slide the tiles", UI.INK, 16, true);
        statusText.setGravity(Gravity.CENTER);
        statusText.setPadding(0, 0, 0, UI.dp(this, 10));
        content.addView(statusText);

        LinearLayout status = UI.row(this);
        status.setGravity(Gravity.CENTER_HORIZONTAL);
        content.addView(status, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        movesText = UI.chip(this, "Moves 0", ACCENT, 14);
        bestText = UI.chip(this, "Best " + (best == 0 ? "-" : best), UI.GOLD, 14);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        lp.setMargins(UI.dp(this, 4), 0, UI.dp(this, 4), 0);
        status.addView(movesText, lp);
        status.addView(bestText, lp);

        content.addView(UI.space(this, 12));

        cells = new TextView[4][4];
        LinearLayout grid = UI.col(this);
        for (int r = 0; r < 4; r++) {
            LinearLayout row = UI.row(this);
            row.setGravity(Gravity.CENTER_HORIZONTAL);
            for (int c = 0; c < 4; c++) {
                final int rr = r;
                final int cc = c;
                TextView t = UI.text(this, "", UI.INK, 22, true);
                t.setGravity(Gravity.CENTER);
                LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(0, UI.dp(this, 66), 1f);
                lp2.setMargins(UI.dp(this, 2), UI.dp(this, 2), UI.dp(this, 2), UI.dp(this, 2));
                row.addView(t, lp2);
                t.setOnClickListener(v -> onTileTap(rr, cc));
                cells[r][c] = t;
            }
            grid.addView(row);
        }
        content.addView(grid, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        content.addView(UI.space(this, 14));

        TextView scramble = UI.ghost(this, "Scramble", 15, 13);
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        content.addView(scramble, sp);
        scramble.setOnClickListener(v -> scramble());

        TextView setup = UI.ghost(this, "Back to Setup", 15, 13);
        LinearLayout.LayoutParams su = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        su.topMargin = UI.dp(this, 8);
        content.addView(setup, su);
        setup.setOnClickListener(v -> showSetup());

        scramble();
    }

    private void scramble() {
        solved = false;
        moves = 0;
        movesText.setText("Moves 0");
        statusText.setText("Slide the tiles");
        statusText.setTextColor(UI.INK);
        for (int i = 0; i < 16; i++) {
            board[i] = i + 1;
        }
        board[15] = 0;
        emptyIndex = 15;
        for (int i = 0; i < 300; i++) {
            int dir = UI.RND.nextInt(4);
            int[] e = {emptyIndex / 4, emptyIndex % 4};
            int nr = e[0] + (dir == 0 ? -1 : dir == 1 ? 1 : 0);
            int nc = e[1] + (dir == 2 ? -1 : dir == 3 ? 1 : 0);
            if (nr < 0 || nr > 3 || nc < 0 || nc > 3) continue;
            swap(emptyIndex, nr * 4 + nc);
        }
        refresh();
        if (isSolved()) scramble();
    }

    private void swap(int a, int b) {
        int t = board[a];
        board[a] = board[b];
        board[b] = t;
        if (board[a] == 0) emptyIndex = a;
        if (board[b] == 0) emptyIndex = b;
    }

    private boolean isSolved() {
        for (int i = 0; i < 15; i++) {
            if (board[i] != i + 1) return false;
        }
        return board[15] == 0;
    }

    private void onTileTap(int r, int c) {
        if (solved) return;
        int idx = r * 4 + c;
        int er = emptyIndex / 4;
        int ec = emptyIndex % 4;
        if (Math.abs(r - er) + Math.abs(c - ec) != 1) return;
        swap(idx, emptyIndex);
        moves++;
        movesText.setText("Moves " + moves);
        refresh();
        if (isSolved()) {
            solved = true;
            if (best == 0 || moves < best) {
                best = moves;
                HiScores.put(this, HS_KEY, best);
            }
            bestText.setText("Best " + best);
            statusText.setText("Solved in " + moves + " moves! \uD83C\uDF89");
            statusText.setTextColor(ACCENT);
        }
    }

    private void refresh() {
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                int v = board[r * 4 + c];
                TextView t = cells[r][c];
                if (v == 0) {
                    t.setText("");
                    t.setBackgroundDrawable(UI.fill(UI.withAlpha(UI.INK, 6), 10, this));
                    t.setTextColor(UI.INK);
                } else {
                    t.setText(String.valueOf(v));
                    t.setBackgroundDrawable(UI.fill(UI.withAlpha(ACCENT, 22), 10, this));
                    t.setTextColor(ACCENT);
                }
            }
        }
    }
}