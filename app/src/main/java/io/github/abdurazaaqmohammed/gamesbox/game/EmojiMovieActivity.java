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

public class EmojiMovieActivity extends BaseGameActivity {

    private static final int ACCENT = UI.BLUE;

    private LinearLayout content;
    private final List<Integer> order = new ArrayList<>();
    private int cursor;
    private int score;
    private TextView scoreT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        content = UI.screen(this, "Emoji Movie",
                "Guess the movie from the emojis", ACCENT);
        order.clear();
        for (int i = 0; i < Data.EMOJI_MOVIES.length; i++) order.add(i);
        Collections.shuffle(order, UI.RND);
        cursor = 0;
        score = 0;
        render();
    }

    private void render() {
        content.removeAllViews();

        scoreT = UI.text(this, "Score: " + score, UI.GOLD, 15, true);
        scoreT.setGravity(Gravity.CENTER);
        content.addView(scoreT);

        String[] entry = Data.EMOJI_MOVIES[order.get(cursor)];
        final String emoji = entry[0];
        final String answer = entry[1];
        final String wrongA = entry[2];
        final String wrongB = entry[3];
        final String wrongC = entry[4];

        TextView e = UI.text(this, emoji, UI.INK, 40, false);
        e.setGravity(Gravity.CENTER);
        e.setPadding(0, UI.dp(this, 16), 0, UI.dp(this, 16));
        content.addView(e);

        TextView guess = UI.text(this, "Which movie?", UI.INK_SOFT, 15, false);
        guess.setGravity(Gravity.CENTER);
        guess.setPadding(0, 0, 0, UI.dp(this, 12));
        content.addView(guess);

        final List<String> options = new ArrayList<>();
        options.add(answer);
        options.add(wrongA);
        options.add(wrongB);
        options.add(wrongC);
        Collections.shuffle(options, UI.RND);

        final boolean[] answered = {false};
        for (int i = 0; i < 4; i++) {
            final String opt = options.get(i);
            final boolean correct = opt.equals(answer);
            TextView b = UI.ghost(this, opt, 15, 14);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, UI.dp(this, 3), 0, UI.dp(this, 3));
            content.addView(b, lp);
            b.setOnClickListener(v -> {
                if (answered[0]) return;
                answered[0] = true;
                if (correct) {
                    score++;
                    scoreT.setText("Score: " + score);
                    b.setBackgroundDrawable(UI.fill(UI.MINT, 12, EmojiMovieActivity.this));
                    UI.popIn(b, 200);
                } else {
                    b.setBackgroundDrawable(UI.fill(UI.RED, 12, EmojiMovieActivity.this));
                    UI.shake(b);
                }
                for (int i1 = 0; i1 < content.getChildCount(); i1++) {
                    View child = content.getChildAt(i1);
                    if (child instanceof TextView && child != b
                            && ((TextView) child).getText().toString().equals(answer)) {
                        child.setBackgroundDrawable(UI.fill(UI.MINT, 12,
                                EmojiMovieActivity.this));
                    }
                }
                showNext();
            });
        }
    }

    private void showNext() {
        content.postDelayed(() -> {
            cursor++;
            if (cursor >= order.size()) {
                showGameOver();
                return;
            }
            render();
        }, 1200);
    }

    private void showGameOver() {
        content.removeAllViews();
        TextView done = UI.text(this, "Game over", UI.INK, 28, true);
        done.setGravity(Gravity.CENTER);
        content.addView(done);

        TextView finalScore = UI.text(this, "Final score: " + score + " / " + order.size(),
                UI.GOLD, 20, true);
        finalScore.setGravity(Gravity.CENTER);
        finalScore.setPadding(0, UI.dp(this, 10), 0, UI.dp(this, 18));
        content.addView(finalScore);

        TextView again = UI.button(this, "Play Again", UI.BLUE, UI.BLUE_D, 16, 15);
        content.addView(again, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        again.setOnClickListener(v -> {
            Collections.shuffle(order, UI.RND);
            cursor = 0;
            score = 0;
            render();
        });
    }
}
