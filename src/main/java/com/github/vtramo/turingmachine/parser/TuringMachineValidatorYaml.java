package com.github.vtramo.turingmachine.parser;

import com.github.vtramo.turingmachine.TuringMachineApplication;
import jakarta.json.stream.JsonParser;
import jakarta.json.stream.JsonParsingException;
import lombok.Getter;
import org.leadpony.justify.api.JsonValidationService;
import org.leadpony.justify.api.Problem;
import org.snakeyaml.engine.v2.exceptions.MarkedYamlEngineException;

import java.io.StringReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class TuringMachineValidatorYaml {
    private static final Path JSON_SCHEMA_PATH = Path.of("/turing-machine-schema.json");
    private final JsonValidationService service;
    private final org.leadpony.justify.api.JsonSchema schema;

    public TuringMachineValidatorYaml() {
        service = JsonValidationService.newInstance();
        schema = service.readSchema(TuringMachineApplication.getResourceAsInputStream(JSON_SCHEMA_PATH.toString()));
    }

    public ValidationResult validate(final String mdtYamlDefinition) {
        if (mdtYamlDefinition.isBlank()) {
            return ValidationResult.emptyCode(mdtYamlDefinition.length());
        }

        final ProblemHandler problemHandler = new ProblemHandler();
        final List<ValidationMessage> syntaxValidationMessages = new ArrayList<>();

        try (final JsonParser jsonParser = service.createParser(new StringReader(mdtYamlDefinition), schema, problemHandler)) {
            while (jsonParser.hasNext()) {
                jsonParser.next();
            }
        } catch (final JsonParsingException parsingException) {
            final List<ValidationMessage> validationMessages = handleParsingException(parsingException);
            syntaxValidationMessages.addAll(validationMessages);
        }

        final List<ValidationMessage> schemaValidationMessages = problemHandler.getValidationMessages();
        schemaValidationMessages.addAll(syntaxValidationMessages);

        final boolean containsError = !schemaValidationMessages.isEmpty();
        return new ValidationResult(containsError, schemaValidationMessages);
    }

    private static List<ValidationMessage> handleParsingException(final JsonParsingException parsingException) {
        final List<ValidationMessage> syntaxValidationMessages = new ArrayList<>();
        final MarkedYamlEngineException scannerException = (MarkedYamlEngineException) parsingException.getCause();
        scannerException.getContextMark()
            .ifPresent(mark -> {
                final int line = mark.getLine();
                final int offset = mark.getColumn();
                final String succinctMessage = scannerException.getProblem();
                final String detailMessage = scannerException.getMessage();
                final ValidationMessage validationMessage = new ValidationMessage(line, offset, succinctMessage, detailMessage);
                syntaxValidationMessages.add(validationMessage);
            });
        return syntaxValidationMessages;
    }

    @Getter
    private static class ProblemHandler implements org.leadpony.justify.api.ProblemHandler {
        final List<ValidationMessage> validationMessages = new ArrayList<>();

        @Override
        public void handleProblems(final List<Problem> problems) {
            problems.forEach(problem -> {
                final String detailMessage = problem.getContextualMessage();
                final String succinctMessage = problem.getMessage();
                final int lastSquareBracketIndex = detailMessage.indexOf(']', 1);
                final String rowOffsetString = detailMessage.substring(1, lastSquareBracketIndex);
                final String[] rowAndOffset = rowOffsetString.split(",");
                final int line = Integer.parseInt(rowAndOffset[0].trim()) - 1;
                final int offset = Integer.parseInt(rowAndOffset[1].trim());
                final ValidationMessage validationMessage = new ValidationMessage(line, offset, succinctMessage, detailMessage);
                validationMessages.add(validationMessage);
            });
        }
    }

}
