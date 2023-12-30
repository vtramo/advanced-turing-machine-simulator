package com.github.vtramo.turingmachine.ui;

import com.github.vtramo.turingmachine.engine.TuringMachine;
import com.github.vtramo.turingmachine.parser.TuringMachineParserYaml;
import com.github.vtramo.turingmachine.parser.TuringMachineValidatorYaml;
import com.github.vtramo.turingmachine.parser.ValidationMessage;
import com.github.vtramo.turingmachine.parser.ValidationResult;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXScrollPane;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import lombok.Setter;
import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class TuringMachineCompilerView extends GridPane {
    public static final String COMPILE_BUTTON_STYLE_CLASS = "standard-button";
    public static final String COMPILE_BUTTON_TEXT = "Compile";
    public static final String GRID_STYLE_CLASS = "grid-background";
    public static final String TEXT_STYLE_CLASS = "text-compiler";

    private final List<Runnable> onErrorListeners = new ArrayList<>();
    private final List<Consumer<TuringMachine>> onSuccessListeners = new ArrayList<>();
    private final TuringMachineParserYaml turingMachineParserYaml = new TuringMachineParserYaml();
    private final TuringMachineValidatorYaml turingMachineValidatorYaml = new TuringMachineValidatorYaml();
    private final ObservableList<Node> gridPaneChildren = getChildren();
    private MFXScrollPane scrollPane;
    private MFXButton compileButton;
    @Setter
    private Supplier<String> codeSupplier;

    public TuringMachineCompilerView() {
        configureGridPane();
        createScrollPane();
        createCompileButton();
    }

    private void createCompileButton() {
        compileButton = new MFXButton() {{ setOnMouseClicked(__ -> compile(codeSupplier.get())); }};
        compileButton.setMaxHeight(Double.MAX_VALUE);
        compileButton.setMaxWidth(Double.MAX_VALUE);
        final ObservableList<String> styleClasses = compileButton.getStyleClass();
        styleClasses.clear();
        styleClasses.add(COMPILE_BUTTON_STYLE_CLASS);
        compileButton.setText(COMPILE_BUTTON_TEXT);
        gridPaneChildren.add(compileButton);
        GridPane.setHalignment(compileButton, HPos.CENTER);
        GridPane.setValignment(compileButton, VPos.CENTER);
        GridPane.setHgrow(compileButton, Priority.ALWAYS);
        GridPane.setVgrow(compileButton, Priority.ALWAYS);
        GridPane.setRowIndex(compileButton, 1);
    }

    private void createScrollPane() {
        scrollPane = new MFXScrollPane();
        gridPaneChildren.add(scrollPane);
        GridPane.setHgrow(scrollPane, Priority.ALWAYS);
        GridPane.setVgrow(scrollPane, Priority.ALWAYS);
        scrollPane.hbarPolicyProperty().setValue(ScrollPane.ScrollBarPolicy.NEVER);
    }

    private void configureGridPane() {
        getStyleClass().add(GRID_STYLE_CLASS);
        final ObservableList<ColumnConstraints> allColumnConstraints = getColumnConstraints();
        final ObservableList<RowConstraints> allRowConstraints = getRowConstraints();

        final ColumnConstraints columnConstraints1 = new ColumnConstraints();
        columnConstraints1.setHgrow(Priority.SOMETIMES);
        columnConstraints1.setMinWidth(10);
        allColumnConstraints.add(columnConstraints1);

        final RowConstraints rowConstraints1 = new RowConstraints();
        final RowConstraints rowConstraints2 = new RowConstraints();
        rowConstraints1.setMinHeight(10);
        rowConstraints1.setVgrow(Priority.ALWAYS);
        rowConstraints2.setMaxHeight(35);
        rowConstraints2.setMinHeight(35);
        rowConstraints2.setPrefHeight(35);
        rowConstraints2.setVgrow(Priority.SOMETIMES);
        allRowConstraints.add(rowConstraints1);
        allRowConstraints.add(rowConstraints2);
    }

    public void addOnSuccessListener(final Consumer<TuringMachine> mdtConsumer) {
        onSuccessListeners.add(mdtConsumer);
    }
    public void addOnErrorListener(final Runnable runnable) {
        onErrorListeners.add(runnable);
    }

    @SneakyThrows
    private void compile(final String code) {
        clearLogs();

        final TextFlow textFlow = new TextFlow();
        textFlow.setPrefWidth(getWidth());
        textFlow.setMaxWidth(getWidth());
        final ObservableList<Node> logs = textFlow.getChildren();

        final ValidationResult validationResult = turingMachineValidatorYaml.validate(code);
        if (validationResult.containsErrors()) {
            final List<Text> errorLogs = buildErrorLogs(validationResult.validationMessages());
            logs.addAll(errorLogs);
            onErrorListeners.forEach(Runnable::run);
        } else {
            final TuringMachine turingMachine = turingMachineParserYaml.parse(code);
            final Text successLog = buildSuccessfulCompilationLog(turingMachine);
            logs.add(successLog);
            onSuccessListeners.forEach(mdtConsumer -> mdtConsumer.accept(turingMachine));
        }

        scrollPane.setContent(textFlow);
    }

    private List<Text> buildErrorLogs(final List<ValidationMessage> validationMessages) {
        final List<Text> texts = new ArrayList<>(validationMessages.size());
        for (final ValidationMessage validationMessage: validationMessages) {
            final String detailErrorMessage = validationMessage.detailMessage();
            texts.add(buildErrorMessage(detailErrorMessage));
        }
        return texts;
    }

    private Text buildErrorMessage(final String errorMessage) {
        final Text errorText = new Text("[ERROR] " + errorMessage);
        errorText.getStyleClass().add(TEXT_STYLE_CLASS);
        errorText.setFill(Color.RED);
        return errorText;
    }

    private Text buildSuccessfulCompilationLog(final TuringMachine turingMachine) {
        final String mdtName = turingMachine.getName();
        final int tapes = turingMachine.getTotalTapes();
        final Text errorText = new Text("[INFO] " + buildSuccessMessage(mdtName, tapes));
        errorText.getStyleClass().add(TEXT_STYLE_CLASS);
        errorText.setFill(Color.GREEN);
        return errorText;
    }

    private String buildSuccessMessage(final String mdtName, final int tapes) {
        return String.format("""
        Turing's code contains no errors. The %s-tape Turing machine named %s was successfully loaded. Enjoy!
        """, tapes, mdtName);
    }

    public void clearLogs() {
        scrollPane.setContent(null);
    }
}