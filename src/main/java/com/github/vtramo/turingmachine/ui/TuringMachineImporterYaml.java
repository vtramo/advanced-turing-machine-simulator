package com.github.vtramo.turingmachine.ui;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.vtramo.turingmachine.engine.TuringMachine;
import com.github.vtramo.turingmachine.parser.TuringMachineParserYaml;
import com.github.vtramo.turingmachine.parser.TuringMachineValidatorYaml;
import com.github.vtramo.turingmachine.parser.ValidationResult;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.dialogs.MFXGenericDialog;
import io.github.palexdev.materialfx.dialogs.MFXGenericDialogBuilder;
import io.github.palexdev.materialfx.dialogs.MFXStageDialog;
import io.github.palexdev.materialfx.enums.ScrimPriority;
import io.github.palexdev.mfxresources.fonts.MFXFontIcon;
import javafx.collections.ObservableList;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

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

    private enum OpenNewWindowDialogResult { THIS_WINDOW, NEW_WINDOW, CANCEL }

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
        initializeDialog();

        final ValidationResult validationResult = turingMachineValidatorYaml.validate(turingMachineCodeYaml);
        if (validationResult.containsErrors()) {
            showBadTuringMachineYamlDefinitionDialog();
            return Optional.empty();
        }

        final TuringMachine turingMachine = parseTuringMachineYamlDefinition(turingMachineCodeYaml);
        final String turingMachineName = turingMachine.getName();

        final OpenNewWindowDialogResult openNewWindowDialogResult = askForOpenNewWindow(turingMachineName);
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

    private OpenNewWindowDialogResult askForOpenNewWindow(final String turingMachineName) {
        final OpenNewWindowDialogResult[] openNewWindowDialogResult = new OpenNewWindowDialogResult[1];
        final MFXFontIcon infoIcon = new MFXFontIcon("fas-circle-question", 18);
        dialogContent.setHeaderIcon(infoIcon);
        dialogContent.setContentText(STR."Where would you like to open the Turing machine '\{turingMachineName}'?");

        dialogContent.addActions(Map.entry(new MFXButton("This window"), ___ -> {
            openNewWindowDialogResult[0] = OpenNewWindowDialogResult.THIS_WINDOW;
            dialog.close();
        }));

        dialogContent.addActions(Map.entry(new MFXButton("New window"), ___ -> {
            openNewWindowDialogResult[0] = OpenNewWindowDialogResult.NEW_WINDOW;
            dialog.close();
        }));

        dialogContent.addActions(Map.entry(new MFXButton("Cancel"), ___ -> {
            openNewWindowDialogResult[0] = OpenNewWindowDialogResult.CANCEL;
            dialog.close();
        }));

        dialogContent.setOnClose(___ -> {
            openNewWindowDialogResult[0] = OpenNewWindowDialogResult.CANCEL;
            dialog.close();
        });

        dialog.showAndWait();
        return openNewWindowDialogResult[0];
    }

    private void initializeDialog() {
        dialogContent = MFXGenericDialogBuilder.build()
            .makeScrollable(true)
            .get();
        dialogContent.setShowMinimize(false);
        dialogContent.setShowAlwaysOnTop(false);
        dialogContent.setMaxSize(400, 200);

        dialog = MFXGenericDialogBuilder.build(dialogContent)
            .toStageDialogBuilder()
            .initOwner(stage)
            .initModality(Modality.APPLICATION_MODAL)
            .setDraggable(true)
            .setOwnerNode(ownerPaneForDialogs)
            .setScrimPriority(ScrimPriority.WINDOW)
            .setScrimOwner(true)
            .get();
    }

    private static TuringMachine parseTuringMachineYamlDefinition(final String programYaml) {
        try {
            return turingMachineParserYaml.parse(programYaml);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
