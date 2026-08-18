package io.github.abdurazaaqmohammed.gamesbox.game;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.os.Vibrator;
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

public class WordChainActivity extends BaseGameActivity {

    private static final int ACCENT = UI.SLATE;
    private static final int ACCENT_D = UI.SLATE_D;

    private LinearLayout content;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private UI.Stepper playersStepper;
    private int players;
    private boolean running;

    @Override
    protected boolean isGameInProgress() {
        return running;
    }
    private char letter;
    private long startAt;
    private long totalMillis;
    private EditText input;
    private final List<Integer> challengeOrder = new ArrayList<>();
    private int challengeCursor;

    private String playerName(int i) {
        return PlayerNames.get(this, i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        content = UI.screen(this, "Word Chain",
                "Last-letter chain", ACCENT);
        showSetup();
    }

    private void showSetup() {
        running = false;
        content.removeAllViews();

        TextView rules = UI.text(this,
                playerName(0) + " says a word starting with the letter shown and types it in. The next word must start with the last letter of the word before it. A hidden timer buzzes - whoever holds the phone when it goes off does a challenge.",
                UI.INK_SOFT, 14, false);
        rules.setPadding(0, 0, 0, UI.dp(this, 16));
        content.addView(rules);

        playersStepper = new UI.Stepper(this, "Players", 2, 10, 4, 1);
        content.addView(playersStepper.row);

        content.addView(UI.space(this, 16));

        TextView start = UI.button(this, "Start Round", ACCENT, ACCENT_D, 17, 16);
        content.addView(start, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        start.setOnClickListener(v -> {
            players = playersStepper.get();
            newRound();
        });
    }

    private void newRound() {
        running = false;
        handler.removeCallbacks(finish);
        letter = (char) ('A' + UI.RND.nextInt(26));
        content.removeAllViews();

        TextView label = UI.text(this, "Starting letter", UI.INK_SOFT, 13, true);
        label.setGravity(Gravity.CENTER);
        label.setPadding(0, UI.dp(this, 10), 0, UI.dp(this, 8));
        content.addView(label);

        TextView l = UI.text(this, String.valueOf(letter), UI.INK, 72, true);
        l.setGravity(Gravity.CENTER);
        l.setPadding(0, 0, 0, UI.dp(this, 12));
        content.addView(l);

        TextView instr = UI.text(this, playerName(0) + ": say a word starting with " + letter
                + ", type it here, then pass the phone.", UI.INK_SOFT, 14, false);
        instr.setGravity(Gravity.CENTER);
        instr.setPadding(0, 0, 0, UI.dp(this, 16));
        content.addView(instr);

        addInputAndButton();
    }

    private void addInputAndButton() {
        input = new EditText(this);
        input.setHint("Type a word...");
        input.setTextColor(UI.INK);
        input.setHintTextColor(UI.INK_SOFT);
        input.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        input.setGravity(Gravity.CENTER);
        input.setSingleLine(true);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setImeOptions(EditorInfo.IME_ACTION_DONE);
        input.setBackgroundDrawable(UI.cardBgDeep(this));
        input.setPadding(UI.dp(this, 16), UI.dp(this, 13), UI.dp(this, 16), UI.dp(this, 13));
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

        TextView lock = UI.button(this, "Lock it in", ACCENT, ACCENT_D, 16, 15);
        content.addView(lock, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        lock.setOnClickListener(v -> submit());
    }

    private void submit() {
        String w = input.getText().toString().trim();
        if (w.length() == 0) {
            UI.shake(input);
            return;
        }
        char first = w.charAt(0);
        if (Character.toLowerCase(first) != Character.toLowerCase(letter)) {
            input.setHint("Must start with " + letter);
            UI.shake(input);
            return;
        }
        char last = w.charAt(w.length() - 1);
        if (!Character.isLetter(last)) {
            input.setHint("End with a letter");
            UI.shake(input);
            return;
        }
        letter = Character.toUpperCase(last);
        if (!running) {
            startHiddenTimer();
        }
        showNext();
    }

    private void startHiddenTimer() {
        running = true;
        totalMillis = 15000 + UI.RND.nextInt(26000);
        startAt = SystemClock.elapsedRealtime();
        handler.postDelayed(finish, totalMillis);
    }

    private void showNext() {
        content.removeAllViews();
        input = null;
        hideKeyboard();

        TextView label = UI.text(this, "Last letter", UI.INK_SOFT, 13, true);
        label.setGravity(Gravity.CENTER);
        label.setPadding(0, UI.dp(this, 10), 0, UI.dp(this, 8));
        content.addView(label);

        TextView l = UI.text(this, String.valueOf(letter), UI.INK, 72, true);
        l.setGravity(Gravity.CENTER);
        l.setPadding(0, 0, 0, UI.dp(this, 12));
        content.addView(l);

        TextView instr = UI.text(this, "Say a word starting with " + letter
                + ", type it here, then pass the phone.", UI.INK_SOFT, 14, false);
        instr.setGravity(Gravity.CENTER);
        instr.setPadding(0, 0, 0, UI.dp(this, 16));
        content.addView(instr);

        addInputAndButton();
    }

    private final Runnable finish = new Runnable() {
        public void run() {
            if (!running) return;
            running = false;
            timeUp();
        }
    };

    private void timeUp() {
        handler.removeCallbacks(finish);
        bigVibrate();
        content.removeAllViews();
        input = null;

        TextView up = UI.text(this, "Time's up", UI.INK, 30, true);
        up.setGravity(Gravity.CENTER);
        content.addView(up);

        TextView who = UI.text(this, "Whoever is holding the phone\ndoes the challenge:",
                UI.INK_SOFT, 15, false);
        who.setGravity(Gravity.CENTER);
        who.setPadding(0, UI.dp(this, 10), 0, UI.dp(this, 16));
        content.addView(who);

        TextView ch = UI.card(this, nextChallenge(), UI.INK, 18);
        content.addView(ch, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        content.addView(UI.space(this, 16));

        TextView next = UI.button(this, "Next Round", ACCENT, ACCENT_D, 16, 15);
        content.addView(next, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        next.setOnClickListener(v -> newRound());
    }

    private String nextChallenge() {
        if (challengeOrder.isEmpty()) {
            for (int i = 0; i < Data.HOT_POTATO_CHALLENGES.length; i++) challengeOrder.add(i);
            Collections.shuffle(challengeOrder, UI.RND);
            challengeCursor = 0;
        }
        String c = Data.HOT_POTATO_CHALLENGES[challengeOrder.get(challengeCursor)];
        challengeCursor++;
        if (challengeCursor >= challengeOrder.size()) challengeOrder.clear();
        return c;
    }

    private void bigVibrate() {
        try {
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (v != null) {
                v.vibrate(new long[]{0, 400, 120, 400, 120, 500}, -1);
            }
        } catch (Throwable t) {
        }
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        running = false;
        handler.removeCallbacks(finish);
    }
}
