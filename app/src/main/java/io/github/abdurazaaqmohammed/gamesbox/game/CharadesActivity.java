package io.github.abdurazaaqmohammed.gamesbox.game;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import io.github.abdurazaaqmohammed.gamesbox.ui.UI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CharadesActivity extends BaseGameActivity {

    private static final int ACCENT = UI.GOLD;
    private static final String[] PACK_LABELS = {"Easy", "Medium", "Hard", "Animals", "Objects", "Mixed"};

    private LinearLayout content;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final List<TextView> packChips = new ArrayList<>();
    private int packIndex = 0;
    private UI.Stepper timerStepper;
    private int seconds = 60;
    private String word;
    private int score;
    private final Set<String> used = new HashSet<>();
    private final List<Integer> order = new ArrayList<>();
    private int cursor;
    private boolean running;
    private int remaining;
    private TextView timerText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        content = UI.screen(this, "Charades",
                "Act it out, no words", ACCENT);
        showSetup();
    }

    private void showSetup() {
        running = false;
        content.removeAllViews();

        TextView rules = UI.text(this,
                "One player secretly reads the word, then acts it out while the timer runs. No talking, no mouthing the words.",
                UI.INK_SOFT, 14, false);
        rules.setPadding(0, 0, 0, UI.dp(this, 14));
        content.addView(rules);

        TextView packLabel = UI.text(this, "Pack", UI.INK_SOFT, 12, true);
        content.addView(packLabel);

        LinearLayout chipsRow = UI.row(this);
        chipsRow.setGravity(Gravity.CENTER_HORIZONTAL);
        for (int i = 0; i < PACK_LABELS.length; i++) {
            final int idx = i;
            TextView chip = UI.chipGhost(this, PACK_LABELS[i], UI.INK, 13);
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
                refreshChips();
            });
            packChips.add(chip);
        }
        content.addView(chipsRow);
        refreshChips();

        timerStepper = new UI.Stepper(this, "Seconds", 20, 120, 60, 10);
        content.addView(timerStepper.row);

        content.addView(UI.space(this, 16));

        TextView start = UI.button(this, "Start Playing", UI.GOLD, UI.GOLD_D, 17, 16);
        content.addView(start, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        start.setOnClickListener(v -> {
            seconds = timerStepper.get();
            score = 0;
            used.clear();
            nextWord();
            showWord();
        });
    }

    private void refreshChips() {
        for (int i = 0; i < packChips.size(); i++) {
            packChips.get(i).setTextColor(i == packIndex ? UI.INK : UI.withAlpha(UI.INK, 120));
            packChips.get(i).setBackgroundDrawable(UI.stroke(
                    i == packIndex ? ACCENT : UI.withAlpha(UI.INK, 60), 40, this));
            packChips.get(i).setText("★ " + PACK_LABELS[i]);
        }
    }

    private String[] currentPack() {
        if (packIndex == 5) {
            return null; // mixed -> random each word
        }
        switch (packIndex) {
            case 1: return Data.CHARADES_MEDIUM;
            case 2: return Data.CHARADES_HARD;
            case 3: return Data.CHARADES_ANIMALS;
            case 4: return Data.CHARADES_OBJECTS;
            default: return Data.CHARADES_EASY;
        }
    }

    private void nextWord() {
        String[] pack = currentPack();
        if (pack == null) {
            String[][] pools = {Data.CHARADES_EASY, Data.CHARADES_MEDIUM,
                    Data.CHARADES_HARD, Data.CHARADES_ANIMALS, Data.CHARADES_OBJECTS};
            pack = pools[UI.RND.nextInt(pools.length)];
        }
        order.clear();
        for (int i = 0; i < pack.length; i++) {
            if (!used.contains(pack[i])) order.add(i);
        }
        if (order.isEmpty()) {
            used.clear();
            for (int i = 0; i < pack.length; i++) order.add(i);
        }
        Collections.shuffle(order, UI.RND);
        word = pack[order.get(0)];
        used.add(word);
    }

    private void showWord() {
        running = false;
        content.removeAllViews();

        TextView scoreT = UI.text(this, "Score: " + score, UI.GOLD, 15, true);
        scoreT.setGravity(Gravity.CENTER);
        content.addView(scoreT);

        TextView cat = UI.text(this, "Pass the phone to the actor",
                UI.INK_SOFT, 14, false);
        cat.setGravity(Gravity.CENTER);
        cat.setPadding(0, UI.dp(this, 10), 0, UI.dp(this, 6));
        content.addView(cat);

        TextView w = UI.text(this, word, UI.INK, 32, true);
        w.setGravity(Gravity.CENTER);
        w.setPadding(0, UI.dp(this, 8), 0, 0);
        content.addView(w);

        TextView hint = UI.text(this, "Act it out, don't say it",
                UI.INK_SOFT, 13, false);
        hint.setGravity(Gravity.CENTER);
        hint.setPadding(0, UI.dp(this, 6), 0, UI.dp(this, 14));
        content.addView(hint);

        TextView start = UI.button(this, "Start Timer", UI.GOLD, UI.GOLD_D, 17, 16);
        content.addView(start, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        start.setOnClickListener(v -> showActing());

        TextView skip = UI.ghost(this, "Skip", 14, 12);
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        slp.topMargin = UI.dp(this, 10);
        content.addView(skip, slp);
        skip.setOnClickListener(v -> {
            nextWord();
            showWord();
        });
    }

    private void showActing() {
        content.removeAllViews();
        running = true;
        remaining = seconds;

        TextView head = UI.text(this, "Act it out", UI.INK, 24, true);
        head.setGravity(Gravity.CENTER);
        content.addView(head);

        TextView hint = UI.text(this, "No words, use gestures only.",
                UI.INK_SOFT, 13, false);
        hint.setGravity(Gravity.CENTER);
        hint.setPadding(0, UI.dp(this, 6), 0, UI.dp(this, 16));
        content.addView(hint);

        timerText = UI.text(this, String.valueOf(remaining), UI.INK, 72, true);
        timerText.setGravity(Gravity.CENTER);
        content.addView(timerText);

        TextView got = UI.button(this, "Got it", UI.MINT, UI.MINT_D, 17, 16);
        content.addView(got, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        got.setOnClickListener(v -> {
            running = false;
            score++;
            nextWord();
            showWord();
        });

        TextView no = UI.ghost(this, "Skip", 15, 13);
        LinearLayout.LayoutParams nlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        nlp.topMargin = UI.dp(this, 10);
        content.addView(no, nlp);
        no.setOnClickListener(v -> {
            running = false;
            nextWord();
            showWord();
        });

        handler.postDelayed(tick, 1000);
    }

    private final Runnable tick = new Runnable() {
        public void run() {
            if (!running) return;
            remaining--;
            if (timerText != null) {
                timerText.setText(String.valueOf(remaining));
                if (remaining <= 5) timerText.setTextColor(UI.RED);
            }
            if (remaining <= 0) {
                running = false;
                timeUp();
                return;
            }
            handler.postDelayed(this, 1000);
        }
    };

    private void timeUp() {
        content.removeAllViews();
        TextView up = UI.text(this, "Time's up", UI.RED, 28, true);
        up.setGravity(Gravity.CENTER);
        content.addView(up);

        TextView who = UI.text(this, "Did anyone guess it?\nThe word was " + word,
                UI.INK_SOFT, 16, false);
        who.setGravity(Gravity.CENTER);
        who.setPadding(0, UI.dp(this, 10), 0, UI.dp(this, 18));
        content.addView(who);

        TextView next = UI.button(this, "Next Word", UI.GOLD, UI.GOLD_D, 16, 15);
        content.addView(next, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        next.setOnClickListener(v -> {
            nextWord();
            showWord();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        running = false;
    }
}
