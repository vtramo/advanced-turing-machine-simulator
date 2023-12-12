package com.github.vtramo.turingmachine.ui;

import com.github.vtramo.turingmachine.engine.Symbol;

public class BlankSymbolView extends SvgSymbolView {
    private static final String svgPathId = "E1-MJMAIN-2294";
    private static final String svgPathContent = "M77 0Q65 4 61 16V301L62 585Q72 598 81 598Q94 598 101 583V40H565V583Q573 598 585 598Q598 598 605 583V15Q602 10 592 1L335 0H77Z";

    public BlankSymbolView() {
        this.setMinSize(30, 30);
        this.setMaxSize(30, 30);
        this.setPrefSize(30, 30);
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

    @Override
    public char getChar() {
        return Symbol.BLANK.getSymbol();
    }

    public static BlankSymbolView blank() {
        return new BlankSymbolView();
    }
}
