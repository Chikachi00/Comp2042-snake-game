package com.comp2042.ui;

import com.comp2042.game.Coin;
import com.comp2042.game.Game;
import com.comp2042.game.GameSession;
import com.comp2042.game.Level;
import com.comp2042.game.Snake;

import javafx.scene.Scene;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MainMenuController {

    private static final int X_VALUE = 20;
    private static final int Y_VALUE = 20;
    private static final boolean POWER_UP = true;
    private static final char DIFFICULTY = 'N';

    private Scene gameScene;
    @FXML private ComboBox<Level> levelSelector;

    @FXML private void initialize() {
        levelSelector.getItems().setAll(Level.values());
        levelSelector.setValue(Level.ONE);
    }

    private final ViewManager viewManager;

    public MainMenuController(ViewManager viewManager) {
        this.viewManager = viewManager;
    }

    public void handleStart() {
        Stage gameStage = viewManager.createGameStage();
        launchSnake(gameStage);
        viewManager.showGame(gameScene);
    }

    private void launchSnake(Stage gameStage) {
        Game game = new Game(X_VALUE, Y_VALUE, POWER_UP, DIFFICULTY);
        GameSession session = new GameSession(game, levelSelector.getValue());
        Snake snek = session.getSnake();
        Coin coin = session.getCoin();

        GameView gameView = new GameView(game);
        GameController controller = new GameController(session, gameView, gameStage, viewManager::showMainMenu);

        StackPane gamePane = new StackPane();
        gameScene = new Scene(gamePane, game.getWidth() * game.getMultiplier(), game.getHeight() * game.getMultiplier());

        gameScene.setOnKeyPressed(controller::handle);

        gameView.makeScene(gameScene, snek, coin);
        gameView.setMenuActions(controller::resume, controller::restart, controller::returnToMainMenu);
        gameView.attachTo(gamePane);
        gameView.updateProgress(session);
        gameView.drawGrid(session.getCoin(), session.getSnake(), session.getObstacle());
    }

}
