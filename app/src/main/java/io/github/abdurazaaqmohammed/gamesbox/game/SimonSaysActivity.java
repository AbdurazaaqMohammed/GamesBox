package io.github.abdurazaaqmohammed.gamesbox.game;

import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import io.github.abdurazaaqmohammed.gamesbox.PlayerNames;
import io.github.abdurazaaqmohammed.gamesbox.ui.UI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SimonSaysActivity extends BaseGameActivity {

    private static final int ACCENT = UI.RED;
    private static final int ACCENT_D = UI.RED_D;

    private LinearLayout content;
    private UI.Stepper playersStepper;
    private int players;
    private boolean[] alive;
    private boolean[] failed;
    private int aliveCount;
    private int round;
    private final List<Integer> order = new ArrayList<>();
    private int cursor;

    private String playerName(int i) {
        return PlayerNames.get(this, i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        content = UI.screen(this, "Simon Says",
                "One order, everyone obeys", ACCENT);
        for (int i = 0; i < Data.SIMON_COMMANDS.length; i++) order.add(i);
        Collections.shuffle(order, UI.RND);
        showSetup();
    }

    private void showSetup() {
        content.removeAllViews();

        TextView rules = UI.text(this,
                "The phone shows ONE command for everyone: a \"Simon says\" order means everyone should do it, a bare order means everyone must stay still. After everyone acts, mark who failed - wrong players are out. Skip the judging if everyone behaved. Last player standing wins.",
                UI.INK_SOFT, 14, false);
        rules.setPadding(0, 0, 0, UI.dp(this, 16));
        content.addView(rules);

        playersStepper = new UI.Stepper(this, "Players", 2, 10, 4, 1);
        content.addView(playersStepper.row);

        content.addView(UI.space(this, 16));

        TextView start = UI.button(this, "Start Game", ACCENT, ACCENT_D, 17, 16);
        content.addView(start, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        start.setOnClickListener(v -> {
            players = playersStepper.get();
            alive = new boolean[players];
            failed = new boolean[players];
            for (int i = 0; i < players; i++) alive[i] = true;
            aliveCount = players;
            round = 0;
            showRound();
        });
    }

    private void showRound() {
        round++;
        content.removeAllViews();

        TextView roundTitle = UI.text(this, "Round " + round, UI.INK, 14, true);
        roundTitle.setGravity(Gravity.CENTER);
        roundTitle.setPadding(0, UI.dp(this, 2), 0, UI.dp(this, 8));
        content.addView(roundTitle);

        final boolean simon = UI.RND.nextBoolean();
        final String cmd = Data.SIMON_COMMANDS[order.get(cursor)];
        cursor++;
        if (cursor >= order.size()) {
            Collections.shuffle(order, UI.RND);
            cursor = 0;
        }

        TextView c = UI.card(this, simon ? "Simon says " + cmd : cmd, UI.INK, 22);
        content.addView(c, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView note = UI.text(this, simon
                ? "Simon said it - everyone should DO this."
                : "No \"Simon says\" - everyone must STAY STILL.",
                UI.INK_SOFT, 13, false);
        note.setGravity(Gravity.CENTER);
        note.setPadding(0, UI.dp(this, 10), 0, UI.dp(this, 14));
        content.addView(note);

        for (int i = 0; i < players; i++) failed[i] = false;

        TextView ask = UI.text(this, "Who failed to obey?", UI.INK, 15, true);
        ask.setGravity(Gravity.LEFT);
        ask.setPadding(0, 0, 0, UI.dp(this, 8));
        content.addView(ask);

        final TextView[] obeyBtns = new TextView[players];
        final TextView[] failBtns = new TextView[players];
        for (int p = 0; p < players; p++) {
            if (!alive[p]) continue;
            LinearLayout row = UI.row(this);
            row.setGravity(Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            rlp.bottomMargin = UI.dp(this, 8);
            content.addView(row, rlp);

            TextView lbl = UI.text(this, playerName(p), UI.INK, 14, true);
            row.addView(lbl, new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

            final int fp = p;
            TextView obey = UI.button(this, "Obeyed", UI.MINT, UI.MINT_D, 13, 10);
            TextView fail = UI.ghost(this, "Failed", 13, 10);
            obeyBtns[p] = obey;
            failBtns[p] = fail;

            LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            blp.setMargins(UI.dp(this, 3), 0, UI.dp(this, 3), 0);
            row.addView(obey, blp);
            row.addView(fail, blp);

            obey.setOnClickListener(v -> {
                failed[fp] = false;
                refreshJudge(obeyBtns[fp], failBtns[fp], false);
            });
            fail.setOnClickListener(v -> {
                failed[fp] = true;
                refreshJudge(obeyBtns[fp], failBtns[fp], true);
            });
        }

        content.addView(UI.space(this, 4));

        LinearLayout buttons = UI.row(this);
        buttons.setGravity(Gravity.CENTER_HORIZONTAL);
        TextView skip = UI.ghost(this, "Skip - all obeyed", 14, 13);
        TextView end = UI.button(this, "End Round", ACCENT, ACCENT_D, 14, 13);
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        slp.setMargins(UI.dp(this, 3), 0, UI.dp(this, 3), 0);
        buttons.addView(skip, slp);
        buttons.addView(end, slp);
        content.addView(buttons);

        skip.setOnClickListener(v -> nextRound());
        end.setOnClickListener(v -> {
            applyJudgments();
            nextRound();
        });

        content.addView(UI.space(this, 10));
        content.addView(status());
    }

    private void refreshJudge(TextView obey, TextView fail, boolean isFailed) {
        if (isFailed) {
            obey.setBackgroundDrawable(UI.fill(UI.withAlpha(UI.INK, 6), 12, obey.getContext()));
            obey.setTextColor(UI.INK_SOFT);
            fail.setBackgroundDrawable(UI.fill(UI.RED, 12, fail.getContext()));
            fail.setTextColor(UI.WHITE);
        } else {
            obey.setBackgroundDrawable(UI.fill(UI.MINT, 12, obey.getContext()));
            obey.setTextColor(UI.WHITE);
            fail.setBackgroundDrawable(UI.fill(UI.withAlpha(UI.INK, 6), 12, fail.getContext()));
            fail.setTextColor(UI.INK_SOFT);
        }
    }

    private void applyJudgments() {
        boolean anyFailed = false;
        for (int i = 0; i < players; i++) {
            if (alive[i] && failed[i]) anyFailed = true;
        }
        if (!anyFailed) return;
        int eliminated = 0;
        for (int i = 0; i < players; i++) {
            if (alive[i] && failed[i]) {
                alive[i] = false;
                eliminated++;
            }
        }
        if (eliminated >= aliveCount) {
            for (int i = 0; i < players; i++) {
                if (!alive[i]) alive[i] = true;
            }
            return;
        }
        aliveCount -= eliminated;
    }

    private void nextRound() {
        if (aliveCount <= 1) {
            for (int i = 0; i < players; i++) {
                if (alive[i]) {
                    showWinner(i);
                    return;
                }
            }
            showWinner(-1);
            return;
        }
        showRound();
    }

    private TextView status() {
        StringBuilder sb = new StringBuilder("In:  ");
        for (int i = 0; i < players; i++) {
            if (alive[i]) sb.append("P").append(i + 1).append("  ");
        }
        sb.append("\nOut: ");
        for (int i = 0; i < players; i++) {
            if (!alive[i]) sb.append("P").append(i + 1).append("  ");
        }
        TextView s = UI.text(this, sb.toString(), UI.INK_SOFT, 13, true);
        s.setGravity(Gravity.CENTER);
        return s;
    }

    private void showWinner(int winner) {
        content.removeAllViews();

        TextView title = UI.text(this, "Game over!", UI.INK, 26, true);
        title.setGravity(Gravity.CENTER);
        content.addView(title);

        TextView sub = UI.text(this, winner >= 0
                ? playerName(winner) + " wins!\nLast one standing."
                : "Everyone failed. Nobody wins this round.",
                UI.INK_SOFT, 15, false);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, UI.dp(this, 10), 0, UI.dp(this, 16));
        content.addView(sub);

        TextView emoji = UI.text(this, "🏆", UI.INK, 64, false);
        emoji.setGravity(Gravity.CENTER);
        content.addView(emoji);

        content.addView(UI.space(this, 16));

        TextView again = UI.button(this, "Play Again", ACCENT, ACCENT_D, 16, 15);
        content.addView(again, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        again.setOnClickListener(v -> showSetup());
    }
}
