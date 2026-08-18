package io.github.abdurazaaqmohammed.gamesbox.game;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MadLibsActivity extends BaseGameActivity {

    private static final int ACCENT = UI.OLIVE;
    private static final int ACCENT_D = UI.OLIVE_D;

    private static final Pattern BLANK = Pattern.compile("\\{([a-z]+)\\}");

    private LinearLayout content;
    private final List<Integer> order = new ArrayList<>();
    private final List<Part> parts = new ArrayList<>();
    private final List<String> words = new ArrayList<>();
    private int storyIndex;
    private int partCursor;
    private int totalBlanks;
    private EditText input;

    private static final class Part {
        final boolean blank;
        final String text;

        Part(boolean blank, String text) {
            this.blank = blank;
            this.text = text;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        content = UI.screen(this, "Mad Libs",
                "Fill in the blanks, read the story", ACCENT);
        for (int i = 0; i < Data.MAD_LIBS.length; i++) order.add(i);
        Collections.shuffle(order, UI.RND);
        showStart();
    }

    private void showStart() {
        content.removeAllViews();

        TextView rules = UI.text(this,
                "The phone asks for words one at a time - a noun, a verb, an adjective... Take turns (or pass the phone around) to fill them all in. Then read the silly finished story out loud.",
                UI.INK_SOFT, 14, false);
        rules.setPadding(0, 0, 0, UI.dp(this, 16));
        content.addView(rules);

        content.addView(UI.space(this, 12));

        TextView start = UI.button(this, "Start Story", ACCENT, ACCENT_D, 17, 16);
        content.addView(start, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        start.setOnClickListener(v -> beginStory());
    }

    private void beginStory() {
        parts.clear();
        words.clear();
        partCursor = 0;
        totalBlanks = 0;

        String story = Data.MAD_LIBS[order.get(storyIndex)];
        Matcher m = BLANK.matcher(story);
        int pos = 0;
        while (m.find()) {
            if (m.start() > pos) parts.add(new Part(false, story.substring(pos, m.start())));
            parts.add(new Part(true, m.group(1)));
            totalBlanks++;
            pos = m.end();
        }
        if (pos < story.length()) parts.add(new Part(false, story.substring(pos)));

        showAsk();
    }

    private int nextBlank() {
        for (int i = partCursor; i < parts.size(); i++) {
            if (parts.get(i).blank) return i;
        }
        return -1;
    }

    private void showAsk() {
        content.removeAllViews();
        hideKeyboard();

        int idx = nextBlank();
        if (idx < 0) {
            showStory();
            return;
        }
        String type = parts.get(idx).text;
        partCursor = idx + 1;

        TextView progress = UI.text(this, "Story " + (storyIndex + 1) + " of " + order.size()
                + "      Word " + (words.size() + 1) + " of " + totalBlanks,
                UI.INK_SOFT, 13, true);
        progress.setGravity(Gravity.CENTER);
        progress.setPadding(0, 0, 0, UI.dp(this, 10));
        content.addView(progress);

        TextView typeT = UI.text(this, promptFor(type), UI.INK, 22, true);
        typeT.setGravity(Gravity.CENTER);
        typeT.setPadding(0, 0, 0, UI.dp(this, 14));
        content.addView(typeT);

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
        input.setPadding(UI.dp(this, 16), UI.dp(this, 14), UI.dp(this, 16), UI.dp(this, 14));
        input.setIncludeFontPadding(false);
        content.addView(input, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        input.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                saveWord();
                return true;
            }
            return false;
        });
        input.requestFocus();
        input.postDelayed(this::showKeyboard, 150);

        content.addView(UI.space(this, 14));

        TextView next = UI.button(this, "Next", ACCENT, ACCENT_D, 16, 15);
        content.addView(next, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        next.setOnClickListener(v -> saveWord());
    }

    private void saveWord() {
        String w = input.getText().toString().trim();
        if (w.length() == 0) {
            UI.shake(input);
            return;
        }
        words.add(w);
        showAsk();
    }

    private void showStory() {
        content.removeAllViews();
        input = null;
        hideKeyboard();

        TextView head = UI.text(this, "Read it out loud!", UI.INK, 24, true);
        head.setGravity(Gravity.CENTER);
        head.setPadding(0, 0, 0, UI.dp(this, 14));
        content.addView(head);

        TextView story = UI.text(this, buildStory(), UI.INK, 16, false);
        story.setLineSpacing(UI.dp(this, 5), 1f);
        story.setPadding(UI.dp(this, 18), UI.dp(this, 18), UI.dp(this, 18), UI.dp(this, 18));
        story.setBackgroundDrawable(UI.cardBg(this));
        UI.popIn(story, 250);
        content.addView(story, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        content.addView(UI.space(this, 16));

        TextView again = UI.ghost(this, "Same Story Again", 15, 14);
        content.addView(again, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        again.setOnClickListener(v -> beginStory());

        TextView next = UI.button(this, "Next Story", ACCENT, ACCENT_D, 16, 15);
        LinearLayout.LayoutParams nlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        nlp.topMargin = UI.dp(this, 10);
        content.addView(next, nlp);
        next.setOnClickListener(v -> {
            storyIndex++;
            if (storyIndex >= order.size()) {
                Collections.shuffle(order, UI.RND);
                storyIndex = 0;
            }
            beginStory();
        });
    }

    private CharSequence buildStory() {
        SpannableStringBuilder b = new SpannableStringBuilder();
        int wi = 0;
        for (Part p : parts) {
            if (p.blank) {
                int start = b.length();
                b.append(words.get(wi));
                b.setSpan(new ForegroundColorSpan(ACCENT), start, b.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                b.setSpan(new StyleSpan(Typeface.BOLD), start, b.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                wi++;
            } else {
                b.append(p.text);
            }
        }
        return new SpannableString(b);
    }

    private String promptFor(String type) {
        if ("plural".equals(type)) return "A plural noun";
        if ("verb".equals(type)) return "A verb (action word)";
        if ("verbpast".equals(type)) return "A verb (past tense)";
        if ("adjective".equals(type)) return "An adjective";
        if ("adverb".equals(type)) return "An adverb (ends in -ly)";
        if ("place".equals(type)) return "A place";
        if ("person".equals(type)) return "A person's name";
        if ("animal".equals(type)) return "An animal";
        if ("food".equals(type)) return "A food";
        if ("body".equals(type)) return "A body part";
        if ("number".equals(type)) return "A number";
        if ("exclamation".equals(type)) return "An exclamation!";
        return "A noun";
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
