package com.github.vtramo.turingmachine.ui.dialogs;

import com.github.vtramo.turingmachine.TuringMachineApplication;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXIconWrapper;
import io.github.palexdev.materialfx.controls.MFXSimpleNotification;
import io.github.palexdev.materialfx.controls.MFXTextField;
import io.github.palexdev.materialfx.dialogs.MFXGenericDialog;
import io.github.palexdev.materialfx.dialogs.MFXGenericDialogBuilder;
import io.github.palexdev.materialfx.dialogs.MFXStageDialog;
import io.github.palexdev.materialfx.enums.NotificationPos;
import io.github.palexdev.materialfx.enums.ScrimPriority;
import io.github.palexdev.materialfx.factories.InsetsFactory;
import io.github.palexdev.materialfx.notifications.MFXNotificationSystem;
import io.github.palexdev.materialfx.validation.Constraint;
import io.github.palexdev.materialfx.validation.Severity;
import io.github.palexdev.mfxresources.fonts.IconDescriptor;
import io.github.palexdev.mfxresources.fonts.IconsProviders;
import io.github.palexdev.mfxresources.fonts.MFXFontIcon;
import io.github.palexdev.mfxresources.fonts.fontawesome.FontAwesomeRegular;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.*;
import java.util.function.Consumer;

import static io.github.palexdev.materialfx.validation.Validated.INVALID_PSEUDO_CLASS;

public class TuringMachineSaveAsDialog {
    private static final String TURING_MACHINE_SAVE_AS_DIALOG = "turing-machine-save-as-dialog.fxml";

    @FXML
    private Button changeDirButton;
    @FXML
    private MFXTextField turingMachineFilenameTextField;
    @FXML
    private MFXTextField turingMachineDirectoryPathTextField;
    private MFXStageDialog saveAsMFXStageDialog;

    private final List<Consumer<Path>> onTuringMachineCodeSavedListeners = new ArrayList<>();
    private final Stage stage;
    private final Pane ownerPaneForDialogs;
    private final String turingMachineCode;

    public TuringMachineSaveAsDialog(
        final Stage stage,
        final Pane ownerPaneForDialogs,
        final String turingMachineCode
    ) {
        this.stage = stage;
        this.ownerPaneForDialogs = ownerPaneForDialogs;
        this.turingMachineCode = turingMachineCode;
        buildSaveAsDialog();
    }

    public boolean addOnTuringMachineCodeSavedListener(final Consumer<Path> pathConsumer) {
        return onTuringMachineCodeSavedListeners.add(pathConsumer);
    }

    public void show() {
        saveAsMFXStageDialog.show();
    }

    private void buildSaveAsDialog() {
        final Node saveAsDialogContent = loadSaveAsDialogContent();
        configureChangeDirButton();
        setDirectoryPathTextFieldConstraints();
        setDirectoryPathTextFieldListeners();
        this.saveAsMFXStageDialog = buildSaveAsMFXStageDialog(saveAsDialogContent);
    }

