package io.github.abdurazaaqmohammed.gamesbox;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import io.github.abdurazaaqmohammed.gamesbox.ui.UI;

public class PlayerNamesActivity extends Activity {

    private static final int ACCENT = UI.SKY;

    private LinearLayout content;
    private final EditText[] fields = new EditText[PlayerNames.MAX];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        content = UI.screen(this, "Player Names",
                "Names are saved and used in every game", ACCENT);

        TextView rules = UI.text(this,
                "Type a name for each player and it's saved automatically - every game shows these names instead of \"Player 1\", \"Player 2\" and so on. You can come back and change them anytime.",
                UI.INK_SOFT, 14, false);
        rules.setPadding(0, 0, 0, UI.dp(this, 16));
        content.addView(rules);

        for (int i = 0; i < PlayerNames.MAX; i++) {
            content.addView(nameRow(i));
        }

        content.addView(UI.space(this, 8));

        TextView clear = UI.ghost(this, "Clear All Names", 15, 14);
        content.addView(clear, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        clear.setOnClickListener(v -> {
            PlayerNames.clear(PlayerNamesActivity.this);
            for (EditText f : fields) {
                f.setText("");
            }
        });
    }

    private View nameRow(final int index) {
        LinearLayout row = UI.row(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, 0, 0, UI.dp(this, 10));

        TextView label = UI.text(this, "Player " + (index + 1), UI.INK, 14, true);
        label.setPadding(0, 0, UI.dp(this, 10), 0);
        row.addView(label, new LinearLayout.LayoutParams(
                UI.dp(this, 84), ViewGroup.LayoutParams.WRAP_CONTENT));

        final EditText et = new EditText(this);
        et.setSingleLine(true);
        et.setTextSize(14);
        et.setTextColor(UI.INK);
        et.setHintTextColor(UI.INK_SOFT);
        et.setHint("Enter a name");
        et.setBackgroundDrawable(UI.cardBg(this));
        et.setPadding(UI.dp(this, 12), UI.dp(this, 8), UI.dp(this, 12), UI.dp(this, 8));
        et.setText(PlayerNames.raw(this, index));
        et.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int a, int b, int c) {
            }

            public void onTextChanged(CharSequence s, int a, int b, int c) {
                PlayerNames.set(PlayerNamesActivity.this, index, s.toString());
            }

            public void afterTextChanged(Editable s) {
            }
        });
        fields[index] = et;

        row.addView(et, new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        return row;
    }
}
