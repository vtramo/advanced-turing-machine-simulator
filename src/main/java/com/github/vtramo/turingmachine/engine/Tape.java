package com.github.vtramo.turingmachine.engine;

import com.github.vtramo.turingmachine.engine.exception.InvalidTransitionException;
import com.github.vtramo.turingmachine.engine.exception.StartSymbolNotFoundException;

import java.util.Arrays;
import java.util.Objects;

public class Tape {
    private final char[] string;
    private final int cursor;

    public Tape(char[] string, int cursor) {
        this.string = string;
        this.cursor = cursor;
        checkString();
    }

    private void checkString() {
        Objects.requireNonNull(string);
        if (string.length == 0) throw new IllegalArgumentException("The tape cannot be empty!");
        if (string[0] != Symbol.START.getSymbol()) throw new StartSymbolNotFoundException();
        if (cursor < 0 || cursor >= string.length) throw new IndexOutOfBoundsException();
    }

    public char getCurrentSymbol() {
        return string[cursor];
    }

    public Tape move(final Move move) {
        final Direction direction = move.direction();
        final char currentSymbol = getCurrentSymbol();
        if (currentSymbol == Symbol.START.getSymbol() && direction != Direction.RIGHT) {
            throw new InvalidTransitionException("Start symbol transition not respected!");
        }

        final char symbol = move.symbol();
        final int newCursor = cursor + direction.getValue();
        final boolean crossedToTheRight = (newCursor == string.length);
        final char[] newString = Arrays.copyOf(string, crossedToTheRight ? string.length + 1 : string.length);
        if (crossedToTheRight) newString[newCursor] = Symbol.BLANK.getSymbol();
        newString[cursor] = symbol;
        return Tape.of(newString, newCursor);
    }

    public String getString() {
        return String.valueOf(string);
    }

    public int getSpace() { return string.length; }

    public static Tape of(final char[] string, final int cursor) {
        return new Tape(string, cursor);
    }

    @Override
    public String toString() {
        final String string = new String(this.string);
        return string.substring(0, cursor + 1) + "," +
               string.substring(cursor + 1) + ((cursor + 1 == string.length()) ? "_" : "");
    }
}
