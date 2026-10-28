package com.comp2042;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.util.Duration;

public class GameController {
    private final GameSession session;
    private final GameView gameView;
    private final Stage stage;

    private KeyCode key = KeyCode.LEFT;
    private KeyCode lastKey = KeyCode.LEFT;
    private boolean keyPressed = false;

    public GameController(Snake snek, Coin coin, Game game, GameView gameView, Obstacle obstacle, Stage stage) {
        this.session = new GameSession(game, snek, coin, obstacle);
        this.gameView = gameView;
        this.stage = stage;
        this.stage.setOnHidden(event -> animation.stop());
        animation.setCycleCount(Animation.INDEFINITE);
        animation.setRate(session.getSpeed());
    }

    private final Timeline animation = new Timeline(new KeyFrame(Duration.seconds(1), event -> advanceGame()));

    private void advanceGame() {
        GameSession.StepResult result = session.advance(toMovement(key));
        Snake snake = session.getSnake();

        if (result == GameSession.StepResult.ALREADY_ENDED) {
            stopAnimation();
            gameView.gameOver(snake);
            return;
        }
        if (result == GameSession.StepResult.COIN_EATEN || result == GameSession.StepResult.WON) {
            animation.setRate(session.getSpeed());
        }
        if (result == GameSession.StepResult.WON) {
            gameView.drawGrid(session.getCoin(), snake, session.getObstacle(), gameView.gc, false, 0);
            gameView.gameWon();
            gameView.updateScoreLabel();
            stopAnimation();
            return;
        }

        keyPressed = false;
        if (!snake.getDead() || snake.hitSelf()) {
            gameView.drawGrid(session.getCoin(), snake, session.getObstacle(), gameView.gc, false, 0);
        }
        gameView.gameWon();
        gameView.gameOver(snake);
        gameView.updateScoreLabel();
        if (snake.getDead()) {
            stopAnimation();
        }
    }

    private static char toMovement(KeyCode direction) {
        return switch (direction) {
            case LEFT -> 'L';
            case RIGHT -> 'R';
            case UP -> 'U';
            case DOWN -> 'D';
            default -> throw new IllegalArgumentException("Not a movement direction: " + direction);
        };
    }

    private void stopAnimation() {
        animation.stop();
    }

    public void handle(KeyEvent event) {
        if (event.getCode().isArrowKey() && !session.isOver()) animation.play();

        if (!session.isOver() && isDirectionAllowed(key, event.getCode()) && !keyPressed) {
            lastKey = key;
            key = event.getCode();
            keyPressed = true;
            gameView.turnDir(session.getSnake(), lastKey, key);
        }

        if (event.getCode().equals(KeyCode.P)) {
            animation.stop();
        }

        if (event.getCode().equals(KeyCode.TAB)) {
            reset();
        }

        if (event.getCode().equals(KeyCode.PLUS)) {
            gameView.scaleToFullscreen(true, stage, session.getObstacle(), session.getSnake(), session.getCoin());
            gameView.gameOverLabel.setStyle("-fx-font-size: 50;");
            gameView.gameWonLabel.setStyle("-fx-font-size: 50;");
            gameView.scoreLabel.setStyle("-fx-font-size: 35;");
        }

        if (event.getCode().equals(KeyCode.MINUS)) {
            gameView.scaleToFullscreen(false, stage, session.getObstacle(), session.getSnake(), session.getCoin());
            gameView.gameOverLabel.setStyle("-fx-font-size: 15;");
            gameView.gameWonLabel.setStyle("-fx-font-size: 15;");
            gameView.scoreLabel.setStyle("-fx-font-size: 15;");
        }

        if (event.getCode().equals(KeyCode.ESCAPE)) {
            animation.stop();
            MainMenuController mainMenuController = new MainMenuController();
            mainMenuController.getViewManager().getPrimaryStage().show();
            mainMenuController.getViewManager().getGameStage().close();
        }
    }

    /**
     * Allows continuing forward or turning, but rejects reversing and non-arrow keys.
     *
     * @param currentDirection the snake's current movement direction
     * @param requestedDirection the key requested by the player
     * @return whether the requested direction is a legal movement direction
     */
    static boolean isDirectionAllowed(KeyCode currentDirection, KeyCode requestedDirection) {
        return switch (requestedDirection) {
            case LEFT -> currentDirection != KeyCode.RIGHT;
            case RIGHT -> currentDirection != KeyCode.LEFT;
            case UP -> currentDirection != KeyCode.DOWN;
            case DOWN -> currentDirection != KeyCode.UP;
            default -> false;
        };
    }

    private void reset() {
        animation.jumpTo(Duration.ZERO);
        animation.stop();
        session.reset();
        animation.setRate(session.getSpeed());
        key = KeyCode.LEFT;
        lastKey = KeyCode.LEFT;
        keyPressed = false;

        gameView.gameWon();
        gameView.gameOver(session.getSnake());
        gameView.updateScoreLabel();
        gameView.drawGrid(session.getCoin(), session.getSnake(), session.getObstacle(), gameView.gc, false, 0);
    }
}
