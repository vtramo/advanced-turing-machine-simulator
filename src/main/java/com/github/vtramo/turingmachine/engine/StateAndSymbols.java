package com.github.vtramo.turingmachine.engine;

import java.util.Arrays;
import java.util.Objects;

public record StateAndSymbols(String state, char[] symbols) {
    public int totalSymbols() {
        return symbols.length;
    }

    public static StateAndSymbols of(final String state, final char ... symbols) {
        return new StateAndSymbols(state, symbols);
    }

    @Override
    public int hashCode() {
        return state.hashCode() + Arrays.hashCode(symbols);
    }

    @Override
    public boolean equals(final Object object) {
        return object instanceof StateAndSymbols other &&
            Objects.equals(other.state, this.state) &&
            Arrays.equals(other.symbols, this.symbols);
    }
}
