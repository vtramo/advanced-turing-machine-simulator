package com.github.vtramo.turingmachine;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import lombok.SneakyThrows;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;

public class HomeController {
    private static final String MDT_TAB_TEMPLATE_FXML = "turing-machine-tab-template.fxml";
    private static final Path MDT_PALINDROME_PATH = Path.of("src/main/resources/mdt-palindrome-six-tapes.yaml");

    @FXML
    private TabPane tabPane;
    @FXML
    private Tab addTabButton;

    private final Stage primaryStage;

    public HomeController(final Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    @SneakyThrows
    public void initialize() {
        addListenerToNewTabButton();
        createMdtTab("Palindrome", MDT_PALINDROME_PATH);
    }

    private void addListenerToNewTabButton() {
        final SingleSelectionModel<Tab> singleSelectionModelTab = tabPane.getSelectionModel();
        final ReadOnlyObjectProperty<Tab> readOnlyObjectPropertyTab = singleSelectionModelTab.selectedItemProperty();
        readOnlyObjectPropertyTab.addListener((__, ___, newSelectedTab) -> {
            if (newSelectedTab != addTabButton) return;
            final String mdtTabTitle = buildNextTabTitle();
            createMdtTab(mdtTabTitle, MDT_PALINDROME_PATH);
        });
    }

    private String buildNextTabTitle() {
        final ObservableList<Tab> tabs = tabPane.getTabs();
        return "MDT " + (tabs.size());
    }

    @SneakyThrows
    private Tab createMdtTab(final String title, final Path programYamlPath) {
        final String programYaml = readMdtYamlDefinition(programYamlPath);
        final FXMLLoader fxmlLoader = new FXMLLoader(TuringMachineApplication.class.getResource(MDT_TAB_TEMPLATE_FXML));
        fxmlLoader.setController(new TuringMachineTabController(primaryStage, programYaml));
        final Tab tab = fxmlLoader.load();
        tab.setText(title);
        setTabClosablePolicy(tab);
        addTab(tab);
        selectTab(tab);
        return tab;
    }

    private static String readMdtYamlDefinition(final Path programYamlPath) throws IOException {
        try (final FileInputStream fileInputStream = new FileInputStream(programYamlPath.toFile())) {
            return new String(fileInputStream.readAllBytes());
        }
    }

    private void setTabClosablePolicy(final Tab tab) {
        final ObservableList<Tab> tabs = tabPane.getTabs();
        final BooleanProperty tabBooleanProperty = tab.closableProperty();
        tabBooleanProperty.bind(Bindings.size(tabs).greaterThan(2));
    }

    private void addTab(Tab tab) {
        final ObservableList<Tab> tabs = tabPane.getTabs();
        tabs.add(tabs.size() - 1, tab);
    }

    private void selectTab(Tab tab) {
        final SingleSelectionModel<Tab> singleSelectionModelTab = tabPane.getSelectionModel();
        singleSelectionModelTab.select(tab);
    }
}