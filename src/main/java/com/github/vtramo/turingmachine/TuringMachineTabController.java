package com.github.vtramo.turingmachine;

import com.github.vtramo.turingmachine.engine.*;
import com.github.vtramo.turingmachine.parser.TuringMachineParserYaml;
import com.github.vtramo.turingmachine.ui.*;
import com.github.vtramo.turingmachine.ui.dialogs.TuringMachineHaltedDialogBuilder;
import com.github.vtramo.turingmachine.ui.dialogs.TuringMachineHaltedStateDialog;
import com.github.vtramo.turingmachine.ui.dialogs.TuringMachineImportDialog;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXFilterComboBox;
import io.github.palexdev.materialfx.controls.MFXScrollPane;
import io.github.palexdev.materialfx.controls.MFXSlider;
import io.github.palexdev.materialfx.utils.StringUtils;
import io.github.palexdev.materialfx.utils.others.FunctionalStringConverter;
import javafx.application.Platform;
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
import javafx.util.StringConverter;
import lombok.SneakyThrows;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.github.vtramo.turingmachine.TapeViewController.DELAY_BEFORE_NEXT_MOVE_MS;
import static com.github.vtramo.turingmachine.ui.BlankSymbolView.blank;
import static com.github.vtramo.turingmachine.ui.StartSymbolView.startSymbol;
import static com.github.vtramo.turingmachine.ui.TuringMachineImporterYaml.TuringMachineImportResult;
import static com.github.vtramo.turingmachine.ui.dialogs.TuringMachineImportDialog.OpenNewWindowDialogResult;

public class TuringMachineTabController {
    @FXML
    private Tab turingMachineTab;
    @FXML
    private Label stateLabelValue;
    @FXML
    private Label stepsLabelValue;
    @FXML
    private Label spaceLabelValue;
    @FXML
    private AnchorPane masterAnchorPane;
    @FXML
    private VBox tapesVBox;
    @FXML
    private MFXScrollPane tapesScrollPane;
    @FXML
    private MFXButton playButton;
    @FXML
    private MFXButton pauseButton;
    @FXML
    private MFXButton resetButton;
    @FXML
    private MFXButton stepBackButton;
    @FXML
    private MFXButton stepButton;
    @FXML
    private Button loadInputButton;
    @FXML
    private TextField inputTextField;
    @FXML
    private MFXButton importButton;
    @FXML
    private MFXButton saveButton;
    @FXML
    private MFXSlider speedSlider;
    @FXML
    private ConfigurationListView configurationsListView;
    @FXML
    private CodeAreaYaml codeAreaYaml;
    @FXML
    private TuringMachineCompilerView turingMachineCompilerView;
    @FXML
    private MFXFilterComboBox<TuringMachineStoredProgram> turingMachineExamplesFilterCombo;

    private TapeViewController tapeViewController;
    private String turingMachineCode;
    private String input = "";
    private TuringMachine turingMachine;
    private TuringMachine.Computation computation;
    private TuringMachineArchiver turingMachineArchiver;
    private ObservableList<Configuration> observableConfigurations;
    private boolean isPlaying;
    private boolean stepInProgress;
    private double speedDelayMs = 500D;

    private Path turingMachineCodePath;
    private final HomeController homeController;
    private final Stage primaryStage;

    public TuringMachineTabController(
        final HomeController homeController,
        final Stage primaryStage,
        final String turingMachineCode,
        final Path turingMachineCodePath
    ) {
        this.homeController = homeController;
        this.primaryStage = primaryStage;
        this.turingMachineCode = turingMachineCode;
        this.turingMachineCodePath = turingMachineCodePath;
    }

    @SneakyThrows
    public void initialize() {
        configureButtons();
        configureSpeedSlider();
        configureTuringMachineCompiler();
        configureTuringMachine();
        configureTuringMachineExamplesFilterCombo();
    }

    private void configureButtons() {
        configurePlayButton();
        configurePauseButton();
        configureStepButton();
        configureStepBackButton();
        configureResetButton();
        configureLoadInputButton();
        configureImportButton();
        configureSaveButton();
    }

    private void configurePlayButton() {
        playButton.setOnMouseClicked(__ -> {
            if (!isPlaying) {
                stepBackButton.setDisable(true);
                stepButton.setDisable(true);
                isPlaying = true;
                play();
            }
        });
    }

    private void configurePauseButton() {
        pauseButton.setDisable(true);
        pauseButton.setOnMouseClicked(__ -> {
            isPlaying = false;
            pauseButton.setDisable(true);
        });
    }

    private void configureStepButton() {
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
    }

    private void configureStepBackButton() {
        stepBackButton.setDisable(true);
        stepBackButton.setOnMouseClicked(__ -> {
            if (!stepInProgress) {
                stepInProgress = true;
                stepBack()
                    .thenAccept(___ -> stepInProgress = false);
            }
        });
    }

