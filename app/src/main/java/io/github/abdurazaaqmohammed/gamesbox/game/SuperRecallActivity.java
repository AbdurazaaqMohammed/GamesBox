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

public class SuperRecallActivity extends BaseGameActivity {

    private static final int ACCENT = UI.GOLD;
    private static final int ACCENT_D = UI.GOLD_D;

    private LinearLayout content;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private UI.Stepper playersStepper;
    private int players;
    private int[] bestLevel;
    private int currentPlayer;
    private boolean[] doneThisRound;

    private List<Integer> sequence;
    private int seqPos;
    private boolean inputMode;
    private boolean turnOver;
    private boolean playbackRunning;
    private LinearLayout gridBox;
    private List<TextView> cells;
    private TextView statusLine;

    private String playerName(int i) {
        return PlayerNames.get(this, i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        content = UI.screen(this, "Super Recall",
                "Memory training", ACCENT);
        showSetup();
    }

    private void showSetup() {
        content.removeAllViews();

        TextView rules = UI.text(this,
                "A grid of symbols flashes in a pattern. Copy it back, tap by tap. Every correct round adds one more symbol. The chain gets longer and longer... who can recall the most?",
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
            bestLevel = new int[players];
            doneThisRound = new boolean[players];
            currentPlayer = 0;
            startTurn();
        });
    }

    private void startTurn() {
        turnOver = false;
        inputMode = false;
        playbackRunning = false;
        handler.removeCallbacks(playbackTask);
        content.removeAllViews();

        TextView who = UI.text(this, playerName(currentPlayer),
                UI.INK, 16, true);
        who.setGravity(Gravity.CENTER);
        who.setPadding(0, UI.dp(this, 2), 0, UI.dp(this, 4));
        content.addView(who);

        TextView hint = UI.text(this, "Watch the pattern. Remember the order!",
                UI.INK_SOFT, 13, false);
        hint.setGravity(Gravity.CENTER);
        hint.setPadding(0, 0, 0, UI.dp(this, 12));
        content.addView(hint);

        gridBox = UI.col(this);
        gridBox.setGravity(Gravity.CENTER);
        content.addView(gridBox);

        statusLine = UI.text(this, "", UI.INK, 15, true);
        statusLine.setGravity(Gravity.CENTER);
        statusLine.setPadding(0, UI.dp(this, 12), 0, UI.dp(this, 12));
        content.addView(statusLine);

        LinearLayout passRow = UI.row(this);
        passRow.setGravity(Gravity.CENTER);
        content.addView(passRow, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView pass = UI.ghost(this, "Pass (end turn)", 14, 13);
        passRow.addView(pass);
        pass.setOnClickListener(v -> {
            if (turnOver) return;
            endTurn(false);
        });

        buildGrid();

        sequence = new ArrayList<>();
        sequence.add(UI.RND.nextInt(9));
        statusLine.setText("Level 1 - watch!");
        handler.postDelayed(playbackTask, 600);
    }

    private void buildGrid() {
        cells = new ArrayList<>();
        for (int r = 0; r < 3; r++) {
            LinearLayout row = UI.row(this);
            row.setGravity(Gravity.CENTER);
            gridBox.addView(row);
            for (int c = 0; c < 3; c++) {
                final int idx = r * 3 + c;
                final TextView cell = UI.text(this, "", UI.INK, 34, true);
                cell.setGravity(Gravity.CENTER);
                cell.setBackgroundDrawable(UI.cardBg(this));
                UI.pressy(cell);
                cell.setOnClickListener(v -> onCellTap(idx));
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        UI.dp(this, 88), UI.dp(this, 88));
                lp.setMargins(UI.dp(this, 4), UI.dp(this, 4), UI.dp(this, 4), UI.dp(this, 4));
                row.addView(cell, lp);
                cells.add(cell);
            }
        }
        List<Integer> symbols = new ArrayList<>();
        for (int i = 0; i < 9; i++) symbols.add(i);
        Collections.shuffle(symbols, UI.RND);
        for (int i = 0; i < 9; i++) {
            cells.get(i).setText(Data.MEMORY_EMOJIS[symbols.get(i)]);
        }
    }

    private final Runnable playbackTask = () -> playbackTask(0);

