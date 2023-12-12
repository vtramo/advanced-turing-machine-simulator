package com.github.vtramo.turingmachine.engine;


import lombok.Getter;

import java.util.Objects;

@Getter
public enum Direction {
    STAY("-", 0), RIGHT("->", 1), LEFT("<-", -1);

    private final String symbol;
    private final int value;

    Direction(final String symbol, final int value) {
        this.symbol = symbol;
        this.value = value;
    }

    public Direction inverse() {
        if (Objects.equals(this, STAY)) {
            return STAY;
        } else if (Objects.equals(this, RIGHT)) {
            return LEFT;
        } else if (Objects.equals(this, LEFT)) {
            return RIGHT;
        } else {
            throw new IllegalArgumentException();
        }
    }

    public static Direction of(final String direction) {
        if (Objects.equals(direction, STAY.symbol)) {
            return STAY;
        } else if (Objects.equals(direction, RIGHT.symbol)) {
            return RIGHT;
        } else if (Objects.equals(direction, LEFT.symbol)) {
            return LEFT;
        } else {
            throw new IllegalArgumentException("Direction symbol not recognized!");
        }
    }
}