package com.comp2042.ui;

import com.comp2042.game.Coin;
import com.comp2042.game.Game;
import com.comp2042.game.GameMode;
import com.comp2042.game.GameSession;
import com.comp2042.game.GameSpeed;
import com.comp2042.game.Level;
import com.comp2042.game.Snake;

import javafx.scene.Scene;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * Presents mode, endless-speed and challenge-level choices and starts the selected game.
 */
public class MainMenuController {

    private static final int X_VALUE = 20;
    private static final int Y_VALUE = 20;
    private static final boolean POWER_UP = true;
    private static final char DIFFICULTY = 'N';

    private Scene gameScene;
    @FXML private ComboBox<GameMode> modeSelector;
    @FXML private ComboBox<Level> levelSelector;
    @FXML private HBox levelOptions;
    @FXML private ComboBox<GameSpeed> speedSelector;
    @FXML private HBox speedOptions;
    @FXML private Label modeDescription;
    @FXML private Label bestScoreLabel;

    /**
     * Defaults to medium-speed endless play and prepares the options for each mode.
     */
    @FXML private void initialize() {
        levelSelector.getItems().setAll(Level.values());
        levelSelector.setValue(Level.ONE);
        speedSelector.getItems().setAll(GameSpeed.values());
        speedSelector.setValue(GameSpeed.MEDIUM);
        modeSelector.getItems().setAll(GameMode.values());
        modeSelector.valueProperty().addListener((observable, previous, selected) -> updateModeOptions());
        modeSelector.setValue(GameMode.ENDLESS);
    }

    /** Shows the selected mode's options while retaining the last speed and starting level. */
    private void updateModeOptions() {
        boolean challenge = modeSelector.getValue() == GameMode.CHALLENGE;
        levelOptions.setVisible(challenge);
        levelOptions.setManaged(challenge);
        levelSelector.setDisable(!challenge);
        speedOptions.setVisible(!challenge);
        speedOptions.setManaged(!challenge);
        speedSelector.setDisable(challenge);
        modeDescription.setText(challenge
                ? "Three levels, combo bonuses and optional key shortcuts."
                : "Classic survival with special coins. No level targets or combos.");
        refreshHighScore();
    }

    /** Refreshes the selected mode's saved record when switching modes or returning to the menu. */
    void refreshHighScore() {
        bestScoreLabel.setText("Best: " + viewManager.getHighScores().getBest(modeSelector.getValue()));
    }

    private final ViewManager viewManager;

    /**
     * Connects the FXML controller to window navigation.
     *
     * @param viewManager manager responsible for menu and game windows
     */
    public MainMenuController(ViewManager viewManager) {
        this.viewManager = viewManager;
    }

    /**
     * Starts the chosen endless speed or challenge starting level in a new game window.
     */
    public void handleStart() {
        Stage gameStage = viewManager.createGameStage();
        launchSnake(gameStage);
        viewManager.showGame(gameScene);
    }

    /**
     * Builds the session, controller, view and button/key wiring for the game scene.
     *
     * @param gameStage stage that will host the new game
     */
    private void launchSnake(Stage gameStage) {
        boolean challenge = modeSelector.getValue() == GameMode.CHALLENGE;
        Game game = challenge ? new Game(X_VALUE, Y_VALUE, POWER_UP, DIFFICULTY)
                : new Game(X_VALUE, Y_VALUE, POWER_UP, speedSelector.getValue());
        GameSession session = challenge
                ? new GameSession(game, levelSelector.getValue()) : new GameSession(game);
        Snake snek = session.getSnake();
        Coin coin = session.getCoin();

        GameView gameView = new GameView(game);
        GameController controller = new GameController(session, gameView, gameStage,
                viewManager::showMainMenu, viewManager.getHighScores());

        StackPane gamePane = new StackPane();
        gameScene = new Scene(gamePane, 480, 480);

        // Capture game shortcuts before focused controls consume keys for navigation.
        gameScene.addEventFilter(KeyEvent.KEY_PRESSED, controller::handle);
        gameScene.addEventFilter(KeyEvent.KEY_RELEASED, controller::handle);

        gameView.makeScene(gameScene, snek, coin);
        gameView.setMenuActions(controller::resume, controller::restart, controller::returnToMainMenu);
        gameView.attachTo(gamePane);
        gameView.updateProgress(session);
        gameView.render(session);
    }

}
