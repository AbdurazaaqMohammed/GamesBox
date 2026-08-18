package io.github.abdurazaaqmohammed.gamesbox.game;

import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import io.github.abdurazaaqmohammed.gamesbox.PlayerNames;
import io.github.abdurazaaqmohammed.gamesbox.ui.UI;

public class AccentRouletteActivity extends BaseGameActivity {

    private static final int ACCENT = UI.CORAL;
    private static final int ACCENT_D = UI.CORAL_D;

    private LinearLayout content;
    private UI.Stepper playersStepper;
    private int players;
    private int[] scores;
    private int currentPlayer;
    private boolean[] doneThisRound;

    private String playerName(int i) {
        return PlayerNames.get(this, i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        content = UI.screen(this, "Accent Roulette",
                "Deliver the line in a ridiculous accent", ACCENT);
        showSetup();
    }

    private void showSetup() {
        content.removeAllViews();

        TextView rules = UI.text(this,
                "A random phrase meets a random accent - say 'I've scheduled a meeting' like an Italian opera singer, or 'pass the ketchup' like a deep-voiced cowboy. The group judges the commitment.",
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
            scores = new int[players];
            doneThisRound = new boolean[players];
            currentPlayer = 0;
            showLine();
        });
    }

    private void showLine() {
        content.removeAllViews();

        TextView who = UI.text(this, playerName(currentPlayer) + " performs",
                UI.INK, 16, true);
        who.setGravity(Gravity.CENTER);
        who.setPadding(0, UI.dp(this, 4), 0, UI.dp(this, 10));
        content.addView(who);

        String phrase = Data.ACCENT_PHRASES[UI.RND.nextInt(Data.ACCENT_PHRASES.length)];
        String accent = Data.ACCENTS[UI.RND.nextInt(Data.ACCENTS.length)];

        TextView ph = UI.text(this, "\u201C" + phrase + "\u201D", UI.INK, 24, true);
        ph.setGravity(Gravity.CENTER);
        ph.setLineSpacing(UI.dp(this, 4), 1f);
        ph.setPadding(UI.dp(this, 16), UI.dp(this, 14), UI.dp(this, 16), UI.dp(this, 14));
        ph.setBackgroundDrawable(UI.cardBg(this));
        content.addView(ph, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView line = UI.text(this, "as", UI.INK_SOFT, 13, false);
        line.setGravity(Gravity.CENTER);
        line.setPadding(0, UI.dp(this, 8), 0, UI.dp(this, 2));
        content.addView(line);

        TextView acc = UI.text(this, accent, ACCENT, 26, true);
        acc.setGravity(Gravity.CENTER);
        acc.setPadding(UI.dp(this, 16), UI.dp(this, 12), UI.dp(this, 16), UI.dp(this, 12));
        acc.setBackgroundDrawable(UI.fill(ACCENT, 14, this));
        acc.setTextColor(UI.WHITE);
        content.addView(acc, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView tip = UI.text(this, "Say it out loud. Sell it.",
                UI.INK_SOFT, 13, false);
        tip.setGravity(Gravity.CENTER);
        tip.setPadding(0, UI.dp(this, 12), 0, UI.dp(this, 16));
        content.addView(tip);

        TextView go = UI.button(this, "They're done - judge!", ACCENT, ACCENT_D, 17, 16);
        content.addView(go, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        go.setOnClickListener(v -> showJudging());
    }

    private void showJudging() {
        content.removeAllViews();

        TextView title = UI.text(this, "How was it?", UI.INK, 24, true);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, UI.dp(this, 6), 0, UI.dp(this, 16));
        content.addView(title);

        TextView legendary = UI.button(this, "Legendary  +2", UI.MINT, UI.MINT_D, 16, 15);
        content.addView(legendary, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        legendary.setOnClickListener(v -> award(2));

        LinearLayout.LayoutParams mlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mlp.topMargin = UI.dp(this, 10);
        TextView good = UI.button(this, "Good  +1", UI.GOLD, UI.GOLD_D, 16, 15);
        content.addView(good, mlp);
        good.setOnClickListener(v -> award(1));

        TextView flop = UI.ghost(this, "Flop  +0", 16, 15);
        LinearLayout.LayoutParams flp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        flp.topMargin = UI.dp(this, 10);
        content.addView(flop, flp);
        flop.setOnClickListener(v -> award(0));
    }

    private void award(int points) {
        scores[currentPlayer] += points;
        doneThisRound[currentPlayer] = true;

        content.removeAllViews();

        TextView title = UI.text(this,
                points == 2 ? "Bravo!" : points == 1 ? "Not bad." : "Boo.",
                points == 2 ? UI.MINT : points == 1 ? UI.GOLD : UI.INK_SOFT, 26, true);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, UI.dp(this, 6), 0, UI.dp(this, 6));
        content.addView(title);

        TextView tally = UI.text(this, tally(), UI.INK, 15, true);
        tally.setGravity(Gravity.CENTER);
        tally.setPadding(0, UI.dp(this, 6), 0, UI.dp(this, 16));
        content.addView(tally);

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
                showLine();
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
        content.removeAllViews();

        TextView title = UI.text(this, "Theatre night champions!", UI.INK, 22, true);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, UI.dp(this, 4), 0, UI.dp(this, 14));
        content.addView(title);

        StringBuilder sb = new StringBuilder();
        int[] order = bestToWorst();
        for (int r = 0; r < order.length; r++) {
            int p = order[r];
            sb.append(r + 1).append(". ").append(playerName(p)).append(" - ")
                    .append(scores[p]).append(" pts");
            if (r == 0) sb.append("  🎭");
            if (r < order.length - 1) sb.append("\n");
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
            for (int i = 0; i < players; i++) doneThisRound[i] = false;
            currentPlayer = 0;
            showLine();
        });

        TextView setup = UI.ghost(this, "Back to Setup", 15, 14);
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        slp.topMargin = UI.dp(this, 10);
        content.addView(setup, slp);
        setup.setOnClickListener(v -> showSetup());
    }

    private int[] bestToWorst() {
        int[] order = new int[players];
        for (int i = 0; i < players; i++) order[i] = i;
        for (int i = 0; i < players; i++) {
            for (int j = i + 1; j < players; j++) {
                if (scores[order[j]] > scores[order[i]]) {
                    int t = order[i];
                    order[i] = order[j];
                    order[j] = t;
                }
            }
        }
        return order;
    }

    private String tally() {
        StringBuilder sb = new StringBuilder("Score   ");
        for (int i = 0; i < players; i++) {
            sb.append("P").append(i + 1).append(": ").append(scores[i]).append("  ");
        }
        return sb.toString();
    }
}
