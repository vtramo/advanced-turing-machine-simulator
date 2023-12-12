package com.github.vtramo.turingmachine.parser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.vtramo.turingmachine.engine.DeltaProgram;
import com.github.vtramo.turingmachine.engine.Instruction;
import com.github.vtramo.turingmachine.engine.TuringMachine;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;

import static com.github.vtramo.turingmachine.parser.TuringMachineParserYaml.FieldNames.*;

public class TuringMachineParserYaml {
    abstract static class FieldNames {
        final static String NAME = "name";
        final static String DESCRIPTION = "description";
        final static String TAPES = "tapes";
        final static String INITIAL_STATE = "initial_state";
        final static String TRANSITIONS = "transitions";
        final static String NEXT_STATE = "next_state";
        final static String WRITE_SYMBOLS = "write";
        final static String MOVES = "move";
    }

    private String name;
    private String description;
    private String initialState;
    private DeltaProgram deltaProgram;

    private final ObjectMapper mapper = new ObjectMapper(new YAMLFactory()) {{ this.findAndRegisterModules(); }};

    public TuringMachine parse(final String content) throws JsonProcessingException {
        final JsonNode root = mapper.readTree(content);
        return parse(root);
    }

    public TuringMachine parse(final FileInputStream inputStream) throws IOException {
        final JsonNode root = mapper.readTree(inputStream);
        return parse(root);
    }

    private TuringMachine parse(final JsonNode root) {
        initializeDeltaProgram(root);
        extractMetadata(root);
        extractInitialState(root);
        buildDeltaProgram(root);
        return buildTuringMachine();
    }

    private TuringMachine buildTuringMachine() {
        final TuringMachine turingMachine = new TuringMachine(initialState, deltaProgram);
        turingMachine.setName(name);
        turingMachine.setDescription(description);
        return turingMachine;
    }

    private void initializeDeltaProgram(final JsonNode root) {
        final int numberOfTapes = root.get(TAPES).asInt();
        deltaProgram = new DeltaProgram(numberOfTapes);
    }

    private void extractMetadata(final JsonNode root) {
        name = root.get(NAME).asText();
        description = root.get(DESCRIPTION).asText();
    }

    private void extractInitialState(final JsonNode root) {
        initialState = root.get(INITIAL_STATE).asText();
    }

    private void buildDeltaProgram(final JsonNode root) {
        final JsonNode allTransitions = root.get(TRANSITIONS);
        final Iterator<String> states = allTransitions.fieldNames();
        states.forEachRemaining(state -> {
            final JsonNode stateTransition = allTransitions.get(state);
            final Iterator<String> allCurrentSymbols = stateTransition.fieldNames();
            allCurrentSymbols.forEachRemaining(currentSymbols -> {
                final JsonNode transitionInfo = stateTransition.get(currentSymbols);
                final String nextState = transitionInfo.get(NEXT_STATE).asText();
                final String writeSymbols = transitionInfo.get(WRITE_SYMBOLS).asText();
                final String moves = transitionInfo.get(MOVES).asText();
                final Instruction instruction = buildInstruction(state, currentSymbols, nextState, writeSymbols, moves);
                deltaProgram.addInstruction(instruction);
            });
        });
    }

    private static Instruction buildInstruction(
        final String currentState,
        final String currentSymbols,
        final String nextState,
        final String writeSymbols,
        final String moves
    ) {
        final String left = currentState + "," + currentSymbols;
        final String[] splittedWriteSymbols = writeSymbols.split(",");
        final String[] splittedMoves = moves.split(",");
        final StringBuilder rightBuilder = new StringBuilder(nextState + ",");
        for (int i = 0; i < splittedMoves.length; i++) {
            rightBuilder.append(splittedWriteSymbols[i]).append(",");
            rightBuilder.append(splittedMoves[i]).append(",");
        }
        rightBuilder.deleteCharAt(rightBuilder.lastIndexOf(","));
        final String right = rightBuilder.toString();
        return Instruction.of(left, right);
    }
}
