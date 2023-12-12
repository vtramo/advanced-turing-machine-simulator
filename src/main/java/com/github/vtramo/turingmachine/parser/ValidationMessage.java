package com.github.vtramo.turingmachine.parser;

public record ValidationMessage(int line, int offset, String succinctMessage, String detailMessage) {}
