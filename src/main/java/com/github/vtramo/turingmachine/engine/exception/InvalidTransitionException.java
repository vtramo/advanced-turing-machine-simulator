package com.github.vtramo.turingmachine.engine.exception;

public class InvalidTransitionException extends RuntimeException {
    public InvalidTransitionException(final String msg) {
        super(msg);
    }
}
