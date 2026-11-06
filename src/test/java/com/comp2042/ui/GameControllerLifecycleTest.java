package com.comp2042.ui;

import com.comp2042.game.Coin;
import com.comp2042.game.Game;
import com.comp2042.game.GameSession;
import com.comp2042.game.Level;
import com.comp2042.game.RainbowCoin;
import com.comp2042.game.Snake;
import javafx.animation.Animation;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.awt.Point;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongSupplier;

import static org.junit.jupiter.api.Assertions.*;

/** Exercises keyboard/menu actions together with the real session and Timeline. */
class GameControllerLifecycleTest extends JavaFxTestSupport {
    private static final long SECOND = 1_000_000_000L;

    @ParameterizedTest
    @EnumSource(value = KeyCode.class, names = {"P", "ESCAPE"})
    void resumingBeforeFirstMoveStillWaitsForAnArrow(KeyCode pauseKey) throws Exception {
        runOnFxThread(() -> {
            try (Round round = new Round(Level.ONE)) {
                round.key(pauseKey);
                round.assertPauseMenu();
                round.key(KeyCode.UP);
                assertEquals(new Point(10, 10), round.session.getSnake().getHead());
                assertNotEquals(Animation.Status.RUNNING, round.timer.getStatus());

                round.key(pauseKey);
                assertFalse(round.node("gameMenu").isVisible());
                assertTrue(round.node("startHint").isVisible());
                assertNotEquals(Animation.Status.RUNNING, round.timer.getStatus());
                round.key(KeyCode.LEFT);
                assertEquals(Animation.Status.RUNNING, round.timer.getStatus());
                assertFalse(round.node("startHint").isVisible());
                round.putCoin(new Point(0, 0), false);
                round.tick();
                assertEquals(new Point(9, 10), round.session.getSnake().getHead());
            }
        });
    }

    @ParameterizedTest(name = "extra active time after five seconds: {0} ns")
    @ValueSource(longs = {0, 1})
    void pauseFreezesMovementAndComboUntilResume(long beyondBoundary) throws Exception {
        runOnFxThread(() -> {
            try (Round round = new Round(Level.ONE)) {
                round.key(KeyCode.LEFT);
                round.eat(false);
                assertEquals(5, round.game.getScorePoints());
                round.clock.set(3 * SECOND);
                round.key(KeyCode.P);
                round.assertPauseMenu();
                assertEquals(Animation.Status.PAUSED, round.timer.getStatus());
                Point pausedHead = round.session.getSnake().getHead();
                List<Point> pausedBody = round.session.getSnake().getBody();
                round.clock.set(103 * SECOND);
                round.key(KeyCode.UP);
                assertEquals(pausedHead, round.session.getSnake().getHead());
                assertEquals(pausedBody, round.session.getSnake().getBody());
                assertEquals(5, round.game.getScorePoints());
                assertEquals(1, round.session.getComboCount());

                round.button("resumeButton").fire();
                assertEquals(Animation.Status.RUNNING, round.timer.getStatus());
                assertFalse(round.node("gameMenu").isVisible());
                round.clock.set(105 * SECOND + beyondBoundary);
                round.eat(false);
                assertEquals(new Point(8, 10), round.session.getSnake().getHead(),
                        "An arrow pressed while paused must not change the resumed direction");
                assertEquals(beyondBoundary == 0 ? 12 : 10, round.game.getScorePoints());
                assertEquals(beyondBoundary == 0 ? 2 : 1, round.session.getComboCount());
                assertEquals(beyondBoundary == 0 ? 2 : 0, round.session.getComboBonus());
                assertEquals(beyondBoundary == 0 ? "Combo x2 (+2)" : "Combo x1 (+0)",
                        round.label("comboLabel").getText());
                assertEquals("Score: " + round.game.getScorePoints(), round.label("scoreLabel").getText());
            }
        });
    }

