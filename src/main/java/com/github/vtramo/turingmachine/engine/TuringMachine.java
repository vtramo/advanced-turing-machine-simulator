package com.github.vtramo.turingmachine.engine;

import com.github.vtramo.turingmachine.engine.exception.NoNextConfigurationException;
import com.github.vtramo.turingmachine.engine.exception.NoPreviousConfigurationException;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class TuringMachine {

    @Getter
    private final String initialState;
    private final DeltaProgram program;

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private String description;

    public TuringMachine(final String initialState, final DeltaProgram program) {
        Objects.requireNonNull(initialState);
        Objects.requireNonNull(program);
        this.initialState = initialState;
        this.program = program;
    }

    public int getTotalTapes() {
        return program.getTotalTapes();
    }

    public Computation startComputation(final String input) {
        Objects.requireNonNull(input);
        return new Computation(input);
    }

    public class Computation {
        @Getter
        private Configuration currentConfiguration;
        @Getter
        private int steps;
        @Getter
        private String output;

        private final List<Configuration> configurations = new ArrayList<>();
        private final String input;

        private Computation(final String input) {
            this.input = input;
            buildInitialConfiguration();
        }
        private void buildInitialConfiguration() {
            final int totalTapes = program.getTotalTapes();
            currentConfiguration = Configuration.buildInitialConfiguration(initialState, input, totalTapes);
            configurations.add(currentConfiguration);
        }
        public Configuration step() {
            final Transition transition = getNextTransition();
            return step(transition);
        }
        private Configuration step(final Transition transition) {
            final Tape[] tapes = currentConfiguration.getTapes();
            final int totalTapes = getTotalTapes();
            assert totalTapes == tapes.length && totalTapes == transition.totalTapes();

            final List<Move> moves = transition.moves();
            final Tape[] newTapes = new Tape[totalTapes];
            for (int i = 0; i < totalTapes; i++) {
                final Move move = moves.get(i);
                final Tape tape = tapes[i];
                final Tape newTape = tape.move(move);
                newTapes[i] = newTape;
            }

            steps++;
            currentConfiguration = new Configuration(transition.state(), newTapes);
            if (isHaltingState()) setOutput();
            configurations.add(currentConfiguration);
            return new Configuration(transition.state(), newTapes);
        }
        public Transition getNextTransition() {
            final StateAndSymbols currentStateAndSymbols = getCurrentStateAndSymbols();
            if (TerminalState.isTerminalState(currentStateAndSymbols.state())) {
                throw new NoNextConfigurationException();
            }
            return program.apply(currentStateAndSymbols);
        }
        public Configuration stepBack() {
            if (!hasPreviousConfiguration()) {
                throw new NoPreviousConfigurationException();
            }
            steps--;
            configurations.removeLast();
            currentConfiguration = configurations.getLast();
            return currentConfiguration;
        }
        private void setOutput() {
            if (!isHaltingState()) throw new IllegalStateException();
            final Tape outputTape = currentConfiguration.outputTape();
            final String output = outputTape.getString();
            this.output = output.replaceAll("(_+)$", "").substring(1);
        }
        public StateAndSymbols getCurrentStateAndSymbols() {
            return currentConfiguration.getCurrentStateAndSymbols();
        }
        public boolean hasNextConfiguration() {
            final String currentState = getCurrentState();
            return !TerminalState.isTerminalState(currentState);
        }
        public boolean hasPreviousConfiguration() {
            return configurations.size() - 1 > 0;
        }
        public String getCurrentState() {
            return currentConfiguration.getState();
        }
        public boolean isHalted() {
            return isAcceptingState() || isRejectingStage() || isHaltingState();
        }
        public boolean isHaltingState() {
            return Objects.equals(TerminalState.HALTING_STATE.getSymbol(), getCurrentState());
        }
        public boolean isAcceptingState() {
            return Objects.equals(TerminalState.ACCEPTING_STATE.getSymbol(), getCurrentState());
        }
        public boolean isRejectingStage() {
            return Objects.equals(TerminalState.REJECTING_STATE.getSymbol(), getCurrentState());
        }
        public List<Configuration> getConfigurations() {
            return Collections.unmodifiableList(configurations);
        }
        public int getSpace() { return currentConfiguration.getSpace(); }
    }
}