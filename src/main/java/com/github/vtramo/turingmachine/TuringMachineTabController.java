package com.github.vtramo.turingmachine;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.vtramo.turingmachine.engine.Configuration;
import com.github.vtramo.turingmachine.engine.TuringMachine;
import com.github.vtramo.turingmachine.engine.TerminalState;
import com.github.vtramo.turingmachine.engine.Transition;
import com.github.vtramo.turingmachine.parser.TuringMachineParserYaml;
import com.github.vtramo.turingmachine.parser.TuringMachineValidatorYaml;
import com.github.vtramo.turingmachine.parser.ValidationResult;
import com.github.vtramo.turingmachine.ui.*;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXCircleToggleNode;
import io.github.palexdev.materialfx.controls.MFXScrollPane;
import io.github.palexdev.materialfx.controls.MFXSlider;
import io.github.palexdev.materialfx.dialogs.MFXGenericDialog;
import io.github.palexdev.materialfx.dialogs.MFXGenericDialogBuilder;
import io.github.palexdev.materialfx.dialogs.MFXStageDialog;
import io.github.palexdev.materialfx.enums.ScrimPriority;
import io.github.palexdev.mfxresources.fonts.MFXFontIcon;
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
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.SneakyThrows;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.github.vtramo.turingmachine.ui.BlankSymbolView.blank;
import static com.github.vtramo.turingmachine.ui.StartSymbolView.startSymbol;

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
    private final String programYaml;
    private String input = "";
    private TuringMachine turingMachine;
    private TuringMachine.Computation computation;
    private ObservableList<Configuration> observableConfigurations;
    private boolean isPlaying;
    private boolean stepInProgress;
    private long speedDelayMs = 500L;

    private final Stage primaryStage;

    public TuringMachineTabController(final Stage primaryStage, final String programYaml) {
        this.primaryStage = primaryStage;
        this.programYaml = programYaml;
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

        // TODO: refactor
        importButton.setOnMouseClicked(__ -> {
            final FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("YAML Files", "*.yaml"));
            final Optional<File> optionalFile = Optional.ofNullable(fileChooser.showOpenDialog(primaryStage));
            if (optionalFile.isPresent()) {
                final File file = optionalFile.get();
                try (final FileInputStream fileInputStream = new FileInputStream(file)) {
                    final String programYaml = new String(fileInputStream.readAllBytes());
                    final TuringMachineValidatorYaml turingMachineValidatorYaml = new TuringMachineValidatorYaml();
                    final ValidationResult validationResult = turingMachineValidatorYaml.validate(programYaml);

                    final MFXGenericDialog dialogContent = MFXGenericDialogBuilder.build()
                        .makeScrollable(true)
                        .get();

                    final MFXStageDialog dialog = MFXGenericDialogBuilder.build(dialogContent)
                        .toStageDialogBuilder()
                        .initOwner(primaryStage)
                        .initModality(Modality.APPLICATION_MODAL)
                        .setDraggable(true)
                        .setOwnerNode(masterAnchorPane)
                        .setScrimPriority(ScrimPriority.WINDOW)
                        .setScrimOwner(true)
                        .get();
                    dialogContent.setShowMinimize(false);
                    dialogContent.setShowAlwaysOnTop(false);
                    dialogContent.setMaxSize(400, 200);

                    if (validationResult.containsErrors()) {
                        dialogContent.setContentText("The file is not well-formed.");
                        dialogContent.addActions(Map.entry(new MFXButton("OK"), ___ -> dialog.close()));
                        MFXFontIcon errorIcon = new MFXFontIcon("fas-circle-xmark", 18);
                        dialogContent.setHeaderIcon(errorIcon);
                        dialogContent.setHeaderText("Invalid file");
                        dialogContent.getStyleClass().add("mfx-error-dialog");
                    } else {
                        MFXFontIcon infoIcon = new MFXFontIcon("fas-circle-question", 18);
                        dialogContent.setHeaderIcon(infoIcon);
                        dialogContent.setContentText("Where would you like to open the machine 'name'?");
                        dialogContent.getStyleClass().add("mfx-info-dialog-standard");
                        dialogContent.addActions(Map.entry(new MFXButton("This window"), ___ -> dialog.close()));
                        dialogContent.addActions(Map.entry(new MFXButton("New window"), ___ -> dialog.close()));
                        dialogContent.addActions(Map.entry(new MFXButton("Cancel"), ___ -> dialog.close()));
                    }

                    dialogContent.getStyleClass().add("mfx-dialog-standard");
                    dialog.showDialog();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
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
        turingMachine = turingMachineParserYaml.parse(programYaml);
        computation = turingMachine.startComputation(input);

        codeArea.appendText(programYaml);

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
