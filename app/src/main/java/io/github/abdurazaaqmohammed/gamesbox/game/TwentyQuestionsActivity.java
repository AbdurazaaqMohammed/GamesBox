package io.github.abdurazaaqmohammed.gamesbox.game;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import io.github.abdurazaaqmohammed.gamesbox.ui.UI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TwentyQuestionsActivity extends BaseGameActivity {

    private static final int ACCENT = UI.INDIGO;
    private static final int ACCENT_D = UI.INDIGO_D;
    private static final int MAX_Q = 20;

    private LinearLayout content;
    private final List<Integer> order = new ArrayList<>();
    private int cursor;
    private String word;
    private int asked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        content = UI.screen(this, "20 Questions",
                "Guess the secret word", ACCENT);
        for (int i = 0; i < Data.PACK_MIXED.length; i++) order.add(i);
        Collections.shuffle(order, UI.RND);
        showSetup();
    }

    private void showSetup() {
        content.removeAllViews();

        TextView rules = UI.text(this,
                "One player secretly looks at a word on the phone. Everyone else asks yes or no questions to narrow it down. Try to guess the word in 20 questions or fewer.",
                UI.INK_SOFT, 14, false);
        rules.setPadding(0, 0, 0, UI.dp(this, 16));
        content.addView(rules);

        content.addView(UI.space(this, 12));

        TextView start = UI.button(this, "Start Game", ACCENT, ACCENT_D, 17, 16);
        content.addView(start, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        start.setOnClickListener(v -> newRound());
    }

    private void newRound() {
        word = Data.PACK_MIXED[order.get(cursor)];
        cursor++;
        if (cursor >= order.size()) {
            Collections.shuffle(order, UI.RND);
            cursor = 0;
        }
        asked = 0;
        showThinker();
    }

    private void showThinker() {
        content.removeAllViews();

        TextView label = UI.text(this, "Secret word", UI.INK_SOFT, 13, true);
        label.setGravity(Gravity.CENTER);
        label.setPadding(0, UI.dp(this, 10), 0, UI.dp(this, 8));
        content.addView(label);

        UI.RevealCard card = new UI.RevealCard(this, word, ACCENT);
        content.addView(card, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, UI.dp(this, 190)));

        TextView note = UI.text(this,
                "Only the thinker looks at this. When ready, hand the phone to the askers.",
                UI.INK_SOFT, 14, false);
        note.setGravity(Gravity.CENTER);
        note.setPadding(0, UI.dp(this, 14), 0, UI.dp(this, 18));
        content.addView(note);

        final TextView go = UI.button(this, "I've seen it - ask away", ACCENT, ACCENT_D, 17, 16);
        go.setVisibility(View.GONE);
        content.addView(go, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        go.setOnClickListener(v -> showQuestion());

        card.setOnRevealed(() -> {
            go.setVisibility(View.VISIBLE);
            UI.popIn(go, 200);
        });
    }

    private void showQuestion() {
        content.removeAllViews();

        TextView progress = UI.text(this, "Question " + (asked + 1) + " of " + MAX_Q,
                UI.INK_SOFT, 13, true);
        progress.setGravity(Gravity.CENTER);
        progress.setPadding(0, 0, 0, UI.dp(this, 8));
        content.addView(progress);

        TextView ask = UI.text(this, "Ask a yes-or-no question\nThinker: tap the truthful answer",
                UI.INK, 18, true);
        ask.setGravity(Gravity.CENTER);
        ask.setPadding(0, 0, 0, UI.dp(this, 16));
        content.addView(ask);

        LinearLayout row = UI.row(this);
        row.setGravity(Gravity.CENTER_HORIZONTAL);
        TextView yes = UI.button(this, "Yes", UI.MINT, UI.MINT_D, 15, 14);
        TextView no = UI.button(this, "No", UI.RED, UI.RED_D, 15, 14);
        TextView maybe = UI.ghost(this, "Maybe", 15, 14);
        TextView[] answers = {yes, no, maybe};
        for (TextView b : answers) {
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            lp.setMargins(UI.dp(this, 3), 0, UI.dp(this, 3), 0);
            row.addView(b, lp);
            b.setOnClickListener(v -> countQuestion());
        }
        content.addView(row);

        content.addView(UI.space(this, 14));

        TextView give = UI.ghost(this, "Give up - reveal the word", 15, 14);
        content.addView(give, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        give.setOnClickListener(v -> showResult(false));

        TextView got = UI.button(this, "Someone guessed it!", UI.GOLD, UI.GOLD_D, 16, 15);
        LinearLayout.LayoutParams glp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        glp.topMargin = UI.dp(this, 10);
        content.addView(got, glp);
        got.setOnClickListener(v -> showResult(true));
    }

    private void countQuestion() {
        asked++;
        if (asked >= MAX_Q) {
            showResult(false);
        } else {
            showQuestion();
        }
    }

    private void showResult(boolean guessed) {
        content.removeAllViews();

        TextView title = UI.text(this, guessed ? "Correct!" : "Out of questions",
                guessed ? UI.MINT : UI.RED, 26, true);
        title.setGravity(Gravity.CENTER);
        content.addView(title);

        TextView sub = UI.text(this, guessed
                ? "Someone guessed the word!"
                : "No more questions - someone take a guess",
                UI.INK_SOFT, 14, false);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, UI.dp(this, 8), 0, UI.dp(this, 16));
        content.addView(sub);

        TextView w = UI.card(this, word, UI.INK, 22);
        content.addView(w, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        content.addView(UI.space(this, 16));

        TextView next = UI.button(this, "Next Word", ACCENT, ACCENT_D, 16, 15);
        content.addView(next, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        next.setOnClickListener(v -> newRound());
    }
}
