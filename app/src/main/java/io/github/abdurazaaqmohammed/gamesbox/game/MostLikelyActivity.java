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

public class MostLikelyActivity extends BaseGameActivity {

    private static final int ACCENT = UI.SKY;

    private LinearLayout content;
    private UI.Stepper playersStepper;
    private int players = 4;
    private int[] points;
    private TextView[] chips;
    private TextView promptCard;
    private final List<Integer> order = new ArrayList<>();
    private int cursor;

    private String playerName(int i) {
        return PlayerNames.get(this, i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        content = UI.screen(this, "Most Likely To",
                "Point at the most likely suspect", ACCENT);
        showSetup();
    }

    private void showSetup() {
        content.removeAllViews();

        TextView rules = UI.text(this,
                "Read the prompt out loud, then everyone points at the player who fits best. The group can also vote with the buttons.",
                UI.INK_SOFT, 14, false);
        rules.setPadding(0, 0, 0, UI.dp(this, 16));
        content.addView(rules);

        playersStepper = new UI.Stepper(this, "Players", 3, 10, 4, 1);
        content.addView(playersStepper.row);

        content.addView(UI.space(this, 20));

        TextView start = UI.button(this, "Start Game", UI.SKY, UI.SKY_D, 17, 16);
        content.addView(start, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        start.setOnClickListener(v -> {
            players = playersStepper.get();
            points = new int[players];
            order.clear();
            for (int i = 0; i < Data.MOST_LIKELY.length; i++) order.add(i);
            Collections.shuffle(order, UI.RND);
            cursor = 0;
            render();
        });
    }

    private void render() {
        content.removeAllViews();

        promptCard = UI.card(this, "Who is most likely\n" + Data.MOST_LIKELY[order.get(cursor)],
                UI.INK, 18);
        content.addView(promptCard, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView tip = UI.text(this, "Tap the player everyone points at (+1 point)",
                UI.INK_SOFT, 13, false);
        tip.setGravity(Gravity.CENTER);
        tip.setPadding(0, UI.dp(this, 12), 0, UI.dp(this, 8));
        content.addView(tip);

        chips = new TextView[players];
        LinearLayout row = UI.row(this);
        row.setGravity(Gravity.CENTER_HORIZONTAL);
        int perRow = players <= 4 ? players : 3;
        for (int i = 0; i < players; i++) {
            final int idx = i;
            chips[i] = points[idx] > 0
                    ? UI.button(this, "P" + (i + 1) + "\n" + points[idx],
                            UI.GOLD, UI.GOLD_D, 13, 10)
                    : UI.ghost(this, "P" + (i + 1) + "\n" + points[idx], 13, 10);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            lp.setMargins(UI.dp(this, 3), UI.dp(this, 3), UI.dp(this, 3), UI.dp(this, 3));
            row.addView(chips[i], lp);
            chips[i].setOnClickListener(v -> {
                points[idx]++;
                render();
            });
            if ((i + 1) % perRow == 0) {
                content.addView(row);
                row = UI.row(this);
                row.setGravity(Gravity.CENTER_HORIZONTAL);
            }
        }
        content.addView(row);

        StringBuilder lead = new StringBuilder();
        int max = -1;
        for (int i = 0; i < players; i++) max = Math.max(max, points[i]);
        if (max > 0) {
            for (int i = 0; i < players; i++) {
                if (points[i] == max) {
                    if (lead.length() > 0) lead.append(", ");
                    lead.append(playerName(i));
                }
            }
        }
        TextView leadT = UI.text(this, lead.length() > 0
                ? "Leading: " + lead : " ",
                UI.GOLD, 14, true);
        leadT.setGravity(Gravity.CENTER);
        leadT.setPadding(0, UI.dp(this, 10), 0, UI.dp(this, 8));
        content.addView(leadT);

        TextView next = UI.button(this, "Next Prompt", UI.SKY, UI.SKY_D, 16, 15);
        content.addView(next, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        next.setOnClickListener(v -> {
            cursor++;
            if (cursor >= order.size()) {
                Collections.shuffle(order, UI.RND);
                cursor = 0;
            }
            render();
        });
    }
}