    private void configureResetButton() {
        resetButton.setOnMouseClicked(__ -> reset());
    }

    private void configureLoadInputButton() {
        loadInputButton.getStyleClass().removeFirst();
        loadInputButton.setOnMouseClicked(__ -> loadInputFromTextField());
    }

    private void configureImportButton() {
        importButton.setOnMouseClicked(__ -> {
            importTuringMachine();
        });
    }

    private void configureSaveButton() {
        Platform.runLater(() -> {
            configureTuringMachineArchiver();

            codeAreaYaml.addTextChangeListener((__, ___, ____) -> {
                turingMachineArchiver.onTuringMachineCodeChanged();
            });

            saveButton.setOnMouseClicked(__ -> {
                this.turingMachineCode = turingMachineArchiver.saveTuringMachineCode();
            });

            turingMachineTab.setOnCloseRequest(closeEvent -> {
                turingMachineArchiver.onTuringMachineTabCloseRequest(closeEvent, turingMachine.getName());
            });
        });
    }

    private void configureSpeedSlider() {
        speedSlider.setValue(speedDelayMs - DELAY_BEFORE_NEXT_MOVE_MS + 0.1);
        final DoubleProperty speedSliderDoubleProperty = speedSlider.valueProperty();
        speedSlider.setRotate(180);
        speedSliderDoubleProperty.addListener((__, ___, newValue)
            -> speedDelayMs = newValue.longValue() - DELAY_BEFORE_NEXT_MOVE_MS + 0.1);
    }

    private void configureTuringMachineCompiler() {
        turingMachineCompilerView.setCodeSupplier(() -> codeAreaYaml.getText());
        turingMachineCompilerView.addOnSuccessListener(mdt -> {
            this.turingMachine = mdt;
            reset();
        });
    }

    @SneakyThrows
    private void configureTuringMachine() {
        final TuringMachineParserYaml turingMachineParserYaml = new TuringMachineParserYaml();
        turingMachine = turingMachineParserYaml.parse(turingMachineCode);
        computation = turingMachine.startComputation(input);

        codeAreaYaml.appendText(turingMachineCode);

        createTapes();

        observableConfigurations = FXCollections.observableArrayList(computation.getConfigurations());
        configurationsListView.setItems(observableConfigurations);

        setStepsTextLabel(0);
        setSpaceTextLabel(computation.getSpace());
        setStateTextLabel(turingMachine.getInitialState());
    }

    private void configureTuringMachineExamplesFilterCombo() {
        final ObservableList<TuringMachineStoredProgram> turingMachineStoredPrograms =
            FXCollections.observableArrayList(TuringMachineStoredProgram.turingMachineStoredPrograms);

        final StringConverter<TuringMachineStoredProgram> converter =
            FunctionalStringConverter.to(turingMachineStoredProgram ->
                (turingMachineStoredProgram == null)
                        ? ""
                        : turingMachineStoredProgram.name());

        final Function<String, Predicate<TuringMachineStoredProgram>> filterFunction =
            turingMachineName ->
                turingMachineStoredProgram ->
                    StringUtils.containsIgnoreCase(
                        converter.toString(turingMachineStoredProgram),
                        turingMachineName);

        turingMachineExamplesFilterCombo.setItems(turingMachineStoredPrograms);
        turingMachineExamplesFilterCombo.setConverter(converter);
        turingMachineExamplesFilterCombo.setFilterFunction(filterFunction);

        turingMachineExamplesFilterCombo
            .selectedItemProperty()
            .addListener(__ -> {
                onTuringMachineExampleSelected();
            });
    }

    private void onTuringMachineExampleSelected() {
        final TuringMachineStoredProgram turingMachineStoredProgram = turingMachineExamplesFilterCombo.getSelectedItem();
        if (turingMachineStoredProgram == null) return;
        turingMachineExamplesFilterCombo.clearSelection();
        final TuringMachineImportDialog turingMachineImportDialog = new TuringMachineImportDialog(primaryStage, masterAnchorPane);
        final String turingMachineName = turingMachineStoredProgram.name();
        final OpenNewWindowDialogResult openNewWindowDialogResult = turingMachineImportDialog.askToOpenNewWindow(turingMachineName);

        switch (openNewWindowDialogResult) {
            case NEW_WINDOW ->
                homeController.createTuringMachineTab(
                    turingMachineName,
                    turingMachineStoredProgram.turingMachineCode(),
                    turingMachineStoredProgram.turingMachineCodePath()
                );
            case THIS_WINDOW -> changeTuringMachineOnThisTab(turingMachineStoredProgram);
            case CANCEL -> {}
        }
    }

