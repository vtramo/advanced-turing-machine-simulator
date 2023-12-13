package com.github.vtramo.turingmachine;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.vtramo.turingmachine.engine.Configuration;
import com.github.vtramo.turingmachine.engine.TuringMachine;
import com.github.vtramo.turingmachine.engine.TerminalState;
import com.github.vtramo.turingmachine.engine.Transition;
import com.github.vtramo.turingmachine.parser.TuringMachineParserYaml;
import com.github.vtramo.turingmachine.ui.*;
import io.github.palexdev.materialfx.controls.MFXCircleToggleNode;
import io.github.palexdev.materialfx.controls.MFXScrollPane;
import io.github.palexdev.materialfx.controls.MFXSlider;
import javafx.beans.property.DoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.SneakyThrows;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.github.vtramo.turingmachine.ui.BlankSymbolView.blank;
import static com.github.vtramo.turingmachine.ui.StartSymbolView.startSymbol;
import static com.github.vtramo.turingmachine.ui.TuringMachineImporterYaml.*;

public class TuringMachineTabController {
    @FXML
    private Tab tab;

    @FXML
    private Label stateLabel;
    @FXML
    private Label stepsLabel;
    @FXML
    private Label spaceLabel;
    @FXML
    private AnchorPane masterAnchorPane;

    @FXML
    private VBox tapesVBox;
    @FXML
    private MFXScrollPane tapesScrollPane;

    private TapeViewController tapeViewController;

    @FXML
    private MFXCircleToggleNode playButton;
    @FXML
    private MFXCircleToggleNode pauseButton;
    @FXML
    private MFXCircleToggleNode resetButton;
    @FXML
    private MFXCircleToggleNode stepBackButton;
    @FXML
    private MFXCircleToggleNode stepButton;
    @FXML
    private Button loadInputButton;
    @FXML
    private TextField inputTextField;
    @FXML
    private MFXCircleToggleNode importButton;
    @FXML
    private MFXSlider speedSlider;
    @FXML
    private ConfigurationListView configurationsListView;

    @FXML
    private CodeAreaYaml codeArea;
    @FXML
    private TuringMachineCompilerView turingMachineCompilerView;
    private String yamlProgram;
    private String input = "";
    private TuringMachine turingMachine;
    private TuringMachine.Computation computation;
    private ObservableList<Configuration> observableConfigurations;
    private boolean isPlaying;
    private boolean stepInProgress;
    private long speedDelayMs = 500L;

    private final HomeController homeController;
    private final Stage primaryStage;

    public TuringMachineTabController(final HomeController homeController, final Stage primaryStage, final String yamlProgram) {
        this.homeController = homeController;
        this.primaryStage = primaryStage;
        this.yamlProgram = yamlProgram;
    }

    @SneakyThrows
    public void initialize() {
        configureButtonOnMouseClickedListeners();
        configureSpeedSlider();
        configureMdtCompiler();
        configureMdt();
    }

    private void configureButtonOnMouseClickedListeners() {
        playButton.setOnMouseClicked(__ -> {
            if (!isPlaying) {
                isPlaying = true;
                play();
            }
        });

        pauseButton.setOnMouseClicked(__ -> isPlaying = false);

        stepButton.setOnMouseClicked(__ -> {
            if (!stepInProgress) {
                stepInProgress = true;
                step()
                    .thenAccept(___ -> {
                        stepInProgress = false;
                        if (computation.isHalted()) {
                            stepButton.setDisable(true);
                            playButton.setDisable(true);
                            isPlaying = false;
                            showMachineHaltedDialog();
                        }
                    });
            }
        });

        stepBackButton.setDisable(true);
        stepBackButton.setOnMouseClicked(__ -> {
            if (!stepInProgress) {
                stepInProgress = true;
                stepBack()
                    .thenAccept(___ -> stepInProgress = false);
            }
        });

        resetButton.setOnMouseClicked(__ -> reset());

        loadInputButton.getStyleClass().removeFirst();
        loadInputButton.setOnMouseClicked(__ -> loadInputFromTextField());

        importButton.setOnMouseClicked(__ -> {
            importTuringMachine();
        });
    }

    private void configureSpeedSlider() {
        speedSlider.setValue(speedDelayMs);
        final DoubleProperty speedSliderDoubleProperty = speedSlider.valueProperty();
        speedSliderDoubleProperty.addListener((__, ___, newValue) -> speedDelayMs = newValue.longValue());
    }

    private void configureMdtCompiler() {
        turingMachineCompilerView.setCodeSupplier(() -> codeArea.getText());
        turingMachineCompilerView.addOnSuccessListener(mdt -> {
            this.turingMachine = mdt;
            reset();
        });
    }

    private void configureMdt() throws JsonProcessingException {
        final TuringMachineParserYaml turingMachineParserYaml = new TuringMachineParserYaml();
        turingMachine = turingMachineParserYaml.parse(yamlProgram);
        computation = turingMachine.startComputation(input);

        codeArea.appendText(yamlProgram);

        createTapes();

        observableConfigurations = FXCollections.observableArrayList(computation.getConfigurations());
        configurationsListView.setItems(observableConfigurations);

        setStepsTextLabel(0);
        setSpaceTextLabel(computation.getSpace());
        setStateTextLabel(turingMachine.getInitialState());
    }

