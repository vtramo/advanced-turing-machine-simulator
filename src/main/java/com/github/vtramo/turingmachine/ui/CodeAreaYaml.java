package com.github.vtramo.turingmachine.ui;

import com.github.vtramo.turingmachine.parser.TuringMachineValidatorYaml;
import com.github.vtramo.turingmachine.parser.ValidationMessage;
import com.github.vtramo.turingmachine.parser.ValidationResult;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Point2D;
import javafx.scene.control.Label;
import javafx.stage.Popup;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.Caret;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.event.MouseOverTextEvent;
import org.fxmisc.richtext.model.Paragraph;
import org.fxmisc.richtext.model.StyleSpan;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.reactfx.collection.LiveList;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class CodeAreaYaml extends VirtualizedScrollPane<CodeArea> {

    private static final double LINE_WIDTH = 22.80D;
    private static final String KEYWORD_YAML_STYLE = "keyword";
    private static final String STRING_YAML_STYLE = "string";
    private static final String ERROR_YAML_STYLE = "error";

    private static final String KEYWORD_PATTERN = "\\S*:";
    private static final String STRING_PATTERN = "('.*')|(\".*\")|(\\|((\\n|.*)  +.*)+)";
    private static final String KEYWORD_GROUP = "KEYWORD";
    private static final Pattern PATTERN = Pattern.compile("(?<" + KEYWORD_GROUP + ">" + KEYWORD_PATTERN + ")|(?<STRING>" + STRING_PATTERN + ")");

    private final CodeArea codeArea;
    private final Popup popup = new Popup();
    private final Label popupMessage = buildPopupMessage();
    private final Map<Integer, String> errorMessageByCharacterIndex = new HashMap<>();
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor(runnable -> {
        final Thread thread = Executors.defaultThreadFactory().newThread(runnable);
        thread.setDaemon(true);
        return thread;
    });

    public CodeAreaYaml() {
        super(new CodeArea());
        codeArea = getContent();

        getStyleClass().add("mfx-scroll-pane");
        codeArea.setStyle("-fx-font-family: 'Source Code Pro Medium'; -fx-font-size: 12pt;");

        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        codeArea.setShowCaret(Caret.CaretVisibility.ON);
        addTextChangeListener();
        addMouseOverTextListener();

        heightProperty()
            .addListener(__
                -> addPaddingLines());
    }

    public void appendText(final String text) {
        codeArea.appendText(text);
    }

    public String getText() {
        return codeArea.getText();
    }

    private void addPaddingLines() {
        final LiveList<Paragraph<Collection<String>, String, Collection<String>>> paragraphs = codeArea.getParagraphs();
        final double codeAreaHeight = getHeight();
        int totalParagraphs = paragraphs.size();
        while (totalParagraphs * LINE_WIDTH < codeAreaHeight) {
            appendText("\n");
            totalParagraphs = paragraphs.size();
        }
        moveToZeroLine();
    }

    private void moveToZeroLine() {
        codeArea.moveTo(0);
        codeArea.requestFollowCaret();
    }

    private void addTextChangeListener() {
        final ObservableValue<String> stringObservableValue = codeArea.textProperty();
        stringObservableValue.addListener((observable, oldText, newText) -> {
            errorMessageByCharacterIndex.clear();
            codeArea.setStyleSpans(0, computeHighlighting(newText));
            executorService.schedule(() ->
                Platform.runLater(() -> computeValidation(codeArea.getText())),
                    500, TimeUnit.MILLISECONDS);
        });

    }

    private void addMouseOverTextListener() {
        codeArea.setMouseOverTextDelay(Duration.ofMillis(500));

        addEventHandler(MouseOverTextEvent.MOUSE_OVER_TEXT_BEGIN, e -> {
            final int characterIndex = e.getCharacterIndex();
            final Point2D position = e.getScreenPosition();
            if (errorMessageByCharacterIndex.containsKey(characterIndex)) {
                final String errorMsg = errorMessageByCharacterIndex.get(characterIndex);
                popupMessage.setText(errorMsg);
                popup.show(this, position.getX(), position.getY() + 10);
            }
        });

        addEventHandler(MouseOverTextEvent.MOUSE_OVER_TEXT_END, e -> {
            popup.hide();
        });
    }

    private void computeValidation(final String text) {
        final TuringMachineValidatorYaml turingMachineValidatorYaml = new TuringMachineValidatorYaml();
        final ValidationResult validationResult = turingMachineValidatorYaml.validate(text);

        if (validationResult.containsErrors()) {
            final List<ValidationMessage> validationMessages = validationResult.validationMessages();
            for (final ValidationMessage validationMessage: validationMessages) {
                final int paragraphIndex = validationMessage.line();
                final int start = codeArea.getAbsolutePosition(paragraphIndex, 0);
                final int paragraphLength = codeArea.getParagraphLength(paragraphIndex);
                assignErrorMessageToIntRange(start, start + paragraphLength, validationMessage.succinctMessage());
                final int offset = Math.max(validationMessage.offset(), paragraphLength);
                final int end = start + offset;
                final StyleSpan<Collection<String>> styleSpan = new StyleSpan<>(Collections.singleton(ERROR_YAML_STYLE), end - start);
                codeArea.setStyleSpans(paragraphIndex, 0, StyleSpans.singleton(styleSpan));
            }
        }
    }

    private StyleSpans<? extends Collection<String>> computeHighlighting(final String yamlText) {
        int lastKwEnd = 0;
        final StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        final Matcher matcher = PATTERN.matcher(yamlText);

        while (matcher.find()) {
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
            final boolean isKeywordMatch = Optional.ofNullable(matcher.group(KEYWORD_GROUP)).isPresent();

            final String styleClass = isKeywordMatch ? KEYWORD_YAML_STYLE : STRING_YAML_STYLE;
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());

            lastKwEnd = matcher.end();
        }

        spansBuilder.add(Collections.emptyList(), yamlText.length() - lastKwEnd);
        return spansBuilder.create();
    }

    private void assignErrorMessageToIntRange(final int from, final int to, final String errorMsg) {
        IntStream
            .iterate(from, i -> i <= to, i -> i + 1)
            .forEach(i -> errorMessageByCharacterIndex.put(i, errorMsg));
    }

    private Label buildPopupMessage() {
        final Label popupMsg = new Label();
        popupMsg.setStyle(
            "-fx-background-color: black;" +
            "-fx-text-fill: white;" +
            "-fx-padding: 5;"
        );
        popup.getContent().add(popupMsg);
        return popupMsg;
    }
}