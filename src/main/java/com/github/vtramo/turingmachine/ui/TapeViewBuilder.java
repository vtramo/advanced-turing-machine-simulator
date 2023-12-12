package com.github.vtramo.turingmachine.ui;

import java.util.ArrayList;
import java.util.List;

public class TapeViewBuilder {

    private final List<CellView> cells = new ArrayList<>();
    private double width;
    private double height;

    public TapeViewBuilder withCell(final CellView cell) {
        cells.add(cell);
        return this;
    }

    public TapeViewBuilder withSize(final double width, final double height) {
        this.width = width;
        this.height = height;
        return this;
    }

    public TapeView build() {
        return new TapeView(width, height, cells.toArray(new CellView[0]));
    }

    public static TapeViewBuilder builder() {
        return new TapeViewBuilder();
    }
}
