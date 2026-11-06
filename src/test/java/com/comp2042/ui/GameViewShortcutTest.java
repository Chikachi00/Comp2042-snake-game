package com.comp2042.ui;

import com.comp2042.game.*;
import javafx.geometry.Bounds;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import java.awt.Point;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongSupplier;
import static org.junit.jupiter.api.Assertions.*;

class GameViewShortcutTest extends JavaFxTestSupport {
    @Test void hudDoesNotCoverBoardAndCountdownAndGateReflectTheCurrentSession() throws Exception {
        runOnFxThread(() -> {
            AtomicLong clock = new AtomicLong();
            Constructor<GameSession> constructor = GameSession.class.getDeclaredConstructor(Game.class, Level.class, LongSupplier.class);
            constructor.setAccessible(true);
            Game game = new Game(20, 20, false, 'N');
            GameSession session = constructor.newInstance(game, Level.TWO, (LongSupplier) clock::get);
            GameView view = new GameView(game); StackPane root = new StackPane(); Scene scene = new Scene(root, 480, 480);
            view.makeScene(scene, session.getSnake(), session.getCoin()); view.attachTo(root); view.render(session);
            Stage stage = new Stage(); stage.setScene(scene);
            try {
                stage.show(); root.applyCss(); root.layout();
                Canvas canvas = (Canvas) root.lookup("#gridCanvas");
                Bounds board = canvas.localToScene(canvas.getBoundsInLocal());
                Bounds hud = root.lookup("#gameHud").localToScene(root.lookup("#gameHud").getBoundsInLocal());
                Bounds footer = root.lookup("#gameFooter").localToScene(root.lookup("#gameFooter").getBoundsInLocal());
                assertEquals(400, canvas.getWidth()); assertEquals(400, canvas.getHeight());
                assertTrue(hud.getMaxY() <= board.getMinY() + .01);
                assertTrue(footer.getMinY() >= board.getMaxY() - .01);
                view.showPauseMenu(); root.applyCss(); root.layout();
                assertEquals("Main Menu", ((javafx.scene.text.Text) root.lookup("#mainMenuButton").lookup(".text")).getText());
                view.hideMenu();
                putCoin(session, new Point(9, 10)); session.advance(Direction.LEFT); putCoin(session, new Point(0, 0));
                clock.set(2_000_000_000L); view.render(session);
                assertEquals("A: Key ready", ((Label) root.lookup("#shortcutStatus")).getText());
                assertEquals("3.0s", ((Label) root.lookup("#comboTime")).getText());
                assertEquals(.6, ((ProgressBar) root.lookup("#comboBar")).getProgress(), .0001);
                for (int i=0; i<4; i++) session.advance(Direction.DOWN);
                for (int i=0; i<6; i++) session.advance(Direction.RIGHT);
                view.render(session);
                assertEquals("A: Open", ((Label) root.lookup("#shortcutStatus")).getText());
                assertEquals("Shortcut A opened", ((Label) root.lookup("#shortcutNotice")).getText());
                assertEquals(0xfffeffe4, canvas.snapshot(null, null).getPixelReader().getArgb(210, 130));
                session.pause(); clock.set(102_000_000_000L); view.render(session);
                assertEquals("3.0s", ((Label) root.lookup("#comboTime")).getText());
                session.reset(); view.render(session);
                assertEquals("A: Locked", ((Label) root.lookup("#shortcutStatus")).getText());
                assertFalse(root.lookup("#comboBar").isVisible());
            } finally { stage.close(); }
        });
    }

    private static void putCoin(GameSession session, Point position) throws Exception {
        Constructor<Coin> constructor = Coin.class.getDeclaredConstructor(Point.class); constructor.setAccessible(true);
        Field field = GameSession.class.getDeclaredField("coin"); field.setAccessible(true); field.set(session, constructor.newInstance(position));
    }
}
