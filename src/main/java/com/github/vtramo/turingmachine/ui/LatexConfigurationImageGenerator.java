package com.github.vtramo.turingmachine.ui;

import com.github.vtramo.turingmachine.engine.Configuration;
import com.github.vtramo.turingmachine.engine.Symbol;
import javafx.scene.image.Image;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.Objects;

public class LatexConfigurationImageGenerator {

    private static final Map<Symbol, String> specialSymbolLatexFormula = Map.of(
        Symbol.START, "\\vartriangleright",
        Symbol.BLANK, "\\sqcup"
    );

    private static final String DESTINATION_PATH = "src/main/resources/formula.png";

    private final LatexFormulaPngGenerator latexFormulaPNGGenerator = new LatexFormulaPngGenerator();

    public Image generateLatexImage(final Configuration configuration) {
        final String latexFormula = buildLatexFormula(configuration);
        latexFormulaPNGGenerator.generate(latexFormula, 18, DESTINATION_PATH);
        try {
            return new Image(new FileInputStream(DESTINATION_PATH));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private String buildLatexFormula(final Configuration configuration) {
        final StringBuilder latexFormula = new StringBuilder();
        final String configurationString = configuration.toString();

        int numberOfCommas = 0;
        for (int i = 0; i < configurationString.length() - 1; i++) {
            final char symbol = configurationString.charAt(i);

            if (symbol == ',') {
                numberOfCommas++;
            }

            final boolean isCurrentSymbol = (numberOfCommas % 2 == 1 && configurationString.charAt(i + 1) == ',');
            if (isCurrentSymbol) {
                latexFormula.append("\\underline{");
            }

            if (Objects.equals(Symbol.START.getSymbol(), symbol)) {
                latexFormula.append(specialSymbolLatexFormula.get(Symbol.START));
            } else if (Objects.equals(Symbol.BLANK.getSymbol(), symbol)) {
                latexFormula.append(specialSymbolLatexFormula.get(Symbol.BLANK));
            } else {
                latexFormula.append(symbol);
            }

            if (isCurrentSymbol) {
                latexFormula.append("}");
            }
        }
        latexFormula.append(")");
        return latexFormula.toString();
    }
}