    private Node loadSaveAsDialogContent() {
        final FXMLLoader fxmlLoader = new FXMLLoader(TuringMachineApplication.class.getResource(TURING_MACHINE_SAVE_AS_DIALOG));
        fxmlLoader.setController(this);

        final Node saveAsContent;
        try {
            saveAsContent = fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return saveAsContent;
    }

    private void configureChangeDirButton() {
        changeDirButton.setOnMouseClicked(__ -> {
            final DirectoryChooser directoryChooser = new DirectoryChooser();
            final Optional<File> dir = Optional.ofNullable(directoryChooser.showDialog(stage));
            dir.ifPresent(selectedDir -> {
                turingMachineDirectoryPathTextField.setText(selectedDir.toString());
            });
        });
    }

    private void setDirectoryPathTextFieldConstraints() {
        final Constraint directoryExistConstraint = Constraint.Builder.build()
            .setSeverity(Severity.ERROR)
            .setMessage("Directory does not exist!")
            .setCondition(Bindings.createBooleanBinding(
                () -> Files.isDirectory(Path.of(turingMachineDirectoryPathTextField.getText())),
                turingMachineDirectoryPathTextField.textProperty()
            )).get();

        final Constraint directoryWritePermissionConstraint = Constraint.Builder.build()
            .setSeverity(Severity.ERROR)
            .setMessage("Writing permission denied!")
            .setCondition(Bindings.createBooleanBinding(
                () -> isFileWithOwnerWritePermission(Path.of(turingMachineDirectoryPathTextField.getText())),
                turingMachineDirectoryPathTextField.textProperty()
            )).get();

        turingMachineDirectoryPathTextField.getValidator()
            .constraint(directoryExistConstraint)
            .constraint(directoryWritePermissionConstraint);
    }

    private void setDirectoryPathTextFieldListeners() {
        turingMachineDirectoryPathTextField
            .getValidator()
            .validProperty()
            .addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    //                validationLabel.setVisible(false);
                    turingMachineDirectoryPathTextField.pseudoClassStateChanged(PseudoClass.getPseudoClass("invalid"), false);
                }
            });

        turingMachineDirectoryPathTextField
            .textProperty()
            .addListener((observable, oldValue, newValue) -> {
                if (!Objects.equals(oldValue, newValue)) {
                    List<Constraint> constraints = turingMachineDirectoryPathTextField.validate();
                    if (!constraints.isEmpty()) {
                        turingMachineDirectoryPathTextField.pseudoClassStateChanged(INVALID_PSEUDO_CLASS, true);
                        //                    validationLabel.setText(constraints.get(0).getMessage());
                        //                    validationLabel.setVisible(true);
                    }
                }
            });
    }

    private MFXStageDialog buildSaveAsMFXStageDialog(final Node saveAsDialogContent) {
        final MFXGenericDialog saveAsMFXGenericDialogContent = buildSaveAsMFXGenericDialogContent(saveAsDialogContent);
        final MFXStageDialog saveAsStageDialog = buildMFXStageDialog(saveAsMFXGenericDialogContent);

        final BooleanBinding fileCanBeSaved = turingMachineDirectoryPathTextField
            .getValidator()
            .validProperty()
            .and(turingMachineFilenameTextField
                .textProperty()
                .isNotEmpty())
            .and(turingMachineDirectoryPathTextField
                .textProperty()
                .isNotEmpty());

        final MFXButton saveButton = new MFXButton("Save");
        saveButton
            .disableProperty()
            .bind(fileCanBeSaved.not());

        saveAsMFXGenericDialogContent.addActions(
            Map.entry(
                new MFXButton("Close without saving"), ___ -> {
                    saveAsStageDialog.close();
                }),
            Map.entry(
                saveButton, ___ -> {
                    saveTuringMachineCode();
                    showTuringMachineSavedNotification();
                    saveAsStageDialog.close();
                })
        );

        return saveAsStageDialog;
    }

    private void showTuringMachineSavedNotification() {
        initMFXNotificationSystem();
        final TuringMachineSavedNotification notification = new TuringMachineSavedNotification();
        notification.setContentText(STR."""
        Your Turing Machine has been stored at the following location: \{getTuringMachineCodeAbsolutePath()}. \
        You can now proceed with your next operations or revisit your machine anytime!
        """);
        MFXNotificationSystem.instance()
            .setPosition(NotificationPos.TOP_LEFT)
            .publish(notification);
    }

    private void initMFXNotificationSystem() {
        MFXNotificationSystem.instance().initOwner(stage);
    }

    private void saveTuringMachineCode() {
        final Path turingMachineAbsolutePath = getTuringMachineCodeAbsolutePath();
        try {
            if (!Files.exists(turingMachineAbsolutePath)) {
                Files.createFile(turingMachineAbsolutePath);
            }
            Files.writeString(turingMachineAbsolutePath, turingMachineCode);
        } catch (IOException e) {
            throw new RuntimeException();
        }
        onTuringMachineCodeSavedListeners.forEach(consumer -> consumer.accept(turingMachineAbsolutePath));
    }

    private Path getTuringMachineCodeAbsolutePath() {
        return Path.of(STR."\{turingMachineDirectoryPathTextField.getText()}/\{getTuringMachineCodeFilename()}");
    }

    private String getTuringMachineCodeFilename() {
        String turingMachineCodeFilename = turingMachineFilenameTextField.getText();
        if (!turingMachineCodeFilename.endsWith(".yaml") && !turingMachineCodeFilename.endsWith(".yml")) {
            turingMachineCodeFilename = STR."\{turingMachineCodeFilename}.yaml";
        }
        return turingMachineCodeFilename;
    }

    private MFXStageDialog buildMFXStageDialog(MFXGenericDialog dialogContent) {
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

    private static MFXGenericDialog buildSaveAsMFXGenericDialogContent(final Node content) {
        final MFXGenericDialog dialogContent = MFXGenericDialogBuilder.build()
            .makeScrollable(true)
            .get();
        dialogContent.setShowMinimize(false);
        dialogContent.setShowAlwaysOnTop(false);
        dialogContent.setMaxSize(600, 400);
        final MFXFontIcon floppyDiskIcon = new MFXFontIcon("fas-floppy-disk", 18);
        dialogContent.setHeaderIcon(floppyDiskIcon);
        dialogContent.setHeaderText("Save Turing machine code as...");
        dialogContent.setContent(content);
        dialogContent.getStyleClass().add("mfx-info-dialog-standard");
        return dialogContent;
    }

    private boolean isFileWithOwnerWritePermission(final Path filePath) {
        try {
            return Files
                .getPosixFilePermissions(filePath)
                .contains(PosixFilePermission.OWNER_WRITE);
        } catch (IOException e) {
            return false;
        }
    }

    private static class TuringMachineSavedNotification extends MFXSimpleNotification {
        private final StringProperty contentText = new SimpleStringProperty();

        public TuringMachineSavedNotification() {
            MFXFontIcon mfxFontIcon = new MFXFontIcon();
            IconDescriptor iconDescriptor = FontAwesomeRegular.FLOPPY_DISK;
            mfxFontIcon.setIconsProvider(IconsProviders.FONTAWESOME_REGULAR);
            mfxFontIcon.setDescription(iconDescriptor.getDescription());
            mfxFontIcon.setSize(16);
            MFXIconWrapper mfxIconWrapper = new MFXIconWrapper(mfxFontIcon, 32);

            Label headerLabel = new Label();
            StringProperty headerText = new SimpleStringProperty("Turing machine code saved!");
            headerLabel.textProperty().bind(headerText);

            HBox header = new HBox(10, mfxIconWrapper, headerLabel);
            header.setAlignment(Pos.CENTER_LEFT);
            header.setPadding(InsetsFactory.of(5, 0, 5, 0));
            header.setMaxWidth(Double.MAX_VALUE);

            Label contentLabel = new Label();
            contentLabel.getStyleClass().add("content");
            contentLabel.textProperty().bind(contentText);
            contentLabel.setWrapText(true);
            contentLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            contentLabel.setAlignment(Pos.TOP_LEFT);

            BorderPane container = new BorderPane();
            container.getStyleClass().add("notification");
            container.setTop(header);
            container.setCenter(contentLabel);
            container.getStylesheets()
                .add(getClass()
                        .getResource("/com/github/vtramo/turingmachine/css/Notification.css")
                        .toString());
            container.setMinHeight(200);
            container.setMaxWidth(400);

            setContent(container);
        }

        public void setContentText(String contentText) {
            this.contentText.set(contentText);
        }
    }

    public static class Builder {
        private Stage stage;
        private Pane ownerPaneForDialogs;
        private String turingMachineCode;
        private final List<Consumer<Path>> onTuringMachineCodeSavedListeners = new ArrayList<>();

        public Builder stage(Stage stage) {
            this.stage = stage;
            return this;
        }

        public Builder ownerPaneForDialogs(Pane ownerPaneForDialogs) {
            this.ownerPaneForDialogs = ownerPaneForDialogs;
            return this;
        }

        public Builder turingMachineCode(String turingMachineCode) {
            this.turingMachineCode = turingMachineCode;
            return this;
        }

        public Builder addOnTuringMachineCodeSavedListener(final Consumer<Path> pathConsumer) {
            this.onTuringMachineCodeSavedListeners.add(pathConsumer);
            return this;
        }

        public TuringMachineSaveAsDialog build() {
            TuringMachineSaveAsDialog dialog = new TuringMachineSaveAsDialog(stage, ownerPaneForDialogs, turingMachineCode);
            for (Consumer<Path> listener: onTuringMachineCodeSavedListeners) {
                dialog.addOnTuringMachineCodeSavedListener(listener);
            }
            return dialog;
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}