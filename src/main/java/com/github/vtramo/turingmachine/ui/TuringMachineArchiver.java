package com.github.vtramo.turingmachine.ui;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.dialogs.MFXGenericDialog;
import io.github.palexdev.materialfx.dialogs.MFXGenericDialogBuilder;
import io.github.palexdev.materialfx.dialogs.MFXStageDialog;
import io.github.palexdev.materialfx.enums.ScrimPriority;
import io.github.palexdev.mfxresources.fonts.MFXFontIcon;
import javafx.event.Event;
import javafx.scene.control.Tab;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.Builder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class TuringMachineArchiver {

    private final Stage stage;
    private final Pane ownerPaneForDialogs;
    private final Tab turingMachineTab;
    private final CodeAreaYaml codeAreaYaml;
    private Path turingMachineYamlProgramPath;
    private String turingMachineYamlProgram;
    private boolean isTuringMachineCodeChangedLastTime;

    @Builder
    public TuringMachineArchiver(
        final Stage stage,
        final Pane ownerPaneForDialogs,
        final Tab turingMachineTab,
        final CodeAreaYaml codeAreaYaml,
        final String turingMachineYamlProgram,
        final Path turingMachineYamlProgramPath
    ) {
        this.stage = stage;
        this.ownerPaneForDialogs = ownerPaneForDialogs;
        this.turingMachineTab = turingMachineTab;
        this.codeAreaYaml = codeAreaYaml;
        this.turingMachineYamlProgram = turingMachineYamlProgram;
        this.turingMachineYamlProgramPath = turingMachineYamlProgramPath;
    }

    public boolean isTuringMachineCodeChanged() {
        final int yamlProgramChecksum = turingMachineYamlProgram.hashCode();
        final int codeAreaYamlChecksum = codeAreaYaml.getText().hashCode();
        return yamlProgramChecksum != codeAreaYamlChecksum;
    }

    public String saveTuringMachineCode() {
        if (!isTuringMachineCodeChanged()) return turingMachineYamlProgram;

        try {
            final String turingMachineYamlProgram = codeAreaYaml.getText();
            Files.write(turingMachineYamlProgramPath, turingMachineYamlProgram.getBytes());
            removeAsteriskToTuringMachineTabTitle();
            this.turingMachineYamlProgram = turingMachineYamlProgram;
            isTuringMachineCodeChangedLastTime = false;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return turingMachineYamlProgram;
    }

    public void onTuringMachineCodeChanged() {
        if (isTuringMachineCodeChanged()) {
            if (!isTuringMachineCodeChangedLastTime) {
                addAsteriskToTuringMachineTabTitle();
            }

            isTuringMachineCodeChangedLastTime = true;
            return;
        }

        if (isTuringMachineCodeChangedLastTime) {
            isTuringMachineCodeChangedLastTime = false;
            removeAsteriskToTuringMachineTabTitle();
        }
    }

    private void addAsteriskToTuringMachineTabTitle() {
        turingMachineTab.setText(STR."*\{turingMachineTab.getText()}");
    }

    private void removeAsteriskToTuringMachineTabTitle() {
        final String turingMachineTabTitle = turingMachineTab.getText();
        final String turingMachineTabTitleWithAsterisk = turingMachineTabTitle.replaceFirst("\\*", "");
        turingMachineTab.setText(turingMachineTabTitleWithAsterisk);
    }

    public void onTuringMachineTabCloseRequest(final Event closeEvent, final String turingMachineName) {
        if (!isTuringMachineCodeChangedLastTime) return ;

        final MFXGenericDialog dialogContent = buildCloseRequestWarningDialogContent(turingMachineName);
        final MFXStageDialog dialog = buildCloseRequestWarningDialog(dialogContent);

        dialogContent.addActions(Map.entry(new MFXButton("Close without saving"), ___ -> {
            dialog.close();
        }));

        dialogContent.addActions(Map.entry(new MFXButton("Cancel"), ___ -> {
            closeEvent.consume();
            dialog.close();
        }));

        dialogContent.setOnClose(___ -> {
            closeEvent.consume();
            dialog.close();
        });

        final String saveAsButtonText = (turingMachineYamlProgramPath == null) ? "Save as..." : "Save";
        final MFXButton saveAsButton = new MFXButton(saveAsButtonText);
        dialogContent.addActions(Map.entry(saveAsButton, ___ -> {
            if (isExistingTuringMachineProgram()) {
                saveTuringMachineCode();
            } else {

            }

            dialog.close();
        }));

        dialog.showAndWait();
    }

    private boolean isExistingTuringMachineProgram() {
        return turingMachineYamlProgramPath != null;
    }

    private MFXStageDialog buildCloseRequestWarningDialog(MFXGenericDialog dialogContent) {
        final MFXStageDialog dialog = MFXGenericDialogBuilder.build(dialogContent)
            .toStageDialogBuilder()
            .initOwner(stage)
            .initModality(Modality.APPLICATION_MODAL)
            .setDraggable(true)
            .setOwnerNode(ownerPaneForDialogs)
            .setScrimPriority(ScrimPriority.WINDOW)
            .setScrimOwner(true)
            .get();
        return dialog;
    }

    private static MFXGenericDialog buildCloseRequestWarningDialogContent(String turingMachineName) {
        final MFXGenericDialog dialogContent = MFXGenericDialogBuilder.build()
            .makeScrollable(true)
            .get();
        dialogContent.setShowMinimize(false);
        dialogContent.setShowAlwaysOnTop(false);
        dialogContent.setMaxSize(400, 200);
        final MFXFontIcon warnIcon = new MFXFontIcon("fas-circle-exclamation", 18);
        dialogContent.setHeaderIcon(warnIcon);
        dialogContent.setHeaderText("Unsaved changes to the Turing code");
        dialogContent.setContentText(STR."Save changes to Turing machine \"\{turingMachineName}\" before closing?");
        dialogContent.getStyleClass().add("mfx-warn-dialog");
        return dialogContent;
    }
}