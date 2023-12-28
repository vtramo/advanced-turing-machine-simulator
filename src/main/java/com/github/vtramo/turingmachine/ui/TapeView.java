package com.github.vtramo.turingmachine.ui;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

import static com.github.vtramo.turingmachine.ui.BlankSymbolView.blank;
import static com.github.vtramo.turingmachine.ui.CursorView.cursor;
import static com.github.vtramo.turingmachine.ui.StartSymbolView.startSymbol;

public class TapeView extends Pane {

    public static final int CELL_WIDTH = 80;
    private final List<CellView> visibleCells = new ArrayList<>();
    private final int cellWindowSize;
    private final int indexPointedCell;

    @Getter
    private final CursorView cursorView;

    @Getter
    private final List<CellView> limitlessTape;
    private int tapeLeftVisibleCellIndex;
    private int tapeRightVisibleCellIndex;
    private int tapeCurrentVisibleCellIndex;

    private final ObservableList<Node> children = getChildren();

    public TapeView(final double width, final double height, final CellView... cells) {
        limitlessTape = new ArrayList<>(cells.length);
        cellWindowSize = (int) (width / CELL_WIDTH);
        tapeLeftVisibleCellIndex = -(cellWindowSize / 2);
        tapeRightVisibleCellIndex = (cellWindowSize / 2) - 1;
        tapeCurrentVisibleCellIndex = 0;

        setWidth(width);
        setHeight(height);

        addInputOnTape(cells);
        final double midX = getWidth() / 2;
        setVisibleCellsPositionFrom(midX);

        addPaddingCellsToTheRight();
        final int totPaddingCellsAddedToTheLeft = addPaddingCellsToTheLeft();
        this.indexPointedCell = totPaddingCellsAddedToTheLeft;

        this.cursorView = createCursorView();
    }

    private void addInputOnTape(final CellView... cells) {
        for (int i = 0; i < cells.length; i++) {
            final CellView cell = cells[i];
            if (i < cellWindowSize / 2) {
                this.visibleCells.add(cell);
                this.children.add(cell);
            }
            limitlessTape.add(cell);
        }
    }

    private void setVisibleCellsPositionFrom(final double fromX) {
        setCellsPositionsFrom(fromX, visibleCells);
    }

    private void setCellsPositionsFrom(final double fromX, final List<CellView> cells) {
        for (int i = 0; i < cells.size(); i++) {
            final CellView cell = cells.get(i);
            final double x = fromX + (i * CELL_WIDTH);
            cell.setLayoutX(x);
        }
    }

    private CursorView createCursorView() {
        final CursorView cursorView = cursor();
        cursorView.setLayoutX((getWidth() / 2) + 22.5);
        cursorView.setLayoutY(70);
        children.add(cursorView);
        return cursorView;
    }

    private int addPaddingCellsToTheRight() {
        int totPaddingCellsAdded = 0;

        CellView lastCell = visibleCells.getLast();
        double lastCellMaxX = lastCell.getBoundsInParent().getMaxX();
        while (lastCellMaxX + CELL_WIDTH < getWidth()) {
            totPaddingCellsAdded++;
            final CellView blank = new CellView(blank());
            addCellInLastPosition(blank, +2);
            limitlessTape.add(blank);
            lastCell = visibleCells.getLast();
            lastCellMaxX = lastCell.getBoundsInParent().getMaxX();
        }

        final CellView blank = new CellView(blank());
        addCellInLastPosition(blank, +2);
        return totPaddingCellsAdded + 1;
    }

    private int addPaddingCellsToTheLeft() {
        int totPaddingCellsAdded = 0;

        CellView firstCell = visibleCells.getFirst();
        double firstCellMinX = firstCell.getBoundsInParent().getMinX();
        while (firstCellMinX - 78 > getMinWidth()) {
            totPaddingCellsAdded++;
            final CellView blank = new CellView(blank());
            addCellInFirstPosition(blank, +2);
            firstCell = visibleCells.getFirst();
            firstCellMinX = firstCell.getBoundsInParent().getMinX();
        }

        final CellView blank = new CellView(blank());
        addCellInFirstPosition(blank, +2);
        return totPaddingCellsAdded + 1;
    }

