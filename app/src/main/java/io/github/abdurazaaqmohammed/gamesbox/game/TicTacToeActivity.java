package io.github.abdurazaaqmohammed.gamesbox.game;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import io.github.abdurazaaqmohammed.gamesbox.HiScores;
import io.github.abdurazaaqmohammed.gamesbox.ui.UI;

public class TicTacToeActivity extends BaseGameActivity {

    private static final int ACCENT = UI.BLUE;
    private static final int ACCENT_D = UI.BLUE_D;
    private static final String HS_KEY = "ttt";
    private static final int EMPTY = 0;
    private static final int PLAYER = 1;
    private static final int AI = 2;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private LinearLayout content;
    private TextView[][] cells;
    private TextView statusText;
    private TextView scoreText;
    private int[] board = new int[9];
    private int difficulty;
    private int streak;
    private int best;
    private int pWins;
    private int aiWins;
    private int draws;
    private boolean playerTurn;
    private boolean gameOver;
    private boolean inGame;

    @Override
    protected boolean isGameInProgress() {
        return inGame;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        content = UI.screen(this, "Tic Tac Toe", "Beat the computer", ACCENT);
        showSetup();
    }

    private void showSetup() {
        handler.removeCallbacksAndMessages(null);
        inGame = false;
        content.removeAllViews();

        TextView rules = UI.text(this,
                "Play X, the computer plays O. Tap a square to place your mark. Get three in a row - across, down or diagonal - to win. Keep winning for a streak.",
                UI.INK_SOFT, 14, false);
        rules.setPadding(0, 0, 0, UI.dp(this, 16));
        content.addView(rules);

        UI.Toggle diff = new UI.Toggle(this, "Difficulty",
                new String[]{"Easy", "Normal", "Hard"}, ACCENT, 1);
        content.addView(diff.row);

        content.addView(UI.space(this, 16));

        TextView start = UI.button(this, "Start Game", ACCENT, ACCENT_D, 17, 16);
        content.addView(start, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        start.setOnClickListener(v -> startGame(diff.get()));
    }

    private void startGame(int d) {
        difficulty = d;
        inGame = true;
        pWins = 0;
        aiWins = 0;
        draws = 0;
        streak = 0;
        best = HiScores.get(this, HS_KEY);
        content.removeAllViews();

        statusText = UI.text(this, "Your turn", UI.INK, 16, true);
        statusText.setGravity(Gravity.CENTER);
        statusText.setPadding(0, 0, 0, UI.dp(this, 10));
        content.addView(statusText);

        scoreText = UI.text(this, "You 0 - 0 AI   Streak 0", UI.INK_SOFT, 13, false);
        scoreText.setGravity(Gravity.CENTER);
        scoreText.setPadding(0, 0, 0, UI.dp(this, 12));
        content.addView(scoreText);

        cells = new TextView[3][3];
        for (int r = 0; r < 3; r++) {
            LinearLayout row = UI.row(this);
            row.setGravity(Gravity.CENTER_HORIZONTAL);
            for (int c = 0; c < 3; c++) {
                final int rr = r;
                final int cc = c;
                TextView t = UI.text(this, "", UI.INK, 40, true);
                t.setGravity(Gravity.CENTER);
                t.setBackgroundDrawable(UI.cardBg(this));
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, UI.dp(this, 96), 1f);
                lp.setMargins(UI.dp(this, 3), UI.dp(this, 3), UI.dp(this, 3), UI.dp(this, 3));
                row.addView(t, lp);
                t.setOnClickListener(v -> onCellTap(rr, cc));
                cells[r][c] = t;
            }
            content.addView(row, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }

        content.addView(UI.space(this, 14));

        TextView again = UI.ghost(this, "New Round", 15, 13);
        LinearLayout.LayoutParams ap = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        content.addView(again, ap);
        again.setOnClickListener(v -> newRound());

        TextView setup = UI.ghost(this, "Back to Setup", 15, 13);
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        sp.topMargin = UI.dp(this, 8);
        content.addView(setup, sp);
        setup.setOnClickListener(v -> showSetup());

        newRound();
    }

    private void newRound() {
        handler.removeCallbacksAndMessages(null);
        for (int i = 0; i < 9; i++) board[i] = EMPTY;
        playerTurn = true;
        gameOver = false;
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                cells[r][c].setText("");
                cells[r][c].setBackgroundDrawable(UI.cardBg(this));
                cells[r][c].setEnabled(true);
            }
        }
        statusText.setText("Your turn");
        statusText.setTextColor(UI.INK);
    }

    private void onCellTap(int r, int c) {
        if (!playerTurn || gameOver) return;
        int idx = r * 3 + c;
        if (board[idx] != EMPTY) return;
        board[idx] = PLAYER;
        cells[r][c].setText("X");
        cells[r][c].setTextColor(ACCENT);
        if (checkEnd()) return;
        playerTurn = false;
        statusText.setText("Computer thinking...");
        handler.postDelayed(this::aiTurn, 450);
    }

    private void aiTurn() {
        if (gameOver) return;
        int idx = bestMove();
        int r = idx / 3;
        int c = idx % 3;
        board[idx] = AI;
        cells[r][c].setText("O");
        cells[r][c].setTextColor(UI.RED);
        if (checkEnd()) return;
        playerTurn = true;
        statusText.setText("Your turn");
        statusText.setTextColor(UI.INK);
    }

    private boolean checkEnd() {
        int w = winner(board);
        if (w == PLAYER) {
            gameOver = true;
            streak++;
            best = HiScores.best(this, HS_KEY, streak);
            pWins++;
            endRound("You win! \uD83C\uDF89", ACCENT);
            return true;
        }
        if (w == AI) {
            gameOver = true;
            streak = 0;
            aiWins++;
            endRound("Computer wins", UI.RED);
            return true;
        }
        if (isFull()) {
            gameOver = true;
            streak = 0;
            draws++;
            endRound("It's a draw", UI.INK_SOFT);
            return true;
        }
        return false;
    }

    private void endRound(String msg, int color) {
        statusText.setText(msg);
        statusText.setTextColor(color);
        scoreText.setText("You " + pWins + " - " + aiWins + " AI   Draws " + draws
                + "   Best streak " + best);
        scoreText.setTextColor(color);
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                cells[r][c].setEnabled(false);
            }
        }
    }

    private int winner(int[] b) {
        int[][] lines = {
                {0, 1, 2}, {3, 4, 5}, {6, 7, 8},
                {0, 3, 6}, {1, 4, 7}, {2, 5, 8},
                {0, 4, 8}, {2, 4, 6}
        };
        for (int[] l : lines) {
            if (b[l[0]] != EMPTY && b[l[0]] == b[l[1]] && b[l[1]] == b[l[2]]) {
                return b[l[0]];
            }
        }
        return EMPTY;
    }

    private boolean isFull() {
        for (int v : board) {
            if (v == EMPTY) return false;
        }
        return true;
    }

    private java.util.List<Integer> empties() {
        java.util.List<Integer> out = new java.util.ArrayList<>();
        for (int i = 0; i < 9; i++) {
            if (board[i] == EMPTY) out.add(i);
        }
        return out;
    }

    private int bestMove() {
        java.util.List<Integer> e = empties();
        if (e.isEmpty()) return -1;
        if (difficulty == 0) {
            return e.get(UI.RND.nextInt(e.size()));
        }
        int aiWin = winningMove(AI);
        if (aiWin >= 0) return aiWin;
        if (difficulty == 2) {
            int playerBlock = winningMove(PLAYER);
            if (playerBlock >= 0) return playerBlock;
            return bestMinimax();
        }
        int block = winningMove(PLAYER);
        if (block >= 0) {
            if (UI.RND.nextInt(3) > 0) return block;
        } else if (UI.RND.nextInt(3) == 0) {
            return e.get(UI.RND.nextInt(e.size()));
        }
        return block >= 0 ? block : e.get(UI.RND.nextInt(e.size()));
    }

    private int winningMove(int who) {
        for (int i = 0; i < 9; i++) {
            if (board[i] == EMPTY) {
                board[i] = who;
                boolean won = winner(board) == who;
                board[i] = EMPTY;
                if (won) return i;
            }
        }
        return -1;
    }

    private int bestMinimax() {
        int bestVal = Integer.MIN_VALUE;
        java.util.List<Integer> bestMoves = new java.util.ArrayList<>();
        for (int i = 0; i < 9; i++) {
            if (board[i] != EMPTY) continue;
            board[i] = AI;
            int v = minimax(0, false);
            board[i] = EMPTY;
            if (v > bestVal) {
                bestVal = v;
                bestMoves.clear();
                bestMoves.add(i);
            } else if (v == bestVal) {
                bestMoves.add(i);
            }
        }
        return bestMoves.get(UI.RND.nextInt(bestMoves.size()));
    }

    private int minimax(int depth, boolean aiTurn) {
        int w = winner(board);
        if (w == AI) return 10 - depth;
        if (w == PLAYER) return depth - 10;
        if (isFull()) return 0;
        java.util.List<Integer> e = empties();
        if (aiTurn) {
            int bestVal = Integer.MIN_VALUE;
            for (int i : e) {
                board[i] = AI;
                bestVal = Math.max(bestVal, minimax(depth + 1, false));
                board[i] = EMPTY;
            }
            return bestVal;
        } else {
            int bestVal = Integer.MAX_VALUE;
            for (int i : e) {
                board[i] = PLAYER;
                bestVal = Math.min(bestVal, minimax(depth + 1, true));
                board[i] = EMPTY;
            }
            return bestVal;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
