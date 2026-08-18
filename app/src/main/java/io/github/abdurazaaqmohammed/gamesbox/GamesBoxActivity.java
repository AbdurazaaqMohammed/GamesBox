package io.github.abdurazaaqmohammed.gamesbox;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import io.github.abdurazaaqmohammed.gamesbox.game.BullsAndCowsActivity;
import io.github.abdurazaaqmohammed.gamesbox.game.BowlingActivity;
import io.github.abdurazaaqmohammed.gamesbox.game.CharadesActivity;
import io.github.abdurazaaqmohammed.gamesbox.game.AccentRouletteActivity;
import io.github.abdurazaaqmohammed.gamesbox.game.EmojiMovieActivity;
import io.github.abdurazaaqmohammed.gamesbox.game.FiveSecondActivity;
import io.github.abdurazaaqmohammed.gamesbox.game.GenreSwapActivity;
import io.github.abdurazaaqmohammed.gamesbox.game.HeadsUpActivity;
import io.github.abdurazaaqmohammed.gamesbox.game.HigherOrLowerActivity;
import io.github.abdurazaaqmohammed.gamesbox.game.HotPotatoActivity;
import io.github.abdurazaaqmohammed.gamesbox.game.ImposterActivity;
import io.github.abdurazaaqmohammed.gamesbox.game.KnifeHitActivity;
import io.github.abdurazaaqmohammed.gamesbox.game.MadLibsActivity;
import io.github.abdurazaaqmohammed.gamesbox.game.MemoryMatchActivity;
import io.github.abdurazaaqmohammed.gamesbox.game.MostLikelyActivity;
import io.github.abdurazaaqmohammed.gamesbox.game.NeverHaveIActivity;
import io.github.abdurazaaqmohammed.gamesbox.game.PictionaryActivity;
import io.github.abdurazaaqmohammed.gamesbox.game.ReactionActivity;
import io.github.abdurazaaqmohammed.gamesbox.game.RhymeTimeActivity;
import io.github.abdurazaaqmohammed.gamesbox.game.ScavengerHuntActivity;
import io.github.abdurazaaqmohammed.gamesbox.game.SimonSaysActivity;
import io.github.abdurazaaqmohammed.gamesbox.game.SpikeArenaActivity;
import io.github.abdurazaaqmohammed.gamesbox.game.StopBusActivity;
import io.github.abdurazaaqmohammed.gamesbox.game.StoryChainActivity;
import io.github.abdurazaaqmohammed.gamesbox.game.SuperRecallActivity;
import io.github.abdurazaaqmohammed.gamesbox.game.TabooActivity;
import io.github.abdurazaaqmohammed.gamesbox.game.TimeMasterActivity;
import io.github.abdurazaaqmohammed.gamesbox.game.TruthOrDareActivity;
import io.github.abdurazaaqmohammed.gamesbox.game.TwentyQuestionsActivity;
import io.github.abdurazaaqmohammed.gamesbox.game.TwoTruthsActivity;
import io.github.abdurazaaqmohammed.gamesbox.game.WavelengthActivity;
import io.github.abdurazaaqmohammed.gamesbox.game.WerewolfActivity;
import io.github.abdurazaaqmohammed.gamesbox.game.WhackAMoleActivity;
import io.github.abdurazaaqmohammed.gamesbox.game.WordChainActivity;
import io.github.abdurazaaqmohammed.gamesbox.game.WouldYouRatherActivity;
import io.github.abdurazaaqmohammed.gamesbox.ui.UI;

public class GamesBoxActivity extends Activity {

    private static final class GameInfo {
        final String name;
        final String tag;
        final int accent;
        final Class<?> cls;

        GameInfo(String name, String tag, int accent, Class<?> cls) {
            this.name = name;
            this.tag = tag;
            this.accent = accent;
            this.cls = cls;
        }
    }

    private static final class SectionInfo {
        final String title;
        final GameInfo[] games;

        SectionInfo(String title, GameInfo[] games) {
            this.title = title;
            this.games = games;
        }
    }

