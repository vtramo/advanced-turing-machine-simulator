package com.github.vtramo.turingmachine.ui;

import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import com.github.vtramo.turingmachine.engine.TerminalState;

public class TuringMachineHaltedDialogBuilder {
    private TerminalState terminalState;
    private Stage owner;
    private Pane ownerNode;
    private String input;
    private String output;
    private int totalSteps;
    private int totalSpace;

    public TuringMachineHaltedDialogBuilder withTerminalState(TerminalState terminalState) {
        this.terminalState = terminalState;
        return this;
    }

    public TuringMachineHaltedDialogBuilder withOwner(Stage owner) {
        this.owner = owner;
        return this;
    }

    public TuringMachineHaltedDialogBuilder withOwnerNode(Pane ownerNode) {
        this.ownerNode = ownerNode;
        return this;
    }

    public TuringMachineHaltedDialogBuilder withInput(String input) {
        this.input = input;
        return this;
    }

    public TuringMachineHaltedDialogBuilder withOutput(String output) {
        this.output = output;
        return this;
    }

    public TuringMachineHaltedDialogBuilder withTotalSteps(int totalSteps) {
        this.totalSteps = totalSteps;
        return this;
    }

    public TuringMachineHaltedDialogBuilder withTotalSpace(int totalSpace) {
        this.totalSpace = totalSpace;
        return this;
    }

    public TuringMachineHaltedStateDialogView build() {
        if (output == null) {
            return new TuringMachineHaltedStateDialogView(terminalState, owner, ownerNode, input, totalSteps, totalSpace);
        } else {
            return new TuringMachineHaltedStateDialogView(terminalState, owner, ownerNode, input, output, totalSteps, totalSpace);
        }
    }
}
