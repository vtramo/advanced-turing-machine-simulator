package com.github.vtramo.turingmachine.ui;

import com.github.vtramo.turingmachine.engine.Configuration;
import io.github.palexdev.materialfx.controls.MFXListView;
import io.github.palexdev.materialfx.controls.cell.MFXListCell;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

public class ConfigurationListView extends MFXListView<Configuration> {
    private static final double ORIGINAL_WIDTH = 438.0;

    private final LatexConfigurationImageGenerator latexConfigurationImageGenerator = new LatexConfigurationImageGenerator();

    private double maxWidth;

    public ConfigurationListView() {
        setCellFactory(configuration -> new MFXListCell<>(this, configuration) {
            static final int WIDTH_OFFSET = 8;

            @Override
            public void updateItem(Configuration item) {
                final Image configurationImage = latexConfigurationImageGenerator.generateLatexImage(item);
                final ImageView configurationImageView = new ImageView(configurationImage);
                final ObservableList<Node> children = getChildren();
                children.clear();
                children.add(configurationImageView);
                final double currentListViewWidth = ConfigurationListView.this.getWidth();
                final double imageWidth = configurationImageView.getLayoutBounds().getWidth();
                maxWidth = Math.max(currentListViewWidth, imageWidth + WIDTH_OFFSET);
                ConfigurationListView.this.setPrefWidth(maxWidth);
            }

            @Override
            protected void updateSelection(MouseEvent event) { /* NO-OP */ }
        });
    }

    public void restoreToOriginalWidth() {
        setPrefWidth(ORIGINAL_WIDTH);
        setWidth(ORIGINAL_WIDTH);
    }
}