    private static final SectionInfo[] SECTIONS = {
            new SectionInfo("Party Setup", new GameInfo[]{
                    new GameInfo("Player Names", "Set names for every game", UI.SKY, PlayerNamesActivity.class),
            }),
            new SectionInfo("Talk & Truth", new GameInfo[]{
                    new GameInfo("Would You Rather", "Pass and pick", UI.PINK, WouldYouRatherActivity.class),
                    new GameInfo("Never Have I Ever", "Drop a finger", UI.MINT, NeverHaveIActivity.class),
                    new GameInfo("Most Likely To", "Point at them", UI.SKY, MostLikelyActivity.class),
                    new GameInfo("Truth or Dare", "Pick one", UI.CORAL, TruthOrDareActivity.class),
                    new GameInfo("Two Truths and a Lie", "Spot the fib", UI.ROSE, TwoTruthsActivity.class),
            }),
            new SectionInfo("Word Games", new GameInfo[]{
                    new GameInfo("Stop the Bus", "Word race", UI.TEAL, StopBusActivity.class),
                    new GameInfo("Mad Libs", "Fill-in silly stories", UI.OLIVE, MadLibsActivity.class),
                    new GameInfo("Taboo", "Don't say the banned words", UI.LIME, TabooActivity.class),
                    new GameInfo("5 Second Rule", "Name 3 things fast", UI.CYAN, FiveSecondActivity.class),
                    new GameInfo("Word Chain", "Last-letter chain", UI.SLATE, WordChainActivity.class),
                    new GameInfo("Rhyme Time", "Pass the rhyming phone", UI.PINK, RhymeTimeActivity.class),
                    new GameInfo("Story Chain", "Each player adds a sentence", UI.AMBER, StoryChainActivity.class),
            }),
            new SectionInfo("Act & Draw", new GameInfo[]{
                    new GameInfo("Charades", "Act it out", UI.GOLD, CharadesActivity.class),
                    new GameInfo("Heads Up", "Guess the word, tilt to score", UI.ORANGE, HeadsUpActivity.class),
                    new GameInfo("Pictionary", "Draw it, guess it", UI.TAUPE, PictionaryActivity.class),
                    new GameInfo("Genre Swap", "Pitch a movie in a weird genre", UI.ROSE, GenreSwapActivity.class),
                    new GameInfo("Accent Roulette", "Deliver the line in a silly accent", UI.CORAL, AccentRouletteActivity.class),
            }),
            new SectionInfo("Deduce & Guess", new GameInfo[]{
                    new GameInfo("Emoji Movie", "Guess the title", UI.BLUE, EmojiMovieActivity.class),
                    new GameInfo("20 Questions", "Guess the secret word", UI.INDIGO, TwentyQuestionsActivity.class),
                    new GameInfo("Higher or Lower", "Guess the next number", UI.SKY, HigherOrLowerActivity.class),
                    new GameInfo("Bulls and Cows", "Crack the code", UI.CYAN, BullsAndCowsActivity.class),
                    new GameInfo("Wavelength", "Guesstimate on a spectrum", UI.INDIGO, WavelengthActivity.class),
                    new GameInfo("Imposter", "Find the faker", UI.PURPLE, ImposterActivity.class),
                    new GameInfo("Werewolf", "A night of deception", UI.PURPLE, WerewolfActivity.class),
            }),
            new SectionInfo("Reflexes & Skill", new GameInfo[]{
                    new GameInfo("Hot Potato", "Say a word, pass the phone", UI.RED, HotPotatoActivity.class),
                    new GameInfo("Simon Says", "One order, judge your group", UI.RED, SimonSaysActivity.class),
                    new GameInfo("Reaction", "Tap when it turns green", UI.LIME, ReactionActivity.class),
                    new GameInfo("Time Master", "Stop at the target time", UI.TEAL, TimeMasterActivity.class),
                    new GameInfo("Don't Touch the Spikes", "Flap the bird, dodge spikes", UI.ORANGE, SpikeArenaActivity.class),
                    new GameInfo("Knife Hit", "Throw all blades, dodge stuck knives", UI.AMBER, KnifeHitActivity.class),
                    new GameInfo("Whack-a-Mole", "Whack only your color", UI.OLIVE, WhackAMoleActivity.class),
                    new GameInfo("Bowling", "Swipe to aim, roll true", UI.CYAN, BowlingActivity.class),
                    new GameInfo("Super Recall", "Copy the symbol pattern", UI.GOLD, SuperRecallActivity.class),
                    new GameInfo("Memory Match", "Flip and match", UI.AMBER, MemoryMatchActivity.class),
            }),
            new SectionInfo("Get Moving", new GameInfo[]{
                    new GameInfo("Scavenger Hunt", "Race to find the items", UI.MINT, ScavengerHuntActivity.class),
            }),
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildUI();
    }

