package com.github.vtramo.turingmachine.ui.dialogs;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.dialogs.MFXGenericDialog;
import io.github.palexdev.materialfx.dialogs.MFXStageDialog;
import io.github.palexdev.mfxresources.fonts.MFXFontIcon;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.util.Map;

public class TuringMachineImportDialog {
    public enum OpenNewWindowDialogResult { THIS_WINDOW, NEW_WINDOW, CANCEL}

    private MFXStageDialog dialog;
    private MFXGenericDialog dialogContent;
    private final Stage stage;
    private final Pane ownerPaneForDialogs;

    public TuringMachineImportDialog(final Stage stage, final Pane ownerPaneForDialogs) {
        this.stage = stage;
        this.ownerPaneForDialogs = ownerPaneForDialogs;
    }

    public OpenNewWindowDialogResult askToOpenNewWindow(final String turingMachineName) {
        initializeDialog();
        final OpenNewWindowDialogResult[] openNewWindowDialogResult = new OpenNewWindowDialogResult[1];
        final MFXFontIcon infoIcon = new MFXFontIcon("fas-circle-question", 18);
        dialogContent.setHeaderIcon(infoIcon);
        dialogContent.setContentText("Where would you like to open the Turing machine '" + turingMachineName + "'?");

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
        final Pair<MFXStageDialog, MFXGenericDialog> mfxStageDialogMFXGenericDialogPair =
            TuringMachineDialogUtils.buildMFXDialog(stage, ownerPaneForDialogs);
        this.dialog = mfxStageDialogMFXGenericDialogPair.getKey();
        this.dialogContent = mfxStageDialogMFXGenericDialogPair.getValue();
    }
}
