package com.github.vtramo.turingmachine.ui;

import com.github.vtramo.turingmachine.TuringMachineApplication;
import com.github.vtramo.turingmachine.engine.TerminalState;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXScrollPane;
import io.github.palexdev.materialfx.dialogs.MFXGenericDialog;
import io.github.palexdev.materialfx.dialogs.MFXGenericDialogBuilder;
import io.github.palexdev.materialfx.dialogs.MFXStageDialog;
import io.github.palexdev.materialfx.enums.ScrimPriority;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import static com.github.vtramo.turingmachine.engine.TerminalState.ACCEPTING_STATE;
import static com.github.vtramo.turingmachine.engine.TerminalState.REJECTING_STATE;
import static com.github.vtramo.turingmachine.ui.TapeView.CELL_WIDTH;

public class TuringMachineHaltedStateDialogView {
    private static final String MDT_DECIDING_STATE_FXML = "turing-machine-deciding-state-dialog.fxml";
    private static final String MDT_HALTING_STATE_DIALOG_FXML = "turing-machine-halting-state-dialog.fxml";
    private static final double DIALOG_WIDTH = 960;
    private static final double TAPE_SCROLL_PANE_HEIGHT = 80;
    private static final Map<TerminalState, Text> textByTerminalState = Map.of(
        ACCEPTING_STATE, new Text("ACCEPTED") {{ setFill(Color.GREEN); setFont(Font.font("Source Code Pro Medium", 24)); }},
        REJECTING_STATE, new Text("REJECTED") {{ setFill(Color.RED); setFont(Font.font("Source Code Pro Medium", 24)); }}
    );

    @FXML
    private TextFlow terminalStateTextFlow;
    @FXML
    private MFXScrollPane inputScrollPane;
    @FXML
    private AnchorPane inputTapeAnchorPaneContent;
    @FXML
    private AnchorPane outputTapeAnchorPaneContent;
    @FXML
    private Label stepsLabel;
    @FXML
    private Label spaceLabel;
    @FXML
    private VBox dialogContent;

    private final TerminalState terminalState;
    private final String input;
    private String output;
    private final Stage owner;
    private final Pane ownerNode;
    private MFXStageDialog dialog;

    public TuringMachineHaltedStateDialogView(
        final TerminalState terminalState,
        final Stage owner,
        final Pane ownerNode,
        final String input,
        final String output,
        final int steps,
        final int space
    ) {
        this(terminalState, owner, ownerNode, input, steps, space);
        this.output = output;
        addTapeIn(outputTapeAnchorPaneContent, output);
    }

    public TuringMachineHaltedStateDialogView(
        final TerminalState terminalState,
        final Stage owner,
        final Pane ownerNode,
        final String input,
        final int steps,
        final int space
    ) {
        this.terminalState = terminalState;
        this.input = input;
        this.owner = owner;
        this.ownerNode = ownerNode;
        createDialogStage();
        addTapeIn(inputTapeAnchorPaneContent, input);
        if (!Objects.equals(terminalState, TerminalState.HALTING_STATE)) {
            setPrimaryTextForDecidingLanguage();
        }
        setStepsLabelValue(steps);
        setSpaceLabelValue(space);
    }

    private void createDialogStage() {
        final String dialogFxmlPath = Objects.equals(terminalState, TerminalState.HALTING_STATE)
            ? MDT_HALTING_STATE_DIALOG_FXML
            : MDT_DECIDING_STATE_FXML;
        final FXMLLoader fxmlLoader = new FXMLLoader(TuringMachineApplication.class.getResource(dialogFxmlPath));
        fxmlLoader.setController(this);

        final VBox terminalStateDialogPane;
        try {
            terminalStateDialogPane = fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        final MFXGenericDialog dialogContent = new MFXGenericDialog();
        dialogContent.setShowMinimize(false);
        dialogContent.setShowAlwaysOnTop(false);
        dialogContent.setContent(terminalStateDialogPane);

        dialog = MFXGenericDialogBuilder.build(dialogContent)
            .toStageDialogBuilder()
            .initOwner(owner)
            .initModality(Modality.APPLICATION_MODAL)
            .setDraggable(true)
            .setOwnerNode(ownerNode)
            .setCenterInOwnerNode(true)
            .setScrimPriority(ScrimPriority.WINDOW)
            .setScrimOwner(true)
            .get();
        dialog.setWidth(DIALOG_WIDTH);

        dialogContent.addActions(Map.entry(new MFXButton("OK"), __ -> dialog.close()));
    }

    private void addTapeIn(final AnchorPane tapeAnchorPaneContent, final String string) {
        final double tapeWidth = ((string.length() + 2) * CELL_WIDTH);

        TapeView tape;
        if (tapeWidth < DIALOG_WIDTH) {
            final double fromX = (DIALOG_WIDTH - tapeWidth) / 2;
            tape = TapeView.buildStaticTape(string, fromX);
        } else {
            tape = TapeView.buildStaticTape(string);
            tapeAnchorPaneContent.setPrefHeight(TAPE_SCROLL_PANE_HEIGHT + 15);
        }

        final CursorView cursorView = tape.getCursorView();
        tape.getChildren().remove(cursorView);
        tapeAnchorPaneContent.getChildren().add(tape);
    }


    private void setPrimaryTextForDecidingLanguage() {
        final Text terminalStateText = textByTerminalState.get(terminalState);
        final ObservableList<Node> textFlowChildren = terminalStateTextFlow.getChildren();
        final Node lastNode = textFlowChildren.removeLast();
        textFlowChildren.add(terminalStateText);
        textFlowChildren.add(lastNode);
    }

    private void setStepsLabelValue(int steps) {
        stepsLabel.setText(stepsLabel.getText() + steps);
    }

    private void setSpaceLabelValue(int space) {
        spaceLabel.setText(spaceLabel.getText() + space);
    }

    public void show() {
        dialog.showDialog();
        dialog.centerOnScreen();
    }

    public static TuringMachineHaltedDialogBuilder builder() {
        return new TuringMachineHaltedDialogBuilder();
    }
}