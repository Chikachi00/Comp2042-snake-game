package com.comp2042.ui;

import com.comp2042.game.Coin;
import com.comp2042.game.Direction;
import com.comp2042.game.Game;
import com.comp2042.game.GameSession;
import com.comp2042.game.Obstacle;
import com.comp2042.game.Snake;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.awt.Point;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Checks the rendered sprites, including straight, corner and tail segments. */
class GameViewScalingTest extends JavaFxTestSupport {
    @ParameterizedTest(name = "scale={0}, corner={1}")
    @CsvSource({"1.5, false", "2.0, false", "1.5, true", "2.0, true"})
    void snakeSpritesScaleWithTheBoard(double scale, boolean corner) throws Exception {
        runOnFxThread(() -> {
            Board board = board(corner);
            int[] normal = snakeBounds(board.canvas.snapshot(null, null));

            // Use fixed scales so the regression does not depend on the test monitor.
            field("fullScale").setDouble(board.view, scale);
            board.canvas.setWidth(400 * scale);
            board.canvas.setHeight(400 * scale);
            board.view.drawGrid(board.coin, board.snake, board.obstacle);
            int[] enlarged = snakeBounds(board.canvas.snapshot(null, null));

            assertEquals(normal[0] * scale, enlarged[0], 2, "Snake width should follow the board scale");
            assertEquals(normal[1] * scale, enlarged[1], 2, "Snake height should follow the board scale");
        });
    }

    @Test
    void repeatedFullscreenAndWindowedReturnPreserveTheBoard() throws Exception {
        runOnFxThread(() -> {
            Board board = board(true);
            WritableImage normal = board.canvas.snapshot(null, null);
            board.view.resizeForFullscreen(true, board.obstacle, board.snake, board.coin);
            double fullscreenWidth = board.canvas.getWidth();
            double fullscreenHeight = board.canvas.getHeight();
            WritableImage fullscreen = board.canvas.snapshot(null, null);
            board.view.resizeForFullscreen(true, board.obstacle, board.snake, board.coin);
            assertEquals(fullscreenWidth, board.canvas.getWidth());
            assertEquals(fullscreenHeight, board.canvas.getHeight());
            assertSamePixels(fullscreen, board.canvas.snapshot(null, null));

            board.view.resizeForFullscreen(false, board.obstacle, board.snake, board.coin);
            assertEquals(400, board.canvas.getWidth());
            assertEquals(400, board.canvas.getHeight());
            assertSamePixels(normal, board.canvas.snapshot(null, null));
        });
    }

    @ParameterizedTest(name = "native fullscreen exit while {0}")
    @ValueSource(strings = {"ready", "paused", "restarted"})
    void nativeFullscreenExitRestoresTheCurrentBoard(String state) throws Exception {
        runOnFxThread(() -> {
            Board board = board(true);
            Stage stage = new Stage();
            GameSession session = new GameSession(board.game, board.snake, board.coin, board.obstacle);
            GameController controller = new GameController(session, board.view, stage, () -> { });
            StackPane root = new StackPane();
            board.view.attachTo(root);
            stage.setScene(new Scene(root, 480, 480));
            try {
                stage.show();
                WritableImage normal = board.canvas.snapshot(null, null);
                controller.handle(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.PLUS,
                        false, false, false, false));
                assertTrue(stage.isFullScreen());
                assertTrue(board.canvas.getWidth() > 400, "The canvas must be enlarged before exiting");
                if (state.equals("paused")) controller.pause();
                if (state.equals("restarted")) controller.restart();

                // Native exit changes the stage property without calling the view's resize helper.
                stage.setFullScreen(false);
                assertEquals(400, board.canvas.getWidth());
                assertEquals(400, board.canvas.getHeight());
                assertEquals(1, field("fullScale").getDouble(board.view));
                assertEquals("-fx-font-size: 20;", root.lookup("#scoreLabel").getStyle());
                WritableImage restored = board.canvas.snapshot(null, null);
                if (!state.equals("restarted")) assertSamePixels(normal, restored);
                board.view.drawGrid(session.getCoin(), session.getSnake(), session.getObstacle());
                assertSamePixels(board.canvas.snapshot(null, null), restored);
            } finally {
                stage.close();
            }
        });
    }

    private static Board board(boolean corner) throws Exception {
        Game game = new Game(20, 20, false, 'N');
        Obstacle obstacle = new Obstacle(20, 20);
        Snake snake = new Snake(20, 20, false, obstacle);
        grow(snake, Direction.LEFT);
        grow(snake, Direction.LEFT);
        Coin coin = coinAt(new Point(0, 0));
        GameView view = new GameView(game);
        view.makeScene(new Scene(new StackPane(), 400, 400), snake, coin);
        if (corner) {
            view.turnDir(snake, Direction.LEFT, Direction.UP);
            grow(snake, Direction.UP);
        }
        view.drawGrid(coin, snake, obstacle);
        return new Board(game, view, (Canvas) field("gridCanvas").get(view), snake, coin, obstacle);
    }

    private static void grow(Snake snake, Direction direction) throws Exception {
        snake.move(direction);
        assertTrue(snake.ateCoin(coinAt(snake.getHead())));
    }

    private static Coin coinAt(Point position) throws Exception {
        Constructor<Coin> constructor = Coin.class.getDeclaredConstructor(Point.class);
        constructor.setAccessible(true);
        return constructor.newInstance(position);
    }

    /** Measures the green sprite pixels, excluding the board and red coin. */
    private static int[] snakeBounds(WritableImage image) {
        int minX = (int) image.getWidth();
        int minY = (int) image.getHeight();
        int maxX = -1;
        int maxY = -1;
        PixelReader pixels = image.getPixelReader();
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int argb = pixels.getArgb(x, y);
                int red = (argb >>> 16) & 255;
                int green = (argb >>> 8) & 255;
                int blue = argb & 255;
                if (green > 40 && green > red * 1.3 && green > blue * 1.3) {
                    minX = Math.min(minX, x);
                    minY = Math.min(minY, y);
                    maxX = Math.max(maxX, x);
                    maxY = Math.max(maxY, y);
                }
            }
        }
        assertTrue(maxX >= minX && maxY >= minY, "The snake must be visible");
        return new int[] {maxX - minX + 1, maxY - minY + 1};
    }

    private static void assertSamePixels(WritableImage expected, WritableImage actual) {
        assertEquals(expected.getWidth(), actual.getWidth());
        assertEquals(expected.getHeight(), actual.getHeight());
        PixelReader expectedPixels = expected.getPixelReader();
        PixelReader actualPixels = actual.getPixelReader();
        for (int y = 0; y < expected.getHeight(); y++) {
            for (int x = 0; x < expected.getWidth(); x++) {
                if (expectedPixels.getArgb(x, y) != actualPixels.getArgb(x, y)) {
                    assertEquals(expectedPixels.getArgb(x, y), actualPixels.getArgb(x, y),
                            "Board changed at " + x + "," + y);
                }
            }
        }
    }

    private static Field field(String name) throws Exception {
        Field field = GameView.class.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }

    private record Board(Game game, GameView view, Canvas canvas, Snake snake, Coin coin, Obstacle obstacle) { }
}
