package com.github.vtramo.turingmachine.ui;

import com.github.vtramo.turingmachine.ui.dialogs.TuringMachineSaveAsDialog;
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
import lombok.Setter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Supplier;

public class TuringMachineArchiver {
    private final Stage stage;
    private final Pane ownerPaneForDialogs;
    private final Tab turingMachineTab;
    private final Supplier<String> turingMachineCodeSupplier;
    @Setter
    private Path turingMachineCodePath;
    @Setter
    private String turingMachineCode;
    private boolean isTuringMachineCodeChangedLastTime;
    private boolean isTuringMachineStored;

    @Builder
    public TuringMachineArchiver(
        final Stage stage,
        final Pane ownerPaneForDialogs,
        final Tab turingMachineTab,
        final Supplier<String> turingMachineCodeSupplier,
        final String turingMachineCode,
        final Path turingMachineCodePath
    ) {
        this.stage = stage;
        this.ownerPaneForDialogs = ownerPaneForDialogs;
        this.turingMachineTab = turingMachineTab;
        this.turingMachineCodeSupplier = turingMachineCodeSupplier;
        this.turingMachineCode = turingMachineCode;
        this.turingMachineCodePath = turingMachineCodePath;
        reset();
    }

    public void reset() {
        isTuringMachineStored = (turingMachineCodePath != null);
        isTuringMachineCodeChangedLastTime = false;
        onTuringMachineCodeChanged();
    }

    public boolean isTuringMachineCodeChanged() {
        final int yamlProgramChecksum = turingMachineCode.trim().hashCode();
        final int codeAreaYamlChecksum = turingMachineCodeSupplier.get().trim().hashCode();
        return yamlProgramChecksum != codeAreaYamlChecksum;
    }

    public String saveTuringMachineCode() {
        if (!isExistingTuringMachineProgram()) {
            showTuringMachineSaveAsDialog();
        } else {
            if (!isTuringMachineCodeChanged()) return turingMachineCode;
            try {
                this.turingMachineCode = turingMachineCodeSupplier.get();
                Files.write(turingMachineCodePath, turingMachineCode.getBytes());
                removeAsteriskToTuringMachineTabTitle();
                isTuringMachineCodeChangedLastTime = false;
                isTuringMachineStored = true;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return turingMachineCode;
    }

    public void onTuringMachineCodeChanged() {
        if (!isTuringMachineStored || isTuringMachineCodeChanged()) {
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
        turingMachineTab.setText("*" + turingMachineTab.getText());
    }

    private void removeAsteriskToTuringMachineTabTitle() {
        final String turingMachineTabTitle = turingMachineTab.getText();
        final String turingMachineTabTitleWithAsterisk = turingMachineTabTitle.replaceFirst("\\*", "");
        turingMachineTab.setText(turingMachineTabTitleWithAsterisk);
    }

    public void onTuringMachineTabCloseRequest(final Event closeEvent, final String turingMachineName) {
        if (!isTuringMachineCodeChangedLastTime) return ;

        final MFXGenericDialog dialogContent = buildCloseRequestWarningDialogContent(turingMachineName);
        final MFXStageDialog dialog = buildStageDialog(dialogContent);

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

        final String saveAsButtonText = (!isExistingTuringMachineProgram()) ? "Save as..." : "Save";
        final MFXButton saveAsButton = new MFXButton(saveAsButtonText);
        dialogContent.addActions(Map.entry(saveAsButton, ___ -> {
            if (isExistingTuringMachineProgram()) {
                saveTuringMachineCode();
            } else {
                showTuringMachineSaveAsDialog();
            }

            dialog.close();
        }));

        dialog.showAndWait();
    }

    private void showTuringMachineSaveAsDialog() {
        final TuringMachineSaveAsDialog turingMachineSaveAsDialog = TuringMachineSaveAsDialog.builder()
            .turingMachineCode(turingMachineCode)
            .stage(stage)
            .ownerPaneForDialogs(ownerPaneForDialogs)
            .addOnTuringMachineCodeSavedListener(turingMachineCodePath -> {
                this.turingMachineCodePath = turingMachineCodePath;
                isTuringMachineStored = true;
                removeAsteriskToTuringMachineTabTitle();
            })
            .build();

        turingMachineSaveAsDialog.show();
    }

    private boolean isExistingTuringMachineProgram() {
        if (turingMachineCodePath == null) return false;
        if (Files.exists(turingMachineCodePath)) return true;
        addAsteriskToTuringMachineTabTitle();
        return false;
    }

    private MFXStageDialog buildStageDialog(MFXGenericDialog dialogContent) {
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

    private static MFXGenericDialog buildCloseRequestWarningDialogContent(final String turingMachineName) {
        final MFXGenericDialog dialogContent = MFXGenericDialogBuilder.build()
            .makeScrollable(true)
            .get();
        dialogContent.setShowMinimize(false);
        dialogContent.setShowAlwaysOnTop(false);
        dialogContent.setMaxSize(400, 200);
        final MFXFontIcon warnIcon = new MFXFontIcon("fas-circle-exclamation", 18);
        dialogContent.setHeaderIcon(warnIcon);
        dialogContent.setHeaderText("Unsaved changes to the Turing code");
        dialogContent.setContentText("Save changes to Turing machine " + turingMachineName + " before closing?");
        dialogContent.getStyleClass().add("mfx-warn-dialog");
        return dialogContent;
    }
}