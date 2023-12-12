package com.github.vtramo.turingmachine.parser;

import java.util.List;

public record ValidationResult(boolean containsErrors, List<ValidationMessage> validationMessages) {
    public static ValidationResult emptyCode(final int offset) {
        final String codeIsEmptyMessage = "Code is empty";
        return new ValidationResult(true,
            List.of(new ValidationMessage(0, offset, codeIsEmptyMessage, codeIsEmptyMessage)));
    }
}
