package com.github.vtramo.turingmachine.ui;

import javafx.geometry.Insets;
import lombok.Getter;

@Getter
public class CursorView extends SvgSymbolView {

    public static final char CURSOR_CHAR = 0;
    private static final String svgPathId = "E1-MJAMS-25B2";
    private static final String svgPathContent = "M99 -20Q84 -11 84 0Q84 5 148 145T278 424L342 563Q347 575 360 575Q368 575 375 570Q376 569 441 430T571 148T637 0Q637 -11 622 -20H99Z";
    public CursorView() {
        super();
        this.setMinSize(35, 35);
        this.setPadding(new Insets(10, 10, 10, 10));
        this.setMaxSize(35, 35);
        this.setPrefSize(35, 35);
        this.setRotate(180);
        this.setStyle("-fx-background-color: black;");
    }

    @Override
    protected String getSvgPathId() {
        return svgPathId;
    }

    @Override
    protected String getSvgPathContent() {
        return svgPathContent;
    }

    public static CursorView cursor() {
        return new CursorView();
    }

    @Override
    public char getChar() {
        return CURSOR_CHAR;
    }
}