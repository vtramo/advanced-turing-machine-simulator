package com.github.vtramo.turingmachine.engine;

import com.github.vtramo.turingmachine.engine.exception.DuplicateTransitionException;
import com.github.vtramo.turingmachine.engine.exception.MalformedInstructionException;
import lombok.Getter;

import java.util.*;

import static com.github.vtramo.turingmachine.engine.Transition.rejectingTransition;
import static java.util.stream.Collectors.toSet;

public class DeltaProgram {

    @Getter
    private final int totalTapes;
    private final Map<StateAndSymbols, Transition> program = new HashMap<>();
    private final Set<StateAndSymbols> stateAndSymbolsWithAsterisks = new HashSet<>();

    public DeltaProgram(final int totalTapes) {
        if (totalTapes <= 0) {
            throw new IllegalArgumentException("The number of tapes must be greater than or equal to one!");
        }

        this.totalTapes = totalTapes;
    }

    public int addInstruction(final Instruction instruction) {
        return addInstruction(instruction.stateAndSymbols(), instruction.transition());
    }

    public int addInstruction(final StateAndSymbols stateAndSymbols, final Transition transition) {
        checkInstructionValidity(stateAndSymbols, transition);
        if (stateAndSymbols.containsAsterisks()) {
            stateAndSymbolsWithAsterisks.add(stateAndSymbols);
        }
        program.put(stateAndSymbols, transition);
        return program.size();
    }

    private void checkInstructionValidity(final StateAndSymbols stateAndSymbols, final Transition transition) {
        if (!Objects.equals(transition.totalTapes(), totalTapes)) {
            throw new MalformedInstructionException("The symbols must be as many as there are tapes!");
        }

        if (!Objects.equals(stateAndSymbols.totalSymbols(), totalTapes)) {
            throw new MalformedInstructionException("The transition must specify exactly one move for each tape!");
        }

        if (program.containsKey(stateAndSymbols)) {
            throw new DuplicateTransitionException();
        }
    }

    public Transition apply(final StateAndSymbols stateAndSymbols) {
        final Transition transition = program.get(stateAndSymbols);
        return transition == null ? findAsteriskTransition(stateAndSymbols) : transition;
    }

    private Transition findAsteriskTransition(final StateAndSymbols stateAndSymbols) {
        if (stateAndSymbolsWithAsterisks.isEmpty()) return rejectingTransition(stateAndSymbols);

        final Set<StateAndSymbols> survivingStateAndSymbolsWithAsterisks = stateAndSymbolsWithAsterisks.stream()
            .filter(stateAndSymbolsWithAsterisk -> Objects.equals(stateAndSymbolsWithAsterisk.state(), stateAndSymbols.state()))
            .collect(toSet());

        char[] symbols = stateAndSymbols.symbols();
        final Iterator<StateAndSymbols> survivingStateAndSymbolsWithAsterisksIterator = survivingStateAndSymbolsWithAsterisks.iterator();
        while (survivingStateAndSymbolsWithAsterisksIterator.hasNext()) {
            final StateAndSymbols stateAndSymbolsWithAsterisk = survivingStateAndSymbolsWithAsterisksIterator.next();
            final char[] symbolsWithAsterisks = stateAndSymbolsWithAsterisk.symbols();
            boolean survivor = true;
            for (int i = 0; i < symbols.length; i++) {
                if (symbols[i] != symbolsWithAsterisks[i] && symbolsWithAsterisks[i] != '*') {
                    survivor = false;
                    break;
                }
            }

            if (!survivor) {
                survivingStateAndSymbolsWithAsterisksIterator.remove();
            }
        }

        StateAndSymbols winningStateAndSymbols = null;
        if (survivingStateAndSymbolsWithAsterisks.isEmpty()) {
            return rejectingTransition(stateAndSymbols);
        } else if (survivingStateAndSymbolsWithAsterisks.size() == 1) {
            winningStateAndSymbols = survivingStateAndSymbolsWithAsterisks.iterator().next();
        } else {
            boolean breakFor = false;
            for (int i = 0; i < totalTapes; i++) {
                for (final StateAndSymbols survivingStateAndSymbolsWithAsterisk: survivingStateAndSymbolsWithAsterisks) {
                    symbols = survivingStateAndSymbolsWithAsterisk.symbols();
                    if (symbols[i] != '*') {
                        breakFor = true;
                        winningStateAndSymbols = survivingStateAndSymbolsWithAsterisk;
                        break;
                    }
                }
                if (breakFor) break;
            }
        }

        Transition transition = program.get(winningStateAndSymbols);
        if (transition.containsAsterisks()) {
            transition = transition.replaceAsterisksWith(stateAndSymbols.symbols());
        }
        return transition;
    }
}