    private void playbackTask(final int index) {
        if (turnOver) return;
        if (index >= sequence.size()) {
            playbackRunning = false;
            inputMode = true;
            seqPos = 0;
            statusLine.setText("Your turn - repeat the pattern!");
            return;
        }
        playbackRunning = true;
        int cellIdx = sequence.get(index);
        cells.get(cellIdx).setBackgroundDrawable(UI.fill(ACCENT, 14, this));
        cells.get(cellIdx).setTextColor(UI.WHITE);
        UI.popIn(cells.get(cellIdx), 120);
        handler.postDelayed(() -> {
            if (turnOver) return;
            cells.get(sequence.get(index)).setBackgroundDrawable(UI.cardBg(SuperRecallActivity.this));
            cells.get(sequence.get(index)).setTextColor(UI.INK);
            handler.postDelayed(() -> playbackTask(index + 1), 200);
        }, 600);
    }

    private void onCellTap(final int idx) {
        if (turnOver || !inputMode || playbackRunning) return;
        if (idx == sequence.get(seqPos)) {
            cells.get(idx).setBackgroundDrawable(UI.fill(ACCENT, 14, this));
            cells.get(idx).setTextColor(UI.WHITE);
            UI.popIn(cells.get(idx), 100);
            seqPos++;
            if (seqPos >= sequence.size()) {
                inputMode = false;
                statusLine.setText("Level " + sequence.size() + " complete!");
                handler.postDelayed(() -> {
                    if (turnOver) return;
                    sequence.add(UI.RND.nextInt(9));
                    resetCellsBg();
                    statusLine.setText("Level " + sequence.size() + " - watch!");
                    handler.postDelayed(playbackTask, 500);
                }, 700);
            }
        } else {
            UI.shake(cells.get(idx));
            endTurn(true);
        }
    }

    private void resetCellsBg() {
        for (int i = 0; i < cells.size(); i++) {
            cells.get(i).setBackgroundDrawable(UI.cardBg(this));
            cells.get(i).setTextColor(UI.INK);
        }
    }

    private void endTurn(boolean failed) {
        if (turnOver) return;
        turnOver = true;
        inputMode = false;
        playbackRunning = false;
        handler.removeCallbacks(playbackTask);
        int level = sequence.size() - 1;
        if (level > bestLevel[currentPlayer]) bestLevel[currentPlayer] = level;
        doneThisRound[currentPlayer] = true;
        resetCellsBg();

        TextView title = UI.text(this, failed ? "Oops!" : "Turn over",
                failed ? UI.RED : UI.INK, 28, true);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, UI.dp(this, 6), 0, UI.dp(this, 6));
        content.addView(title);

        TextView result = UI.text(this,
                playerName(currentPlayer) + " recalled "
                        + level + " symbol" + (level == 1 ? "" : "s"),
                UI.INK, 18, true);
        result.setGravity(Gravity.CENTER);
        result.setPadding(0, UI.dp(this, 6), 0, UI.dp(this, 16));
        content.addView(result);

        final boolean allDone = allPlayersDone();

        TextView next = UI.button(this, allDone ? "See standings" : "Next Player",
                ACCENT, ACCENT_D, 16, 15);
        content.addView(next, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        next.setOnClickListener(v -> {
            if (allDone) {
                showStandings();
            } else {
                currentPlayer = (currentPlayer + 1) % players;
                startTurn();
            }
        });
    }

    private boolean allPlayersDone() {
        for (boolean d : doneThisRound) {
            if (!d) return false;
        }
        return true;
    }

    private void showStandings() {
        handler.removeCallbacks(playbackTask);
        content.removeAllViews();

        TextView title = UI.text(this, "Memory champs!", UI.INK, 24, true);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, UI.dp(this, 4), 0, UI.dp(this, 14));
        content.addView(title);

        List<Integer> rank = new ArrayList<>();
        for (int i = 0; i < players; i++) rank.add(i);
        Collections.sort(rank, (a, b) -> bestLevel[b] - bestLevel[a]);

        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < rank.size(); r++) {
            int p = rank.get(r);
            sb.append(r + 1).append(". ").append(playerName(p)).append(" - ")
                    .append(bestLevel[p]).append(" symbol").append(bestLevel[p] == 1 ? "" : "s");
            if (r == 0) sb.append("  🧠");
            if (r < rank.size() - 1) sb.append("\n");
        }
        TextView list = UI.text(this, sb, UI.INK, 16, true);
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
            for (int i = 0; i < players; i++) doneThisRound[i] = false;
            currentPlayer = 0;
            startTurn();
        });

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
        handler.removeCallbacks(playbackTask);
    }
}
