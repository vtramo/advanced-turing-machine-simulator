import com.github.vtramo.turingmachine.engine.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.EmptySource;

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

        @Test
        void sum() {
            final String input = "0001;0001";
            final String initialState = "s";
            final int tapes = 3;

            final DeltaProgram deltaProgram = new DeltaProgram(tapes);
            deltaProgram.addInstruction(Instruction.of("s, >, >, >", "s, >, ->, >, ->, >, ->"));
            deltaProgram.addInstruction(Instruction.of("s, 0, _, _", "s, 0, ->, _, -, 0, ->"));
            deltaProgram.addInstruction(Instruction.of("s, 1, _, _", "s, 1, ->, _, -, 0, ->"));
            deltaProgram.addInstruction(Instruction.of("s, ;, _, _", "c, ;, ->, _, -, 0, ->"));
            deltaProgram.addInstruction(Instruction.of("c, 0, _, _", "c, 0, ->, 0, ->, 0, ->"));
            deltaProgram.addInstruction(Instruction.of("c, 1, _, _", "c, 1, ->, 1, ->, 0, ->"));
            deltaProgram.addInstruction(Instruction.of("c, _, _, _", "b, _, <-, _, -, _, -"));
            deltaProgram.addInstruction(Instruction.of("b, 0, _, _", "b, 0, <-, _, -, _, -"));
            deltaProgram.addInstruction(Instruction.of("b, 1, _, _", "b, 1, <-, _, -, _, -"));
            deltaProgram.addInstruction(Instruction.of("b, ;, _, _", "q0, ;, <-, _, <-, _, <-"));
            deltaProgram.addInstruction(Instruction.of("q0, 0, 0, 0", "q0, 0, <-, 0, <-, 0, <-"));
            deltaProgram.addInstruction(Instruction.of("q0, 0, 1, 0", "q0, 0, <-, 1, <-, 1, <-"));
            deltaProgram.addInstruction(Instruction.of("q0, 1, 0, 0", "q0, 1, <-, 0, <-, 1, <-"));
            deltaProgram.addInstruction(Instruction.of("q0, 1, 1, 0", "q1, 1, <-, 1, <-, 0, <-"));
            deltaProgram.addInstruction(Instruction.of("q1, 0, 0, 0", "q0, 0, <-, 0, <-, 1, <-"));
            deltaProgram.addInstruction(Instruction.of("q1, 0, 1, 0", "q1, 0, <-, 1, <-, 0, <-"));
            deltaProgram.addInstruction(Instruction.of("q1, 1, 0, 0", "q1, 1, <-, 0, <-, 0, <-"));
            deltaProgram.addInstruction(Instruction.of("q1, 1, 1, 0", "q1, 1, <-, 1, <-, 1, <-"));
            deltaProgram.addInstruction(Instruction.of("q0, >, >, 0", "h, >, ->, >, ->, 0, -"));
            deltaProgram.addInstruction(Instruction.of("q0, >, 0, 0", "c2, >, ->, 0, <-, 0, <-"));
            deltaProgram.addInstruction(Instruction.of("q0, >, 1, 0", "c2, >, ->, 1, <-, 1, <-"));
            deltaProgram.addInstruction(Instruction.of("q0, 0, >, 0", "c1, 0, <-, >, ->, 0, <-"));
            deltaProgram.addInstruction(Instruction.of("q0, 1, >, 0", "c1, 1, <-, >, ->, 1, <-"));
            deltaProgram.addInstruction(Instruction.of("c2, 0, 0, 0", "c2, 0, -, 0, <-, 0, <-"));
            deltaProgram.addInstruction(Instruction.of("c2, 1, 0, 0", "c2, 0, -, 0, <-, 0, <-"));
            deltaProgram.addInstruction(Instruction.of("c2, >, 0, 0", "c2, 0, -, 0, <-, 0, <-"));
            deltaProgram.addInstruction(Instruction.of("c2, _, 0, 0", "c2, 0, -, 0, <-, 0, <-"));
            deltaProgram.addInstruction(Instruction.of("c2, ;, 0, 0", "c2, 0, -, 0, <-, 0, <-"));
            deltaProgram.addInstruction(Instruction.of("c2, 0, 1, 0", "c2, 0, -, 1, <-, 1, <-"));
            deltaProgram.addInstruction(Instruction.of("c2, 1, 1, 0", "c2, 0, -, 1, <-, 1, <-"));
            deltaProgram.addInstruction(Instruction.of("c2, >, 1, 0", "c2, 0, -, 1, <-, 1, <-"));
            deltaProgram.addInstruction(Instruction.of("c2, _, 1, 0", "c2, 0, -, 1, <-, 1, <-"));
            deltaProgram.addInstruction(Instruction.of("c2, ;, 1, 0", "c2, 0, -, 1, <-, 1, <-"));
            deltaProgram.addInstruction(Instruction.of("c2, 0, >, 0", "h, 0, -, >, ->, 0, <-"));
            deltaProgram.addInstruction(Instruction.of("c2, 1, >, 0", "h, 1, -, >, ->, 0, <-"));
            deltaProgram.addInstruction(Instruction.of("c2, >, >, 0", "h, >, -, >, ->, 0, <-"));
            deltaProgram.addInstruction(Instruction.of("c2, _, >, 0", "h, _, -, >, ->, 0, <-"));
            deltaProgram.addInstruction(Instruction.of("c1, 0, 0, 0", "c1, 0, <-, 0, -, 0, <-"));
            deltaProgram.addInstruction(Instruction.of("c1, 0, 1, 0", "c1, 0, <-, 1, -, 0, <-"));
            deltaProgram.addInstruction(Instruction.of("c1, 1, 0, 0", "c1, 1, <-, 0, -, 1, <-"));
            deltaProgram.addInstruction(Instruction.of("c1, 1, 1, 0", "c1, 1, <-, 1, -, 1, <-"));
            deltaProgram.addInstruction(Instruction.of("c1, >, 0, 0", "h, >, ->, 0, -, 0, <-"));
            deltaProgram.addInstruction(Instruction.of("c1, >, 1, 0", "h, >, ->, 1, -, 0, <-"));


            final TuringMachine turingMachine = new TuringMachine(initialState, deltaProgram);
            final TuringMachine.Computation computation = turingMachine.startComputation(input);
            Configuration finalConfiguration = null;
            while (computation.hasNextConfiguration()) {
                finalConfiguration = computation.step();
            }

            assertThat(finalConfiguration, is(notNullValue()));
            assertThat(computation.getOutput(), is(equalTo("000000010")));
        }
    }
}