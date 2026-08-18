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

public class TruthOrDareActivity extends BaseGameActivity {

    private static final int ACCENT = UI.CORAL;

    private static final int MODE_BOTH = 0;
    private static final int MODE_TRUTH = 1;
    private static final int MODE_DARE = 2;

    private LinearLayout content;
    private UI.Stepper playersStepper;
    private UI.Toggle modeToggle;
    private int players = 4;
    private int mode = MODE_BOTH;
    private int current = 0;
    private TextView promptCard;
    private final List<Integer> truthOrder = new ArrayList<>();
    private final List<Integer> dareOrder = new ArrayList<>();
    private int truthCursor;
    private int dareCursor;

    private String playerName(int i) {
        return PlayerNames.get(this, i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        content = UI.screen(this, "Truth or Dare",
                "Pick your poison", ACCENT);
        showSetup();
    }

    private void showSetup() {
        content.removeAllViews();
        TextView rules = UI.text(this,
                "Take turns picking Truth or Dare.\nBe honest, be brave.",
                UI.INK_SOFT, 14, false);
        rules.setPadding(0, 0, 0, UI.dp(this, 16));
        content.addView(rules);

        playersStepper = new UI.Stepper(this, "Players", 2, 10, 4, 1);
        content.addView(playersStepper.row);

        modeToggle = new UI.Toggle(this, "Mode",
                new String[]{"Both", "Truths only", "Dares only"}, UI.CORAL, mode);
        content.addView(modeToggle.row);

        content.addView(UI.space(this, 20));

        TextView start = UI.button(this, "Start Game", UI.CORAL, UI.CORAL_D, 17, 16);
        content.addView(start, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        start.setOnClickListener(v -> {
            players = playersStepper.get();
            mode = modeToggle.get();
            current = 0;
            truthOrder.clear();
            for (int i = 0; i < Data.TRUTHS.length; i++) truthOrder.add(i);
            Collections.shuffle(truthOrder, UI.RND);
            dareOrder.clear();
            for (int i = 0; i < Data.DARES.length; i++) dareOrder.add(i);
            Collections.shuffle(dareOrder, UI.RND);
            truthCursor = 0;
            dareCursor = 0;
            render();
        });
    }

    private void render() {
        content.removeAllViews();

        TextView who = UI.text(this, playerName(current),
                UI.GOLD, 22, true);
        who.setGravity(Gravity.CENTER);
        content.addView(who);

        TextView pick = UI.text(this, "Pick one:",
                UI.INK_SOFT, 15, false);
        pick.setGravity(Gravity.CENTER);
        pick.setPadding(0, UI.dp(this, 4), 0, UI.dp(this, 14));
        content.addView(pick);

        LinearLayout row = UI.row(this);
        row.setGravity(Gravity.CENTER_HORIZONTAL);

        final TextView truth = UI.button(this, "Truth", UI.SKY, UI.SKY_D, 16, 16);
        final TextView dare = UI.button(this, "Dare", UI.RED, UI.RED_D, 16, 16);

        if (mode == MODE_DARE) {
            LinearLayout.LayoutParams dlp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            row.addView(dare, dlp);
        } else if (mode == MODE_TRUTH) {
            LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            row.addView(truth, tlp);
        } else {
            LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            tlp.setMargins(UI.dp(this, 3), 0, UI.dp(this, 3), 0);
            row.addView(truth, tlp);
            LinearLayout.LayoutParams dlp = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            dlp.setMargins(UI.dp(this, 3), 0, UI.dp(this, 3), 0);
            row.addView(dare, dlp);
        }

        truth.setOnClickListener(v -> {
            String t = Data.TRUTHS[truthOrder.get(truthCursor)];
            truthCursor++;
            if (truthCursor >= truthOrder.size()) {
                Collections.shuffle(truthOrder, UI.RND);
                truthCursor = 0;
            }
            promptCard.setText(t);
            promptCard.setTextColor(UI.SKY);
            UI.bounceIn(promptCard, 300);
        });

        dare.setOnClickListener(v -> {
            String d = Data.DARES[dareCursor];
            dareCursor++;
            if (dareCursor >= dareOrder.size()) {
                Collections.shuffle(dareOrder, UI.RND);
                dareCursor = 0;
            }
            promptCard.setText(d);
            promptCard.setTextColor(UI.RED);
            UI.bounceIn(promptCard, 300);
        });
        content.addView(row);

        promptCard = UI.card(this, "Tap Truth or Dare above",
                UI.INK_SOFT, 18);
        LinearLayout.LayoutParams plp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        plp.topMargin = UI.dp(this, 14);
        content.addView(promptCard, plp);

        content.addView(UI.space(this, 12));

        TextView next = UI.button(this, "Next Player", UI.CORAL, UI.CORAL_D, 16, 15);
        content.addView(next, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        next.setOnClickListener(v -> {
            current = (current + 1) % players;
            render();
        });
    }
}
