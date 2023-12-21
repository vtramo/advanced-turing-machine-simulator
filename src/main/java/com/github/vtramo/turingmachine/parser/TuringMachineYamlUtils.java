package com.github.vtramo.turingmachine.parser;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;

public class TuringMachineYamlUtils {
    public static String readTuringMachineYamlDefinition(final Path turingMachineYamlProgram) {
        try (final FileInputStream fileInputStream = new FileInputStream(turingMachineYamlProgram.toFile())) {
            return new String(fileInputStream.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