    private void createTapes() {
        final int tapes = turingMachine.getTotalTapes();
        final TapeView[] tapeViews = new TapeView[tapes];
        tapeViews[0] = TapeView.buildInputTape(input, 1440, 1080);
        for (int i = 1; i < tapes; i++) {
            tapeViews[i] = TapeViewBuilder.builder()
                .withCell(new CellView(startSymbol()))
                .withCell(new CellView(blank()))
                .withSize(1440, 1080)
                .build();
        }
        tapesVBox.getChildren().addAll(tapeViews);
        tapesScrollPane.setFitToHeight(tapes <= 6);
        tapeViewController = new TapeViewController(tapeViews);
    }

    private void importTuringMachine() {
        final TuringMachineImporterYaml turingMachineImporterYaml = new TuringMachineImporterYaml(primaryStage, masterAnchorPane);
        final Optional<TuringMachineImportResult> optionalTuringMachineImportResult = turingMachineImporterYaml.importTuringMachineFromYamlDefinition();
        if (optionalTuringMachineImportResult.isEmpty()) return;

        final TuringMachineImportResult turingMachineImportResult = optionalTuringMachineImportResult.get();
        final TuringMachine importedTuringMachine = turingMachineImportResult.turingMachine();
        final String importedYamlProgram = turingMachineImportResult.yamlProgram();
        turingMachine = importedTuringMachine;
        yamlProgram = importedYamlProgram;

        if (turingMachineImportResult.openNewWindow()) {
            final String turingMachineName = turingMachine.getName();
            homeController.createTuringMachineTab(turingMachineName, yamlProgram);
        } else {
            codeArea.setText(yamlProgram);
            reset();
        }

    }

    private void play() {
        if (!isPlaying || !computation.hasNextConfiguration()) return;
        step()
            .thenAccept(__ -> {
                if (computation.isHalted()) {
                    stepButton.setDisable(true);
                    playButton.setDisable(true);
                    isPlaying = false;
                    showMachineHaltedDialog();
                    return;
                }
                play();
            });
    }

    @SneakyThrows
    private void showMachineHaltedDialog() {
        if (!computation.isHalted()) throw new RuntimeException();

        final int steps = computation.getSteps();
        final int space = computation.getSpace();

        final TerminalState terminalState = determineTerminalState();
        final TuringMachineHaltedDialogBuilder turingMachineHaltedDialogBuilder =
            TuringMachineHaltedStateDialogView.builder()
                .withInput(input)
                .withTerminalState(terminalState)
                .withOwner(primaryStage)
                .withOwnerNode(masterAnchorPane)
                .withTotalSteps(steps)
                .withTotalSpace(space);

        if (Objects.equals(terminalState, TerminalState.HALTING_STATE)) {
            turingMachineHaltedDialogBuilder.withOutput(computation.getOutput());
        }

        final TuringMachineHaltedStateDialogView machineHaltedDialog = turingMachineHaltedDialogBuilder.build();
        machineHaltedDialog.show();
    }

    private TerminalState determineTerminalState() {
        if (!computation.isHalted()) throw new RuntimeException();

        if (computation.isAcceptingState()) {
            return TerminalState.ACCEPTING_STATE;
        }

        if (computation.isRejectingStage()) {
            return TerminalState.REJECTING_STATE;
        }

        return TerminalState.HALTING_STATE;
    }

    private CompletableFuture<Void> step() {
        final Transition transition = computation.getNextTransition();
        stepBackButton.setDisable(false);
        computation.step();
        final List<Configuration> configurations = computation.getConfigurations();
        observableConfigurations.addFirst(configurations.getLast());
        setStepsTextLabel(computation.getSteps());
        setSpaceTextLabel(computation.getSpace());
        setStateTextLabel(computation.getCurrentState());
        if (!computation.hasNextConfiguration()) stepButton.setDisable(true);
        return tapeViewController.step(transition, speedDelayMs);
    }

    private CompletableFuture<Void> stepBack() {
        final Configuration previousConfiguration = computation.stepBack();
        observableConfigurations.removeFirst();
        stepButton.setDisable(false);
        playButton.setDisable(false);
        setStepsTextLabel(computation.getSteps());
        setSpaceTextLabel(computation.getSpace());
        setStateTextLabel(computation.getCurrentState());
        final Transition lastTransition = computation.getNextTransition();
        if (!computation.hasPreviousConfiguration()) stepBackButton.setDisable(true);
        return tapeViewController.stepBack(previousConfiguration, lastTransition, speedDelayMs);
    }

    private void setStateTextLabel(final String state) {
        stateLabel.setText(STR."State: \{state}");
    }

    private void setStepsTextLabel(final int steps) {
        stepsLabel.setText(STR."Steps: \{steps}");
    }

    private void setSpaceTextLabel(final int space) {
        spaceLabel.setText(STR."Space: \{space}");
    }

    private void reset() {
        isPlaying = false;
        stepInProgress = false;
        stepBackButton.setDisable(true);
        stepButton.setDisable(false);
        playButton.setDisable(false);
        tapesVBox.getChildren().clear();
        createTapes();
        computation = turingMachine.startComputation(input);
        observableConfigurations = FXCollections.observableArrayList(computation.getConfigurations());
        configurationsListView.restoreToOriginalWidth();
        configurationsListView.setItems(observableConfigurations);
        setStateTextLabel(turingMachine.getInitialState());
        setStepsTextLabel(0);
        setSpaceTextLabel(computation.getSpace());
    }

    private void loadInputFromTextField() {
        input = inputTextField.getText();
        reset();
    }
}
