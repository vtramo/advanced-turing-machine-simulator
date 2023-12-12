package com.github.vtramo.turingmachine;

import com.github.vtramo.turingmachine.engine.Configuration;
import com.github.vtramo.turingmachine.engine.Direction;
import com.github.vtramo.turingmachine.engine.Move;
import com.github.vtramo.turingmachine.engine.Transition;
import com.github.vtramo.turingmachine.ui.SymbolView;
import com.github.vtramo.turingmachine.ui.TapeView;
import javafx.animation.KeyFrame;
import javafx.animation.ParallelTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class TapeViewController {
    private static final long DELAY_BEFORE_NEXT_MOVE_MS = 100L;
    private final List<TapeView> tapeViews = new ArrayList<>();

    public TapeViewController(final TapeView... tapeViews) {
        this.tapeViews.addAll(Arrays.stream(tapeViews).toList());
    }

    public CompletableFuture<Void> step(final Transition transition, long delayMs) {
        final CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        final List<Move> moves = transition.moves();
        final Timeline[] timelines = new Timeline[moves.size()];

        for (int i = 0; i < moves.size(); i++) {
            final TapeView tape = tapeViews.get(i);
            final Move move = moves.get(i);
            final Direction direction = move.direction();

            final char symbol = move.symbol();
            tape.write(SymbolView.of(symbol));

            switch (direction) {
                case RIGHT -> timelines[i] = tape.right(delayMs);
                case LEFT -> timelines[i] = tape.left(delayMs);
                case STAY -> timelines[i] = new Timeline(new KeyFrame(Duration.millis(delayMs)));
            }
        }

        startParallelTransition(timelines, () -> Platform.runLater(() -> completableFuture.complete(null)));

        return completableFuture;
    }

    public CompletableFuture<Void> stepBack(
        final Configuration previousConfiguration,
        final Transition lastTransition,
        final long delayMs
    ) {
        final CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        final char[] previousSymbols = previousConfiguration.currentSymbols();
        final List<Move> moves = lastTransition.moves();
        final Timeline[] timelines = new Timeline[moves.size()];

        for (int i = 0; i < moves.size(); i++) {
            final TapeView tapeView = tapeViews.get(i);
            final Move move = moves.get(i);
            final Direction direction = move.direction();
            final Direction inverseDirection = direction.inverse();

            switch (inverseDirection) {
                case RIGHT -> timelines[i] = tapeView.right(delayMs);
                case LEFT -> timelines[i] = tapeView.left(delayMs);
                case STAY -> timelines[i] = new Timeline(new KeyFrame(Duration.millis(delayMs)));
            }
        }

        startParallelTransition(timelines, () -> Platform.runLater(() -> {
            for (int i = 0; i < tapeViews.size(); i++) {
                final char symbol = previousSymbols[i];
                final TapeView tapeView = tapeViews.get(i);
                tapeView.write(SymbolView.of(symbol));
            }
            completableFuture.complete(null);
        }));

        return completableFuture;
    }

    private static void startParallelTransition(final Timeline[] timelines, final Runnable onFinish) {
        final javafx.animation.Transition parallelTransition = new ParallelTransition(timelines);

        parallelTransition.setOnFinished(__ ->
            Thread.ofVirtual().start(() -> {
                    try {
                        Thread.sleep(DELAY_BEFORE_NEXT_MOVE_MS);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    onFinish.run();
                }
            ));

        parallelTransition.play();
    }
}
