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

import io.github.abdurazaaqmohammed.gamesbox.ui.UI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BullsAndCowsActivity extends BaseGameActivity {

    private static final int ACCENT = UI.CYAN;
    private static final int ACCENT_D = UI.CYAN_D;

    private LinearLayout content;
    private UI.Toggle modeToggle;
    private UI.Stepper digitsStepper;
    private boolean wordMode;
    private int digitCount;
    private String secret;
    private int guesses;
    private StringBuilder history;
    private EditText input;
    private final List<Integer> wordOrder = new ArrayList<>();
    private int wordCursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        content = UI.screen(this, "Bulls and Cows",
                "Crack the code", ACCENT);
        for (int i = 0; i < Data.BULLS_WORDS.length; i++) wordOrder.add(i);
        Collections.shuffle(wordOrder, UI.RND);
        showSetup();
    }

    private void showSetup() {
        content.removeAllViews();

        TextView rules = UI.text(this,
                "One player thinks of a secret number (or word). Everyone else takes turns typing guesses. After each guess the phone shows how close you are: bulls = right character in the right spot, cows = right character in the wrong spot. First to crack it wins.",
                UI.INK_SOFT, 14, false);
        rules.setPadding(0, 0, 0, UI.dp(this, 16));
        content.addView(rules);

        modeToggle = new UI.Toggle(this, "Secret type",
                new String[]{"Numbers", "Words"}, ACCENT, 0);
        content.addView(modeToggle.row);

        digitsStepper = new UI.Stepper(this, "Digits", 3, 5, 4, 1);
        content.addView(digitsStepper.row);

        content.addView(UI.space(this, 16));

        TextView start = UI.button(this, "Start Game", ACCENT, ACCENT_D, 17, 16);
        content.addView(start, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        start.setOnClickListener(v -> {
            wordMode = modeToggle.get() == 1;
            digitCount = digitsStepper.get();
            showSecret();
        });
    }

    private String randomSecret() {
        if (wordMode) {
            String w = Data.BULLS_WORDS[wordOrder.get(wordCursor)];
            wordCursor++;
            if (wordCursor >= wordOrder.size()) {
                Collections.shuffle(wordOrder, UI.RND);
                wordCursor = 0;
            }
            return w;
        }
        List<Integer> digits = new ArrayList<>();
        for (int i = 0; i < 10; i++) digits.add(i);
        Collections.shuffle(digits, UI.RND);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < digitCount; i++) sb.append(digits.get(i));
        return sb.toString();
    }

    private void showSecret() {
        content.removeAllViews();

        TextView label = UI.text(this, "Thinker's turn", UI.INK_SOFT, 13, true);
        label.setGravity(Gravity.CENTER);
        label.setPadding(0, UI.dp(this, 10), 0, UI.dp(this, 8));
        content.addView(label);

        TextView note = UI.text(this, wordMode
                ? "Think of a word with no repeated letters, type it, then pass the phone."
                : "Think of a " + digitCount + "-digit number with no repeated digits, type it, then pass the phone.",
                UI.INK, 16, true);
        note.setGravity(Gravity.CENTER);
        note.setPadding(0, 0, 0, UI.dp(this, 14));
        content.addView(note);

        input = new EditText(this);
        input.setHint(wordMode ? "Type your word..." : "Type your number...");
        input.setTextColor(UI.INK);
        input.setHintTextColor(UI.INK_SOFT);
        input.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        input.setGravity(Gravity.CENTER);
        input.setSingleLine(true);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setImeOptions(EditorInfo.IME_ACTION_DONE);
        input.setBackgroundDrawable(UI.cardBgDeep(this));
        input.setPadding(UI.dp(this, 16), UI.dp(this, 14), UI.dp(this, 16), UI.dp(this, 14));
        input.setIncludeFontPadding(false);
        content.addView(input, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        input.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                lockSecret();
                return true;
            }
            return false;
        });
        input.requestFocus();
        input.postDelayed(this::showKeyboard, 150);

        content.addView(UI.space(this, 14));

        TextView lock = UI.button(this, "Lock it in", ACCENT, ACCENT_D, 16, 15);
        content.addView(lock, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        lock.setOnClickListener(v -> lockSecret());

        TextView random = UI.ghost(this, wordMode ? "Random word" : "Random number", 15, 14);
        LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rlp.topMargin = UI.dp(this, 10);
        content.addView(random, rlp);
        random.setOnClickListener(v -> {
            secret = randomSecret();
            showThinker();
        });
    }

    private void lockSecret() {
        String s = input.getText().toString().trim().toLowerCase();
        if (!validSecret(s)) {
            UI.shake(input);
            return;
        }
        secret = s;
        showThinker();
    }

    private boolean validSecret(String s) {
        if (s.length() == 0) return false;
        if (wordMode) {
            if (s.length() < 3) return false;
        } else {
            if (s.length() != digitCount) return false;
        }
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (wordMode ? !Character.isLetter(c) : !Character.isDigit(c)) return false;
            for (int j = i + 1; j < s.length(); j++) {
                if (s.charAt(j) == c) return false;
            }
        }
        return true;
    }

    private void showThinker() {
        content.removeAllViews();
        input = null;
        hideKeyboard();

        TextView label = UI.text(this, "Your secret", UI.INK_SOFT, 13, true);
        label.setGravity(Gravity.CENTER);
        label.setPadding(0, UI.dp(this, 10), 0, UI.dp(this, 8));
        content.addView(label);

        TextView w = UI.text(this, secret, UI.INK, 40, true);
        w.setGravity(Gravity.CENTER);
        w.setPadding(0, 0, 0, UI.dp(this, 16));
        content.addView(w);

        TextView note = UI.text(this,
                "Only the thinker looks at this. When ready, hand the phone to the guessers.",
                UI.INK_SOFT, 14, false);
        note.setGravity(Gravity.CENTER);
        note.setPadding(0, 0, 0, UI.dp(this, 18));
        content.addView(note);

        TextView go = UI.button(this, "I've seen it - start guessing", ACCENT, ACCENT_D, 17, 16);
        content.addView(go, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        go.setOnClickListener(v -> {
            guesses = 0;
            history = new StringBuilder();
            showGuess();
        });
    }

    private void showGuess() {
        content.removeAllViews();

        TextView progress = UI.text(this, "Guess " + (guesses + 1)
                + (wordMode ? "  (word, no repeats)"
                : "  (" + digitCount + " digits, no repeats)"),
                UI.INK_SOFT, 13, true);
        progress.setGravity(Gravity.CENTER);
        progress.setPadding(0, UI.dp(this, 4), 0, UI.dp(this, 10));
        content.addView(progress);

        input = new EditText(this);
        input.setHint("Type your guess...");
        input.setTextColor(UI.INK);
        input.setHintTextColor(UI.INK_SOFT);
        input.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        input.setGravity(Gravity.CENTER);
        input.setSingleLine(true);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setImeOptions(EditorInfo.IME_ACTION_DONE);
        input.setBackgroundDrawable(UI.cardBgDeep(this));
        input.setPadding(UI.dp(this, 16), UI.dp(this, 14), UI.dp(this, 16), UI.dp(this, 14));
        input.setIncludeFontPadding(false);
        content.addView(input, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        input.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submitGuess();
                return true;
            }
            return false;
        });
        input.requestFocus();
        input.postDelayed(this::showKeyboard, 150);

        content.addView(UI.space(this, 12));

        TextView guessBtn = UI.button(this, "Submit guess", ACCENT, ACCENT_D, 16, 15);
        content.addView(guessBtn, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        guessBtn.setOnClickListener(v -> submitGuess());

        if (history != null && history.length() > 0) {
            TextView hist = UI.text(this, history.toString(), UI.INK, 14, false);
            hist.setGravity(Gravity.LEFT);
            hist.setLineSpacing(UI.dp(this, 4), 1f);
            hist.setPadding(UI.dp(this, 14), UI.dp(this, 12), UI.dp(this, 14), UI.dp(this, 12));
            hist.setBackgroundDrawable(UI.cardBg(this));
            LinearLayout.LayoutParams hlp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            hlp.topMargin = UI.dp(this, 12);
            content.addView(hist, hlp);
        }
    }

    private void submitGuess() {
        String g = input.getText().toString().trim().toLowerCase();
        if (!validGuess(g)) {
            UI.shake(input);
            return;
        }
        if (g.equals(secret)) {
            guesses++;
            showSolved();
            return;
        }
        int bulls = 0, cows = 0;
        for (int i = 0; i < g.length(); i++) {
            char c = g.charAt(i);
            int si = secret.indexOf(c);
            if (si < 0) continue;
            if (si == i) bulls++;
            else cows++;
        }
        guesses++;
        history.append(guesses).append(". ").append(g)
                .append("  →  ").append(bulls).append(" bull").append(bulls == 1 ? "" : "s")
                .append(", ").append(cows).append(" cow").append(cows == 1 ? "" : "s")
                .append("\n");
        showGuess();
    }

    private boolean validGuess(String g) {
        if (g.length() != secret.length()) return false;
        for (int i = 0; i < g.length(); i++) {
            char c = g.charAt(i);
            if (wordMode ? !Character.isLetter(c) : !Character.isDigit(c)) return false;
            for (int j = i + 1; j < g.length(); j++) {
                if (g.charAt(j) == c) return false;
            }
        }
        return true;
    }

    private void showSolved() {
        content.removeAllViews();
        input = null;
        hideKeyboard();

        TextView title = UI.text(this, "Cracked it!", UI.MINT, 28, true);
        title.setGravity(Gravity.CENTER);
        content.addView(title);

        TextView sub = UI.text(this, "The secret was", UI.INK_SOFT, 14, false);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, UI.dp(this, 8), 0, UI.dp(this, 12));
        content.addView(sub);

        TextView w = UI.card(this, secret, UI.INK, 22);
        content.addView(w, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView g = UI.text(this, "Solved in " + guesses + " guess" + (guesses == 1 ? "" : "es") + "!",
                UI.GOLD, 16, true);
        g.setGravity(Gravity.CENTER);
        g.setPadding(0, UI.dp(this, 14), 0, UI.dp(this, 16));
        content.addView(g);

        TextView again = UI.button(this, "New Secret", ACCENT, ACCENT_D, 16, 15);
        content.addView(again, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        again.setOnClickListener(v -> showSecret());

        TextView setup = UI.ghost(this, "Change Settings", 15, 14);
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        slp.topMargin = UI.dp(this, 10);
        content.addView(setup, slp);
        setup.setOnClickListener(v -> showSetup());
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
