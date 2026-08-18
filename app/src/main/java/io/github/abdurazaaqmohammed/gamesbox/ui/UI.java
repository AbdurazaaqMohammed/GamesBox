package io.github.abdurazaaqmohammed.gamesbox.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import io.github.abdurazaaqmohammed.gamesbox.PlayerNamesActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class UI {

    public static final int THEME_LIGHT = 0;
    public static final int THEME_DARK = 1;
    public static final int THEME_BLACK = 2;

    private static final String PREFS = "GamesBox";
    private static final String KEY_THEME = "theme";

    public static int INK = 0xFF242426;
    public static int INK_SOFT = 0xFF727276;
    public static int PAPER = 0xFFF6F5F1;
    public static int PAPER_D = 0xFFECEAE4;
    public static int CARD = 0xFFFFFFFF;
    public static int LINE = 0xFFE2E0D9;
    public static final int WHITE = 0xFFFFFFFF;
    public static final int WHITE_SOFT = 0xFFFAF8F4;
    public static int INK_DEEP = 0xFF2C2C31;

    public static final int PURPLE = 0xFF6E62A8;
    public static final int PURPLE_D = 0xFF5B4F93;
    public static final int PINK = 0xFFB26A8B;
    public static final int PINK_D = 0xFF9D5477;
    public static final int MINT = 0xFF55927B;
    public static final int MINT_D = 0xFF457E69;
    public static final int GOLD = 0xFFA9823C;
    public static final int GOLD_D = 0xFF926F2D;
    public static final int SKY = 0xFF5B7FB0;
    public static final int SKY_D = 0xFF496A98;
    public static final int CORAL = 0xFFB2705E;
    public static final int CORAL_D = 0xFF9D5C4B;
    public static final int RED = 0xFFB4503F;
    public static final int RED_D = 0xFF9F4233;
    public static final int TEAL = 0xFF468F8F;
    public static final int TEAL_D = 0xFF377B7B;
    public static final int BLUE = 0xFF55689E;
    public static final int BLUE_D = 0xFF46567F;
    public static final int ORANGE = 0xFFC06B35;
    public static final int ORANGE_D = 0xFFA95A29;
    public static final int OLIVE = 0xFF7A8B4F;
    public static final int OLIVE_D = 0xFF66763F;
    public static final int INDIGO = 0xFF5C5FA8;
    public static final int INDIGO_D = 0xFF4A4D8C;
    public static final int LIME = 0xFF8FB03F;
    public static final int LIME_D = 0xFF7A9632;
    public static final int CYAN = 0xFF3B9EBF;
    public static final int CYAN_D = 0xFF31859F;
    public static final int ROSE = 0xFFC05E7E;
    public static final int ROSE_D = 0xFFA74C6A;
    public static final int AMBER = 0xFFD48A2B;
    public static final int AMBER_D = 0xFFB8771F;
    public static final int SLATE = 0xFF5E7087;
    public static final int SLATE_D = 0xFF4D5C70;
    public static final int TAUPE = 0xFF9B7B5E;
    public static final int TAUPE_D = 0xFF856549;

    public static final Random RND = new Random();

    private UI() {
    }

    public static int dp(Context c, float v) {
        return Math.round(v * c.getResources().getDisplayMetrics().density);
    }

    public static int sp(Context c, float v) {
        return Math.round(v * c.getResources().getDisplayMetrics().scaledDensity);
    }

    public static View space(Context c, int heightDp) {
        View v = new View(c);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(c, heightDp));
        v.setLayoutParams(lp);
        return v;
    }

    public static void letterSpacing(TextView t, float em) {
        if (Build.VERSION.SDK_INT >= 21) {
            t.setLetterSpacing(em);
        }
    }

    public static int darker(int color, float f) {
        int a = Color.alpha(color);
        int r = Math.max(0, (int) (Color.red(color) * f));
        int g = Math.max(0, (int) (Color.green(color) * f));
        int b = Math.max(0, (int) (Color.blue(color) * f));
        return Color.argb(a, r, g, b);
    }

    public static int withAlpha(int color, int alpha) {
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
    }

    public static GradientDrawable gradient(int from, int to, GradientDrawable.Orientation dir) {
        GradientDrawable g = new GradientDrawable(dir, new int[]{from, to});
        return g;
    }

    public static GradientDrawable fill(int color, int radiusDp, Context c) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        if (radiusDp > 0) g.setCornerRadius(dp(c, radiusDp));
        return g;
    }

    public static GradientDrawable btnGrad(int from, int to, int radiusDp, Context c) {
        GradientDrawable g = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{from, to});
        if (radiusDp > 0) g.setCornerRadius(dp(c, radiusDp));
        return g;
    }

    public static GradientDrawable stroke(int color, int radiusDp, Context c) {
        GradientDrawable g = new GradientDrawable();
        g.setStroke(dp(c, 2), color);
        if (radiusDp > 0) g.setCornerRadius(dp(c, radiusDp));
        return g;
    }

    public static GradientDrawable cardBg(Context c) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(CARD);
        g.setCornerRadius(dp(c, 14));
        return g;
    }

    public static GradientDrawable cardBgDeep(Context c) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(PAPER_D);
        g.setCornerRadius(dp(c, 14));
        return g;
    }

    public static void bg(Activity a, int top, int bottom) {
        applyTheme(a);
        if (Build.VERSION.SDK_INT >= 21) {
            a.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            a.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            a.getWindow().setStatusBarColor(INK_DEEP);
            a.getWindow().setNavigationBarColor(INK_DEEP);
        }
        GradientDrawable g = new GradientDrawable();
        g.setColor(PAPER);
        a.getWindow().setBackgroundDrawable(g);
    }

    public static int getTheme(Context c) {
        return c.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getInt(KEY_THEME, THEME_LIGHT);
    }

    public static void setTheme(Context c, int theme) {
        SharedPreferences.Editor e = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().putInt(KEY_THEME, theme);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            e.apply();
        } else e.commit();
        applyPalette(theme);
    }

    public static void applyTheme(Context c) {
        applyPalette(getTheme(c));
    }

    private static void applyPalette(int theme) {
        if (theme == THEME_DARK) {
            INK = 0xFFE9E9EB;
            INK_SOFT = 0xFF9B9BA2;
            PAPER = 0xFF1E1E22;
            PAPER_D = 0xFF28282D;
            CARD = 0xFF2D2D33;
            LINE = 0xFF3C3C43;
            INK_DEEP = 0xFF131316;
        } else if (theme == THEME_BLACK) {
            INK = 0xFFF0F0F2;
            INK_SOFT = 0xFF8A8A92;
            PAPER = 0xFF000000;
            PAPER_D = 0xFF0E0E10;
            CARD = 0xFF141417;
            LINE = 0xFF232327;
            INK_DEEP = 0xFF000000;
        } else {
            INK = 0xFF242426;
            INK_SOFT = 0xFF727276;
            PAPER = 0xFFF6F5F1;
            PAPER_D = 0xFFECEAE4;
            CARD = 0xFFFFFFFF;
            LINE = 0xFFE2E0D9;
            INK_DEEP = 0xFF2C2C31;
        }
    }

    public static TextView text(Context c, CharSequence s, int color, float sp, boolean bold) {
        TextView t = new TextView(c);
        t.setText(s);
        t.setTextColor(color);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        t.setTypeface(Typeface.DEFAULT, bold ? Typeface.BOLD : Typeface.NORMAL);
        t.setIncludeFontPadding(false);
        return t;
    }

    public static TextView button(Context c, String s, int from, int to, float textSp, int padYdp) {
        TextView b = new TextView(c);
        b.setText(s);
        b.setTextColor(WHITE);
        b.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSp);
        b.setTypeface(Typeface.DEFAULT_BOLD);
        b.setGravity(Gravity.CENTER);
        b.setPadding(dp(c, 18), dp(c, padYdp), dp(c, 18), dp(c, padYdp));
        b.setBackgroundDrawable(fill(from, 12, c));
        b.setIncludeFontPadding(false);
        pressy(b);
        return b;
    }

    public static TextView ghost(Context c, String s, float textSp, int padYdp) {
        TextView b = new TextView(c);
        b.setText(s);
        b.setTextColor(INK);
        b.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSp);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setGravity(Gravity.CENTER);
        b.setPadding(dp(c, 14), dp(c, padYdp), dp(c, 14), dp(c, padYdp));
        GradientDrawable g = new GradientDrawable();
        g.setColor(withAlpha(INK, 6));
        g.setStroke(dp(c, 1), withAlpha(INK, 45));
        g.setCornerRadius(dp(c, 12));
        b.setBackgroundDrawable(g);
        b.setIncludeFontPadding(false);
        pressy(b);
        return b;
    }

    public static TextView chip(Context c, String s, int color, float textSp) {
        TextView b = new TextView(c);
        b.setText(s);
        b.setTextColor(color);
        b.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSp);
        b.setTypeface(Typeface.DEFAULT_BOLD);
        b.setGravity(Gravity.CENTER);
        b.setPadding(dp(c, 14), dp(c, 9), dp(c, 14), dp(c, 9));
        b.setBackgroundDrawable(fill(withAlpha(color, 15), 12, c));
        b.setIncludeFontPadding(false);
        return b;
    }

    public static TextView chipGhost(Context c, String s, int color, float textSp) {
        TextView b = new TextView(c);
        b.setText(s);
        b.setTextColor(color);
        b.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSp);
        b.setTypeface(Typeface.DEFAULT_BOLD);
        b.setGravity(Gravity.CENTER);
        b.setPadding(dp(c, 14), dp(c, 9), dp(c, 14), dp(c, 9));
        GradientDrawable g = new GradientDrawable();
        g.setColor(withAlpha(color, 7));
        g.setStroke(dp(c, 1), withAlpha(color, 60));
        g.setCornerRadius(dp(c, 14));
        b.setBackgroundDrawable(g);
        b.setIncludeFontPadding(false);
        pressy(b);
        return b;
    }

    public static LinearLayout row(Context c) {
        LinearLayout l = new LinearLayout(c);
        l.setOrientation(LinearLayout.HORIZONTAL);
        l.setGravity(Gravity.CENTER_VERTICAL);
        return l;
    }

    public static LinearLayout col(Context c) {
        LinearLayout l = new LinearLayout(c);
        l.setOrientation(LinearLayout.VERTICAL);
        return l;
    }

    public static void pad(View v, Context c, float l, float t, float r, float b) {
        v.setPadding(dp(c, l), dp(c, t), dp(c, r), dp(c, b));
    }

    public static void pressy(final View v) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) v.setOnTouchListener((view, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    view.setAlpha(0.82f);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    view.setAlpha(1f);
                    break;
            }
            return false;
        });
    }

    public static void slideUp(View v, long ms) {
        AnimationSet set = new AnimationSet(true);
        TranslateAnimation t = new TranslateAnimation(0, 0, dp(v.getContext(), 24), 0);
        t.setDuration(ms);
        AlphaAnimation a = new AlphaAnimation(0f, 1f);
        a.setDuration(ms);
        set.addAnimation(a);
        set.addAnimation(t);
        set.setFillAfter(true);
        v.startAnimation(set);
    }

    public static void popIn(View v, long ms) {
        AnimationSet set = new AnimationSet(true);
        ScaleAnimation s = new ScaleAnimation(0.95f, 1f, 0.95f, 1f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        s.setDuration(ms);
        AlphaAnimation a = new AlphaAnimation(0f, 1f);
        a.setDuration(ms);
        set.addAnimation(a);
        set.addAnimation(s);
        set.setFillAfter(true);
        v.startAnimation(set);
    }

    public static void fadeIn(View v, long ms) {
        AlphaAnimation a = new AlphaAnimation(0f, 1f);
        a.setDuration(ms);
        a.setFillAfter(true);
        v.startAnimation(a);
    }

    public static void shake(View v) {
        AnimationSet set = new AnimationSet(true);
        TranslateAnimation t = new TranslateAnimation(0, dp(v.getContext(), 26), 0, 0);
        t.setDuration(220);
        t.setRepeatCount(1);
        t.setRepeatMode(Animation.REVERSE);
        set.addAnimation(t);
        v.startAnimation(set);
    }

    public static void bounceIn(View v, long ms) {
        AnimationSet set = new AnimationSet(true);
        ScaleAnimation s = new ScaleAnimation(0.9f, 1f, 0.9f, 1f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        s.setDuration(ms);
        AlphaAnimation a = new AlphaAnimation(0f, 1f);
        a.setDuration(ms);
        set.addAnimation(a);
        set.addAnimation(s);
        set.setFillAfter(true);
        v.startAnimation(set);
    }

    public static LinearLayout screen(Activity a, String title, String subtitle, int accent) {
        bg(a, PAPER, PAPER);
        LinearLayout root = col(a);
        root.setGravity(Gravity.TOP);
        root.setPadding(dp(a, 20), dp(a, 10), dp(a, 20), dp(a, 20));

        LinearLayout head = row(a);
        TextView back = text(a, "<", INK, 24, true);
        back.setGravity(Gravity.CENTER);
        back.setBackgroundDrawable(fill(withAlpha(INK, 8), 20, a));
        LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(dp(a, 42), dp(a, 42));
        head.addView(back, blp);
        back.setOnClickListener(v -> a.finish());

        LinearLayout titles = col(a);
        titles.setPadding(dp(a, 14), 0, 0, 0);
        TextView t1 = text(a, title, INK, 22, true);
        titles.addView(t1);
        if (subtitle != null && subtitle.length() > 0) {
            TextView t2 = text(a, subtitle, INK_SOFT, 13, false);
            t2.setPadding(0, dp(a, 3), 0, 0);
            titles.addView(t2);
        }
        head.addView(titles, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        View accentBar = new View(a);
        accentBar.setBackgroundDrawable(fill(accent, 2, a));
        LinearLayout.LayoutParams ablp = new LinearLayout.LayoutParams(dp(a, 30), dp(a, 3));
        ablp.topMargin = dp(a, 10);
        root.addView(accentBar, ablp);

        LinearLayout body = col(a);
        body.setPadding(0, dp(a, 16), 0, 0);
        ScrollView scroll = new ScrollView(a);
        scroll.setFillViewport(true);
        scroll.setVerticalScrollBarEnabled(false);
        scroll.addView(body, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(scroll, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        a.setContentView(root);
        return body;
    }

    public static TextView card(Context c, String s, int textColor, float sp) {
        TextView t = new TextView(c);
        t.setText(s);
        t.setTextColor(textColor);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        t.setGravity(Gravity.CENTER);
        t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        t.setBackgroundDrawable(cardBg(c));
        t.setPadding(dp(c, 20), dp(c, 22), dp(c, 20), dp(c, 22));
        t.setIncludeFontPadding(false);
        return t;
    }

    public static class Stepper {
        public final LinearLayout row;
        public int value;
        public final int min;
        public final int max;
        private final TextView valueView;

        public Stepper(final Context c, String label, int min, int max, int initial, int step) {
            this.min = min;
            this.max = max;
            this.value = initial;

            boolean players = "Players".equals(label);
            LinearLayout root = players ? UI.col(c) : UI.row(c);
            root.setPadding(0, 0, 0, 0);

            LinearLayout inner = UI.row(c);
            inner.setPadding(0, 0, 0, 0);

            TextView lbl = UI.text(c, label, INK, 15, true);
            inner.addView(lbl, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

            final TextView minus = UI.text(c, "-", INK, 18, true);
            minus.setGravity(Gravity.CENTER);
            minus.setBackgroundDrawable(fill(withAlpha(INK, 8), 20, c));
            minus.setOnClickListener(v -> {
                value -= step;
                if (value < min) value = min;
                refresh(c);
            });
            inner.addView(minus, new LinearLayout.LayoutParams(dp(c, 42), dp(c, 42)));

            valueView = UI.text(c, String.valueOf(value), INK, 18, true);
            valueView.setGravity(Gravity.CENTER);
            inner.addView(valueView, new LinearLayout.LayoutParams(dp(c, 54), dp(c, 42)));

            final TextView plus = UI.text(c, "+", INK, 18, true);
            plus.setGravity(Gravity.CENTER);
            plus.setBackgroundDrawable(fill(withAlpha(INK, 8), 20, c));
            plus.setOnClickListener(v -> {
                value += step;
                if (value > max) value = max;
                refresh(c);
            });
            inner.addView(plus, new LinearLayout.LayoutParams(dp(c, 42), dp(c, 42)));

            root.addView(inner, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            if (players) {
                TextView edit = UI.ghost(c, "Edit Players", 15, 14);
                LinearLayout.LayoutParams elp = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                elp.topMargin = dp(c, 10);
                root.addView(edit, elp);
                edit.setOnClickListener(v ->
                        c.startActivity(new Intent(c, PlayerNamesActivity.class)));
            }

            row = root;
        }

        private void refresh(Context c) {
            valueView.setText(String.valueOf(value));
        }

        public int get() {
            return value;
        }
    }

    public static class Toggle {
        public final LinearLayout row;
        public int index;
        private final TextView[] chips;
        private final int accent;
        private final View.OnClickListener onChanged;

        public Toggle(final Context c, String label, String[] labels, int accent, int initial) {
            this(c, label, labels, accent, initial, null);
        }

        public Toggle(final Context c, String label, String[] labels, int accent, int initial,
                      final View.OnClickListener onChanged) {
            this.accent = accent;
            this.index = initial;
            this.onChanged = onChanged;
            chips = new TextView[labels.length];

            LinearLayout container = col(c);
            if (label != null) {
                TextView lbl = text(c, label, INK_SOFT, 12, true);
                lbl.setPadding(0, 0, 0, dp(c, 6));
                container.addView(lbl);
            }

            LinearLayout chipsRow = row(c);
            chipsRow.setGravity(Gravity.CENTER_HORIZONTAL);
            for (int i = 0; i < labels.length; i++) {
                final int idx = i;
                chips[i] = text(c, labels[i], accent, 13, true);
                chips[i].setGravity(Gravity.CENTER);
                chips[i].setPadding(dp(c, 8), dp(c, 9), dp(c, 8), dp(c, 9));
                chips[i].setIncludeFontPadding(false);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                lp.setMargins(dp(c, 3), dp(c, 3), dp(c, 3), dp(c, 3));
                chipsRow.addView(chips[i], lp);
                chips[i].setOnClickListener(v -> {
                    index = idx;
                    refresh();
                    if (Toggle.this.onChanged != null) {
                        Toggle.this.onChanged.onClick(v);
                    }
                });
            }
            container.addView(chipsRow);
            refresh();
            row = container;
        }

        private void refresh() {
            for (int i = 0; i < chips.length; i++) {
                boolean sel = i == index;
                chips[i].setBackgroundDrawable(sel
                        ? fill(accent, 14, chips[i].getContext())
                        : stroke(withAlpha(accent, 90), 14, chips[i].getContext()));
                chips[i].setTextColor(sel ? WHITE : accent);
            }
        }

        public int get() {
            return index;
        }
    }

    /**
     * A card that hides a secret until you swipe up. The cover slides up with
     * your finger, so you see the text mid-swipe. Drag past halfway and it
     * locks open (firing onRevealed); release early and it snaps shut. A
     * plain tap also fully reveals it as a fallback.
     */
    public static class RevealCard extends View {

        private static final float LOCK_AT = 0.55f;

        private final String secret;
        private final Paint textPaint;
        private final Paint coverPaint;
        private final Paint hintPaint;
        private final Paint handlePaint;
        private float reveal;
        private boolean locked;
        private boolean fired;
        private float downY;
        private boolean dragged;
        private Runnable onRevealed;

        public RevealCard(Context c, String secret, int accent) {
            super(c);
            this.secret = secret;

            textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            textPaint.setColor(INK);
            textPaint.setTextSize(dp(c, 30));
            textPaint.setTypeface(Typeface.DEFAULT_BOLD);
            textPaint.setTextAlign(Paint.Align.CENTER);

            coverPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            coverPaint.setColor(CARD);

            hintPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            hintPaint.setColor(accent);
            hintPaint.setTextSize(dp(c, 15));
            hintPaint.setTypeface(Typeface.DEFAULT_BOLD);
            hintPaint.setTextAlign(Paint.Align.CENTER);

            handlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            handlePaint.setColor(withAlpha(accent, 130));
            handlePaint.setStrokeWidth(dp(c, 4));
            handlePaint.setStrokeCap(Paint.Cap.ROUND);
        }

        public void setOnRevealed(Runnable r) {
            onRevealed = r;
        }

        public boolean isRevealed() {
            return locked;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float w = getWidth();
            float h = getHeight();
            float radius = dp(getContext(), 16);

            canvas.save();
            canvas.clipRect(0, 0, w, h);
            drawSecret(canvas, w, h);
            if (reveal < 1f) {
                float shift = reveal * h;
                canvas.translate(0, -shift);
                canvas.drawRoundRect(new RectF(0, 0, w, h), radius, radius, coverPaint);
                float handleY = h - dp(getContext(), 18);
                float cx = w / 2f;
                canvas.drawLine(cx - dp(getContext(), 24), handleY,
                        cx + dp(getContext(), 24), handleY, handlePaint);
                canvas.drawText("swipe up", cx, handleY - dp(getContext(), 24), hintPaint);
            }
            canvas.restore();
        }

        private void drawSecret(Canvas canvas, float w, float h) {
            float maxW = w - dp(getContext(), 24);
            float maxH = h - dp(getContext(), 18);
            float size = dp(getContext(), 34);
            List<String> lines = null;
            while (size >= dp(getContext(), 12)) {
                textPaint.setTextSize(size);
                lines = wrap(secret, textPaint, maxW);
                float lineH = size * 1.35f;
                float totalH = lineH * lines.size();
                boolean fits = totalH <= maxH;
                if (fits) {
                    for (String line : lines) {
                        if (textPaint.measureText(line) > maxW) {
                            fits = false;
                            break;
                        }
                    }
                }
                if (fits) break;
                size -= dp(getContext(), 2);
            }
            float lineH = size * 1.35f;
            float totalH = lineH * lines.size();
            float y = (h - totalH) / 2f + textPaint.getTextSize();
            for (String line : lines) {
                canvas.drawText(line, w / 2f, y, textPaint);
                y += lineH;
            }
        }

        private List<String> wrap(String s, Paint p, float maxW) {
            List<String> out = new ArrayList<>();
            String[] hard = s.split("\n");
            for (String part : hard) {
                String[] words = part.split(" ");
                StringBuilder line = new StringBuilder();
                for (String word : words) {
                    String trial = line.length() == 0 ? word : line + " " + word;
                    if (p.measureText(trial) > maxW && line.length() > 0) {
                        out.add(line.toString());
                        line.setLength(0);
                        line.append(word);
                    } else {
                        line.append(line.length() == 0 ? word : " " + word);
                    }
                }
                if (line.length() > 0) out.add(line.toString());
            }
            return out;
        }

        private void unlock() {
            if (!locked) {
                locked = true;
                reveal = 1f;
                invalidate();
            }
            if (!fired && onRevealed != null) {
                fired = true;
                onRevealed.run();
            }
        }

        private void snapBack() {
            reveal = 0f;
            invalidate();
        }

        @Override
        public boolean onTouchEvent(MotionEvent ev) {
            if (locked) return true;
            int action = ev.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    downY = ev.getY();
                    dragged = false;
                    return true;
                case MotionEvent.ACTION_MOVE: {
                    float dY = downY - ev.getY();
                    if (Math.abs(dY) > ViewConfiguration.get(getContext()).getScaledTouchSlop()) {
                        dragged = true;
                    }
                    if (dragged && dY > 0) {
                        reveal = Math.min(1f, dY / getHeight());
                        invalidate();
                    }
                    return true;
                }
                case MotionEvent.ACTION_UP: {
                    if (!dragged) {
                        unlock();
                    } else if (reveal >= LOCK_AT) {
                        unlock();
                    } else {
                        snapBack();
                    }
                    return true;
                }
                case MotionEvent.ACTION_CANCEL:
                    if (reveal < 1f) snapBack();
                    return true;
            }
            return true;
        }
    }
}
