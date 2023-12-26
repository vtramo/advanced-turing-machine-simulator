import com.github.vtramo.turingmachine.engine.Configuration;
import com.github.vtramo.turingmachine.engine.TerminalState;
import com.github.vtramo.turingmachine.engine.TuringMachine;
import com.github.vtramo.turingmachine.engine.TuringMachinePrograms;
import com.github.vtramo.turingmachine.parser.TuringMachineParserYaml;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.EmptySource;

import java.io.FileInputStream;
import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@DisplayName("A bunch of Turing machines")
public class TuringMachineTest {

    TuringMachine turingMachine;

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("A two-tape Turing machine deciding the language L = {x∈{0,1}* | x is palindrome}")
    class IsPalindromeTuringMachine {

        @BeforeAll
        void createIsPalindromeMdT() {
            turingMachine = TuringMachinePrograms.isPalindromeTwoTapes();
        }

        @ParameterizedTest
        @EmptySource
        @CsvFileSource(resources = "/palindrome-strings.csv", numLinesToSkip = 1)
        @DisplayName("Should accept palindromic strings")
        public void palindromeStrings(final String input) {
            final TuringMachine.Computation computation = turingMachine.startComputation(input);
            Configuration finalConfiguration = null;
            while (computation.hasNextConfiguration()) {
                finalConfiguration = computation.step();
            }

            assertThat(finalConfiguration, is(notNullValue()));
            assertThat(computation.getCurrentState(), is(equalTo(TerminalState.ACCEPTING_STATE.getSymbol())));
        }

        @ParameterizedTest
        @CsvFileSource(resources = "/non-palindrome-strings.csv", numLinesToSkip = 1)
        @DisplayName("Should reject non-palindromic strings")
        public void notPalindromeStrings(final String input) {
            final TuringMachine.Computation computation = turingMachine.startComputation(input);
            Configuration finalConfiguration = null;
            while (computation.hasNextConfiguration()) {
                finalConfiguration = computation.step();
            }

            assertThat(finalConfiguration, is(notNullValue()));
            assertThat(computation.getCurrentState(), is(equalTo(TerminalState.REJECTING_STATE.getSymbol())));
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("A Turing machine that performs the sum of two binary numbers")
    class SumOfTwoBinaryNumbersTuringMachine {

        @BeforeAll
        @SneakyThrows
        void createTuringMachine() {
            final TuringMachineParserYaml turingMachineParserYaml = new TuringMachineParserYaml();
            turingMachine = turingMachineParserYaml.parse(new FileInputStream(
                Path.of("src/test/resources/turing-machine-sum-three-strings.yaml")
                    .toFile()));
        }

        @ParameterizedTest
        @CsvFileSource(resources = "/sum-strings.csv", numLinesToSkip = 1)
        @DisplayName("Should return correct results")
        public void sum(final String input, String expectedOutput) {
            final TuringMachine.Computation computation = turingMachine.startComputation(input);
            Configuration finalConfiguration = null;
            while (computation.hasNextConfiguration()) {
                finalConfiguration = computation.step();
            }

            assertThat(finalConfiguration, is(notNullValue()));
            assertThat(computation.isHaltingState(), is(true));
            assertThat(computation.getOutput(), is(equalTo(expectedOutput)));
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("A Turing machine that performs the two's complement operation on binary numbers")
    class TwosComplement {

        @BeforeAll
        @SneakyThrows
        void createTuringMachine() {
            final TuringMachineParserYaml turingMachineParserYaml = new TuringMachineParserYaml();
            turingMachine = turingMachineParserYaml.parse(new FileInputStream(
                Path.of("src/test/resources/turing-machine-two-s-complement.yaml")
                    .toFile()));
        }

        @ParameterizedTest
        @CsvFileSource(resources = "/two-s-complement-strings.csv", numLinesToSkip = 1)
        @DisplayName("Should return correct results")
        public void testTwosComplement(final String input, String expectedOutput) {
            final TuringMachine.Computation computation = turingMachine.startComputation(input);
            Configuration finalConfiguration = null;
            while (computation.hasNextConfiguration()) {
                finalConfiguration = computation.step();
            }

            assertThat(finalConfiguration, is(notNullValue()));
            assertThat(computation.isHaltingState(), is(true));
            assertThat(computation.getOutput(), is(equalTo(expectedOutput)));
        }
    }
}