    private void configureTuringMachineArchiver() {
        this.turingMachineArchiver = TuringMachineArchiver.builder()
            .turingMachineTab(turingMachineTab)
            .turingMachineCodeSupplier(() -> codeAreaYaml.getText())
            .stage(primaryStage)
            .ownerPaneForDialogs(masterAnchorPane)
            .turingMachineCode(turingMachineCode)
            .turingMachineCodePath(turingMachineCodePath)
            .build();
    }

    private void importTuringMachine() {
        final TuringMachineImporterYaml turingMachineImporterYaml = new TuringMachineImporterYaml(primaryStage, masterAnchorPane);
        final Optional<TuringMachineImportResult> optionalTuringMachineImportResult = turingMachineImporterYaml.importTuringMachine();
        if (optionalTuringMachineImportResult.isEmpty()) return;

        final TuringMachineImportResult turingMachineImportResult = optionalTuringMachineImportResult.get();

        if (turingMachineImportResult.openNewWindow()) {
            final String turingMachineName = turingMachine.getName();
            final Path turingMachineYamlProgramPath = turingMachineImportResult.turingMachineCodePath();
            homeController.createTuringMachineTab(turingMachineName, turingMachineCode, turingMachineYamlProgramPath);
        } else {
            changeTuringMachineOnThisTab(turingMachineImportResult);
        }

    }

    private void changeTuringMachineOnThisTab(TuringMachineImportResult turingMachineImportResult) {
        final TuringMachine importedTuringMachine = turingMachineImportResult.turingMachine();
        final String importedYamlProgram = turingMachineImportResult.turingMachineCode();
        final Path turingMachineCodePath = turingMachineImportResult.turingMachineCodePath();

        changeTuringMachineOnThisTab(importedTuringMachine, importedYamlProgram, turingMachineCodePath);
    }

    private void changeTuringMachineOnThisTab(TuringMachineStoredProgram turingMachineStoredProgram) {
        final TuringMachine turingMachine = turingMachineStoredProgram.turingMachine();
        final String turingMachineCode = turingMachineStoredProgram.turingMachineCode();
        final Path turingMachineCodePath = turingMachineStoredProgram.turingMachineCodePath();

        changeTuringMachineOnThisTab(turingMachine, turingMachineCode, turingMachineCodePath);
    }

    private void changeTuringMachineOnThisTab(
        final TuringMachine turingMachine,
        final String turingMachineCode,
        final Path turingMachineCodePath
    ) {
        this.turingMachine = turingMachine;
        this.turingMachineCode = turingMachineCode;
        this.turingMachineCodePath = turingMachineCodePath;

        this.turingMachineArchiver.setTuringMachineCode(turingMachineCode);
        this.turingMachineArchiver.setTuringMachineCodePath(turingMachineCodePath);
        this.turingMachineArchiver.reset();

        codeAreaYaml.setText(this.turingMachineCode);
        turingMachineTab.setText(turingMachine.getName());

        turingMachineCompilerView.clearLogs();
        reset();
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
        playButton.setDisable(true);
        pauseButton.setDisable(false);
        step()
            .thenAccept(__ -> {
                if (computation.isHalted()) {
                    stepButton.setDisable(true);
                    playButton.setDisable(true);
                    pauseButton.setDisable(true);
                    isPlaying = false;
                    showMachineHaltedDialog();
                    return;
                }

                if (!isPlaying) {
                    playButton.setDisable(false);
                    pauseButton.setDisable(true);
                    if (computation.hasNextConfiguration()) {
                        stepButton.setDisable(false);
                    }
                    if (computation.hasPreviousConfiguration()) {
                        stepBackButton.setDisable(false);
                    }
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
            TuringMachineHaltedStateDialog.builder()
                .withInput(input)
                .withTerminalState(terminalState)
                .withOwner(primaryStage)
                .withOwnerNode(masterAnchorPane)
                .withTotalSteps(steps)
                .withTotalSpace(space);

        if (Objects.equals(terminalState, TerminalState.HALTING_STATE)) {
            turingMachineHaltedDialogBuilder.withOutput(computation.getOutput());
        }

        final TuringMachineHaltedStateDialog machineHaltedDialog = turingMachineHaltedDialogBuilder.build();
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
        if (!isPlaying) {
            stepBackButton.setDisable(false);
        }
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
        stateLabelValue.setText(state);
    }

    private void setStepsTextLabel(final int steps) {
        stepsLabelValue.setText(String.valueOf(steps));
    }

    private void setSpaceTextLabel(final int space) {
        spaceLabelValue.setText(String.valueOf(space));
    }

    private void reset() {
        isPlaying = false;
        stepInProgress = false;
        stepBackButton.setDisable(true);
        stepButton.setDisable(false);
        playButton.setDisable(false);
        pauseButton.setDisable(true);
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
