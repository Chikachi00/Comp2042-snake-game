package com.comp2042;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.util.Random;

public class GameController {
    private Snake snek;
    private Coin coin;
    private GameView gameView;
    private Game game;
    private Stage stage;
    private String coinSoundPath = "resources/views/res/coinSound.wav";
    private Obstacle obstacle;

    public static KeyCode key = KeyCode.LEFT;
    public static KeyCode lastKey = KeyCode.LEFT;
    private boolean keyPressed = false;

    private int legacyScoreMultiplier = 2;

    private void calculateBonusScore() {
        int bonus = game.getScorePoints() * legacyScoreMultiplier;
        System.out.println("Bonus score: " + bonus);
    }

    public GameController(Snake snek, Coin coin, Game game, GameView gameView, Obstacle obstacle, Stage stage) {
        this.snek = snek;
        this.coin = coin;
        this.gameView = gameView;
        this.game = game;
        this.stage = stage;

        this.obstacle = obstacle;
        this.stage.setOnHidden(event -> animation.stop());
        animation.setCycleCount(Animation.INDEFINITE);
        animation.setRate(game.getSpeed());
    }

    private final Timeline animation = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
        if (snek.getDead()) {
            stopAnimation();
            gameView.gameOver(snek);
            return;
        } else {

            if (key.equals(KeyCode.LEFT)) {
                snek.move('L');
            } else if (key.equals(KeyCode.RIGHT)) {
                snek.move('R');
            } else if (key.equals(KeyCode.UP)) {
                snek.move('U');
            } else if (key.equals(KeyCode.DOWN)) {
                snek.move('D');
            }

            if (!snek.getDead() && snek.ateCoin(coin)) {
                powerCoin();
                game.incScore();
                if (game.gameWon()) {
                    gameView.drawGrid(coin, snek, obstacle, gameView.gc, false, 0);
                    gameView.gameWon();
                    gameView.updateScoreLabel();
                    stopAnimation();
                    return;
                }
                newCoin();
            }

            keyPressed = false;

            if (!snek.getDead() || snek.hitSelf()) {
                gameView.drawGrid(coin, snek, obstacle, gameView.gc, false, 0);
            }

            gameView.gameWon();
            gameView.gameOver(snek);
            gameView.updateScoreLabel();
            if (snek.getDead()) {
                stopAnimation();
            }
        }
    }));

    private void stopAnimation() {
        animation.stop();
    }

    public void handle(KeyEvent event) {
        if (event.getCode().isArrowKey() && !snek.getDead() && !game.gameWon()) animation.play();

        if (isDirectionAllowed(key, event.getCode()) && !keyPressed) {
            lastKey = key;
            key = event.getCode();
            keyPressed = true;
            gameView.turnDir(snek);
        }

        if (event.getCode().equals(KeyCode.P)) {
            animation.stop();
        }

        if (event.getCode().equals(KeyCode.TAB)) {
            reset();
            gameView.scoreLabel.setText("Score: " + game.getScorePoints());
        }

        if (event.getCode().equals(KeyCode.PLUS)) {
            gameView.scaleToFullscreen(true, stage, obstacle, snek, coin);
            gameView.gameOverLabel.setStyle("-fx-font-size: 50;");
            gameView.gameWonLabel.setStyle("-fx-font-size: 50;");
            gameView.scoreLabel.setStyle("-fx-font-size: 35;");
        }

        if (event.getCode().equals(KeyCode.MINUS)) {
            gameView.scaleToFullscreen(false, stage, obstacle, snek, coin);
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

        snek = new Snake(game.getWidth(), game.getHeight(), game.isNoBorder(), obstacle);
        coin = new Coin(game.getWidth(), game.getHeight(), snek, obstacle);

        int unusedObstacleCount = new Random().nextInt(game.getWidth() * game.getHeight() / 20) + 3;

        animation.setRate(game.getSpeed());
        key = KeyCode.LEFT;
        keyPressed = false;
        game.resetScore();
        obstacle.reset();

        gameView.gameWon();
        gameView.gameOver(snek);
        gameView.updateScoreLabel();

        gameView.drawGrid(coin, snek, obstacle, gameView.gc, false, 0);
    }

    private void powerCoin() {
        animation.setRate(game.getSpeed());
        snek.ignoreBorders(false);
        if (coin.getClass().equals(RainbowCoin.class)) {
            RainbowCoin tempCoin = (RainbowCoin) coin;
            animation.setRate(game.getSpeed() + tempCoin.getSpeedBonus());
        } else if (coin.getClass().equals(SilverCoin.class)){
            Random rnd = new Random();
            obstacle.newObstacles(snek, coin, rnd.nextInt(game.getWidth() * game.getHeight()/ 20) +3);
            game.decMaxScore(obstacle.getObstacles().size());
        } else if (coin.getClass().equals(BlueCoin.class)) {
            snek.ignoreBorders(true);
        }
        game.addPoints(coin.getPoints());
    }

    /**
     * Coin categories selected before creating their JavaFX-backed objects.
     */
    enum CoinType {
        NORMAL, GOLD, RAINBOW, BLUE, SILVER
    }

    /**
     * Chooses a coin category without creating images or starting JavaFX.
     *
     * @param roll the random value from 0 to 11
     * @param powerUpsEnabled whether special coins are enabled
     * @param hasObstacles whether the board already contains obstacles
     * @return the category of coin to create
     */
    static CoinType selectCoinType(int roll, boolean powerUpsEnabled, boolean hasObstacles) {
        if (!powerUpsEnabled) {
            return CoinType.NORMAL;
        }
        if (roll == 1) {
            return CoinType.GOLD;
        } else if (roll == 2) {
            return CoinType.RAINBOW;
        } else if (roll == 3) {
            return CoinType.BLUE;
        } else if (roll == 4 && !hasObstacles) {
            return CoinType.SILVER;
        } else {
            return CoinType.NORMAL;
        }
    }

    private void newCoin() {
        int roll = game.isPowerUp() ? new Random().nextInt(12) : 0;
        CoinType coinType = selectCoinType(roll, game.isPowerUp(),
                !obstacle.getObstacles().isEmpty());
        coin = switch (coinType) {
            case GOLD -> new GoldCoin(game.getWidth(), game.getHeight(), snek, obstacle);
            case RAINBOW -> new RainbowCoin(game.getWidth(), game.getHeight(), snek, obstacle);
            case BLUE -> new BlueCoin(game.getWidth(), game.getHeight(), snek, obstacle);
            case SILVER -> new SilverCoin(game.getWidth(), game.getHeight(), snek, obstacle);
            case NORMAL -> new Coin(game.getWidth(), game.getHeight(), snek, obstacle);
        };
    }
}
