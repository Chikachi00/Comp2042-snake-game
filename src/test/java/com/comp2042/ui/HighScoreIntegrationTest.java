package com.comp2042.ui;

import com.comp2042.game.*;
import com.comp2042.persistence.HighScoreStore;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.awt.Point;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.function.LongSupplier;

import static org.junit.jupiter.api.Assertions.*;

class HighScoreIntegrationTest extends JavaFxTestSupport {
    @TempDir Path directory;

    @ParameterizedTest
    @EnumSource(GameMode.class)
    void pickupSavesImmediatelyAndRestartRetainsOnlyTheRecord(GameMode mode) throws Exception {
        runOnFxThread(() -> {
            Path file = directory.resolve("scores.properties");
            HighScoreStore scores = new HighScoreStore(file);
            try (Round round = new Round(mode, Level.ONE, scores)) {
                round.eat();
                assertEquals(5, new HighScoreStore(file).getBest(mode));
                assertEquals("Best: 5", round.text("bestScoreLabel"));
                assertEquals(0, scores.getBest(mode == GameMode.ENDLESS ? GameMode.CHALLENGE : GameMode.ENDLESS));
                round.controller.restart();
                assertEquals("Score: 0", round.text("scoreLabel"));
                assertEquals("Best: 5", round.text("bestScoreLabel"));
                round.eat();
                round.die();
                assertFalse(round.text("menuDetails").contains("New high score"), "Tying a record is not a new best");
                round.controller.restart();
                round.eat(); round.eat();
                int expected = mode == GameMode.ENDLESS ? 10 : 12;
                assertEquals(expected, new HighScoreStore(file).getBest(mode));
                round.die();
                assertEquals("Final score: " + expected + "\nBest: " + expected + "\nNew high score!", round.text("menuDetails"));
            }
        });
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void lossAndVictoryShowFinalAndHistoricalScores(boolean won) throws Exception {
        runOnFxThread(() -> {
            HighScoreStore scores = new HighScoreStore(directory.resolve("scores.properties"));
            scores.record(GameMode.CHALLENGE, 500);
            try (Round round = new Round(GameMode.CHALLENGE, Level.THREE, scores)) {
                for (int i = 0; i < (won ? 10 : 1); i++) round.eat();
                if (!won) round.die();
                assertEquals(won ? "You won!" : "Game over", round.text("menuTitle"));
                assertEquals("Final score: " + (won ? 120 : 5) + "\nBest: 500", round.text("menuDetails"));
                assertEquals(500, new HighScoreStore(directory.resolve("scores.properties")).getBest(GameMode.CHALLENGE));
            }
        });
    }

    @Test void challengeRecordUsesTotalPointsAcrossAllLevels() throws Exception {
        runOnFxThread(() -> {
            Path file = directory.resolve("scores.properties");
            try (Round round = new Round(GameMode.CHALLENGE, Level.ONE, new HighScoreStore(file))) {
                for (int i = 0; i < 5; i++) round.eat();
                assertEquals(Level.TWO, round.session.getLevel());
                assertEquals("Best: 45", round.text("bestScoreLabel"));
                for (int i = 0; i < 8; i++) round.eat();
                assertEquals(Level.THREE, round.session.getLevel());
                assertEquals("Best: 135", round.text("bestScoreLabel"));
                for (int i = 0; i < 10; i++) round.eat();
                assertTrue(round.session.isOver());
                assertEquals(255, new HighScoreStore(file).getBest(GameMode.CHALLENGE));
                round.controller.restart();
                assertEquals(Level.ONE, round.session.getLevel());
                assertEquals("Best: 255", round.text("bestScoreLabel"));
                assertEquals("Score: 0", round.text("scoreLabel"));
            }
        });
    }

    @SuppressWarnings("unchecked")
    @Test void realMenuSwitchesRecordsAndRefreshesAfterReturningFromAGame() throws Exception {
        runOnFxThread(() -> {
            Path file = directory.resolve("scores.properties");
            HighScoreStore scores = new HighScoreStore(file);
            scores.record(GameMode.ENDLESS, 125); scores.record(GameMode.CHALLENGE, 240);
            Stage primary = new Stage();
            MenuManager manager = new MenuManager(primary, new HighScoreStore(file));
            try {
                manager.initializeMainMenu();
                assertEquals("Best: 125", label(primary.getScene(), "bestScoreLabel"));
                ComboBox<GameMode> mode = (ComboBox<GameMode>) primary.getScene().lookup("#modeSelector");
                mode.setValue(GameMode.CHALLENGE);
                assertEquals("Best: 240", label(primary.getScene(), "bestScoreLabel"));
                ((Button) primary.getScene().lookup("#btn_start")).fire();
                assertEquals("Best: 240", label(manager.current.getScene(), "bestScoreLabel"));
                manager.getHighScores().record(GameMode.CHALLENGE, 300);
                Event.fireEvent(manager.current.getScene(), key(KeyCode.P));
                ((Button) manager.current.getScene().lookup("#mainMenuButton")).fire();
                assertTrue(primary.isShowing());
                assertEquals("Best: 300", label(primary.getScene(), "bestScoreLabel"));
                mode.setValue(GameMode.ENDLESS);
                assertEquals("Best: 125", label(primary.getScene(), "bestScoreLabel"));
                assertEquals(300, new HighScoreStore(file).getBest(GameMode.CHALLENGE));
            } finally {
                if (manager.current != null) manager.current.close();
                primary.close();
            }
        });
    }

    private static final class Round implements AutoCloseable {
        final Game game = new Game(20, 20, false, 'N');
        final GameSession session;
        final GameView view = new GameView(game);
        final Stage stage = new Stage();
        final StackPane root = new StackPane();
        final Scene scene = new Scene(root, 480, 480);
        final GameController controller;
        final Timeline timer;

        Round(GameMode mode, Level start, HighScoreStore scores) throws Exception {
            Constructor<GameSession> constructor = GameSession.class.getDeclaredConstructor(Game.class, Level.class, LongSupplier.class);
            constructor.setAccessible(true);
            session = mode == GameMode.ENDLESS ? new GameSession(game)
                    : constructor.newInstance(game, start, (LongSupplier) () -> 0L);
            controller = new GameController(session, view, stage, () -> { }, scores);
            view.makeScene(scene, session.getSnake(), session.getCoin());
            view.setMenuActions(controller::resume, controller::restart, controller::returnToMainMenu);
            view.attachTo(root); view.render(session);
            stage.setScene(scene); stage.show();
            timer = (Timeline) field(GameController.class, "animation").get(controller);
        }

        void eat() throws Exception {
            Point head = session.getSnake().getHead();
            putCoin(new Point(head.x - 1, head.y));
            tick();
        }
        void putCoin(Point position) throws Exception {
            Constructor<Coin> constructor = Coin.class.getDeclaredConstructor(Point.class);
            constructor.setAccessible(true);
            field(GameSession.class, "coin").set(session, constructor.newInstance(position));
        }
        void tick() {
            timer.getKeyFrames().get(0).getOnFinished().handle(new ActionEvent());
            timer.stop();
        }
        void die() throws Exception {
            putCoin(new Point(19, 0));
            for (int i = 0; !session.isOver() && i < 21; i++) tick();
            assertTrue(session.getSnake().getDead());
        }
        String text(String id) { return label(scene, id); }
        @Override public void close() { timer.stop(); stage.close(); }
    }

    private static final class MenuManager extends ViewManager {
        Stage current;
        MenuManager(Stage stage, HighScoreStore scores) { super(stage, scores); }
        @Override Stage createGameStage() { current = super.createGameStage(); return current; }
    }
    private static Field field(Class<?> owner, String name) throws Exception {
        Field field = owner.getDeclaredField(name); field.setAccessible(true); return field;
    }
    private static String label(Scene scene, String id) { return ((Label) scene.lookup("#" + id)).getText(); }
    private static KeyEvent key(KeyCode code) { return new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code, false, false, false, false); }
}
