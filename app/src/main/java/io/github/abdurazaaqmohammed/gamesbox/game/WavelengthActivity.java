package io.github.abdurazaaqmohammed.gamesbox.game;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import io.github.abdurazaaqmohammed.gamesbox.PlayerNames;
import io.github.abdurazaaqmohammed.gamesbox.ui.UI;

public class WavelengthActivity extends BaseGameActivity {

    private static final int ACCENT = UI.INDIGO;
    private static final int ACCENT_D = UI.INDIGO_D;

    private LinearLayout content;
    private UI.Stepper playersStepper;
    private UI.Toggle diffToggle;
    private int players;
    private int[] scores;
    private int currentPlayer;
    private boolean[] doneThisRound;
    private int zoneHalf;

    private String leftLabel;
    private String rightLabel;
    private int targetPos;
    private SpectrumView spectrumView;

    private String playerName(int i) {
        return PlayerNames.get(this, i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        content = UI.screen(this, "Wavelength",
                "Guesstimate on a spectrum", ACCENT);
        showSetup();
    }

    private void showSetup() {
        content.removeAllViews();

        TextView rules = UI.text(this,
                "The clue giver sees a hidden target and hints the group toward it with one word or short phrase - 'pizza' for Hot-Cold, say. The group agrees on a spot, the clue giver locks it in. Hit the zone and score.",
                UI.INK_SOFT, 14, false);
        rules.setPadding(0, 0, 0, UI.dp(this, 16));
        content.addView(rules);

        playersStepper = new UI.Stepper(this, "Players", 2, 10, 4, 1);
        content.addView(playersStepper.row);

        diffToggle = new UI.Toggle(this, "Difficulty",
                new String[]{"Easy ±20", "Normal ±15", "Hard ±10"}, ACCENT, 1);
        content.addView(diffToggle.row);

        content.addView(UI.space(this, 16));

        TextView start = UI.button(this, "Start Game", ACCENT, ACCENT_D, 17, 16);
        content.addView(start, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        start.setOnClickListener(v -> {
            players = playersStepper.get();
            int[] zones = {20, 15, 10};
            zoneHalf = zones[diffToggle.get()];
            scores = new int[players];
            doneThisRound = new boolean[players];
            currentPlayer = 0;
            showPeek();
        });
    }

    private void showPeek() {
        content.removeAllViews();

        String[] pair = Data.SPECTRUMS[UI.RND.nextInt(Data.SPECTRUMS.length)].split("\\|");
        leftLabel = pair[0];
        rightLabel = pair[1];
        targetPos = 15 + UI.RND.nextInt(71);
        int lo = Math.max(0, targetPos - zoneHalf / 2);
        int hi = Math.min(100, targetPos + zoneHalf / 2);

        TextView who = UI.text(this, playerName(currentPlayer) + " is the clue giver",
                UI.INK, 16, true);
        who.setGravity(Gravity.CENTER);
        who.setPadding(0, UI.dp(this, 4), 0, UI.dp(this, 10));
        content.addView(who);

        TextView secret = UI.text(this, "Only you look!",
                UI.RED, 14, true);
        secret.setGravity(Gravity.CENTER);
        secret.setPadding(0, 0, 0, UI.dp(this, 10));
        content.addView(secret);

        TextView spectrum = UI.text(this, leftLabel + "  →  " + rightLabel,
                UI.INK, 22, true);
        spectrum.setGravity(Gravity.CENTER);
        spectrum.setPadding(UI.dp(this, 16), UI.dp(this, 14), UI.dp(this, 16), UI.dp(this, 14));
        spectrum.setBackgroundDrawable(UI.cardBg(this));
        content.addView(spectrum, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView target = UI.text(this, "The target zone is roughly between "
                + lo + " and " + hi,
                ACCENT, 17, true);
        target.setGravity(Gravity.CENTER);
        target.setPadding(0, UI.dp(this, 14), 0, UI.dp(this, 6));
        content.addView(target);

        TextView note = UI.text(this,
                "Hint the group toward that spot. No numbers, no pointing - a single word or phrase.",
                UI.INK_SOFT, 13, false);
        note.setGravity(Gravity.CENTER);
        note.setPadding(0, UI.dp(this, 6), 0, UI.dp(this, 18));
        content.addView(note);

        TextView go = UI.button(this, "I've seen it - let's play",
                ACCENT, ACCENT_D, 16, 15);
        content.addView(go, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        go.setOnClickListener(v -> showClue());
    }

    private void showClue() {
        content.removeAllViews();

        TextView who = UI.text(this, "Clue giver: " + playerName(currentPlayer),
                UI.INK, 15, true);
        who.setGravity(Gravity.CENTER);
        who.setPadding(0, UI.dp(this, 2), 0, UI.dp(this, 8));
        content.addView(who);

        TextView hint = UI.text(this,
                "Give ONE hint. The group agrees - the clue giver drags the marker there.",
                UI.INK_SOFT, 13, false);
        hint.setGravity(Gravity.CENTER);
        hint.setPadding(0, 0, 0, UI.dp(this, 10));
        content.addView(hint);

        spectrumView = new SpectrumView(this);
        spectrumView.setLabels(leftLabel, rightLabel);
        spectrumView.setGuess(50);
        spectrumView.setRevealed(false);
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, UI.dp(this, 180));
        content.addView(spectrumView, slp);

        TextView lock = UI.button(this, "Lock it in", ACCENT, ACCENT_D, 16, 15);
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        llp.topMargin = UI.dp(this, 16);
        content.addView(lock, llp);
        lock.setOnClickListener(v -> showReveal());
    }

    private void showReveal() {
        content.removeAllViews();

        int guess = spectrumView.getGuess();
        boolean hit = Math.abs(guess - targetPos) <= zoneHalf;

        if (hit) {
            scores[currentPlayer] += 1;
        }
        doneThisRound[currentPlayer] = true;

        TextView title = UI.text(this, hit ? "Hit!  +1" : "So close...  +0",
                hit ? UI.MINT : UI.RED, 30, true);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, UI.dp(this, 4), 0, UI.dp(this, 10));
        content.addView(title);

        SpectrumView sv = new SpectrumView(this);
        sv.setLabels(leftLabel, rightLabel);
        sv.setGuess(guess);
        sv.setRevealed(true);
        sv.setTarget(targetPos, zoneHalf);
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, UI.dp(this, 180));
        content.addView(sv, slp);

        TextView detail = UI.text(this,
                "Target zone: " + Math.max(0, targetPos - zoneHalf) + " to "
                        + Math.min(100, targetPos + zoneHalf) + "\n"
                        + "You picked " + guess,
                UI.INK_SOFT, 14, false);
        detail.setGravity(Gravity.CENTER);
        detail.setLineSpacing(UI.dp(this, 4), 1f);
        detail.setPadding(0, UI.dp(this, 12), 0, UI.dp(this, 8));
        content.addView(detail);

        TextView tally = UI.text(this, tally(), UI.GOLD, 14, true);
        tally.setGravity(Gravity.CENTER);
        tally.setPadding(0, 0, 0, UI.dp(this, 16));
        content.addView(tally);

        final boolean allDone = allPlayersDone();

        TextView next = UI.button(this, allDone ? "See standings" : "Next clue giver",
                ACCENT, ACCENT_D, 16, 15);
        content.addView(next, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        next.setOnClickListener(v -> {
            if (allDone) {
                showStandings();
            } else {
                currentPlayer = (currentPlayer + 1) % players;
                showPeek();
            }
        });
    }

    private void showStandings() {
        content.removeAllViews();

        TextView title = UI.text(this, "Round complete!", UI.INK, 24, true);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, UI.dp(this, 4), 0, UI.dp(this, 14));
        content.addView(title);

        StringBuilder sb = new StringBuilder();
        int[] order = bestToWorst();
        for (int r = 0; r < order.length; r++) {
            int p = order[r];
            sb.append(r + 1).append(". ").append(playerName(p)).append(" - ")
                    .append(scores[p]).append(" hit" + (scores[p] == 1 ? "" : "s"));
            if (r == 0) sb.append("  📡");
            if (r < order.length - 1) sb.append("\n");
        }
        TextView list = UI.text(this, sb.toString(), UI.INK, 16, true);
        list.setGravity(Gravity.CENTER);
        list.setLineSpacing(UI.dp(this, 5), 1f);
        list.setPadding(UI.dp(this, 16), UI.dp(this, 14), UI.dp(this, 16), UI.dp(this, 14));
        list.setBackgroundDrawable(UI.cardBg(this));
        content.addView(list, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        content.addView(UI.space(this, 16));

        TextView again = UI.button(this, "New Round", ACCENT, ACCENT_D, 16, 15);
        content.addView(again, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        again.setOnClickListener(v -> {
            for (int i = 0; i < players; i++) doneThisRound[i] = false;
            currentPlayer = 0;
            showPeek();
        });

        TextView setup = UI.ghost(this, "Back to Setup", 15, 14);
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        slp.topMargin = UI.dp(this, 10);
        content.addView(setup, slp);
        setup.setOnClickListener(v -> showSetup());
    }

    private boolean allPlayersDone() {
        for (boolean d : doneThisRound) {
            if (!d) return false;
        }
        return true;
    }

    private int[] bestToWorst() {
        int[] order = new int[players];
        for (int i = 0; i < players; i++) order[i] = i;
        for (int i = 0; i < players; i++) {
            for (int j = i + 1; j < players; j++) {
                if (scores[order[j]] > scores[order[i]]) {
                    int t = order[i];
                    order[i] = order[j];
                    order[j] = t;
                }
            }
        }
        return order;
    }

    private String tally() {
        StringBuilder sb = new StringBuilder("Score   ");
        for (int i = 0; i < players; i++) {
            sb.append("P").append(i + 1).append(": ").append(scores[i]).append("  ");
        }
        return sb.toString();
    }

    private static final class SpectrumView extends View {

        private final Paint bgPaint;
        private final Paint zonePaint;
        private final Paint targetPaint;
        private final Paint markerPaint;
        private final Paint textPaint;
        private String left = "Left";
        private String right = "Right";
        private float guess = 50f;
        private boolean revealed;
        private float target = 50f;
        private float zoneHalf = 15f;

        SpectrumView(Context c) {
            super(c);
            bgPaint = new Paint();
            bgPaint.setAntiAlias(true);
            bgPaint.setColor(0xFFE9E6E0);

            zonePaint = new Paint();
            zonePaint.setAntiAlias(true);
            zonePaint.setColor(UI.withAlpha(UI.INDIGO, 40));

            targetPaint = new Paint();
            targetPaint.setAntiAlias(true);
            targetPaint.setColor(UI.INDIGO);
            targetPaint.setStrokeWidth(UI.dp(c, 3));

            markerPaint = new Paint();
            markerPaint.setAntiAlias(true);
            markerPaint.setColor(UI.INK);
            markerPaint.setStrokeWidth(UI.dp(c, 4));

            textPaint = new Paint();
            textPaint.setAntiAlias(true);
            textPaint.setColor(UI.INK);
            textPaint.setTextSize(UI.dp(c, 13));
            textPaint.setFakeBoldText(true);
        }

        void setLabels(String l, String r) {
            left = l;
            right = r;
            invalidate();
        }

        void setGuess(int g) {
            guess = g;
            invalidate();
        }

        int getGuess() {
            return Math.round(guess);
        }

        void setRevealed(boolean r) {
            revealed = r;
            invalidate();
        }

        void setTarget(int t, int half) {
            target = t;
            zoneHalf = half;
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            float w = getWidth();
            float h = getHeight();
            float padX = w * 0.06f;
            float barTop = h * 0.32f;
            float barBot = h * 0.62f;
            float barW = w - padX * 2f;
            RectF bar = new RectF(padX, barTop, w - padX, barBot);
            float radius = UI.dp(getContext(), 16);

            canvas.drawRoundRect(bar, radius, radius, bgPaint);

            if (revealed) {
                float zx = padX + barW * (target - zoneHalf) / 100f;
                float zxx = padX + barW * (target + zoneHalf) / 100f;
                RectF zone = new RectF(zx, barTop, zxx, barBot);
                canvas.drawRoundRect(zone, radius, radius, zonePaint);
                float tx = padX + barW * target / 100f;
                canvas.drawLine(tx, barTop, tx, barBot, targetPaint);
            }

            float gx = padX + barW * guess / 100f;
            canvas.drawLine(gx, barTop - UI.dp(getContext(), 8),
                    gx, barBot + UI.dp(getContext(), 8), markerPaint);

            canvas.drawText(left, padX, h * 0.84f, textPaint);
            float rw = textPaint.measureText(right);
            canvas.drawText(right, w - padX - rw, h * 0.84f, textPaint);
        }

        @Override
        public boolean onTouchEvent(MotionEvent ev) {
            if (revealed) return true;
            int action = ev.getAction();
            if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
                float w = getWidth();
                float padX = w * 0.06f;
                float barW = w - padX * 2f;
                float p = (ev.getX() - padX) / barW * 100f;
                guess = Math.max(0f, Math.min(100f, p));
                invalidate();
                return true;
            }
            return true;
        }
    }
}
