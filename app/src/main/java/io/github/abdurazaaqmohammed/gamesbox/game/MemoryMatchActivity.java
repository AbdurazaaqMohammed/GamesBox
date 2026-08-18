package io.github.abdurazaaqmohammed.gamesbox.game;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import io.github.abdurazaaqmohammed.gamesbox.PlayerNames;
import io.github.abdurazaaqmohammed.gamesbox.ui.UI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MemoryMatchActivity extends BaseGameActivity {

    private static final int ACCENT = UI.AMBER;
    private static final int ACCENT_D = UI.AMBER_D;
    private static final int COLS = 4;

    private LinearLayout content;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private UI.Toggle sizeToggle;
    private UI.Stepper playersStepper;
    private int players;
    private int rows;
    private int pairs;
    private String[] values;
    private boolean[] matched;
    private int[] flips;
    private int currentPlayer;
    private int firstPick;
    private boolean flipping;
    private int pairsLeft;
    private int flipA;
    private int flipB;
    private TextView[] tiles;
    private TextView statusText;

    private String playerName(int i) {
        return PlayerNames.get(this, i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        content = UI.screen(this, "Memory Match",
                "Flip and match the emoji", ACCENT);
        showSetup();
    }

    private void showSetup() {
        content.removeAllViews();
        handler.removeCallbacks(flipBack);

        TextView rules = UI.text(this,
                "A grid of face-down emoji tiles. On your turn flip two. If they match you keep the pair and go again; if not, flip them back and pass the phone. Fewest total flips to clear the board wins.",
                UI.INK_SOFT, 14, false);
        rules.setPadding(0, 0, 0, UI.dp(this, 16));
        content.addView(rules);

        sizeToggle = new UI.Toggle(this, "Grid size", new String[]{"4x4", "4x6"}, ACCENT, 0);
        content.addView(sizeToggle.row);

        playersStepper = new UI.Stepper(this, "Players", 2, 6, 3, 1);
        content.addView(playersStepper.row);

        content.addView(UI.space(this, 16));

        TextView start = UI.button(this, "Start Round", ACCENT, ACCENT_D, 17, 16);
        content.addView(start, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        start.setOnClickListener(v -> {
            rows = sizeToggle.get() == 0 ? 4 : 6;
            players = playersStepper.get();
            newRound();
        });
    }

    private void newRound() {
        handler.removeCallbacks(flipBack);
        pairs = rows * COLS / 2;
        values = new String[rows * COLS];
        matched = new boolean[rows * COLS];
        flips = new int[players];
        currentPlayer = 0;
        firstPick = -1;
        flipping = false;
        pairsLeft = pairs;
        tiles = new TextView[rows * COLS];

        List<String> pool = new ArrayList<>();
        for (int i = 0; i < Data.MEMORY_EMOJIS.length; i++) pool.add(Data.MEMORY_EMOJIS[i]);
        Collections.shuffle(pool, UI.RND);
        List<String> deck = new ArrayList<>();
        for (int i = 0; i < pairs; i++) {
            deck.add(pool.get(i));
            deck.add(pool.get(i));
        }
        Collections.shuffle(deck, UI.RND);
        for (int i = 0; i < values.length; i++) values[i] = deck.get(i);

        renderBoard();
    }

    private void renderBoard() {
        content.removeAllViews();

        TextView who = UI.text(this, playerName(currentPlayer) + " - flip two tiles",
                UI.INK, 16, true);
        who.setGravity(Gravity.CENTER);
        who.setPadding(0, UI.dp(this, 4), 0, UI.dp(this, 8));
        content.addView(who);

        statusText = UI.text(this, status(), UI.INK_SOFT, 13, true);
        statusText.setGravity(Gravity.CENTER);
        statusText.setPadding(0, 0, 0, UI.dp(this, 10));
        content.addView(statusText);

        LinearLayout grid = UI.col(this);
        for (int r = 0; r < rows; r++) {
            LinearLayout row = UI.row(this);
            row.setGravity(Gravity.TOP);
            for (int c = 0; c < COLS; c++) {
                final int idx = r * COLS + c;
                TextView tile = UI.text(this, "❓", UI.INK_SOFT, 20, false);
                tile.setGravity(Gravity.CENTER);
                tile.setBackgroundDrawable(UI.cardBg(this));
                tile.setIncludeFontPadding(false);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        0, UI.dp(this, 58), 1f);
                lp.setMargins(UI.dp(this, 3), UI.dp(this, 3), UI.dp(this, 3), UI.dp(this, 3));
                row.addView(tile, lp);
                tile.setOnClickListener(v -> onTile(idx));
                tiles[idx] = tile;
            }
            grid.addView(row, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
        content.addView(grid);

        TextView note = UI.text(this, "Tap a tile to flip it. Matched pairs stay open.",
                UI.INK_SOFT, 12, false);
        note.setGravity(Gravity.CENTER);
        note.setPadding(0, UI.dp(this, 8), 0, 0);
        content.addView(note);
    }

    private void onTile(int idx) {
        if (flipping) return;
        if (matched[idx]) return;
        if (idx == firstPick) return;

        flips[currentPlayer]++;
        reveal(idx);

        if (firstPick < 0) {
            firstPick = idx;
            return;
        }
        int a = firstPick;
        firstPick = -1;
        if (values[a].equals(values[idx])) {
            matched[a] = true;
            matched[idx] = true;
            pairsLeft--;
            tiles[a].setBackgroundDrawable(UI.fill(ACCENT, 10, this));
            tiles[idx].setBackgroundDrawable(UI.fill(ACCENT, 10, this));
            updateStatus();
            if (pairsLeft == 0) {
                endGame();
            }
        } else {
            flipping = true;
            flipA = a;
            flipB = idx;
            handler.removeCallbacks(flipBack);
            handler.postDelayed(flipBack, 650);
        }
    }

    private final Runnable flipBack = new Runnable() {
        public void run() {
            hide(flipA);
            hide(flipB);
            flipping = false;
            currentPlayer = (currentPlayer + 1) % players;
            renderBoard();
        }
    };

    private void reveal(int idx) {
        tiles[idx].setText(values[idx]);
        tiles[idx].setTextColor(UI.INK);
        UI.popIn(tiles[idx], 180);
        updateStatus();
    }

    private void hide(int idx) {
        tiles[idx].setText("❓");
        tiles[idx].setTextColor(UI.INK_SOFT);
    }

    private void updateStatus() {
        if (statusText != null) statusText.setText(status());
    }

    private String status() {
        StringBuilder sb = new StringBuilder("Pairs left: ").append(pairsLeft).append("   ");
        for (int i = 0; i < players; i++) {
            sb.append("P").append(i + 1).append(": ").append(flips[i]).append("  ");
        }
        return sb.toString();
    }

    private void endGame() {
        content.removeAllViews();

        int winner = 0;
        for (int i = 1; i < players; i++) {
            if (flips[i] < flips[winner]) winner = i;
        }

        TextView title = UI.text(this, "Cleared!", UI.INK, 26, true);
        title.setGravity(Gravity.CENTER);
        content.addView(title);

        TextView sub = UI.text(this, playerName(winner) + " wins with the fewest flips.",
                UI.INK, 16, true);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, UI.dp(this, 6), 0, UI.dp(this, 16));
        content.addView(sub);

        List<Integer> rank = new ArrayList<>();
        for (int i = 0; i < players; i++) rank.add(i);
        Collections.sort(rank, (a, b) -> flips[a] - flips[b]);
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < rank.size(); r++) {
            int p = rank.get(r);
            sb.append(r + 1).append(". ").append(playerName(p))
                    .append(" - ").append(flips[p]).append(flips[p] == 1 ? " flip" : " flips");
            if (p == winner) sb.append("  🏆");
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
        again.setOnClickListener(v -> newRound());

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
        handler.removeCallbacks(flipBack);
    }
}
