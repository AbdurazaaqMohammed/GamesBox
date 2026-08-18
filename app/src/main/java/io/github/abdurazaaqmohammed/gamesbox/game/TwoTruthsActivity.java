package io.github.abdurazaaqmohammed.gamesbox.game;

import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import io.github.abdurazaaqmohammed.gamesbox.PlayerNames;
import io.github.abdurazaaqmohammed.gamesbox.ui.UI;

public class TwoTruthsActivity extends BaseGameActivity {

    private static final int ACCENT = UI.ROSE;
    private static final int ACCENT_D = UI.ROSE_D;

    private LinearLayout content;
    private UI.Stepper playersStepper;
    private int players;
    private int[] points;
    private int currentPlayer;
    private int[] votes;
    private TextView[] voteBtns;

    private String playerName(int i) {
        return PlayerNames.get(this, i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        content = UI.screen(this, "Two Truths and a Lie",
                "Spot the fib", ACCENT);
        showSetup();
    }

    private void showSetup() {
        content.removeAllViews();

        TextView rules = UI.text(this,
                "Each player says two truths and one lie. Everyone else taps which claim they think is the lie. If the group catches the fib, they score. If not, the liar scores double.",
                UI.INK_SOFT, 14, false);
        rules.setPadding(0, 0, 0, UI.dp(this, 16));
        content.addView(rules);

        playersStepper = new UI.Stepper(this, "Players", 3, 10, 4, 1);
        content.addView(playersStepper.row);

        content.addView(UI.space(this, 16));

        TextView start = UI.button(this, "Start Game", ACCENT, ACCENT_D, 17, 16);
        content.addView(start, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        start.setOnClickListener(v -> {
            players = playersStepper.get();
            points = new int[players];
            currentPlayer = 0;
            showClaim();
        });
    }

    private void showClaim() {
        content.removeAllViews();

        TextView who = UI.text(this, playerName(currentPlayer) + ": say your\n"
                + "two truths and one lie", UI.INK, 20, true);
        who.setGravity(Gravity.CENTER);
        who.setPadding(0, UI.dp(this, 4), 0, UI.dp(this, 10));
        content.addView(who);

        TextView note = UI.text(this,
                "Remember the order of your claims - 1, 2 and 3 - then hand the phone to the group.",
                UI.INK_SOFT, 14, false);
        note.setGravity(Gravity.CENTER);
        note.setPadding(0, 0, 0, UI.dp(this, 18));
        content.addView(note);

        TextView vote = UI.button(this, "Group: vote on the lie", ACCENT, ACCENT_D, 17, 16);
        content.addView(vote, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        vote.setOnClickListener(v -> showVote());
    }

    private void showVote() {
        votes = new int[3];
        content.removeAllViews();

        TextView ask = UI.text(this, "Which claim was the lie?", UI.INK, 20, true);
        ask.setGravity(Gravity.CENTER);
        ask.setPadding(0, 0, 0, UI.dp(this, 8));
        content.addView(ask);

        TextView note = UI.text(this,
                "Pass the phone around - each person taps the claim they think is a lie.",
                UI.INK_SOFT, 13, false);
        note.setGravity(Gravity.CENTER);
        note.setPadding(0, 0, 0, UI.dp(this, 14));
        content.addView(note);

        voteBtns = new TextView[3];
        for (int i = 0; i < 3; i++) {
            final int idx = i;
            voteBtns[i] = UI.ghost(this, "Claim " + (i + 1), 15, 16);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, UI.dp(this, 3), 0, UI.dp(this, 3));
            content.addView(voteBtns[i], lp);
            voteBtns[i].setOnClickListener(v -> {
                votes[idx]++;
                refreshVotes();
            });
        }

        content.addView(UI.space(this, 8));

        TextView done = UI.button(this, "Done voting", ACCENT, ACCENT_D, 16, 15);
        content.addView(done, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        done.setOnClickListener(v -> showPick());
    }

    private void refreshVotes() {
        for (int i = 0; i < 3; i++) {
            String label = "Claim " + (i + 1);
            if (votes[i] > 0) label += " (" + votes[i] + ")";
            voteBtns[i].setText(label);
        }
    }

    private void showPick() {
        content.removeAllViews();

        TextView ask = UI.text(this, playerName(currentPlayer) + ": which claim\n"
                + "was actually the lie?", UI.INK, 20, true);
        ask.setGravity(Gravity.CENTER);
        ask.setPadding(0, UI.dp(this, 4), 0, UI.dp(this, 14));
        content.addView(ask);

        for (int i = 0; i < 3; i++) {
            final int idx = i;
            TextView b = UI.ghost(this, "Claim " + (i + 1) + " was the lie", 15, 16);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, UI.dp(this, 3), 0, UI.dp(this, 3));
            content.addView(b, lp);
            b.setOnClickListener(v -> pick(idx));
        }
    }

    private void pick(int lie) {
        int best = 0;
        int bestVotes = votes[0];
        for (int i = 1; i < 3; i++) {
            if (votes[i] > bestVotes) {
                best = i;
                bestVotes = votes[i];
            }
        }
        boolean groupWon = best == lie;
        if (groupWon) {
            for (int i = 0; i < players; i++) points[i]++;
        } else {
            points[currentPlayer] += 2;
        }
        showResult(groupWon, lie);
    }

    private void showResult(boolean groupWon, int lie) {
        content.removeAllViews();

        TextView title = UI.text(this, groupWon
                ? "Group wins!"
                : playerName(currentPlayer) + " tricked you!",
                groupWon ? UI.MINT : UI.GOLD, 24, true);
        title.setGravity(Gravity.CENTER);
        content.addView(title);

        TextView detail = UI.text(this, groupWon
                ? "The lie was Claim " + (lie + 1) + " - everyone gets +1."
                : "The lie was Claim " + (lie + 1) + " - the liar gets +2.",
                UI.INK_SOFT, 14, false);
        detail.setGravity(Gravity.CENTER);
        detail.setPadding(0, UI.dp(this, 8), 0, UI.dp(this, 12));
        content.addView(detail);

        TextView tally = UI.text(this, tally(), UI.GOLD, 14, true);
        tally.setGravity(Gravity.CENTER);
        tally.setPadding(0, 0, 0, UI.dp(this, 16));
        content.addView(tally);

        TextView next = UI.button(this, "Next Player", ACCENT, ACCENT_D, 16, 15);
        content.addView(next, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        next.setOnClickListener(v -> {
            currentPlayer = (currentPlayer + 1) % players;
            showClaim();
        });
    }

    private String tally() {
        StringBuilder sb = new StringBuilder("Scores  ");
        for (int i = 0; i < players; i++) {
            sb.append("P").append(i + 1).append(": ").append(points[i]).append("  ");
        }
        return sb.toString();
    }
}
