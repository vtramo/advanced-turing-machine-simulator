package com.github.vtramo.turingmachine.ui;

import javafx.scene.Node;
import javafx.scene.layout.Region;
import javafx.scene.shape.SVGPath;

public abstract class SvgSymbolView extends Region implements SymbolView {

    protected SvgSymbolView() {
        this.setShape(buildSvgPath());
    }

    protected SVGPath buildSvgPath() {
        SVGPath svgPath = new SVGPath();
        svgPath.setId(getSvgPathId());
        svgPath.setContent(getSvgPathContent());
        return svgPath;
    }

    abstract protected String getSvgPathId();

    abstract protected String getSvgPathContent();

    @Override
    public Node getNode() {
        return this;
    }
}
