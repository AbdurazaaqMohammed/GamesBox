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
import android.view.View;
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

public class RhymeTimeActivity extends BaseGameActivity {

    private static final int ACCENT = UI.PINK;
    private static final int ACCENT_D = UI.PINK_D;

    private LinearLayout content;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private UI.Stepper playersStepper;
    private int players;
    private boolean running;
    private String currentWord;
    private long startAt;
    private long totalMillis;
    private EditText input;
    private TextView override;
    private final List<Integer> challengeOrder = new ArrayList<>();
    private int challengeCursor;
    private final List<Integer> wordOrder = new ArrayList<>();
    private int wordCursor;

    private String playerName(int i) {
        return PlayerNames.get(this, i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        content = UI.screen(this, "Rhyme Time",
                "Pass the rhyming phone", ACCENT);
        for (int i = 0; i < Data.RHYME_WORDS.length; i++) wordOrder.add(i);
        Collections.shuffle(wordOrder, UI.RND);
        showSetup();
    }

    private void showSetup() {
        running = false;
        content.removeAllViews();

        TextView rules = UI.text(this,
                playerName(0) + " reads the word, says a word that rhymes with it out loud and types it in. The next player must rhyme with THAT word. A hidden timer buzzes - whoever holds the phone when it goes off does a challenge.",
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

    private String nextWord() {
        String w = Data.RHYME_WORDS[wordOrder.get(wordCursor)];
        wordCursor++;
        if (wordCursor >= wordOrder.size()) {
            Collections.shuffle(wordOrder, UI.RND);
            wordCursor = 0;
        }
        return w;
    }

    private void newRound() {
        running = false;
        handler.removeCallbacks(finish);
        currentWord = nextWord();
        showRhyme();
    }

    private void showRhyme() {
        content.removeAllViews();
        input = null;
        hideKeyboard();

        TextView label = UI.text(this, "Rhyme with", UI.INK_SOFT, 13, true);
        label.setGravity(Gravity.CENTER);
        label.setPadding(0, UI.dp(this, 10), 0, UI.dp(this, 8));
        content.addView(label);

        TextView w = UI.text(this, currentWord, UI.INK, 56, true);
        w.setGravity(Gravity.CENTER);
        w.setPadding(0, 0, 0, UI.dp(this, 12));
        content.addView(w);

        TextView instr = UI.text(this,
                "Say a word that rhymes with " + currentWord
                        + ", type it here, then pass the phone.",
                UI.INK_SOFT, 14, false);
        instr.setGravity(Gravity.CENTER);
        instr.setPadding(0, 0, 0, UI.dp(this, 16));
        content.addView(instr);

        addInputAndButton();
    }

    private void addInputAndButton() {
        input = new EditText(this);
        input.setHint("Type a rhyming word...");
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
                submit(false);
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
        lock.setOnClickListener(v -> submit(false));

        override = UI.ghost(this, "It rhymes anyway - pass it", 13, 12);
        LinearLayout.LayoutParams olp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        olp.topMargin = UI.dp(this, 10);
        content.addView(override, olp);
        override.setVisibility(View.GONE);
        override.setOnClickListener(v -> submit(true));
    }

    private void submit(boolean forced) {
        String w = input.getText().toString().trim();
        if (w.length() == 0) {
            UI.shake(input);
            return;
        }
        if (!rhymes(w, currentWord)) {
            if (!forced) {
                input.setHint("Hmm - does \"" + w + "\" rhyme with " + currentWord + "?");
                UI.shake(input);
                if (override != null) override.setVisibility(View.VISIBLE);
                return;
            }
        }
        currentWord = w.toLowerCase();
        if (!running) {
            startHiddenTimer();
        }
        showRhyme();
    }

    private void startHiddenTimer() {
        running = true;
        totalMillis = 15000 + UI.RND.nextInt(26000);
        startAt = SystemClock.elapsedRealtime();
        handler.postDelayed(finish, totalMillis);
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
        hideKeyboard();

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

    private static boolean rhymes(String a, String b) {
        String ka = rhymeKey(a);
        String kb = rhymeKey(b);
        return ka.length() > 1 && ka.equals(kb);
    }

    private static String rhymeKey(String w) {
        w = w.toLowerCase().trim();
        if (w.length() > 1 && w.endsWith("e")) w = w.substring(0, w.length() - 1);
        if (w.length() > 1 && w.endsWith("s") && !w.endsWith("ss")) w = w.substring(0, w.length() - 1);
        int i = -1;
        for (int j = w.length() - 1; j >= 0; j--) {
            char c = w.charAt(j);
            if (c == 'a' || c == 'e' || c == 'i' || c == 'o' || c == 'u') {
                i = j;
                break;
            }
        }
        if (i < 0) return w;
        return w.substring(i);
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
