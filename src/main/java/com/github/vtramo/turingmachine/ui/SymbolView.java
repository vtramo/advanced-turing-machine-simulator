package com.github.vtramo.turingmachine.ui;

import com.github.vtramo.turingmachine.engine.Symbol;
import javafx.scene.Node;

import java.util.Objects;

import static com.github.vtramo.turingmachine.ui.BlankSymbolView.blank;
import static com.github.vtramo.turingmachine.ui.StartSymbolView.startSymbol;

public interface SymbolView {
    Node getNode();
    char getChar();

    static SymbolView of(final char symbol) {
        if (Objects.equals(symbol, Symbol.BLANK.getSymbol())) {
            return blank();
        } else if (Objects.equals(symbol, Symbol.START.getSymbol())) {
            return startSymbol();
        } else if (symbol == CursorView.CURSOR_CHAR) {
            return new CursorView();
        } else {
            return new CharSymbolView(symbol);
        }
    }
}
