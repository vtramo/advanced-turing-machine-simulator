package com.github.vtramo.turingmachine.engine.exception;

public class MalformedInstructionException extends RuntimeException {
    public MalformedInstructionException(final String msg) {
        super(msg);
    }
}
