package com.comp2042.ui;

import com.comp2042.game.Coin;
import com.comp2042.game.Direction;
import com.comp2042.game.Game;
import com.comp2042.game.GameSession;
import com.comp2042.game.GameSpeed;
import com.comp2042.game.Level;
import com.comp2042.game.RainbowCoin;
import javafx.animation.Animation;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import java.awt.Point;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Exercises level transitions through the actual JavaFX timer callback. */
class GameControllerTimingTest extends JavaFxTestSupport {
    @ParameterizedTest(name = "{0}: last coin rainbow={1}")
    @CsvSource({"ONE, false", "ONE, true", "TWO, false", "TWO, true"})
    void nextLevelRestoresTimerSpeedAfterRainbow(Level startingLevel, boolean rainbowLast) throws Exception {
        runOnFxThread(() -> {
            Game game = new Game(20, 20, false, 'N');
            GameSession session = new GameSession(game, startingLevel);
            GameView view = new GameView(game);
            view.makeScene(new Scene(new StackPane(), 400, 400), session.getSnake(), session.getCoin());
            Stage stage = new Stage();
            GameController controller = new GameController(session, view, stage, () -> { });
            Timeline timer = (Timeline) field(GameController.class, "animation").get(controller);
            try {
                controller.handle(leftKey());
                int target = startingLevel.target();
                for (int eaten = 1; eaten <= target; eaten++) {
                    Point head = session.getSnake().getHead();
                    boolean rainbow = eaten == target - 1 || (eaten == target && rainbowLast);
                    putCoin(session, new Point(head.x - 1, head.y), rainbow);
                    tick(timer);
                    if (eaten == target - 1) {
                        assertEquals(game.getSpeed() + 5, timer.getRate(), "Rainbow should accelerate the timer");
                    }
                }

                assertEquals(startingLevel.next(), session.getLevel());
                assertEquals(0, session.getLevelCoins());
                assertEquals(new Point(10, 10), session.getSnake().getHead());
                assertEquals(Animation.Status.STOPPED, timer.getStatus());
                assertEquals(game.getSpeed(), session.getSpeed());
                assertEquals(game.getSpeed(), timer.getRate(), "Next level must clear the previous timer rate");

                putCoin(session, new Point(0, 0), false);
                controller.handle(leftKey());
                tick(timer);
                assertEquals(Animation.Status.RUNNING, timer.getStatus());
                assertEquals(new Point(9, 10), session.getSnake().getHead());
                assertEquals(Direction.LEFT, session.getSnake().getDirection());
                assertEquals(game.getSpeed(), timer.getRate(), "New level must start at its base speed");
            } finally {
                timer.stop();
                stage.close();
            }
        });
    }

    @ParameterizedTest
    @EnumSource(GameSpeed.class)
    void endlessTimerKeepsSelectedSpeedThroughEffectsPauseAndRestart(GameSpeed speed) throws Exception {
        runOnFxThread(() -> {
            Game game = new Game(20, 20, true, speed);
            GameSession session = new GameSession(game);
            GameView view = new GameView(game);
            view.makeScene(new Scene(new StackPane(), 480, 480), session.getSnake(), session.getCoin());
            Stage stage = new Stage();
            GameController controller = new GameController(session, view, stage, () -> { });
            Timeline timer = (Timeline) field(GameController.class, "animation").get(controller);
            try {
                controller.handle(leftKey());
                assertEquals(speed.movesPerSecond(), timer.getRate());
                putCoin(session, new Point(9, 10), true);
                tick(timer);
                assertEquals(speed.movesPerSecond() + 5, timer.getRate());
                controller.pause();
                assertEquals(Animation.Status.PAUSED, timer.getStatus());
                controller.resume();
                assertEquals(Animation.Status.RUNNING, timer.getStatus());
                assertEquals(speed.movesPerSecond() + 5, timer.getRate());

                putCoin(session, new Point(8, 10), false);
                tick(timer);
                assertEquals(speed.movesPerSecond(), timer.getRate());
                putCoin(session, new Point(7, 10), true);
                tick(timer);
                assertEquals(speed.movesPerSecond() + 5, timer.getRate());
                controller.restart();
                assertEquals(speed.movesPerSecond(), timer.getRate());
                assertEquals(0, session.getScorePoints());
                assertEquals(Animation.Status.STOPPED, timer.getStatus());
            } finally {
                timer.stop();
                stage.close();
            }
        });
    }

    private static void putCoin(GameSession session, Point position, boolean rainbow) throws Exception {
        Class<? extends Coin> type = rainbow ? RainbowCoin.class : Coin.class;
        Constructor<? extends Coin> constructor = type.getDeclaredConstructor(Point.class);
        constructor.setAccessible(true);
        field(GameSession.class, "coin").set(session, constructor.newInstance(position));
    }

    private static Field field(Class<?> type, String name) throws Exception {
        Field field = type.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }

    private static KeyEvent leftKey() {
        return new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.LEFT, false, false, false, false);
    }

    private static void tick(Timeline timer) {
        timer.getKeyFrames().get(0).getOnFinished().handle(new ActionEvent());
    }
}