    @ParameterizedTest(name = "start={0}, restart using button={1}")
    @CsvSource({"ONE, true", "ONE, false", "TWO, true", "TWO, false", "THREE, true", "THREE, false"})
    void restartingWhilePausedRestoresTheSelectedRun(Level startingLevel, boolean useButton) throws Exception {
        runOnFxThread(() -> {
            try (Round round = new Round(startingLevel)) {
                List<Point> startingObstacles = round.session.getObstacle().getObstacles();
                round.key(KeyCode.LEFT);
                if (startingLevel.next() != null) {
                    for (int i = 0; i < startingLevel.target(); i++) round.eat(false);
                    assertEquals(startingLevel.next(), round.session.getLevel());
                    round.key(KeyCode.LEFT);
                }
                round.eat(true);
                assertEquals(round.game.getSpeed() + 5, round.timer.getRate());
                assertTrue(round.game.getScorePoints() > 0);
                assertTrue(round.session.getComboCount() > 0);
                Snake oldSnake = round.session.getSnake();
                round.key(KeyCode.ESCAPE);
                round.assertPauseMenu();
                round.clock.addAndGet(100 * SECOND);

                if (useButton) round.button("restartButton").fire();
                else round.key(KeyCode.TAB);

                assertNotSame(oldSnake, round.session.getSnake());
                assertEquals(startingLevel, round.session.getLevel());
                assertEquals(startingObstacles, round.session.getObstacle().getObstacles());
                round.assertReadyWithNoScore();
                assertFalse(round.session.getObstacle().getObstacles().contains(round.session.getCoin().getPos()));
                assertFalse(round.session.getSnake().getBody().contains(round.session.getCoin().getPos()));
                assertNotEquals(round.session.getSnake().getHead(), round.session.getCoin().getPos());
                assertEquals(startingLevel + "  |  Coins: 0/" + startingLevel.target(),
                        round.label("progressLabel").getText());
                round.controller.resume();
                assertEquals(Animation.Status.STOPPED, round.timer.getStatus());

                // Restart must also clear the previous pause and direction/keypress state.
                round.clock.addAndGet(SECOND);
                round.key(KeyCode.UP);
                round.putCoin(new Point(0, 0), false);
                round.tick();
                assertEquals(new Point(10, 9), round.session.getSnake().getHead());
                assertEquals(Animation.Status.RUNNING, round.timer.getStatus());
                assertEquals(round.game.getSpeed(), round.timer.getRate());
                assertEquals(0, round.session.getComboCount());
            }
        });
    }

