package io.github.abdurazaaqmohammed.gamesbox.game;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import io.github.abdurazaaqmohammed.gamesbox.PlayerNames;
import io.github.abdurazaaqmohammed.gamesbox.ui.UI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ImposterActivity extends BaseGameActivity {

    private static final int PHASE_SETUP = 0;
    private static final int PHASE_ROLE = 1;
    private static final int PHASE_CLUE = 2;
    private static final int PHASE_VOTE = 3;
    private static final int PHASE_REVEAL = 4;

    private static final int ACCENT = UI.PURPLE;

    private LinearLayout content;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private static final int MODE_KNOWN = 0;
    private static final int MODE_SECRET = 1;

    private UI.Stepper playersStepper;
    private UI.Stepper imposterStepper;
    private UI.Toggle modeToggle;
    private final List<TextView> packChips = new ArrayList<>();
    private int packIndex = 0;

    private int players = 4;
    private int imposters = 1;
    private int mode = MODE_KNOWN;
    private String imposterWord;

    private int phase = PHASE_SETUP;
    private int round = 1;
    private String word;
    private String categoryName;
    private int categoryPack;
    private int[] imposterPlayers;
    private boolean[] isImposter;
    private final boolean[] seen = new boolean[12];
    private int roleCursor;
    private int clueCursor;
    private int voteCursor;
    private int[] votes;
    private int[] score = new int[12];
    private final Set<String> usedWords = new HashSet<>();

    private String playerName(int i) {
        return PlayerNames.get(this, i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        content = UI.screen(this, "Imposter",
                "Find the faker among you", ACCENT);
        showSetup();
    }

    private void clear() {
        content.removeAllViews();
    }

    private void showSetup() {
        phase = PHASE_SETUP;
        clear();

        TextView rules = UI.text(this,
                "Everyone gets the same secret word - except the imposters. Say a one-word clue each, then vote out the imposter.",
                UI.INK_SOFT, 14, false);
        rules.setPadding(0, 0, 0, UI.dp(this, 14));
        content.addView(rules);

        final TextView modeDesc = UI.text(this, mode == MODE_KNOWN
                ? "Imposters are told they are imposters and only see the category."
                : "Imposters get a different word and don't know they are imposters.",
                UI.INK_SOFT, 12, false);
        modeDesc.setPadding(0, 0, 0, UI.dp(this, 6));
        modeToggle = new UI.Toggle(this, "Imposter mode",
                new String[]{"Imposter knows", "Secret word"}, UI.PURPLE, mode,
                v -> modeDesc.setText(modeToggle.index == MODE_KNOWN
                        ? "Imposters are told they are imposters and only see the category."
                        : "Imposters get a different word and don't know they are imposters."));
        content.addView(modeToggle.row);
        content.addView(modeDesc);

        playersStepper = new UI.Stepper(this, "Players", 3, 10, 4, 1);
        content.addView(playersStepper.row);

        imposterStepper = new UI.Stepper(this, "Imposters", 1, 2, 1, 1);
        content.addView(imposterStepper.row);
        content.addView(space());

        TextView packLabel = UI.text(this, "Word category", UI.INK_SOFT, 12, true);
        content.addView(packLabel);
        content.addView(space());

        LinearLayout chipsRow = UI.row(this);
        chipsRow.setGravity(Gravity.CENTER_HORIZONTAL);
        for (int i = 0; i < Data.PACK_NAMES.length; i++) {
            final int idx = i;
            TextView chip = UI.chipGhost(this, Data.PACK_NAMES[i], UI.INK, 13);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            lp.setMargins(UI.dp(this, 2), UI.dp(this, 3), UI.dp(this, 2), UI.dp(this, 3));
            chipsRow.addView(chip, lp);
            if (i % 2 == 1) {
                content.addView(chipsRow);
                chipsRow = UI.row(this);
                chipsRow.setGravity(Gravity.CENTER_HORIZONTAL);
            }
            chip.setOnClickListener(v -> {
                packIndex = idx;
                refreshPackChips();
            });
            packChips.add(chip);
        }
        content.addView(chipsRow);
        refreshPackChips();
        content.addView(space());

        TextView start = UI.button(this, "Start Round", UI.PURPLE, UI.PURPLE_D, 17, 16);
        content.addView(start, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        start.setOnClickListener(v -> {
            players = playersStepper.get();
            imposters = imposterStepper.get();
            mode = modeToggle.get();
            if (imposters >= players) {
                imposters = Math.max(1, players / 2);
            }
            round = 1;
            score = new int[12];
            startRound();
        });
    }

    private void refreshPackChips() {
        for (int i = 0; i < packChips.size(); i++) {
            packChips.get(i).setTextColor(i == packIndex ? UI.INK : UI.withAlpha(UI.INK, 120));
            packChips.get(i).setBackgroundDrawable(UI.stroke(
                    i == packIndex ? ACCENT : UI.withAlpha(UI.INK, 60), 40, this));
            packChips.get(i).setText("★ " + Data.PACK_NAMES[i]);
        }
    }

    private void startRound() {
        phase = PHASE_ROLE;
        roleCursor = 0;
        isImposter = new boolean[players];

        categoryPack = (packIndex == 0)
                ? 1 + UI.RND.nextInt(Data.IMPOSTER_PACKS.length - 1)
                : packIndex;
        categoryName = Data.PACK_NAMES[categoryPack];

        List<Integer> pool = new ArrayList<>();
        for (int i = 0; i < Data.IMPOSTER_PACKS[categoryPack].length; i++) {
            pool.add(i);
        }
        Collections.shuffle(pool, UI.RND);
        word = null;
        for (int i : pool) {
            String w = Data.IMPOSTER_PACKS[categoryPack][i];
            if (!usedWords.contains(w)) {
                word = w;
                break;
            }
        }
        if (word == null) {
            usedWords.clear();
            word = Data.IMPOSTER_PACKS[categoryPack][pool.get(0)];
        }
        usedWords.add(word);

        imposterWord = null;
        if (mode == MODE_SECRET) {
            String[] pack = Data.IMPOSTER_PACKS[categoryPack];
            int guard = 0;
            do {
                imposterWord = pack[UI.RND.nextInt(pack.length)];
                guard++;
            } while (imposterWord.equals(word) && guard < 20);
        }

        imposterPlayers = new int[imposters];
        List<Integer> idx = new ArrayList<>();
        for (int i = 0; i < players; i++) idx.add(i);
        Collections.shuffle(idx, UI.RND);
        for (int i = 0; i < imposters; i++) {
            imposterPlayers[i] = idx.get(i);
            isImposter[idx.get(i)] = true;
        }

        votes = new int[players];
        for (int i = 0; i < players; i++) votes[i] = -1;
        for (int i = 0; i < 12; i++) seen[i] = false;

        showRole();
    }

    private void showRole() {
        clear();
        TextView roundLabel = UI.text(this, "ROUND " + round, UI.GOLD, 15, true);
        roundLabel.setGravity(Gravity.CENTER);
        content.addView(roundLabel);

        TextView head = UI.text(this, "Pass the phone to\n" + playerName(roleCursor),
                UI.INK, 26, true);
        head.setGravity(Gravity.CENTER);
        head.setPadding(0, UI.dp(this, 14), 0, UI.dp(this, 6));
        content.addView(head);

        TextView tip = UI.text(this,
                "Make sure nobody else can see the screen.\nSwipe up to peek your role.",
                UI.INK_SOFT, 14, false);
        tip.setGravity(Gravity.CENTER);
        content.addView(tip);
        content.addView(space());

        boolean imp = isImposter[roleCursor];
        boolean secret = imp && mode == MODE_SECRET;

        StringBuilder sb = new StringBuilder();
        if (imp && !secret) {
            sb.append("You are the\nIMPOSTER\n\nThe word is about:\n").append(categoryName);
        } else {
            sb.append("Your word is:\n\n").append(secret ? imposterWord : word);
        }
        int accent = (imp && !secret) ? UI.RED : UI.MINT;

        UI.RevealCard card = new UI.RevealCard(this, sb.toString(), accent);
        content.addView(card, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, UI.dp(this, 200)));
        content.addView(space());

        final String next = (roleCursor + 1 >= players) ? "All roles seen — start clues"
                : "Got it — pass to " + playerName(roleCursor + 1);
        final TextView done = UI.button(this, next, UI.MINT, UI.MINT_D, 15, 16);
        done.setVisibility(View.GONE);
        content.addView(done, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        done.setOnClickListener(v -> {
            roleCursor++;
            if (roleCursor >= players) {
                showClueIntro();
            } else {
                showRole();
            }
        });

        card.setOnRevealed(() -> {
            done.setVisibility(View.VISIBLE);
            UI.popIn(done, 200);
        });
    }

    private void showClueIntro() {
        clear();
        TextView head = UI.text(this, "Clue time", UI.INK, 28, true);
        head.setGravity(Gravity.CENTER);
        content.addView(head);

        TextView info = UI.text(this,
                "Word category: " + categoryName + "\n\nEveryone says one word that hints at their secret word."    ,
                UI.INK_SOFT, 16, false);
        info.setGravity(Gravity.CENTER);
        info.setPadding(0, UI.dp(this, 20), 0, 0);
        content.addView(info);
        content.addView(space());

        TextView start = UI.button(this, "Start clues", UI.GOLD, UI.GOLD_D, 17, 16);
        content.addView(start, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        start.setOnClickListener(v -> {
            phase = PHASE_CLUE;
            clueCursor = 0;
            showClueTurn();
        });
    }

    private void showClueTurn() {
        clear();
        TextView head = UI.text(this, playerName(clueCursor), UI.INK, 26, true);
        head.setGravity(Gravity.CENTER);
        content.addView(head);

        TextView tip = UI.text(this, "Say one word that hints at the word.\nEveryone else: listen closely.",
                UI.INK_SOFT, 15, false);
        tip.setGravity(Gravity.CENTER);
        tip.setPadding(0, UI.dp(this, 14), 0, 0);
        content.addView(tip);
        content.addView(space());

        String next = (clueCursor + 1 >= players) ? "All clues done — vote!" : "Next player";
        TextView nxt = UI.button(this, next, UI.GOLD, UI.GOLD_D, 16, 16);
        content.addView(nxt, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        nxt.setOnClickListener(v -> {
            clueCursor++;
            if (clueCursor >= players) {
                phase = PHASE_VOTE;
                voteCursor = 0;
                showVote();
            } else {
                showClueTurn();
            }
        });
    }

    private void showVote() {
        clear();
        TextView head = UI.text(this, playerName(voteCursor) + ",\nwho is the imposter?",
                UI.INK, 24, true);
        head.setGravity(Gravity.CENTER);
        head.setPadding(0, 0, 0, UI.dp(this, 14));
        content.addView(head);

        LinearLayout grid = UI.row(this);
        grid.setGravity(Gravity.CENTER_HORIZONTAL);
        int count = 0;
        for (int i = 0; i < players; i++) {
            if (i == voteCursor) continue;
            final int target = i;
            TextView b = UI.ghost(this, playerName(i), 14, 12);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            lp.setMargins(UI.dp(this, 3), UI.dp(this, 3), UI.dp(this, 3), UI.dp(this, 3));
            grid.addView(b, lp);
            b.setOnClickListener(v -> {
                votes[voteCursor] = target;
                voteCursor++;
                if (voteCursor >= players) {
                    showReveal();
                } else {
                    showVote();
                }
            });
            count++;
            if (count % 3 == 0) {
                content.addView(grid);
                grid = UI.row(this);
                grid.setGravity(Gravity.CENTER_HORIZONTAL);
            }
        }
        content.addView(grid);
    }

    private void showReveal() {
        phase = PHASE_REVEAL;
        clear();

        TextView revealLabel = UI.text(this, "Verdict", UI.GOLD, 15, true);
        revealLabel.setGravity(Gravity.CENTER);
        content.addView(revealLabel);

        TextView wordShow = UI.text(this, "The word was:\n" + word,
                UI.INK, 26, true);
        wordShow.setGravity(Gravity.CENTER);
        wordShow.setPadding(0, UI.dp(this, 10), 0, 0);
        content.addView(wordShow);

        StringBuilder sb = new StringBuilder("Imposter");
        if (imposters > 1) sb.append("s");
        sb.append(": ");
        for (int i = 0; i < imposterPlayers.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(playerName(imposterPlayers[i]));
        }
        TextView imps = UI.text(this, sb.toString(), UI.RED, 18, true);
        imps.setGravity(Gravity.CENTER);
        imps.setPadding(0, UI.dp(this, 8), 0, 0);
        content.addView(imps);

        int[] voteCount = new int[players];
        for (int i = 0; i < players; i++) {
            if (votes[i] >= 0) voteCount[votes[i]]++;
        }
        int maxVotes = 0;
        int maxPlayer = -1;
        boolean tie = false;
        for (int i = 0; i < players; i++) {
            if (voteCount[i] > maxVotes) {
                maxVotes = voteCount[i];
                maxPlayer = i;
                tie = false;
            } else if (voteCount[i] == maxVotes && voteCount[i] > 0) {
                tie = true;
            }
        }

        StringBuilder votesTxt = new StringBuilder();
        for (int i = 0; i < players; i++) {
            votesTxt.append(playerName(i)).append(": ")
                    .append(voteCount[i] == 0 ? "no votes" : voteCount[i] + " vote" + (voteCount[i] > 1 ? "s" : ""));
            if (i < players - 1) votesTxt.append("\n");
        }
        TextView votesShow = UI.text(this, votesTxt.toString(),
                UI.INK_SOFT, 14, false);
        votesShow.setGravity(Gravity.CENTER);
        votesShow.setPadding(0, UI.dp(this, 12), 0, 0);
        content.addView(votesShow);

        boolean caught = false;
        if (!tie && maxPlayer >= 0 && isImposter[maxPlayer]) caught = true;
        TextView outcome = UI.text(this,
                caught ? "Imposter caught" : "The imposter got away",
                caught ? UI.MINT : UI.PINK, 20, true);
        outcome.setGravity(Gravity.CENTER);
        outcome.setPadding(0, UI.dp(this, 12), 0, 0);
        content.addView(outcome);

        for (int i = 0; i < players; i++) {
            if (isImposter[i]) {
                if (!(caught && i == maxPlayer)) score[i]++;
            } else {
                if (caught) score[i]++;
            }
        }

        StringBuilder scoreTxt = new StringBuilder("Scores\n");
        for (int i = 0; i < players; i++) {
            scoreTxt.append(playerName(i)).append(": ").append(score[i]);
            if (i < players - 1) scoreTxt.append("   ");
        }
        TextView scoreShow = UI.text(this, scoreTxt.toString(), UI.INK, 14, true);
        scoreShow.setGravity(Gravity.CENTER);
        scoreShow.setPadding(0, UI.dp(this, 12), 0, 0);
        content.addView(scoreShow);

        content.addView(space());

        TextView next = UI.button(this, "Next Round", UI.PURPLE, UI.PURPLE_D, 16, 15);
        content.addView(next, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        next.setOnClickListener(v -> {
            round++;
            startRound();
        });

        TextView restart = UI.ghost(this, "New Game", 15, 13);
        LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rlp.topMargin = UI.dp(this, 10);
        content.addView(restart, rlp);
        restart.setOnClickListener(v -> showSetup());
    }

    private View space() {
        return UI.space(this, 16);
    }
}
