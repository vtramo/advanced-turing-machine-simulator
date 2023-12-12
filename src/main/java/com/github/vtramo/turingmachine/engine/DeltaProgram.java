package com.github.vtramo.turingmachine.engine;

import com.github.vtramo.turingmachine.engine.exception.DuplicateTransitionException;
import com.github.vtramo.turingmachine.engine.exception.MalformedInstructionException;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DeltaProgram {

    @Getter
    private final int totalTapes;
    private final Map<StateAndSymbols, Transition> program = new HashMap<>();

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
        return transition == null ? Transition.rejectingTransition(stateAndSymbols) : transition;
    }
}