    @ParameterizedTest(name = "won={0}")
    @ValueSource(booleans = {false, true})
    void endMenuDisplaysTheResultAndCanRestart(boolean won) throws Exception {
        runOnFxThread(() -> {
            try (Round round = new Round(Level.THREE)) {
                round.finish(won);
                assertTrue(round.node("gameMenu").isVisible());
                assertEquals(won ? "You won!" : "Game over", ((Label) round.menuChild(0)).getText());
                assertEquals("Final score: " + (won ? 120 : 5), ((Label) round.menuChild(1)).getText());
                assertFalse(round.button("resumeButton").isVisible());
                assertFalse(round.button("resumeButton").isManaged());
                assertTrue(round.button("restartButton").isVisible());
                assertTrue(round.button("mainMenuButton").isVisible());
                assertEquals(Animation.Status.STOPPED, round.timer.getStatus());
                assertEquals(won, round.game.gameWon());
                assertEquals(!won, round.session.getSnake().getDead());
                Point endedHead = round.session.getSnake().getHead();
                round.key(KeyCode.P);
                round.key(KeyCode.ESCAPE);
                round.key(KeyCode.UP);
                assertEquals(endedHead, round.session.getSnake().getHead());
                assertEquals(Animation.Status.STOPPED, round.timer.getStatus());
                assertTrue(round.session.isOver());

                round.button("restartButton").fire();
                assertEquals(Level.THREE, round.session.getLevel());
                round.assertReadyWithNoScore();
                assertFalse(round.node("gameOverLabel").isVisible());
                assertFalse(round.node("gameWonLabel").isVisible());
            }
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {"paused", "lost", "won"})
    void mainMenuButtonStopsTheTimerAndCallsNavigation(String state) throws Exception {
        runOnFxThread(() -> {
            try (Round round = new Round(Level.THREE)) {
                if (state.equals("paused")) {
                    round.key(KeyCode.LEFT);
                    round.key(KeyCode.P);
                    round.assertPauseMenu();
                } else {
                    round.finish(state.equals("won"));
                }
                round.button("mainMenuButton").fire();
                assertEquals(1, round.mainMenuVisits.get());
                assertEquals(Animation.Status.STOPPED, round.timer.getStatus());
            }
        });
    }

    @ParameterizedTest(name = "Tab from {1} while {0}")
    @CsvSource({
            "paused, resumeButton",
            "paused, restartButton",
            "paused, mainMenuButton",
            "lost, restartButton",
            "lost, mainMenuButton",
            "won, restartButton",
            "won, mainMenuButton"
    })
    void tabRestartsTheRunWhenAMenuButtonHasFocus(String state, String buttonId) throws Exception {
        runOnFxThread(() -> {
            try (Round round = new Round(Level.THREE)) {
                round.stage.setScene(round.scene);
                round.stage.show();
                if (state.equals("paused")) {
                    round.key(KeyCode.LEFT);
                    round.eat(true);
                    round.key(KeyCode.P);
                    round.assertPauseMenu();
                } else {
                    round.finish(state.equals("won"));
                }
                Snake oldSnake = round.session.getSnake();
                Button focusedButton = round.button(buttonId);
                round.root.applyCss();
                round.root.layout();
                focusedButton.requestFocus();
                assertSame(focusedButton, round.scene.getFocusOwner());

                // Dispatch through the focused control, including its default focus-traversal behavior.
                Event.fireEvent(focusedButton, new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.TAB,
                        false, false, false, false));

                assertNotSame(oldSnake, round.session.getSnake(), "Tab must reach the restart action");
                assertEquals(Level.THREE, round.session.getLevel());
                assertEquals(0, round.mainMenuVisits.get());
                round.assertReadyWithNoScore();
                round.key(KeyCode.UP);
                round.putCoin(new Point(0, 0), false);
                round.tick();
                assertEquals(new Point(10, 9), round.session.getSnake().getHead());
                assertEquals(Animation.Status.RUNNING, round.timer.getStatus());
            }
        });
    }

    private static final class Round implements AutoCloseable {
        final AtomicLong clock = new AtomicLong();
        final AtomicInteger mainMenuVisits = new AtomicInteger();
        final Game game = new Game(20, 20, false, 'N');
        final GameSession session;
        final Stage stage = new Stage();
        final StackPane root = new StackPane();
        final Scene scene = new Scene(root, 400, 400);
        final GameController controller;
        final Timeline timer;

        Round(Level startingLevel) throws Exception {
            Constructor<GameSession> constructor = GameSession.class.getDeclaredConstructor(
                    Game.class, Level.class, LongSupplier.class);
            constructor.setAccessible(true);
            session = constructor.newInstance(game, startingLevel, (LongSupplier) clock::get);
            GameView view = new GameView(game);
            view.makeScene(scene, session.getSnake(), session.getCoin());
            controller = new GameController(session, view, stage, mainMenuVisits::incrementAndGet);
            scene.addEventFilter(KeyEvent.KEY_PRESSED, controller::handle);
            view.setMenuActions(controller::resume, controller::restart, controller::returnToMainMenu);
            view.attachTo(root);
            view.updateProgress(session);
            timer = (Timeline) field(GameController.class, "animation").get(controller);
        }

        void key(KeyCode code) {
            Event.fireEvent(scene.getFocusOwner() == null ? scene : scene.getFocusOwner(),
                    new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code,
                    false, false, false, false));
        }

        void tick() {
            timer.getKeyFrames().get(0).getOnFinished().handle(new ActionEvent());
        }

        void eat(boolean rainbow) throws Exception {
            Point head = session.getSnake().getHead();
            putCoin(new Point(head.x - 1, head.y), rainbow);
            tick();
        }

        void putCoin(Point point, boolean rainbow) throws Exception {
            Class<? extends Coin> type = rainbow ? RainbowCoin.class : Coin.class;
            Constructor<? extends Coin> constructor = type.getDeclaredConstructor(Point.class);
            constructor.setAccessible(true);
            field(GameSession.class, "coin").set(session, constructor.newInstance(point));
        }

        void finish(boolean won) throws Exception {
            key(KeyCode.LEFT);
            if (won) {
                for (int i = 0; i < Level.THREE.target(); i++) eat(false);
            } else {
                eat(false);
                for (int i = 0; i < 10; i++) {
                    putCoin(new Point(0, 0), false);
                    tick();
                }
            }
            assertTrue(session.isOver());
        }

        void assertReadyWithNoScore() {
            assertFalse(session.isOver());
            assertEquals(new Point(10, 10), session.getSnake().getHead());
            assertEquals(1, session.getSnake().getBody().size());
            assertEquals(0, game.getScorePoints());
            assertEquals(0, session.getLevelCoins());
            assertEquals(0, session.getComboCount());
            assertEquals(0, session.getComboBonus());
            assertEquals(game.getSpeed(), session.getSpeed());
            assertEquals(game.getSpeed(), timer.getRate());
            assertEquals(Animation.Status.STOPPED, timer.getStatus());
            assertFalse(node("gameMenu").isVisible());
            assertTrue(node("startHint").isVisible());
            assertEquals("Score: 0", label("scoreLabel").getText());
            assertEquals("", label("comboLabel").getText());
        }

        void assertPauseMenu() {
            assertTrue(node("gameMenu").isVisible());
            assertTrue(button("resumeButton").isVisible());
            assertTrue(button("resumeButton").isManaged());
            assertEquals("Paused", ((Label) menuChild(0)).getText());
        }

        Node node(String id) { return root.lookup("#" + id); }
        Button button(String id) { return (Button) node(id); }
        Label label(String id) { return (Label) node(id); }
        Node menuChild(int index) { return ((VBox) node("gameMenu")).getChildren().get(index); }

        @Override
        public void close() {
            timer.stop();
            stage.close();
        }
    }

    private static Field field(Class<?> owner, String name) throws Exception {
        Field field = owner.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }
}
