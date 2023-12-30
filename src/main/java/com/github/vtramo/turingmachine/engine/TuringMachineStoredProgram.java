package com.github.vtramo.turingmachine.engine;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.vtramo.turingmachine.TuringMachineApplication;
import com.github.vtramo.turingmachine.parser.TuringMachineParserYaml;

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
        TuringMachineStoredProgram.of(Path.of("/turing-machine-palindrome-two-strings.yaml")),
        TuringMachineStoredProgram.of(Path.of("/turing-machine-sum-three-strings.yaml")),
        TuringMachineStoredProgram.of(Path.of("/turing-machine-two-s-complement.yaml")),
        TuringMachineStoredProgram.of(Path.of("/turing-machine-copy-input-ten-strings.yaml"))
    );

    public static TuringMachineStoredProgram of(final Path turingMachineProgramPath) {
        final String turingMachineProgram = TuringMachineApplication.readResourceAsString(turingMachineProgramPath.toString());
        final TuringMachineParserYaml turingMachineParserYaml = new TuringMachineParserYaml();
        TuringMachine turingMachine;
        try {
            turingMachine = turingMachineParserYaml.parse(turingMachineProgram);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return new TuringMachineStoredProgram(turingMachine.getName(), turingMachine, turingMachineProgram, turingMachineProgramPath);
    }
}
