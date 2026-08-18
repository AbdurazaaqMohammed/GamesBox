package io.github.abdurazaaqmohammed.gamesbox.game;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import io.github.abdurazaaqmohammed.gamesbox.PlayerNames;
import io.github.abdurazaaqmohammed.gamesbox.ui.UI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WerewolfActivity extends BaseGameActivity {

    private static final int ACCENT = UI.PURPLE;
    private static final int ACCENT_D = UI.PURPLE_D;

    private static final int VILLAGER = 0;
    private static final int WEREWOLF = 1;
    private static final int SEER = 2;
    private static final int DOCTOR = 3;

    private LinearLayout content;
    private UI.Stepper playersStepper;
    private int players;
    private int[] role;
    private boolean[] alive;
    private int victim = -1;
    private int saved = -1;
    private List<String> log;

    private String playerName(int i) {
        return PlayerNames.get(this, i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        content = UI.screen(this, "Werewolf",
                "A night of deception", ACCENT);
        showSetup();
    }

    private void showSetup() {
        content.removeAllViews();

        TextView rules = UI.text(this,
                "Pass the phone around at night - roles stay secret. Werewolves pick a victim, the Seer snoops, the Doctor saves. By day the village votes someone out. Wolves win when they outnumber the village. Villagers win when every wolf is gone.",
                UI.INK_SOFT, 14, false);
        rules.setPadding(0, 0, 0, UI.dp(this, 16));
        content.addView(rules);

        playersStepper = new UI.Stepper(this, "Players", 4, 10, 6, 1);
        content.addView(playersStepper.row);

        content.addView(UI.space(this, 16));

        TextView start = UI.button(this, "Deal the roles", ACCENT, ACCENT_D, 17, 16);
        content.addView(start, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        start.setOnClickListener(v -> {
            players = playersStepper.get();
            role = new int[players];
            alive = new boolean[players];
            for (int i = 0; i < players; i++) alive[i] = true;
            log = new ArrayList<>();
            dealRoles();
        });
    }

    private void dealRoles() {
        int wolves = players <= 5 ? 1 : (players <= 9 ? 2 : 3);
        int seers = 1;
        int doctors = players >= 7 ? 1 : 0;
        int villagers = players - wolves - seers - doctors;
        if (villagers < 1) {
            wolves = players - seers - doctors - 1;
            villagers = 1;
        }

        List<Integer> deck = new ArrayList<>();
        for (int i = 0; i < wolves; i++) deck.add(WEREWOLF);
        for (int i = 0; i < seers; i++) deck.add(SEER);
        for (int i = 0; i < doctors; i++) deck.add(DOCTOR);
        for (int i = 0; i < villagers; i++) deck.add(VILLAGER);
        Collections.shuffle(deck, UI.RND);

        role = new int[players];
        for (int i = 0; i < players; i++) role[i] = deck.get(i);
        showDeal(0);
    }

    private void showDeal(final int i) {
        content.removeAllViews();

        TextView who = UI.text(this, playerName(i),
                UI.INK, 18, true);
        who.setGravity(Gravity.CENTER);
        who.setPadding(0, UI.dp(this, 4), 0, UI.dp(this, 10));
        content.addView(who);

        TextView take = UI.text(this, "Take the phone ALONE.\nSwipe up to peek your role.",
                UI.INK_SOFT, 13, false);
        take.setGravity(Gravity.CENTER);
        take.setLineSpacing(UI.dp(this, 4), 1f);
        take.setPadding(0, 0, 0, UI.dp(this, 14));
        content.addView(take);

        StringBuilder secret = new StringBuilder(roleName(role[i]))
                .append("\n\n").append(roleDesc(role[i]));
        if (role[i] == WEREWOLF) {
            StringBuilder pack = new StringBuilder("\n\nYour pack: ");
            boolean first = true;
            for (int j = 0; j < players; j++) {
                if (j != i && role[j] == WEREWOLF) {
                    if (!first) pack.append(", ");
                    pack.append("P").append(j + 1);
                    first = false;
                }
            }
            if (first) pack.append("you are the only wolf");
            secret.append(pack);
        }

        UI.RevealCard card = new UI.RevealCard(this, secret.toString(), roleColor(role[i]));
        content.addView(card, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, UI.dp(this, 260)));

        final TextView done = UI.button(this,
                i < players - 1 ? "Got it - pass to " + playerName(i + 1) : "I've seen it",
                ACCENT, ACCENT_D, 16, 15);
        done.setVisibility(View.GONE);
        LinearLayout.LayoutParams dlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dlp.topMargin = UI.dp(this, 14);
        content.addView(done, dlp);
        done.setOnClickListener(v -> {
            if (i < players - 1) {
                showDeal(i + 1);
            } else {
                showNightFall();
            }
        });

        card.setOnRevealed(() -> {
            done.setVisibility(View.VISIBLE);
            UI.popIn(done, 200);
        });
    }

    private void showNightFall() {
        victim = -1;
        saved = -1;
        content.removeAllViews();

        TextView title = UI.text(this, "Night falls...", UI.INK, 26, true);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, UI.dp(this, 10), 0, UI.dp(this, 8));
        content.addView(title);

        TextView sub = UI.text(this, "Everyone close their eyes and lower the phone face-down.",
                UI.INK_SOFT, 14, false);
        sub.setGravity(Gravity.CENTER);
        sub.setLineSpacing(UI.dp(this, 4), 1f);
        sub.setPadding(0, 0, 0, UI.dp(this, 18));
        content.addView(sub);

        TextView go = UI.button(this, "The werewolves wake up", ACCENT, ACCENT_D, 16, 15);
        content.addView(go, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        go.setOnClickListener(v -> nightNext());
    }

    private void nightNext() {
        if (wolvesAlive() > 0) {
            showWolfPick();
        } else if (roleAlive(SEER)) {
            showSeerPick();
        } else if (roleAlive(DOCTOR)) {
            showDoctorPick();
        } else {
            morning();
        }
    }

    private void showWolfPick() {
        content.removeAllViews();

        TextView title = UI.text(this, "Werewolves, wake up",
                UI.RED, 22, true);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, UI.dp(this, 4), 0, UI.dp(this, 4));
        content.addView(title);

        StringBuilder pack = new StringBuilder("Werewolves: ");
        boolean first = true;
        for (int j = 0; j < players; j++) {
            if (alive[j] && role[j] == WEREWOLF) {
                if (!first) pack.append(", ");
                pack.append("P").append(j + 1);
                first = false;
            }
        }
        TextView pk = UI.text(this, pack.toString(), UI.INK, 14, true);
        pk.setGravity(Gravity.CENTER);
        pk.setPadding(0, 0, 0, UI.dp(this, 10));
        content.addView(pk);

        TextView sub = UI.text(this, "Pick one victim together.",
                UI.INK_SOFT, 13, false);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 0, 0, UI.dp(this, 10));
        content.addView(sub);

        for (int j = 0; j < players; j++) {
            if (!alive[j] || role[j] == WEREWOLF) continue;
            final int target = j;
            TextView opt = choiceRow("P" + (j + 1), false);
            content.addView(opt, matchParams());
            opt.setOnClickListener(v -> {
                victim = target;
                wolvesToSeer();
            });
        }
    }

    private void wolvesToSeer() {
        if (roleAlive(SEER)) {
            showSeerPick();
        } else if (roleAlive(DOCTOR)) {
            showDoctorPick();
        } else {
            morning();
        }
    }

    private void showSeerPick() {
        content.removeAllViews();

        int seer = findRole(SEER);
        TextView title = UI.text(this, "Seer, wake up",
                UI.SKY, 22, true);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, UI.dp(this, 4), 0, UI.dp(this, 2));
        content.addView(title);

        TextView sub = UI.text(this, playerName(seer) + " - take the phone alone.\nChoose a player to investigate.",
                UI.INK_SOFT, 13, false);
        sub.setGravity(Gravity.CENTER);
        sub.setLineSpacing(UI.dp(this, 4), 1f);
        sub.setPadding(0, UI.dp(this, 8), 0, UI.dp(this, 10));
        content.addView(sub);

        for (int j = 0; j < players; j++) {
            if (!alive[j]) continue;
            final int target = j;
            TextView opt = choiceRow("P" + (j + 1), false);
            content.addView(opt, matchParams());
            opt.setOnClickListener(v -> showInvestigation(target));
        }
    }

    private void showInvestigation(final int target) {
        content.removeAllViews();

        boolean wolf = role[target] == WEREWOLF;
        TextView result = UI.text(this,
                "P" + (target + 1) + " is " + (wolf ? "a WEREWOLF!" : "not a Werewolf."),
                wolf ? UI.RED : UI.MINT, 24, true);
        result.setGravity(Gravity.CENTER);
        result.setPadding(0, UI.dp(this, 16), 0, UI.dp(this, 14));
        content.addView(result);

        TextView note = UI.text(this, "Only the Seer should see this.",
                UI.INK_SOFT, 12, false);
        note.setGravity(Gravity.CENTER);
        note.setPadding(0, 0, 0, UI.dp(this, 16));
        content.addView(note);

        TextView done = UI.button(this, "Done - back to sleep", UI.SKY, UI.SKY_D, 16, 15);
        content.addView(done, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        done.setOnClickListener(v -> {
            if (roleAlive(DOCTOR)) {
                showDoctorPick();
            } else {
                morning();
            }
        });
    }

    private void showDoctorPick() {
        content.removeAllViews();

        int doc = findRole(DOCTOR);
        TextView title = UI.text(this, "Doctor, wake up",
                UI.MINT, 22, true);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, UI.dp(this, 4), 0, UI.dp(this, 2));
        content.addView(title);

        TextView sub = UI.text(this, playerName(doc) + " - take the phone alone.\nWho do you save tonight?",
                UI.INK_SOFT, 13, false);
        sub.setGravity(Gravity.CENTER);
        sub.setLineSpacing(UI.dp(this, 4), 1f);
        sub.setPadding(0, UI.dp(this, 8), 0, UI.dp(this, 10));
        content.addView(sub);

        for (int j = 0; j < players; j++) {
            if (!alive[j]) continue;
            final int target = j;
            TextView opt = choiceRow("P" + (j + 1), false);
            content.addView(opt, matchParams());
            opt.setOnClickListener(v -> {
                saved = target;
                morning();
            });
        }
    }

    private void morning() {
        content.removeAllViews();

        boolean wasKilled = false;
        if (victim >= 0 && victim != saved && alive[victim]) {
            alive[victim] = false;
            wasKilled = true;
            log.add("P" + (victim + 1) + " (" + roleName(role[victim]) + ") was killed");
        } else if (victim >= 0 && victim == saved) {
            log.add("The doctor saved P" + (victim + 1));
        }

        TextView title = UI.text(this, "The sun rises...", UI.AMBER, 24, true);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, UI.dp(this, 6), 0, UI.dp(this, 8));
        content.addView(title);

        TextView report;
        if (wasKilled) {
            report = UI.text(this,
                    "P" + (victim + 1) + " was found dead.\nThey were a " + roleName(role[victim]) + ".",
                    UI.RED, 17, true);
        } else if (saved >= 0 && saved == victim) {
            report = UI.text(this, "No one died last night.\nThe doctor saved them!", UI.MINT, 17, true);
        } else {
            report = UI.text(this, "A quiet night. No one died.", UI.INK_SOFT, 17, true);
        }
        report.setGravity(Gravity.CENTER);
        report.setLineSpacing(UI.dp(this, 4), 1f);
        report.setPadding(0, 0, 0, UI.dp(this, 16));
        content.addView(report);

        TextView go = UI.button(this, "Time to vote", ACCENT, ACCENT_D, 16, 15);
        content.addView(go, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        go.setOnClickListener(v -> {
            int win = checkWin();
            if (win != 0) {
                showWin(win);
            } else {
                showDay();
            }
        });
    }

    private void showDay() {
        content.removeAllViews();

        TextView title = UI.text(this, "The village deliberates...",
                UI.INK, 20, true);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, UI.dp(this, 4), 0, UI.dp(this, 4));
        content.addView(title);

        TextView sub = UI.text(this, "Talk it out, accuse, and vote.\nWho gets banished?",
                UI.INK_SOFT, 13, false);
        sub.setGravity(Gravity.CENTER);
        sub.setLineSpacing(UI.dp(this, 4), 1f);
        sub.setPadding(0, UI.dp(this, 8), 0, UI.dp(this, 10));
        content.addView(sub);

        for (int j = 0; j < players; j++) {
            if (!alive[j]) continue;
            final int target = j;
            TextView opt = choiceRow("P" + (j + 1), false);
            content.addView(opt, matchParams());
            opt.setOnClickListener(v -> banished(target));
        }
    }

    private void banished(int target) {
        alive[target] = false;
        log.add("P" + (target + 1) + " (" + roleName(role[target]) + ") was banished");

        content.removeAllViews();

        TextView title = UI.text(this, "P" + (target + 1) + " is banished!",
                UI.INK, 22, true);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, UI.dp(this, 4), 0, UI.dp(this, 6));
        content.addView(title);

        TextView reveal = UI.text(this, "They were a " + roleName(role[target]) + ".",
                roleColor(role[target]), 18, true);
        reveal.setGravity(Gravity.CENTER);
        reveal.setPadding(0, 0, 0, UI.dp(this, 16));
        content.addView(reveal);

        int win = checkWin();
        TextView go = UI.button(this, win != 0 ? "See the outcome" : "Night falls again",
                ACCENT, ACCENT_D, 16, 15);
        content.addView(go, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        go.setOnClickListener(v -> {
            if (win != 0) {
                showWin(win);
            } else {
                showNightFall();
            }
        });
    }

    private int checkWin() {
        int wolves = wolvesAlive();
        if (wolves == 0) return 1;
        int others = 0;
        for (int j = 0; j < players; j++) {
            if (alive[j] && role[j] != WEREWOLF) others++;
        }
        if (wolves >= others) return 2;
        return 0;
    }

    private void showWin(int winner) {
        content.removeAllViews();

        boolean wolvesWin = winner == 2;
        TextView title = UI.text(this, wolvesWin ? "The werewolves win!" : "The village wins!",
                wolvesWin ? UI.RED : UI.MINT, 28, true);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, UI.dp(this, 10), 0, UI.dp(this, 6));
        content.addView(title);

        TextView sub = UI.text(this,
                wolvesWin ? "The wolves have picked off the village." : "Every werewolf is gone.",
                UI.INK_SOFT, 14, false);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 0, 0, UI.dp(this, 12));
        content.addView(sub);

        StringBuilder reveal = new StringBuilder("Roles:\n");
        for (int j = 0; j < players; j++) {
            reveal.append("P").append(j + 1).append(" - ").append(roleName(role[j]));
            if (role[j] == WEREWOLF) reveal.append("  🐺");
            if (!alive[j]) reveal.append("  (out)");
            if (j < players - 1) reveal.append("\n");
        }
        TextView list = UI.text(this, reveal.toString(), UI.INK, 15, true);
        list.setGravity(Gravity.CENTER);
        list.setLineSpacing(UI.dp(this, 4), 1f);
        list.setPadding(UI.dp(this, 16), UI.dp(this, 14), UI.dp(this, 16), UI.dp(this, 14));
        list.setBackgroundDrawable(UI.cardBg(this));
        content.addView(list, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        if (log.size() > 0) {
            StringBuilder lb = new StringBuilder();
            for (String l : log) lb.append("• ").append(l).append("\n");
            TextView lg = UI.text(this, lb.toString().trim(), UI.INK_SOFT, 13, false);
            lg.setGravity(Gravity.CENTER);
            lg.setLineSpacing(UI.dp(this, 4), 1f);
            lg.setPadding(UI.dp(this, 16), UI.dp(this, 10), UI.dp(this, 16), UI.dp(this, 10));
            content.addView(lg);
        }

        content.addView(UI.space(this, 8));

        TextView again = UI.button(this, "Play again", ACCENT, ACCENT_D, 16, 15);
        content.addView(again, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        again.setOnClickListener(v -> dealRoles());

        TextView setup = UI.ghost(this, "Back to Setup", 15, 14);
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        slp.topMargin = UI.dp(this, 10);
        content.addView(setup, slp);
        setup.setOnClickListener(v -> showSetup());
    }

    private int wolvesAlive() {
        int n = 0;
        for (int j = 0; j < players; j++) {
            if (alive[j] && role[j] == WEREWOLF) n++;
        }
        return n;
    }

    private boolean roleAlive(int r) {
        return findRole(r) >= 0;
    }

    private int findRole(int r) {
        for (int j = 0; j < players; j++) {
            if (alive[j] && role[j] == r) return j;
        }
        return -1;
    }

    private TextView choiceRow(String label, boolean accent) {
        TextView t = UI.text(this, label, UI.INK, 16, true);
        t.setGravity(Gravity.CENTER);
        t.setPadding(0, UI.dp(this, 13), 0, UI.dp(this, 13));
        t.setBackgroundDrawable(accent ? UI.fill(ACCENT, 12, this) : UI.cardBg(this));
        UI.pressy(t);
        return t;
    }

    private LinearLayout.LayoutParams matchParams() {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = UI.dp(content.getContext(), 8);
        return lp;
    }

    private String roleName(int r) {
        switch (r) {
            case WEREWOLF: return "Werewolf 🐺";
            case SEER: return "Seer 🔮";
            case DOCTOR: return "Doctor 💉";
            default: return "Villager 👤";
        }
    }

    private int roleColor(int r) {
        switch (r) {
            case WEREWOLF: return UI.RED;
            case SEER: return UI.SKY;
            case DOCTOR: return UI.MINT;
            default: return UI.INK;
        }
    }

    private String roleDesc(int r) {
        switch (r) {
            case WEREWOLF: return "By night you hunt together and take one life. By day you lie through your teeth.";
            case SEER: return "Each night you peek at one player and learn their true role. Trust nobody.";
            case DOCTOR: return "Each night you save one player from the wolves. Choose wisely - you can't save yourself forever.";
            default: return "You sleep soundly. Find the wolves, argue your case, and vote them out at dawn.";
        }
    }
}
