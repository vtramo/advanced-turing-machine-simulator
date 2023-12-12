package com.github.vtramo.turingmachine.ui;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;

import static javafx.scene.paint.Color.BLACK;
import static javafx.scene.paint.Color.TRANSPARENT;

public class CellView extends StackPane {

    private final ObservableList<Node> children = getChildren();

    private SymbolView symbolView;

    public CellView(final SymbolView symbolView) {
        this.symbolView = symbolView;
        buildCell();
        write(symbolView);
    }

    public CellView(final char symbol) {
        this(SymbolView.of(symbol));
    }

    private void buildCell() {
        Rectangle cell = new Rectangle();
        cell.setHeight(75);
        cell.setWidth(75);
        cell.setStrokeType(StrokeType.OUTSIDE);
        cell.setFill(TRANSPARENT);
        cell.setStroke(BLACK);
        cell.setStrokeWidth(2D);
        children.add(cell);
    }

    public void write(final SymbolView symbolView) {
        final Node symbolNode = symbolView.getNode();
        if (children.size() == 2) children.removeLast();
        children.add(symbolNode);
        this.symbolView = symbolView;
    }

    @Override
    public CellView clone() {
        return new CellView(SymbolView.of(symbolView.getChar()));
    }

    public static CellView of(final char symbol) {
        return new CellView(symbol);
    }

    public static CellView of(final SymbolView symbolView) {
        return new CellView(symbolView);
    }
}