    private void buildUI() {
        UI.bg(this, UI.PAPER, UI.PAPER);

        LinearLayout root = UI.col(this);
        root.setGravity(Gravity.TOP);
        root.setPadding(UI.dp(this, 18), UI.dp(this, 18), UI.dp(this, 18), UI.dp(this, 18));

        TextView title = UI.text(this, "GamesBox", UI.INK, 26, true);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        TextView sub = UI.text(this, "App theme",
                UI.INK_SOFT, 13, false);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, UI.dp(this, 14), 0, UI.dp(this, 4));
        root.addView(sub);

        root.addView(themeSelector());

        root.addView(UI.space(this, 4));

        LinearLayout grid = UI.col(this);
        for (SectionInfo sec : SECTIONS) {
            grid.addView(sectionHeader(sec.title, sec.games[0].accent));
            LinearLayout row = UI.row(this);
            row.setGravity(Gravity.TOP);
            int cols = 2;
            for (int i = 0; i < sec.games.length; i++) {
                final GameInfo g = sec.games[i];
                row.addView(card(g), new LinearLayout.LayoutParams(
                        0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                if ((i + 1) % cols == 0) {
                    grid.addView(row, new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    row = UI.row(this);
                    row.setGravity(Gravity.TOP);
                }
            }
            if (sec.games.length % cols != 0) {
                grid.addView(row, new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }
        }

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setVerticalScrollBarEnabled(false);
        scroll.addView(grid, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(scroll, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        setContentView(root);
    }

    private View sectionHeader(String title, int accent) {
        LinearLayout head = UI.row(this);
        head.setGravity(Gravity.CENTER_VERTICAL);
        head.setPadding(0, UI.dp(this, 16), 0, UI.dp(this, 8));

        View bar = new View(this);
        bar.setBackgroundDrawable(UI.fill(accent, 1, this));
        head.addView(bar, new LinearLayout.LayoutParams(
                UI.dp(this, 4), UI.dp(this, 16)));

        TextView t = UI.text(this, title, UI.INK, 15, true);
        t.setGravity(Gravity.START);
        t.setPadding(UI.dp(this, 8), 0, 0, 0);
        head.addView(t);

        return head;
    }

    private View themeSelector() {
        final int current = UI.getTheme(this);
        final String[] names = {"Light", "Dark", "Black"};
        final int[] themes = {UI.THEME_LIGHT, UI.THEME_DARK, UI.THEME_BLACK};

        LinearLayout row = UI.row(this);
        row.setGravity(Gravity.CENTER_HORIZONTAL);
        for (int i = 0; i < names.length; i++) {
            final int t = themes[i];
            TextView chip = current == t
                    ? UI.chip(this, names[i], UI.SKY, 13)
                    : UI.chipGhost(this, names[i], UI.INK, 13);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            lp.setMargins(UI.dp(this, 3), 0, UI.dp(this, 3), 0);
            row.addView(chip, lp);
            chip.setOnClickListener(v -> {
                if (UI.getTheme(GamesBoxActivity.this) != t) {
                    UI.setTheme(GamesBoxActivity.this, t);
                    buildUI();
                }
            });
        }
        return row;
    }

    private View card(final GameInfo g) {
        LinearLayout card = UI.col(this);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(UI.dp(this, 14), UI.dp(this, 14), UI.dp(this, 14), UI.dp(this, 14));
        card.setBackgroundDrawable(UI.cardBg(this));

        View bar = new View(this);
        bar.setBackgroundDrawable(UI.fill(g.accent, 1, this));
        LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(
                UI.dp(this, 24), UI.dp(this, 3));
        card.addView(bar, blp);

        TextView name = UI.text(this, g.name, UI.INK, 16, true);
        name.setGravity(Gravity.START);
        name.setPadding(0, UI.dp(this, 10), 0, 0);
        card.addView(name);

        TextView tag = UI.text(this, g.tag, UI.INK_SOFT, 11, false);
        tag.setGravity(Gravity.START);
        tag.setPadding(0, UI.dp(this, 3), 0, 0);
        card.addView(tag);

        card.setOnClickListener(v -> startActivity(new Intent(GamesBoxActivity.this, g.cls)));
        UI.pressy(card);
        return card;
    }
}
