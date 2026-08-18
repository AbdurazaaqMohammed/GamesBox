package io.github.abdurazaaqmohammed.gamesbox.game;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import io.github.abdurazaaqmohammed.gamesbox.ui.UI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PictionaryActivity extends BaseGameActivity {

    private static final int ACCENT = UI.TAUPE;
    private static final int ACCENT_D = UI.TAUPE_D;

    private LinearLayout content;
    private UI.Stepper playersStepper;
    private int players;
    private int[] teamPoints;
    private int drawerTeam;
    private final List<Integer> order = new ArrayList<>();
    private int cursor;
    private String word;
    private DrawView drawView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        content = UI.screen(this, "Pictionary",
                "Draw it, guess it", ACCENT);
        for (int i = 0; i < Data.PACK_MIXED.length; i++) order.add(i);
        Collections.shuffle(order, UI.RND);
        showSetup();
    }

    private void showSetup() {
        content.removeAllViews();

        TextView rules = UI.text(this,
                "Split into two teams. The drawer secretly looks at a word, then draws it on the phone while their team shouts guesses. Guess it in time to score a point. Then the other team draws.",
                UI.INK_SOFT, 14, false);
        rules.setPadding(0, 0, 0, UI.dp(this, 16));
        content.addView(rules);

        playersStepper = new UI.Stepper(this, "Players", 4, 10, 6, 1);
        content.addView(playersStepper.row);

        content.addView(UI.space(this, 16));

        TextView start = UI.button(this, "Start Game", ACCENT, ACCENT_D, 17, 16);
        content.addView(start, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        start.setOnClickListener(v -> {
            players = playersStepper.get();
            teamPoints = new int[2];
            drawerTeam = 0;
            newRound();
        });
    }

    private void newRound() {
        word = Data.PACK_MIXED[order.get(cursor)];
        cursor++;
        if (cursor >= order.size()) {
            Collections.shuffle(order, UI.RND);
            cursor = 0;
        }
        showDrawer();
    }

    private void showDrawer() {
        content.removeAllViews();

        TextView who = UI.text(this, "Team " + teamName(drawerTeam) + " draws",
                UI.INK, 20, true);
        who.setGravity(Gravity.CENTER);
        who.setPadding(0, UI.dp(this, 4), 0, UI.dp(this, 10));
        content.addView(who);

        TextView label = UI.text(this, "Secret word", UI.INK_SOFT, 13, true);
        label.setGravity(Gravity.CENTER);
        label.setPadding(0, 0, 0, UI.dp(this, 6));
        content.addView(label);

        UI.RevealCard card = new UI.RevealCard(this, word, ACCENT);
        content.addView(card, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, UI.dp(this, 170)));

        TextView note = UI.text(this,
                "The drawer looks at this. Don't let the other team see the phone.",
                UI.INK_SOFT, 14, false);
        note.setGravity(Gravity.CENTER);
        note.setPadding(0, UI.dp(this, 12), 0, UI.dp(this, 18));
        content.addView(note);

        final TextView go = UI.button(this, "I've got it - Draw!", ACCENT, ACCENT_D, 17, 16);
        go.setVisibility(View.GONE);
        content.addView(go, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        go.setOnClickListener(v -> showCanvas());

        card.setOnRevealed(() -> {
            go.setVisibility(View.VISIBLE);
            UI.popIn(go, 200);
        });
    }

    private void showCanvas() {
        content.removeAllViews();

        TextView score = UI.text(this, "Team A: " + teamPoints[0] + "   Team B: "
                + teamPoints[1], UI.GOLD, 14, true);
        score.setGravity(Gravity.CENTER);
        score.setPadding(0, 0, 0, UI.dp(this, 8));
        content.addView(score);

        drawView = new DrawView(this);
        drawView.setBackgroundDrawable(UI.cardBg(this));
        LinearLayout.LayoutParams dlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, UI.dp(this, 320));
        content.addView(drawView, dlp);

        TextView tip = UI.text(this, "Draw with your finger. Teammates guess out loud.",
                UI.INK_SOFT, 13, false);
        tip.setGravity(Gravity.CENTER);
        tip.setPadding(0, UI.dp(this, 8), 0, UI.dp(this, 10));
        content.addView(tip);

        LinearLayout row = UI.row(this);
        row.setGravity(Gravity.CENTER_HORIZONTAL);
        TextView clear = UI.ghost(this, "Clear", 15, 14);
        TextView got = UI.button(this, "Guessed! (+1)", UI.MINT, UI.MINT_D, 15, 14);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        lp.setMargins(UI.dp(this, 3), 0, UI.dp(this, 3), 0);
        row.addView(clear, lp);
        row.addView(got, lp);
        clear.setOnClickListener(v -> drawView.clear());
        got.setOnClickListener(v -> {
            teamPoints[drawerTeam]++;
            showGuessed(true);
        });
        content.addView(row);

        content.addView(UI.space(this, 8));

        TextView give = UI.ghost(this, "Give up", 15, 14);
        content.addView(give, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        give.setOnClickListener(v -> showGuessed(false));
    }

    private void showGuessed(boolean guessed) {
        content.removeAllViews();

        TextView title = UI.text(this, guessed ? "Guessed!" : "Given up",
                guessed ? UI.MINT : UI.RED, 26, true);
        title.setGravity(Gravity.CENTER);
        content.addView(title);

        TextView sub = UI.text(this, "The word was:", UI.INK_SOFT, 14, false);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, UI.dp(this, 8), 0, UI.dp(this, 12));
        content.addView(sub);

        TextView w = UI.card(this, word, UI.INK, 22);
        content.addView(w, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView score = UI.text(this, "Team A: " + teamPoints[0] + "   Team B: "
                + teamPoints[1], UI.GOLD, 16, true);
        score.setGravity(Gravity.CENTER);
        score.setPadding(0, UI.dp(this, 14), 0, UI.dp(this, 16));
        content.addView(score);

        TextView next = UI.button(this, "Next Round", ACCENT, ACCENT_D, 16, 15);
        content.addView(next, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        next.setOnClickListener(v -> {
            drawerTeam = (drawerTeam + 1) % 2;
            newRound();
        });
    }

    private String teamName(int t) {
        return t == 0 ? "A" : "B";
    }

    private static final class DrawView extends View {

        private final List<Path> paths = new ArrayList<>();
        private Path current;
        private final Paint paint;

        DrawView(Context c) {
            super(c);
            paint = new Paint();
            paint.setColor(UI.INK);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(UI.dp(c, 4));
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setAntiAlias(true);
        }

        void clear() {
            paths.clear();
            current = null;
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            for (Path p : paths) canvas.drawPath(p, paint);
        }

        @Override
        public boolean onTouchEvent(MotionEvent ev) {
            int action = ev.getAction() & MotionEvent.ACTION_MASK;
            float x = ev.getX();
            float y = ev.getY();
            if (action == MotionEvent.ACTION_DOWN) {
                current = new Path();
                current.moveTo(x, y);
                paths.add(current);
                invalidate();
                return true;
            } else if (action == MotionEvent.ACTION_MOVE) {
                if (current != null) {
                    current.lineTo(x, y);
                    invalidate();
                }
                return true;
            } else if (action == MotionEvent.ACTION_UP) {
                current = null;
                invalidate();
                return true;
            }
            return super.onTouchEvent(ev);
        }
    }
}
