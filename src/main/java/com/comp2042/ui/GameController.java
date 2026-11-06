package com.comp2042.ui;

import com.comp2042.game.Coin;
import com.comp2042.game.Direction;
import com.comp2042.game.Game;
import com.comp2042.game.GameSession;
import com.comp2042.game.Obstacle;
import com.comp2042.game.Snake;
import com.comp2042.persistence.HighScoreStore;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Coordinates keyboard and menu input with the session, JavaFX timer and view.
 */
public class GameController {
    private final GameSession session;
    private final GameView gameView;
    private final Stage stage;
    private final Runnable returnToMenu;
    private final HighScoreStore highScores;
    private boolean newHighScore;

    private Direction direction = Direction.LEFT;
    private boolean keyPressed = false;
    private final GameFlow flow = new GameFlow();

    /**
     * Connects a session and view to a stage and configures the repeating movement timer.
     *
     * @param session session supplying the game rules and current state
     * @param gameView view to redraw and update
     * @param stage game window whose hide event stops the timer
     * @param returnToMenu callback for returning to the main menu
     */
    public GameController(GameSession session, GameView gameView, Stage stage, Runnable returnToMenu) {
        this(session, gameView, stage, returnToMenu, HighScoreStore.inMemory());
    }

    /**
     * Connects the round to a shared record store; the four-argument form uses memory only.
     * @param session session supplying current rules and score
     * @param gameView view to redraw and update
     * @param stage window whose hide event stops movement and flushes records
     * @param returnToMenu navigation callback
     * @param highScores shared persistent store, or an isolated store for tests
     */
    public GameController(GameSession session, GameView gameView, Stage stage, Runnable returnToMenu,
                          HighScoreStore highScores) {
        this.session = java.util.Objects.requireNonNull(session, "session");
        this.gameView = gameView;
        this.stage = stage;
        this.returnToMenu = java.util.Objects.requireNonNull(returnToMenu, "returnToMenu");
        this.highScores = java.util.Objects.requireNonNull(highScores, "highScores");
        this.stage.setOnHidden(event -> {
            animation.stop();
            updateHighScore();
            highScores.flush();
        });
        this.stage.fullScreenProperty().addListener((observable, wasFullScreen, isFullScreen) -> {
            gameView.updateProgress(session);
            gameView.resizeForFullscreen(isFullScreen, session.getObstacle(), session.getSnake(), session.getCoin());
        });
        animation.setCycleCount(Animation.INDEFINITE);
        animation.setRate(session.getSpeed());
        updateHighScore();
    }

    private final Timeline animation = new Timeline(new KeyFrame(Duration.seconds(1), event -> advanceGame()));

    /**
     * Processes a timer step and updates the view, menus and timer rate for its result.
     */
    private void advanceGame() {
        GameSession.StepResult result = session.advance(direction);
        updateHighScore();
        Snake snake = session.getSnake();

        if (result == GameSession.StepResult.ALREADY_ENDED) {
            stopAnimation();
            gameView.gameOver(snake);
            return;
        }
        if (result == GameSession.StepResult.COIN_EATEN || result == GameSession.StepResult.WON) {
            animation.setRate(session.getSpeed());
        }
        if (result == GameSession.StepResult.LEVEL_COMPLETED) {
            stopAnimation();
            animation.setRate(session.getSpeed());
            flow.reset();
            direction = Direction.LEFT;
            keyPressed = false;
            gameView.resetTurns();
            gameView.showStartHint();
            gameView.render(session);
            gameView.updateScoreLabel();
            gameView.updateProgress(session);
            return;
        }
        if (result == GameSession.StepResult.WON) {
            gameView.render(session);
            gameView.gameWon();
            gameView.updateScoreLabel();
            gameView.updateProgress(session);
            stopAnimation();
            flow.end();
            gameView.showEndMenu(true);
            return;
        }

        keyPressed = false;
        if (!snake.getDead() || snake.hitSelf()) {
            gameView.render(session);
        }
        gameView.gameWon();
        gameView.gameOver(snake);
        gameView.updateScoreLabel();
        gameView.updateProgress(session);
        if (snake.getDead()) {
            stopAnimation();
            flow.end();
            gameView.showEndMenu(false);
        }
    }

    /**
     * Maps arrow keys to model directions.
     *
     * @param key keyboard code to translate
     * @return the movement direction, or null for a non-arrow key
     */
    static Direction directionForKey(KeyCode key) {
        return switch (key) {
            case LEFT -> Direction.LEFT;
            case RIGHT -> Direction.RIGHT;
            case UP -> Direction.UP;
            case DOWN -> Direction.DOWN;
            default -> null;
        };
    }

    /**
     * Stops the movement timer.
     */
    private void stopAnimation() {
        animation.stop();
    }

    /**
     * Handles movement, pause, restart and fullscreen keyboard input.
     *
     * @param event key-press event from the game scene
     */
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
            gameView.hideStartHint();
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
            stage.setFullScreen(true);
        } else if (code == KeyCode.MINUS) {
            stage.setFullScreen(false);
        }
    }

    /**
     * Pauses the timer and combo clock and shows the pause menu.
     */
    public void pause() {
        if (!flow.pause()) return;
        animation.pause();
        session.pause();
        gameView.showPauseMenu();
    }

    /**
     * Hides the pause menu and resumes movement only if it was previously running.
     */
    public void resume() {
        if (!flow.isPaused()) return;
        boolean wasRunning = flow.resume();
        session.resume();
        gameView.hideMenu();
        if (wasRunning) animation.play();
    }

    /**
     * Restarts the selected run and waits for a new arrow key.
     */
    public void restart() {
        reset();
    }

    /**
     * Stops the timer and invokes the navigation callback.
     */
    public void returnToMainMenu() {
        animation.stop();
        updateHighScore();
        highScores.flush();
        returnToMenu.run();
    }

    /**
     * Resets session, timer, direction, input latch and rendered progress for a fresh run.
     */
    private void reset() {
        updateHighScore();
        highScores.flush();
        animation.jumpTo(Duration.ZERO);
        animation.stop();
        session.reset();
        newHighScore = false;
        updateHighScore();
        flow.reset();
        gameView.hideMenu();
        gameView.resetTurns();
        gameView.showStartHint();
        animation.setRate(session.getSpeed());
        direction = Direction.LEFT;
        keyPressed = false;

        gameView.gameWon();
        gameView.gameOver(session.getSnake());
        gameView.updateScoreLabel();
        gameView.updateProgress(session);
        gameView.render(session);
    }

    /** Records only improvements and refreshes mode-specific best and end-of-run feedback. */
    private void updateHighScore() {
        newHighScore |= highScores.record(session.getMode(), session.getScorePoints());
        gameView.updateHighScore(highScores.getBest(session.getMode()), newHighScore);
    }
}