    public Timeline right(final double ms) {
        final CellView nextRightCell = getNextRightCell();
        removeCellInFirstPosition();
        removeCellInLastPosition();
        addCellInLastPosition(nextRightCell);
        addCellInLastPosition(new CellView(blank()));
        tapeLeftVisibleCellIndex++;
        tapeCurrentVisibleCellIndex++;
        tapeRightVisibleCellIndex++;
        return buildTranslateXTimeline(-CELL_WIDTH, ms);
    }

    private CellView getNextRightCell() {
        if (tapeRightVisibleCellIndex + 1 >= limitlessTape.size()) {
            final CellView blank = new CellView(blank());
            limitlessTape.add(blank);
            return blank;
        } else {
            return limitlessTape.get(tapeRightVisibleCellIndex + 1).clone();
        }
    }

    public Timeline left(final double ms) {
        final CellView nextLeftCell = getNextLeftCell();
        removeCellInLastPosition();
        removeCellInFirstPosition();
        addCellInFirstPosition(nextLeftCell);
        addCellInFirstPosition(new CellView(blank()));
        tapeLeftVisibleCellIndex--;
        tapeCurrentVisibleCellIndex--;
        tapeRightVisibleCellIndex--;
        return buildTranslateXTimeline(+CELL_WIDTH, ms);
    }

    private CellView getNextLeftCell() {
        return tapeLeftVisibleCellIndex - 1 < 0
            ? new CellView(blank())
            : limitlessTape
                .get(tapeLeftVisibleCellIndex - 1)
                .clone();
    }

    private Timeline buildTranslateXTimeline(final double offset, final double ms) {
        final KeyFrame[] keyFrames = visibleCells.stream()
            .map(cell -> new KeyValue(cell.translateXProperty(), cell.getTranslateX() + offset))
            .map(keyValue -> new KeyFrame(Duration.millis(ms), keyValue))
            .toArray(KeyFrame[]::new);
        return new Timeline(keyFrames);
    }

    private void addCellInFirstPosition(final CellView cell) {
        addCellInFirstPosition(cell, 0);
    }

    private void addCellInFirstPosition(final CellView cell, final double additionalOffset) {
        final CellView firstCell = visibleCells.getFirst();
        final double firstCellLayoutX = firstCell.getBoundsInParent().getMinX();
        cell.setLayoutX(firstCellLayoutX - CELL_WIDTH + additionalOffset);
        this.visibleCells.addFirst(cell);
        this.children.addFirst(cell);
    }

    private void removeCellInFirstPosition() {
        final CellView removedCell = visibleCells.removeFirst();
        children.remove(removedCell);
    }

    private void addCellInLastPosition(final CellView cell) {
        addCellInLastPosition(cell, 0);
    }

    private void addCellInLastPosition(final CellView cell, final double additionalOffset) {
        final CellView lastCell = visibleCells.getLast();
        final double lastCellLayoutX = lastCell.getBoundsInParent().getMinX();
        cell.setLayoutX(lastCellLayoutX + CELL_WIDTH + additionalOffset);
        this.visibleCells.addLast(cell);
        this.children.addLast(cell);
    }

    private void removeCellInLastPosition() {
        final CellView removedCell = visibleCells.removeLast();
        children.remove(removedCell);
    }

    public void write(final SymbolView symbol) {
        final CellView pointedCell = visibleCells.get(indexPointedCell);
        pointedCell.write(symbol);
        limitlessTape.set(tapeCurrentVisibleCellIndex, pointedCell);
    }

    public static TapeView buildInputTape(final String input, final double parentWidth, final double parentHeight) {
        final TapeViewBuilder inputTapeBuilder = TapeViewBuilder.builder()
            .withSize(parentWidth, parentHeight)
            .withCell(new CellView(startSymbol()));
        for (int i = 0; i < input.length(); i++) {
            final char symbol = input.charAt(i);
            inputTapeBuilder.withCell(CellView.of(symbol));
        }
        return inputTapeBuilder
            .withCell(CellView.of(blank()))
            .build();
    }

    public static TapeView buildStaticTape(final String input) {
        return buildStaticTape(input, 0);
    }

    public static TapeView buildStaticTape(final String input, final double fromX) {
        final int width = ((input.length() + 2) * CELL_WIDTH);
        final TapeView staticTapeView = buildInputTape(input, width, 1080);
        final List<CellView> allCellViews = staticTapeView.getLimitlessTape();
        final ObservableList<Node> children = staticTapeView.getChildren();
        children.clear();
        children.addAll(allCellViews);
        staticTapeView.setCellsPositionsFrom(fromX, allCellViews);
        return staticTapeView;
    }
}