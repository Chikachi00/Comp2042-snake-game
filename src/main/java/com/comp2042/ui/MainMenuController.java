package com.comp2042.ui;

import com.comp2042.game.Coin;
import com.comp2042.game.Game;
import com.comp2042.game.GameMode;
import com.comp2042.game.GameSession;
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
 * Presents mode and level choices and constructs a game from the FXML Start action.
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
    @FXML private Label modeDescription;
    @FXML private Label bestScoreLabel;

    /**
     * Defaults to classic endless play and shows level selection only for a challenge.
     */
    @FXML private void initialize() {
        levelSelector.getItems().setAll(Level.values());
        levelSelector.setValue(Level.ONE);
        modeSelector.getItems().setAll(GameMode.values());
        modeSelector.valueProperty().addListener((observable, previous, selected) -> updateModeOptions());
        modeSelector.setValue(GameMode.ENDLESS);
    }

    /** Updates mode guidance while preserving the last challenge starting-level selection. */
    private void updateModeOptions() {
        boolean challenge = modeSelector.getValue() == GameMode.CHALLENGE;
        levelOptions.setVisible(challenge);
        levelOptions.setManaged(challenge);
        levelSelector.setDisable(!challenge);
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
     * Creates a game window using the selected mode and, for a challenge, starting level.
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
        Game game = new Game(X_VALUE, Y_VALUE, POWER_UP, DIFFICULTY);
        GameSession session = modeSelector.getValue() == GameMode.CHALLENGE
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

        gameView.makeScene(gameScene, snek, coin);
        gameView.setMenuActions(controller::resume, controller::restart, controller::returnToMainMenu);
        gameView.attachTo(gamePane);
        gameView.updateProgress(session);
        gameView.render(session);
    }

}
