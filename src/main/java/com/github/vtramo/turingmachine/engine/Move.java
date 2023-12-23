package com.github.vtramo.turingmachine.engine;

public record Move(char symbol, Direction direction) {
    public boolean symbolIsAsterisk() {
        return symbol == '*';
    }
    public Move replaceAsteriskWith(final char symbol) {
        if (!symbolIsAsterisk()) throw new IllegalArgumentException("This move does not have an asterisk as a symbol!");
        return Move.of(symbol, direction);
    }
    public static Move START_SYMBOL_MOVE = Move.of(Symbol.START.getSymbol(), Direction.RIGHT);

    public static Move of(char symbol, Direction direction) {
        return new Move(symbol, direction);
    }
}
