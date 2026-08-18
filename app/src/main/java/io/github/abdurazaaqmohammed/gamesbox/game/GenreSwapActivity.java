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

public class GenreSwapActivity extends BaseGameActivity {

    private static final int ACCENT = UI.ROSE;
    private static final int ACCENT_D = UI.ROSE_D;

    private LinearLayout content;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private UI.Stepper playersStepper;
    private UI.Toggle timeToggle;
    private int players;
    private int[] scores;
    private int currentPlayer;
    private boolean[] doneThisRound;
    private int roundSeconds;
    private int remaining;
    private TextView timerText;
    private boolean running;

    @Override
    protected boolean isGameInProgress() {
        return running;
    }

    private String playerName(int i) {
        return PlayerNames.get(this, i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        content = UI.screen(this, "Genre Swap",
                "Pitch a movie in a bizarre genre", ACCENT);
        showSetup();
    }

    private void showSetup() {
        running = false;
        content.removeAllViews();

        TextView rules = UI.text(this,
                "Draw a movie and a genre you'd never pair it with - Jaws as a romantic comedy, The Matrix as a kids cartoon. The player has one chance to pitch it as that genre. The group judges how hard they commit.",
                UI.INK_SOFT, 14, false);
        rules.setPadding(0, 0, 0, UI.dp(this, 16));
        content.addView(rules);

        playersStepper = new UI.Stepper(this, "Players", 2, 10, 4, 1);
        content.addView(playersStepper.row);

        timeToggle = new UI.Toggle(this, "Pitch time", new String[]{"20s", "30s", "45s"}, ACCENT, 1);
        content.addView(timeToggle.row);

        content.addView(UI.space(this, 16));

        TextView start = UI.button(this, "Start Game", ACCENT, ACCENT_D, 17, 16);
        content.addView(start, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        start.setOnClickListener(v -> {
            players = playersStepper.get();
            int[] opts = {20, 30, 45};
            roundSeconds = opts[timeToggle.get()];
            scores = new int[players];
            doneThisRound = new boolean[players];
            currentPlayer = 0;
            showPitch();
        });
    }

    private void showPitch() {
        content.removeAllViews();

        TextView who = UI.text(this, playerName(currentPlayer) + " pitches",
                UI.INK, 16, true);
        who.setGravity(Gravity.CENTER);
        who.setPadding(0, UI.dp(this, 4), 0, UI.dp(this, 10));
        content.addView(who);

        String[] swap = Data.GENRE_SWAPS[UI.RND.nextInt(Data.GENRE_SWAPS.length)].split("\\|");

        TextView movie = UI.text(this, swap[0], UI.INK, 30, true);
        movie.setGravity(Gravity.CENTER);
        movie.setPadding(UI.dp(this, 16), UI.dp(this, 14), UI.dp(this, 16), UI.dp(this, 14));
        movie.setBackgroundDrawable(UI.cardBg(this));
        content.addView(movie, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView line = UI.text(this, "as a", UI.INK_SOFT, 13, false);
        line.setGravity(Gravity.CENTER);
        line.setPadding(0, UI.dp(this, 8), 0, UI.dp(this, 2));
        content.addView(line);

        TextView genre = UI.text(this, swap[1], ACCENT, 26, true);
        genre.setGravity(Gravity.CENTER);
        genre.setPadding(UI.dp(this, 16), UI.dp(this, 12), UI.dp(this, 16), UI.dp(this, 12));
        genre.setBackgroundDrawable(UI.fill(ACCENT, 14, this));
        genre.setTextColor(UI.WHITE);
        content.addView(genre, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView tip = UI.text(this, "Pitch " + swap[0] + " as a "
                + swap[1].toLowerCase() + ". Make us believe it.",
                UI.INK_SOFT, 13, false);
        tip.setGravity(Gravity.CENTER);
        tip.setPadding(0, UI.dp(this, 12), 0, UI.dp(this, 16));
        content.addView(tip);

        TextView go = UI.button(this, "Start - " + roundSeconds + " seconds",
                ACCENT, ACCENT_D, 17, 16);
        content.addView(go, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        go.setOnClickListener(v -> countdown());
    }

    private void countdown() {
        running = true;
        remaining = roundSeconds;

        content.removeAllViews();
        timerText = UI.text(this, String.valueOf(remaining), UI.INK, 96, true);
        timerText.setGravity(Gravity.CENTER);
        content.addView(timerText);

        TextView hint = UI.text(this, "PITCH!", UI.INK_SOFT, 16, true);
        hint.setGravity(Gravity.CENTER);
        hint.setPadding(0, 0, 0, UI.dp(this, 16));
        content.addView(hint);

        TextView done = UI.button(this, "Pitch over - judge!", ACCENT, ACCENT_D, 16, 15);
        content.addView(done, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        done.setOnClickListener(v -> endPitch());

        handler.removeCallbacks(tick);
        handler.postDelayed(tick, 1000);
    }

    private final Runnable tick = new Runnable() {
        public void run() {
            if (!running) return;
            remaining--;
            if (remaining > 0) {
                timerText.setText(String.valueOf(remaining));
                if (remaining <= 3) timerText.setTextColor(UI.RED);
                handler.postDelayed(this, 1000);
            } else {
                endPitch();
            }
        }
    };

    private void endPitch() {
        running = false;
        handler.removeCallbacks(tick);

        TextView title = UI.text(this, "How was it?", UI.INK, 24, true);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, UI.dp(this, 6), 0, UI.dp(this, 16));
        content.addView(title);

        TextView nailed = UI.button(this, "Nailed it  +2", UI.MINT, UI.MINT_D, 16, 15);
        content.addView(nailed, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        nailed.setOnClickListener(v -> award(2));

        LinearLayout.LayoutParams mlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mlp.topMargin = UI.dp(this, 10);
        TextView close = UI.button(this, "Close  +1", UI.GOLD, UI.GOLD_D, 16, 15);
        content.addView(close, mlp);
        close.setOnClickListener(v -> award(1));

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

        TextView title = UI.text(this, points == 2 ? "Commendable pitch!" : points == 1 ? "Solid effort." : "Tough crowd.",
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
                showPitch();
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

        TextView title = UI.text(this, "And the Oscar goes to...", UI.INK, 22, true);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, UI.dp(this, 4), 0, UI.dp(this, 14));
        content.addView(title);

        StringBuilder sb = new StringBuilder();
        int[] order = bestToWorst();
        for (int r = 0; r < order.length; r++) {
            int p = order[r];
            sb.append(r + 1).append(". ").append(playerName(p)).append(" - ")
                    .append(scores[p]).append(" pts");
            if (r == 0) sb.append("  🏆");
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
            showPitch();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        running = false;
        handler.removeCallbacks(tick);
    }
}
