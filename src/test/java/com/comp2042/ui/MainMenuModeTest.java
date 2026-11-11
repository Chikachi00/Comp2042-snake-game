package com.comp2042.ui;

import com.comp2042.game.GameMode;
import com.comp2042.game.GameSpeed;
import com.comp2042.game.Level;
import com.comp2042.persistence.HighScoreStore;
import javafx.event.Event;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

class MainMenuModeTest extends JavaFxTestSupport {
    @ParameterizedTest
    @EnumSource(value = KeyCode.class, names = {"P", "ESCAPE"})
    void realGameSceneCapturesShortcutReleasesEvenWhenAButtonHasFocus(KeyCode code) throws Exception {
        runOnFxThread(() -> {
            try (Menu menu = new Menu()) {
                menu.start();
                Scene scene = menu.gameScene();
                Event.fireEvent(scene, new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code,
                        false, false, false, false));
                Button resume = menu.gameButton("resumeButton");
                resume.requestFocus();
                Event.fireEvent(resume, new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code,
                        false, false, false, false));
                assertTrue(scene.lookup("#gameMenu").isVisible());
                Event.fireEvent(resume, new KeyEvent(KeyEvent.KEY_RELEASED, "", "", code,
                        false, false, false, false));
                Event.fireEvent(resume, new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code,
                        false, false, false, false));
                assertFalse(scene.lookup("#gameMenu").isVisible());
                Event.fireEvent(scene, new KeyEvent(KeyEvent.KEY_RELEASED, "", "", code,
                        false, false, false, false));
            }
        });
    }

    @Test
    void hidesLevelChoicesInEndlessAndRetainsChallengeSelectionWhenSwitchingModes() throws Exception {
        runOnFxThread(() -> {
            try (Menu menu = new Menu()) {
                assertEquals(GameMode.ENDLESS, menu.mode().getValue());
                assertArrayEquals(GameMode.values(), menu.mode().getItems().toArray());
                assertFalse(menu.scene().lookup("#levelOptions").isVisible());
                assertFalse(menu.scene().lookup("#levelOptions").isManaged());
                assertTrue(menu.level().isDisabled());
                assertTrue(menu.text("modeDescription").contains("No level targets"));
                assertEquals(GameSpeed.MEDIUM, menu.speed().getValue());
                assertArrayEquals(GameSpeed.values(), menu.speed().getItems().toArray());
                assertTrue(menu.scene().lookup("#speedOptions").isVisible());
                assertTrue(menu.scene().lookup("#speedOptions").isManaged());
                assertFalse(menu.speed().isDisabled());
                menu.speed().setValue(GameSpeed.FAST);

                menu.mode().setValue(GameMode.CHALLENGE);
                assertTrue(menu.scene().lookup("#levelOptions").isVisible());
                assertTrue(menu.scene().lookup("#levelOptions").isManaged());
                assertFalse(menu.level().isDisabled());
                assertFalse(menu.scene().lookup("#speedOptions").isVisible());
                assertFalse(menu.scene().lookup("#speedOptions").isManaged());
                assertTrue(menu.speed().isDisabled());
                assertArrayEquals(Level.values(), menu.level().getItems().toArray());
                assertEquals(Level.ONE, menu.level().getValue());
                menu.level().setValue(Level.THREE);
                menu.mode().setValue(GameMode.ENDLESS);
                assertEquals(GameSpeed.FAST, menu.speed().getValue());
                menu.mode().setValue(GameMode.CHALLENGE);
                assertEquals(Level.THREE, menu.level().getValue());
            }
        });
    }

    @Test
    void navigatingBetweenModesStartsFreshGamesAndKeepsEndlessMenusWorking() throws Exception {
        runOnFxThread(() -> {
            try (Menu menu = new Menu()) {
                menu.start();
                assertEquals("Endless | 8.5 moves/s", menu.gameText("progressLabel"));
                assertEquals("Classic survival", menu.gameText("comboLabel"));
                assertEquals("", menu.gameText("shortcutStatus"));
                assertFalse(menu.gameScene().lookup("#comboBar").isVisible());
                menu.pause();
                assertEquals("Paused", menu.gameText("menuTitle"));
                menu.gameButton("restartButton").fire();
                assertEquals("Endless | 8.5 moves/s", menu.gameText("progressLabel"));
                assertEquals("Score: 0", menu.gameText("scoreLabel"));
                assertTrue(menu.gameScene().lookup("#startHint").isVisible());
                menu.pause();
                menu.back();

                menu.mode().setValue(GameMode.CHALLENGE);
                menu.level().setValue(Level.THREE);
                menu.start();
                assertEquals("Level 3  |  Coins: 0/10", menu.gameText("progressLabel"));
                assertEquals("A: Locked  |  B: Locked", menu.gameText("shortcutStatus"));
                menu.pause();
                menu.back();

                menu.mode().setValue(GameMode.ENDLESS);
                menu.start();
                assertEquals("Endless | 8.5 moves/s", menu.gameText("progressLabel"));
                assertEquals("Score: 0", menu.gameText("scoreLabel"));
                assertEquals("", menu.gameText("shortcutStatus"));
                assertEquals("Collect coins. Avoid walls and your own tail.", menu.gameText("ruleHint"));
            }
        });
    }

    @ParameterizedTest
    @EnumSource(Level.class)
    void challengeStartsAndRestartsAtEachSelectedLevel(Level level) throws Exception {
        runOnFxThread(() -> {
            try (Menu menu = new Menu()) {
                menu.mode().setValue(GameMode.CHALLENGE);
                menu.level().setValue(level);
                menu.start();
                String expected = level + "  |  Coins: 0/" + level.target();
                assertEquals(expected, menu.gameText("progressLabel"));
                menu.pause();
                menu.gameButton("restartButton").fire();
                assertEquals(expected, menu.gameText("progressLabel"));
                assertEquals("Score: 0", menu.gameText("scoreLabel"));
                menu.pause();
                menu.back();
                assertEquals(GameMode.CHALLENGE, menu.mode().getValue());
                assertEquals(level, menu.level().getValue());
            }
        });
    }

    @ParameterizedTest
    @EnumSource(GameSpeed.class)
    void endlessUsesTheChosenSpeedAndRetainsItAcrossRestartAndMenuNavigation(GameSpeed speed)
            throws Exception {
        runOnFxThread(() -> {
            try (Menu menu = new Menu()) {
                menu.speed().setValue(speed);
                menu.scene().getRoot().applyCss();
                menu.scene().getRoot().layout();
                assertTrue(menu.scene().lookup("#speedOptions").localToScene(
                        menu.scene().lookup("#speedOptions").getBoundsInLocal()).getMaxY()
                        < menu.scene().lookup("#bestScoreLabel").getBoundsInParent().getMinY(),
                        "Speed choices must fit above the saved-record label");
                menu.start();
                String expected = "Endless | " + speed.movesPerSecond() + " moves/s";
                assertEquals(expected, menu.gameText("progressLabel"));
                menu.pause();
                menu.gameButton("restartButton").fire();
                assertEquals(expected, menu.gameText("progressLabel"));
                assertEquals("Score: 0", menu.gameText("scoreLabel"));
                menu.pause();
                menu.back();
                assertEquals(speed, menu.speed().getValue());
                menu.mode().setValue(GameMode.CHALLENGE);
                menu.mode().setValue(GameMode.ENDLESS);
                assertEquals(speed, menu.speed().getValue());
                menu.start();
                assertEquals(expected, menu.gameText("progressLabel"));
            }
        });
    }

    private static final class Menu implements AutoCloseable {
        final Stage primary = new Stage();
        final TestViewManager manager = new TestViewManager(primary);

        Menu() {
            manager.initializeMainMenu();
            scene().getRoot().applyCss();
            scene().getRoot().layout();
        }

        Scene scene() { return primary.getScene(); }
        Scene gameScene() { return manager.current.getScene(); }
        @SuppressWarnings("unchecked")
        ComboBox<GameMode> mode() { return (ComboBox<GameMode>) scene().lookup("#modeSelector"); }
        @SuppressWarnings("unchecked")
        ComboBox<Level> level() { return (ComboBox<Level>) scene().lookup("#levelSelector"); }
        @SuppressWarnings("unchecked")
        ComboBox<GameSpeed> speed() { return (ComboBox<GameSpeed>) scene().lookup("#speedSelector"); }
        String text(String id) { return ((Label) scene().lookup("#" + id)).getText(); }
        String gameText(String id) { return ((Label) gameScene().lookup("#" + id)).getText(); }
        Button gameButton(String id) { return (Button) gameScene().lookup("#" + id); }
        void start() { ((Button) scene().lookup("#btn_start")).fire(); }
        void pause() {
            Event.fireEvent(gameScene(), new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.P,
                    false, false, false, false));
            Event.fireEvent(gameScene(), new KeyEvent(KeyEvent.KEY_RELEASED, "", "", KeyCode.P,
                    false, false, false, false));
        }
        void back() {
            Stage previous = manager.current;
            gameButton("mainMenuButton").fire();
            assertFalse(previous.isShowing());
            assertTrue(primary.isShowing());
        }
        @Override public void close() {
            if (manager.current != null) manager.current.close();
            primary.close();
        }
    }

    private static final class TestViewManager extends ViewManager {
        Stage current;
        TestViewManager(Stage stage) { super(stage, HighScoreStore.inMemory()); }
        @Override Stage createGameStage() { current = super.createGameStage(); return current; }
    }
}
