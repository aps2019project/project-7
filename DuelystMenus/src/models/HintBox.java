package models;

import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

class HintBox extends VBox {
    private static final Background MENU_HINT_BACKGROUND = new Background(
            new BackgroundFill(Color.rgb(40, 40, 40), new CornerRadii(Constants.DEFAULT_SPACING * 3), Insets.EMPTY)
    );

    HintBox(String hintText) {
        getChildren().add(makeHintText(hintText));
        setPadding(new Insets(Constants.DEFAULT_SPACING));
        setBackground(MENU_HINT_BACKGROUND);
        setVisible(false);
    }

    private Text makeHintText(String text) {
        Text hint = new Text(text);
        hint.setWrappingWidth(Constants.MENU_HINT_WIDTH);
        hint.setTextAlignment(TextAlignment.CENTER);
        hint.setFont(Constants.MENU_HINT_FONT);
        hint.setFill(Color.WHITE);
        return hint;
    }
}