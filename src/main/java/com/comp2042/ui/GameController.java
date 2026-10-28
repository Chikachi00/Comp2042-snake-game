package com.comp2042.ui;

import com.comp2042.game.Coin;
import com.comp2042.game.Direction;
import com.comp2042.game.Game;
import com.comp2042.game.GameSession;
import com.comp2042.game.Obstacle;
import com.comp2042.game.Snake;

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
    private final Runnable returnToMenu;

    private Direction direction = Direction.LEFT;
    private boolean keyPressed = false;
    private final GameFlow flow = new GameFlow();

    public GameController(Snake snek, Coin coin, Game game, GameView gameView,
                          Obstacle obstacle, Stage stage, Runnable returnToMenu) {
        this.session = new GameSession(game, snek, coin, obstacle);
        this.gameView = gameView;
        this.stage = stage;
        this.returnToMenu = java.util.Objects.requireNonNull(returnToMenu, "returnToMenu");
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
            flow.end();
            gameView.showEndMenu(true);
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
            flow.end();
            gameView.showEndMenu(false);
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
        KeyCode code = event.getCode();
        if (code == KeyCode.TAB) {
            restart();
            event.consume();
            return;
        }
        if (code == KeyCode.P || code == KeyCode.ESCAPE) {
            if (flow.isPaused()) resume();
            else if (!session.isOver()) pause();
            event.consume();
            return;
        }
        if (flow.isPaused() || session.isOver()) return;

        Direction requested = directionForKey(code);
        if (requested != null) {
            flow.start();
            animation.play();
            if (!direction.isOpposite(requested) && !keyPressed) {
                Direction previous = direction;
                direction = requested;
                keyPressed = true;
                gameView.turnDir(session.getSnake(), previous, direction);
            }
        }
        if (code == KeyCode.PLUS) {
            gameView.scaleToFullscreen(true, stage, session.getObstacle(), session.getSnake(), session.getCoin());
        } else if (code == KeyCode.MINUS) {
            gameView.scaleToFullscreen(false, stage, session.getObstacle(), session.getSnake(), session.getCoin());
        }
    }

    public void pause() {
        if (!flow.pause()) return;
        animation.pause();
        gameView.showPauseMenu();
    }

    public void resume() {
        if (!flow.isPaused()) return;
        boolean wasRunning = flow.resume();
        gameView.hideMenu();
        if (wasRunning) animation.play();
    }

    public void restart() {
        reset();
    }

    public void returnToMainMenu() {
        animation.stop();
        returnToMenu.run();
    }

    private void reset() {
        animation.jumpTo(Duration.ZERO);
        animation.stop();
        session.reset();
        flow.reset();
        gameView.hideMenu();
        gameView.resetTurns();
        animation.setRate(session.getSpeed());
        direction = Direction.LEFT;
        keyPressed = false;

        gameView.gameWon();
        gameView.gameOver(session.getSnake());
        gameView.updateScoreLabel();
        gameView.drawGrid(session.getCoin(), session.getSnake(), session.getObstacle());
    }
}
