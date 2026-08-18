package io.github.abdurazaaqmohammed.gamesbox.game;

import android.os.Bundle;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import io.github.abdurazaaqmohammed.gamesbox.PlayerNames;
import io.github.abdurazaaqmohammed.gamesbox.ui.UI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StoryChainActivity extends BaseGameActivity {

    private static final int ACCENT = UI.AMBER;
    private static final int ACCENT_D = UI.AMBER_D;

    private LinearLayout content;
    private UI.Stepper playersStepper;
    private int players;
    private int currentPlayer;
    private int turn;
    private final List<Integer> order = new ArrayList<>();
    private int cursor;
    private String opener;
    private StringBuilder story;
    private EditText input;

    private String playerName(int i) {
        return PlayerNames.get(this, i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        content = UI.screen(this, "Story Chain",
                "Each player adds a sentence", ACCENT);
        for (int i = 0; i < Data.STORY_OPENERS.length; i++) order.add(i);
        Collections.shuffle(order, UI.RND);
        showSetup();
    }

    private void showSetup() {
        content.removeAllViews();

        TextView rules = UI.text(this,
                "A story opening appears. The first player adds a sentence, passes the phone, the next player adds a sentence... until everyone has had a turn. Then read the whole story out loud!",
                UI.INK_SOFT, 14, false);
        rules.setPadding(0, 0, 0, UI.dp(this, 16));
        content.addView(rules);

        playersStepper = new UI.Stepper(this, "Players", 2, 10, 4, 1);
        content.addView(playersStepper.row);

        content.addView(UI.space(this, 16));

        TextView start = UI.button(this, "Start Story", ACCENT, ACCENT_D, 17, 16);
        content.addView(start, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        start.setOnClickListener(v -> {
            players = playersStepper.get();
            newStory();
        });
    }

    private void newStory() {
        opener = Data.STORY_OPENERS[cursor];
        cursor++;
        if (cursor >= Data.STORY_OPENERS.length) {
            Collections.shuffle(order, UI.RND);
            cursor = 0;
        }
        story = new StringBuilder(opener);
        currentPlayer = 0;
        turn = 0;
        showTurn();
    }

    private void showTurn() {
        content.removeAllViews();
        input = null;
        hideKeyboard();

        if (turn >= players) {
            showRead();
            return;
        }

        TextView who = UI.text(this, playerName(currentPlayer)
                + " adds a sentence", UI.INK, 18, true);
        who.setGravity(Gravity.CENTER);
        who.setPadding(0, UI.dp(this, 4), 0, UI.dp(this, 10));
        content.addView(who);

        TextView soFar = UI.text(this, story.toString(), UI.INK_SOFT, 13, false);
        soFar.setPadding(UI.dp(this, 14), UI.dp(this, 12), UI.dp(this, 14), UI.dp(this, 12));
        soFar.setBackgroundDrawable(UI.cardBgDeep(this));
        soFar.setLineSpacing(UI.dp(this, 3), 1f);
        content.addView(soFar, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        content.addView(UI.space(this, 12));

        input = new EditText(this);
        input.setHint("Type your sentence...");
        input.setTextColor(UI.INK);
        input.setHintTextColor(UI.INK_SOFT);
        input.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
        input.setSingleLine(true);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setImeOptions(EditorInfo.IME_ACTION_DONE);
        input.setBackgroundDrawable(UI.cardBgDeep(this));
        input.setPadding(UI.dp(this, 14), UI.dp(this, 13), UI.dp(this, 14), UI.dp(this, 13));
        input.setIncludeFontPadding(false);
        content.addView(input, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        input.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submit();
                return true;
            }
            return false;
        });
        input.requestFocus();
        input.postDelayed(this::showKeyboard, 150);

        content.addView(UI.space(this, 12));

        TextView add = UI.button(this, "Add Sentence", ACCENT, ACCENT_D, 16, 15);
        content.addView(add, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        add.setOnClickListener(v -> submit());
    }

    private void submit() {
        String s = input.getText().toString().trim();
        if (s.length() == 0) {
            UI.shake(input);
            return;
        }
        if (!s.endsWith(".") && !s.endsWith("!") && !s.endsWith("?")) s += ".";
        story.append(" ").append(s);
        currentPlayer = (currentPlayer + 1) % players;
        turn++;
        showTurn();
    }

    private void showRead() {
        content.removeAllViews();

        TextView head = UI.text(this, "Read the story out loud!", UI.INK, 24, true);
        head.setGravity(Gravity.CENTER);
        head.setPadding(0, 0, 0, UI.dp(this, 14));
        content.addView(head);

        TextView full = UI.text(this, story.toString(), UI.INK, 15, false);
        full.setLineSpacing(UI.dp(this, 5), 1f);
        full.setPadding(UI.dp(this, 18), UI.dp(this, 18), UI.dp(this, 18), UI.dp(this, 18));
        full.setBackgroundDrawable(UI.cardBg(this));
        UI.popIn(full, 250);
        content.addView(full, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        content.addView(UI.space(this, 16));

        TextView next = UI.button(this, "New Story", ACCENT, ACCENT_D, 16, 15);
        content.addView(next, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        next.setOnClickListener(v -> newStory());
    }

    private void showKeyboard() {
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null && input != null) imm.showSoftInput(input, 0);
        } catch (Throwable t) {
        }
    }

    private void hideKeyboard() {
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null && getCurrentFocus() != null) {
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }
        } catch (Throwable t) {
        }
    }
}
