package com.github.vtramo.turingmachine.engine;

import lombok.Getter;

import java.util.Objects;

@Getter
public class Configuration {
    private final String state;
    private final Tape[] tapes;
    private final StateAndSymbols currentStateAndSymbols;
    private int space;
    private int spaceInputOutput;

    public Configuration(final String state, final Tape[] tapes) {
        this(state, tapes, buildCurrentStateAndSymbols(state, tapes));
    }

    public Configuration(final String state, final Tape[] tapes, final StateAndSymbols currentStateAndSymbols) {
        this.state = state;
        this.tapes = tapes;
        this.currentStateAndSymbols = currentStateAndSymbols;
        checkTapes();
        calculateSpace();
    }

    private void checkTapes() {
        Objects.requireNonNull(state);
        Objects.requireNonNull(tapes);
        if (state.isEmpty() || state.isBlank()) throw new IllegalArgumentException("The state cannot be empty!");
    }

    private void calculateSpace() {
        int space = 0, spaceInputOutput = 0;

        final int numTapes = tapes.length;
        if (numTapes == 2) {
            space = spaceInputOutput = tapes[0].getSpace() + tapes[1].getSpace();
        } else if (numTapes == 1) {
            space = spaceInputOutput = tapes[0].getSpace();
        } else {
            space = tapes[0].getSpace();
            for (int i = 1; i < numTapes - 1; i++) {
                final int tapeSpace = tapes[i].getSpace();
                space += tapeSpace;
                spaceInputOutput += tapeSpace;
            }
            space += tapes[numTapes - 1].getSpace();
        }

        this.space = space;
        this.spaceInputOutput = spaceInputOutput;
    }

    public char[] currentSymbols() { return currentStateAndSymbols.symbols(); }
    public Tape inputTape() {
        return tapes[0];
    }
    public Tape outputTape() {
        return tapes[tapes.length - 1];
    }

    private static StateAndSymbols buildCurrentStateAndSymbols(final String state, final Tape[] tapes) {
        final char[] symbols = new char[tapes.length];
        int tapeIndex = 0;
        for (final Tape tape: tapes) {
            final char currentSymbol = tape.getCurrentSymbol();
            symbols[tapeIndex++] = currentSymbol;
        }
        return StateAndSymbols.of(state, symbols);
    }

    public static Configuration buildInitialConfiguration(
        final String initialState,
        final String input,
        final int totalTapes
    ) {
        final Tape[] tapes = new Tape[totalTapes];
        final char startSymbol = Symbol.START.getSymbol();
        final String inputWithStartSymbol = startSymbol + input;
        tapes[0] = Tape.of(inputWithStartSymbol.toCharArray(), 0);
        for (int i = 1; i < totalTapes; i++) {
            tapes[i] = Tape.of(new char[] { startSymbol }, 0);
        }
        return new Configuration(initialState, tapes);
    }

    @Override
    public String toString() {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("(")
            .append(state)
            .append(",");
        for (final Tape tape: tapes) {
            stringBuilder.append(tape.toString())
                .append(",");
        }
        stringBuilder.deleteCharAt(stringBuilder.lastIndexOf(","));
        stringBuilder.append(")");
        return stringBuilder.toString();
    }
}
