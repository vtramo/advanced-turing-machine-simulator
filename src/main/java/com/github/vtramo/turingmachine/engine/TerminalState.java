package com.github.vtramo.turingmachine.engine;

import lombok.Getter;

import java.util.Objects;

@Getter
public enum TerminalState {
    ACCEPTING_STATE("yes"), REJECTING_STATE("no"), HALTING_STATE("h");

    private final String symbol;

    TerminalState(String symbol) {
        this.symbol = symbol;
    }

    public static boolean isTerminalState(final String state) {
        return Objects.equals(state, ACCEPTING_STATE.symbol) ||
            Objects.equals(state, REJECTING_STATE.symbol) ||
            Objects.equals(state, HALTING_STATE.symbol);
    }
}
