module com.github.vtramo.mdtturingmachine {
    requires javafx.controls;
    requires javafx.fxml;
    requires MaterialFX;
    requires javafxsvg;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.dataformat.yaml;
    requires org.fxmisc.richtext;
    requires org.fxmisc.flowless;
    requires reactfx;
    requires org.leadpony.justify;
    requires org.snakeyaml.engine.v2;
    requires jlatexmath;
    requires fr.brouillard.oss.cssfx;
    requires static lombok;

    exports com.github.vtramo.turingmachine;
    exports com.github.vtramo.turingmachine.parser;
    exports com.github.vtramo.turingmachine.engine;
    exports com.github.vtramo.turingmachine.engine.exception;
    opens com.github.vtramo.turingmachine to javafx.fxml;
    opens com.github.vtramo.turingmachine.ui to javafx.fxml;
    opens com.github.vtramo.turingmachine.ui.dialogs to javafx.fxml;
}