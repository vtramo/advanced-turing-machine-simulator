package com.github.vtramo.turingmachine;

import fr.brouillard.oss.cssfx.CSSFX;
import io.github.palexdev.materialfx.theming.JavaFXThemes;
import io.github.palexdev.materialfx.theming.MaterialFXStylesheets;
import io.github.palexdev.materialfx.theming.UserAgentBuilder;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;

public class TuringMachineApplication extends Application {
    @Override
    public void start(final Stage primaryStage) throws IOException {
        CSSFX.start();

        UserAgentBuilder.builder()
            .themes(JavaFXThemes.MODENA)
            .themes(MaterialFXStylesheets.forAssemble(true))
            .setDeploy(true)
            .setResolveAssets(true)
            .build()
            .setGlobal();

        final FXMLLoader fxmlLoader = new FXMLLoader(TuringMachineApplication.class.getResource("home.fxml"));
        fxmlLoader.setControllerFactory(c -> new HomeController(primaryStage));
        final Parent parent = fxmlLoader.load();

        primaryStage.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            final KeyCode key = event.getCode();
            final int keyCode = key.getCode();
            if (keyCode == 27) primaryStage.close();
        });

        final Scene scene = new Scene(parent);
        primaryStage.setTitle("Turing Machine");
        primaryStage.setFullScreen(false);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch();
    }

    public static String readResourceAsString(final String resourcePath) {
        try (final InputStream resourceAsStream = TuringMachineApplication.class.getResourceAsStream(resourcePath)) {
            return new String(resourceAsStream.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static InputStream getResourceAsInputStream(final String resourcePath) {
        return TuringMachineApplication.class.getResourceAsStream(resourcePath);
    }
}