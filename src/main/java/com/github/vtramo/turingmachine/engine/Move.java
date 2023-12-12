package com.github.vtramo.turingmachine.engine;

public record Move(char symbol, Direction direction) {
    public static Move START_SYMBOL_MOVE = Move.of(Symbol.START.getSymbol(), Direction.RIGHT);

    public static Move of(char symbol, Direction direction) {
        return new Move(symbol, direction);
    }
}
