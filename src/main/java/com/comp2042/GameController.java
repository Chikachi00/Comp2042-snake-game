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

    private Direction direction = Direction.LEFT;
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
        GameSession.StepResult result = session.advance(direction);
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
            gameView.drawGrid(session.getCoin(), snake, session.getObstacle());
            gameView.gameWon();
            gameView.updateScoreLabel();
            stopAnimation();
            return;
        }

        keyPressed = false;
        if (!snake.getDead() || snake.hitSelf()) {
            gameView.drawGrid(session.getCoin(), snake, session.getObstacle());
        }
        gameView.gameWon();
        gameView.gameOver(snake);
        gameView.updateScoreLabel();
        if (snake.getDead()) {
            stopAnimation();
        }
    }

    static Direction directionForKey(KeyCode key) {
        return switch (key) {
            case LEFT -> Direction.LEFT;
            case RIGHT -> Direction.RIGHT;
            case UP -> Direction.UP;
            case DOWN -> Direction.DOWN;
            default -> null;
        };
    }

    private void stopAnimation() {
        animation.stop();
    }

    public void handle(KeyEvent event) {
        if (event.getCode().isArrowKey() && !session.isOver()) animation.play();

        Direction requested = directionForKey(event.getCode());
        if (!session.isOver() && requested != null && !direction.isOpposite(requested) && !keyPressed) {
            Direction previous = direction;
            direction = requested;
            keyPressed = true;
            gameView.turnDir(session.getSnake(), previous, direction);
        }

        if (event.getCode().equals(KeyCode.P)) {
            animation.stop();
        }

        if (event.getCode().equals(KeyCode.TAB)) {
            reset();
        }

        if (event.getCode().equals(KeyCode.PLUS)) {
            gameView.scaleToFullscreen(true, stage, session.getObstacle(), session.getSnake(), session.getCoin());
        }

        if (event.getCode().equals(KeyCode.MINUS)) {
            gameView.scaleToFullscreen(false, stage, session.getObstacle(), session.getSnake(), session.getCoin());
        }

        if (event.getCode().equals(KeyCode.ESCAPE)) {
            animation.stop();
            MainMenuController mainMenuController = new MainMenuController();
            mainMenuController.getViewManager().getPrimaryStage().show();
            mainMenuController.getViewManager().getGameStage().close();
        }
    }

    private void reset() {
        animation.jumpTo(Duration.ZERO);
        animation.stop();
        session.reset();
        animation.setRate(session.getSpeed());
        direction = Direction.LEFT;
        keyPressed = false;

        gameView.gameWon();
        gameView.gameOver(session.getSnake());
        gameView.updateScoreLabel();
        gameView.drawGrid(session.getCoin(), session.getSnake(), session.getObstacle());
    }
}
