package com.github.vtramo.turingmachine.ui;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.vtramo.turingmachine.engine.TuringMachine;
import com.github.vtramo.turingmachine.parser.TuringMachineParserYaml;
import com.github.vtramo.turingmachine.parser.TuringMachineValidatorYaml;
import com.github.vtramo.turingmachine.parser.ValidationResult;
import com.github.vtramo.turingmachine.ui.dialogs.TuringMachineDialogUtils;
import com.github.vtramo.turingmachine.ui.dialogs.TuringMachineImportDialog;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.dialogs.MFXGenericDialog;
import io.github.palexdev.materialfx.dialogs.MFXStageDialog;
import io.github.palexdev.mfxresources.fonts.MFXFontIcon;
import javafx.collections.ObservableList;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.github.vtramo.turingmachine.ui.dialogs.TuringMachineImportDialog.OpenNewWindowDialogResult;

public class TuringMachineImporterYaml {
    private static final TuringMachineValidatorYaml turingMachineValidatorYaml = new TuringMachineValidatorYaml();
    private static final TuringMachineParserYaml turingMachineParserYaml = new TuringMachineParserYaml();
    private static final FileChooser fileChooser = new FileChooser();

    public record TuringMachineImportResult(
        TuringMachine turingMachine,
        String turingMachineCode,
        Path turingMachineCodePath,
        boolean openNewWindow
    ) {}

    private MFXStageDialog dialog;
    private MFXGenericDialog dialogContent;


    private final Stage stage;
    private final Pane ownerPaneForDialogs;

    public TuringMachineImporterYaml(final Stage stage, final Pane ownerPaneForDialogs) {
        this.stage = stage;
        this.ownerPaneForDialogs = ownerPaneForDialogs;
        addExtensionFilters();
    }

    private void addExtensionFilters() {
        final ObservableList<FileChooser.ExtensionFilter> extensionFilters = fileChooser.getExtensionFilters();
        extensionFilters.add(new FileChooser.ExtensionFilter("YAML Files", "*.yaml"));
    }

    public Optional<TuringMachineImportResult> importTuringMachine() {
        final Optional<File> optionalFile = Optional.ofNullable(fileChooser.showOpenDialog(stage));
        if (optionalFile.isEmpty()) return Optional.empty();

        final File file = optionalFile.get();
        final String turingMachineCodeYaml = readTuringMachineProgram(file);

        initializeMFXDialog();

        final ValidationResult validationResult = turingMachineValidatorYaml.validate(turingMachineCodeYaml);
        if (validationResult.containsErrors()) {
            showBadTuringMachineYamlDefinitionDialog();
            return Optional.empty();
        }

        final TuringMachine turingMachine = parseTuringMachineYamlDefinition(turingMachineCodeYaml);
        final String turingMachineName = turingMachine.getName();

        final TuringMachineImportDialog turingMachineImportDialog = new TuringMachineImportDialog(stage, ownerPaneForDialogs);
        final OpenNewWindowDialogResult openNewWindowDialogResult = turingMachineImportDialog.askToOpenNewWindow(turingMachineName);
        if (Objects.equals(openNewWindowDialogResult, OpenNewWindowDialogResult.CANCEL)) {
            return Optional.empty();
        }

        final boolean openNewWindow = Objects.equals(OpenNewWindowDialogResult.NEW_WINDOW, openNewWindowDialogResult);
        final Path turingMachineCodePath = Path.of(file.getAbsolutePath());
        return Optional.of(new TuringMachineImportResult(
            turingMachine,
            turingMachineCodeYaml,
            turingMachineCodePath,
            openNewWindow));
    }

    private void initializeMFXDialog() {
        final Pair<MFXStageDialog, MFXGenericDialog> mfxStageDialogMFXGenericDialogPair =
            TuringMachineDialogUtils.buildMFXDialog(stage, ownerPaneForDialogs);
        this.dialog = mfxStageDialogMFXGenericDialogPair.getKey();
        this.dialogContent = mfxStageDialogMFXGenericDialogPair.getValue();
    }

    private void showBadTuringMachineYamlDefinitionDialog() {
        dialogContent.setContentText("The file is not well-formed.");
        dialogContent.addActions(Map.entry(new MFXButton("OK"), ___ -> dialog.close()));
        final MFXFontIcon errorIcon = new MFXFontIcon("fas-circle-xmark", 18);
        dialogContent.setHeaderIcon(errorIcon);
        dialogContent.setHeaderText("Invalid file");
        dialogContent.getStyleClass().add("mfx-error-dialog");
        dialog.show();
    }

    private String readTuringMachineProgram(final File file) {
        try (final FileInputStream fileInputStream = new FileInputStream(file)) {
            return new String(fileInputStream.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static TuringMachine parseTuringMachineYamlDefinition(final String programYaml) {
        try {
            return turingMachineParserYaml.parse(programYaml);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
