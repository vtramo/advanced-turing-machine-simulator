package com.github.vtramo.turingmachine.ui;

import com.github.vtramo.turingmachine.engine.Symbol;

public class StartSymbolView extends SvgSymbolView {
    protected static final String svgPathId = "E2-MJAMS-22B3";
    protected static final String svgPathContent = "M83 523Q83 524 85 527T92 535T103 539Q107 539 389 406T680 268Q694 260 694 249Q694 239 687 234Q685 232 395 95L107 -41H101Q90 -40 83 -26V523ZM376 368Q323 393 254 425T155 472L125 487Q123 487 123 249T125 11Q127 12 252 71T502 190L626 249L376 368Z";

    public StartSymbolView() {
        super();
        this.setMinSize(30, 30);
        this.setMaxSize(30, 30);
        this.setPrefSize(30, 30);
        this.setStyle("-fx-background-color: black;");
    }

    public static StartSymbolView startSymbol() {
        return new StartSymbolView();
    }

    @Override
    protected String getSvgPathId() {
        return svgPathId;
    }

    @Override
    protected String getSvgPathContent() {
        return svgPathContent;
    }

    @Override
    public char getChar() {
        return Symbol.START.getSymbol();
    }
}
