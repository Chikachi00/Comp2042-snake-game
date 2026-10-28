package com.comp2042.ui;

import com.comp2042.game.Coin;
import com.comp2042.game.Game;
import com.comp2042.game.Obstacle;
import com.comp2042.game.Snake;

import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MainMenuController {

    private static final int X_VALUE = 20;
    private static final int Y_VALUE = 20;
    private static final boolean POWER_UP = true;
    private static final char DIFFICULTY = 'N';

    private Scene gameScene;

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
        Obstacle obstacle = new Obstacle(game.getWidth(), game.getHeight());
        Snake snek = new Snake(game.getWidth(), game.getHeight(), game.isNoBorder(), obstacle);
        Coin coin = new Coin(game.getWidth(), game.getHeight(), snek, obstacle);

        GameView gameView = new GameView(game);
        GameController controller = new GameController(snek, coin, game, gameView, obstacle, gameStage, viewManager::showMainMenu);

        StackPane gamePane = new StackPane();
        gameScene = new Scene(gamePane, game.getWidth() * game.getMultiplier(), game.getHeight() * game.getMultiplier());

        gameScene.setOnKeyPressed(controller::handle);

        gameView.makeScene(gameScene, snek, coin);
        gameView.attachTo(gamePane);
    }

}
