package com.github.vtramo.turingmachine.ui;

import javafx.scene.Node;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class CharSymbolView extends Text implements SymbolView {

    private final char symbol;

    public CharSymbolView(final char symbol) {
        this.symbol = symbol;
        setText(String.valueOf(symbol));
        setFont(Font.font(null, FontWeight.BOLD, 30));
    }

    @Override
    public Node getNode() {
        return this;
    }

    @Override
    public char getChar() {
        return symbol;
    }
}
