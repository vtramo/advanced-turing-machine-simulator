package com.github.vtramo.turingmachine.engine;

import lombok.Getter;

@Getter
public enum Symbol {
    BLANK('_'), START('>');

    private final char symbol;

    Symbol(final char symbol) {
        this.symbol = symbol;
    }
}
