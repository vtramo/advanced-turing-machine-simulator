package com.github.vtramo.turingmachine.engine;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.vtramo.turingmachine.parser.TuringMachineParserYaml;
import com.github.vtramo.turingmachine.parser.TuringMachineYamlUtils;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

public record TuringMachineStoredProgram(
    String name,
    TuringMachine turingMachine,
    String turingMachineCode,
    Path turingMachineCodePath
) {
    public static Collection<TuringMachineStoredProgram> turingMachineStoredPrograms = List.of(
        TuringMachineStoredProgram.of(Path.of("src/main/resources/turing-machine-palindrome-two-strings.yaml")),
        TuringMachineStoredProgram.of(Path.of("src/main/resources/turing-machine-sum-three-strings.yaml")),
        TuringMachineStoredProgram.of(Path.of("src/main/resources/turing-machine-two-s-complement.yaml"))
    );

    public static TuringMachineStoredProgram of(final Path turingMachineCodePath) {
        final String turingMachineCode = TuringMachineYamlUtils.readTuringMachineYamlDefinition(turingMachineCodePath);
        final TuringMachineParserYaml turingMachineParserYaml = new TuringMachineParserYaml();

        TuringMachine turingMachine;
        try {
            turingMachine = turingMachineParserYaml.parse(turingMachineCode);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return new TuringMachineStoredProgram(turingMachine.getName(), turingMachine, turingMachineCode, turingMachineCodePath);
    }
}
