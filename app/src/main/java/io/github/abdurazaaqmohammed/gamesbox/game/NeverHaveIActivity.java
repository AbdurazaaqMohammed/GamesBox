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

public class NeverHaveIActivity extends BaseGameActivity {

    private static final int ACCENT = UI.MINT;

    private LinearLayout content;
    private final List<Integer> order = new ArrayList<>();
    private int cursor;
    private UI.Stepper playersStepper;
    private UI.Stepper fingersStepper;
    private int players = 4;
    private int startFingers = 10;
    private int[] fingers;
    private TextView[] chips;
    private TextView status;

    private String playerName(int i) {
        return PlayerNames.get(this, i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        content = UI.screen(this, "Never Have I Ever",
                "Finger drop, who's guilty?", ACCENT);
        showSetup();
    }

    private void showSetup() {
        content.removeAllViews();

        TextView rules = UI.text(this,
                "Everyone holds up 10 fingers. Read the statement aloud - anyone who has done it lowers a finger. First to zero loses.",
                UI.INK_SOFT, 14, false);
        rules.setPadding(0, 0, 0, UI.dp(this, 16));
        content.addView(rules);

        playersStepper = new UI.Stepper(this, "Players", 3, 10, 4, 1);
        content.addView(playersStepper.row);

        fingersStepper = new UI.Stepper(this, "Starting fingers", 5, 15, 10, 1);
        content.addView(fingersStepper.row);

        content.addView(UI.space(this, 20));

        TextView start = UI.button(this, "Start Game", UI.MINT, UI.MINT_D, 17, 16);
        content.addView(start, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        start.setOnClickListener(v -> {
            players = playersStepper.get();
            startFingers = fingersStepper.get();
            fingers = new int[players];
            for (int i = 0; i < players; i++) fingers[i] = startFingers;
            buildOrder();
            cursor = 0;
            render();
        });
    }

    private void buildOrder() {
        order.clear();
        for (int i = 0; i < Data.NHIE.length; i++) order.add(i);
        Collections.shuffle(order, UI.RND);
    }

    private void render() {
        content.removeAllViews();

        TextView counter = UI.text(this, "Fingers down: " + countDown() + "/" + players * startFingers,
                UI.GOLD, 13, true);
        counter.setGravity(Gravity.CENTER);
        content.addView(counter);

        TextView statement = UI.card(this,
                "Never have I ever\n" + Data.NHIE[order.get(cursor)],
                UI.INK, 19);
        statement.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        clp.topMargin = UI.dp(this, 8);
        content.addView(statement, clp);

        TextView tapTip = UI.text(this, "Tap the players who have done it (they lose a finger)",
                UI.INK_SOFT, 13, false);
        tapTip.setGravity(Gravity.CENTER);
        tapTip.setPadding(0, UI.dp(this, 12), 0, UI.dp(this, 8));
        content.addView(tapTip);

        chips = new TextView[players];
        LinearLayout row = UI.row(this);
        row.setGravity(Gravity.CENTER_HORIZONTAL);
        int perRow = players <= 4 ? players : 3;
        for (int i = 0; i < players; i++) {
            final int idx = i;
            chips[i] = fingers[i] == 0
                    ? UI.ghost(this, "P" + (i + 1) + "\n" + fingers[i], 13, 10)
                    : UI.button(this, "P" + (i + 1) + "\n" + fingers[i],
                            fingers[i] <= 2 ? UI.RED : UI.MINT,
                            fingers[i] <= 2 ? UI.RED_D : UI.MINT_D,
                            13, 10);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            lp.setMargins(UI.dp(this, 3), UI.dp(this, 3), UI.dp(this, 3), UI.dp(this, 3));
            row.addView(chips[i], lp);
            chips[i].setOnClickListener(v -> {
                if (fingers[idx] > 0) {
                    fingers[idx]--;
                    render();
                }
            });
            if ((i + 1) % perRow == 0) {
                content.addView(row);
                row = UI.row(this);
                row.setGravity(Gravity.CENTER_HORIZONTAL);
            }
        }
        content.addView(row);

        status = UI.text(this, "", UI.GOLD, 15, true);
        status.setGravity(Gravity.CENTER);
        status.setPadding(0, UI.dp(this, 10), 0, 0);
        content.addView(status);
        for (int i = 0; i < players; i++) {
            if (fingers[i] == 0) {
                status.setText(playerName(i) + " is out");
                status.setTextColor(UI.RED);
                break;
            }
        }

        content.addView(UI.space(this, 12));

        if (countDown() == 0) {
            TextView done = UI.text(this, "Everyone is out - game over",
                    UI.GOLD, 18, true);
            done.setGravity(Gravity.CENTER);
            content.addView(done);
            TextView again = UI.button(this, "Play Again", UI.MINT, UI.MINT_D, 16, 15);
            content.addView(again, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            again.setOnClickListener(v -> showSetup());
            return;
        }

        TextView next = UI.button(this, "Next Statement", UI.MINT, UI.MINT_D, 16, 15);
        content.addView(next, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        next.setOnClickListener(v -> {
            cursor++;
            if (cursor >= order.size()) {
                buildOrder();
                cursor = 0;
            }
            render();
        });
    }

    private int countDown() {
        int c = 0;
        for (int f : fingers) c += f;
        return c;
    }
}
