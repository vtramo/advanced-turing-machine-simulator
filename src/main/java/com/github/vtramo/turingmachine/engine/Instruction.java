package com.github.vtramo.turingmachine.engine;

import com.github.vtramo.turingmachine.engine.exception.MalformedInstructionException;

public record Instruction(StateAndSymbols stateAndSymbols, Transition transition) {

    public static Instruction of(final String left, final String right) {
        final String[] leftArray = left.split(",");
        final String[] rightArray = right.split(",");

        final String state = leftArray[0].strip();
        final String nextState = rightArray[0].strip();

        final char[] symbols = new char[leftArray.length - 1];
        for (int i = 1; i < leftArray.length; i++) {
            final char symbol = leftArray[i].strip().toCharArray()[0];
            symbols[i - 1] = symbol;
        }

        final Move[] moves = new Move[symbols.length];
        for (int i = 1, j = 0; i < rightArray.length; i += 2, j++) {
            final char symbol = rightArray[i].strip().toCharArray()[0];
            if (symbol == '*') {
                throw new MalformedInstructionException("It is not possible to write special symbol '*'!");
            }
            final Direction direction = Direction.of(rightArray[i + 1].strip());
            moves[j] = Move.of(symbol, direction);
        }

        final StateAndSymbols stateAndSymbols = StateAndSymbols.of(state, symbols);
        final Transition transition = Transition.of(nextState, moves);
        return new Instruction(stateAndSymbols, transition);
    }
}