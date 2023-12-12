package com.github.vtramo.turingmachine.engine;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

public record Transition(String state, List<Move> moves) {
    public int totalTapes() {
        return moves.size();
    }

    public static Transition of(final String state, final Move ... moves) {
        return new Transition(state, new ArrayList<>(asList(moves)));
    }

    public static Transition rejectingTransition(final StateAndSymbols currentStateAndSymbols) {
        final Move[] moves = new Move[currentStateAndSymbols.totalSymbols()];
        final char[] symbols = currentStateAndSymbols.symbols();
        for (int i = 0; i < symbols.length; i++) {
            final char symbol = symbols[i];
            moves[i] = (symbol == Symbol.START.getSymbol())
                ? Move.START_SYMBOL_MOVE
                : Move.of(symbol, Direction.STAY);
        }
        return Transition.of(TerminalState.REJECTING_STATE.getSymbol(), moves);
    }
}
