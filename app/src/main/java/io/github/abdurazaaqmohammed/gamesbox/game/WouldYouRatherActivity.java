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

public class WouldYouRatherActivity extends BaseGameActivity {

    private static final int ACCENT = UI.PINK;

    private LinearLayout content;
    private UI.Stepper playersStepper;
    private int players = 5;
    private final List<Integer> order = new ArrayList<>();
    private int cursor;
    private int av;
    private int bv;
    private boolean voted;
    private TextView outcome;

    private String playerName(int i) {
        return PlayerNames.get(this, i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        content = UI.screen(this, "Would You Rather",
                "Pass the phone and pick your poison", ACCENT);
        showSetup();
    }

    private void showSetup() {
        content.removeAllViews();

        TextView rules = UI.text(this,
                "Read the question aloud and tap your answer - you can pick in secret or argue it out. Players take turns, the answers keep score.",
                UI.INK_SOFT, 14, false);
        rules.setPadding(0, 0, 0, UI.dp(this, 16));
        content.addView(rules);

        playersStepper = new UI.Stepper(this, "Players", 2, 10, 5, 1);
        content.addView(playersStepper.row);

        content.addView(UI.space(this, 20));

        TextView start = UI.button(this, "Start Game", UI.PINK, UI.PINK_D, 17, 16);
        content.addView(start, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        start.setOnClickListener(v -> {
            players = playersStepper.get();
            buildOrder();
            render();
        });
    }

    private void buildOrder() {
        order.clear();
        for (int i = 0; i < Data.WYR.length; i++) order.add(i);
        Collections.shuffle(order, UI.RND);
        cursor = 0;
        av = 0;
        bv = 0;
        voted = false;
    }

    private void render() {
        content.removeAllViews();
        final String[] pair = Data.WYR[order.get(cursor)].split("\\|", -1);
        final String optA = pair[0];
        final String optB = pair[1];

        TextView q = UI.text(this, "Would you rather...", UI.GOLD, 18, true);
        q.setGravity(Gravity.CENTER);
        content.addView(q);

        TextView pick = UI.text(this, playerName(cursor % players) + " picks",
                UI.INK_SOFT, 13, false);
        pick.setGravity(Gravity.CENTER);
        pick.setPadding(0, UI.dp(this, 4), 0, UI.dp(this, 16));
        content.addView(pick);

        TextView optAView = makeOption(optA, UI.PINK);
        optAView.setOnClickListener(v -> {
            if (voted) return;
            voted = true;
            av++;
            optAView.setTextColor(UI.WHITE);
            outcome.setText("Option A: " + av + "   •   Option B: " + bv);
            UI.popIn(outcome, 250);
        });
        content.addView(optAView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView or = UI.text(this, "OR", UI.withAlpha(UI.INK, 140), 15, true);
        or.setGravity(Gravity.CENTER);
        or.setPadding(0, UI.dp(this, 12), 0, UI.dp(this, 12));
        content.addView(or);

        final TextView optBView = makeOption(optB, UI.SKY);
        optBView.setOnClickListener(v -> {
            if (voted) return;
            voted = true;
            bv++;
            optBView.setTextColor(UI.WHITE);
            outcome.setText("Option A: " + av + "   •   Option B: " + bv);
            UI.popIn(outcome, 250);
        });
        content.addView(optBView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        outcome = UI.text(this, " ", UI.INK, 16, true);
        outcome.setGravity(Gravity.CENTER);
        outcome.setPadding(0, UI.dp(this, 14), 0, UI.dp(this, 10));
        content.addView(outcome);

        content.addView(UI.space(this, 6));

        TextView next = UI.button(this, "Next Prompt", UI.PINK, UI.PINK_D, 16, 15);
        content.addView(next, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        next.setOnClickListener(v -> {
            av = 0;
            bv = 0;
            voted = false;
            cursor++;
            if (cursor >= order.size()) buildOrder();
            render();
        });
    }

    private TextView makeOption(String text, int color) {
        TextView t = UI.text(this, text, UI.WHITE, 17, true);
        t.setGravity(Gravity.CENTER);
        t.setBackgroundDrawable(UI.fill(color, 12, this));
        t.setPadding(UI.dp(this, 20), UI.dp(this, 20), UI.dp(this, 20), UI.dp(this, 20));
        UI.pressy(t);
        return t;
    }
}
