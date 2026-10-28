package com.comp2042.game;

import java.util.Optional;

/** Maintains the rules and state of a single game, independently of the UI timer. */
public final class GameSession {
    public enum StepResult { MOVED, COIN_EATEN, LEVEL_COMPLETED, WON, DIED, ALREADY_ENDED }

    private final Game game;
    private final Obstacle obstacle;
    private final CoinFactory coinFactory;
    private final CoinEffectService coinEffects;
    private final ComboTracker combo;
    private Snake snake;
    private Coin coin;
    private double speed;
    private Level startingLevel;
    private Level level;
    private int levelCoins;

    public GameSession(Game game, Snake snake, Coin coin, Obstacle obstacle) {
        this(game, snake, coin, obstacle, new CoinFactory(), new CoinEffectService());
    }

    /** Starts a run at the selected level on the standard 20 by 20 board. */
    public GameSession(Game game, Level startingLevel) {
        this(game, startingLevel, System::nanoTime);
    }

    GameSession(Game game, Level startingLevel, java.util.function.LongSupplier clock) {
        if (game.getWidth() != 20 || game.getHeight() != 20) {
            throw new IllegalArgumentException("Levels require a 20 by 20 board");
        }
        this.game = game;
        this.obstacle = new Obstacle(game.getWidth(), game.getHeight());
        this.coinFactory = new CoinFactory();
        this.coinEffects = new CoinEffectService();
        this.combo = new ComboTracker(clock);
        this.startingLevel = java.util.Objects.requireNonNull(startingLevel, "startingLevel");
        initializeLevel(startingLevel);
    }

    GameSession(Game game, Snake snake, Coin coin, Obstacle obstacle,
                CoinFactory coinFactory, CoinEffectService coinEffects) {
        this.game = game;
        this.snake = snake;
        this.coin = coin;
        this.obstacle = obstacle;
        this.coinFactory = coinFactory;
        this.coinEffects = coinEffects;
        this.combo = new ComboTracker(System::nanoTime);
        this.speed = game.getSpeed();
    }

    /** Advances one movement and applies any resulting coin, collision or win rules. */
    public StepResult advance(Direction direction) {
        if (isOver()) {
            return StepResult.ALREADY_ENDED;
        }
        snake.move(direction);
        if (snake.getDead()) {
            return StepResult.DIED;
        }
        if (!snake.ateCoin(coin)) {
            return StepResult.MOVED;
        }

        speed = coinEffects.apply(coin, game, snake, obstacle);
        game.incScore();
        if (level != null) {
            game.addPoints(combo.recordCoin());
            levelCoins++;
            if (levelCoins >= level.target() || game.gameWon()) {
                if (level.next() == null) {
                    game.markBoardFull();
                    return StepResult.WON;
                }
                initializeLevel(level.next());
                return StepResult.LEVEL_COMPLETED;
            }
        }
        if (!game.gameWon()) {
            Optional<Coin> nextCoin = coinFactory.createCoin(game, snake, obstacle);
            if (nextCoin.isPresent()) {
                coin = nextCoin.get();
            } else {
                game.markBoardFull();
            }
        }
        return game.gameWon() ? StepResult.WON : StepResult.COIN_EATEN;
    }

    /** Starts a new round using the existing game settings. */
    public void reset() {
        if (startingLevel != null) {
            game.resetScore();
            initializeLevel(startingLevel);
            return;
        }
        obstacle.reset();
        snake = new Snake(game.getWidth(), game.getHeight(), game.isNoBorder(), obstacle);
        coin = new Coin(game.getWidth(), game.getHeight(), snake, obstacle);
        speed = game.getSpeed();
        game.resetScore();
    }

    private void initializeLevel(Level next) {
        level = next;
        levelCoins = 0;
        combo.reset();
        obstacle.setLayout(next.obstaclePositions());
        game.startLevel(obstacle.getObstacles().size());
        snake = new Snake(game.getWidth(), game.getHeight(), game.isNoBorder(), obstacle);
        if (obstacle.occupies(snake.getHead()) || snake.getBody().stream().anyMatch(obstacle::occupies)) {
            throw new IllegalStateException("Level obstacles overlap the starting snake");
        }
        coin = new Coin(game.getWidth(), game.getHeight(), snake, obstacle);
        speed = game.getSpeed();
    }

    public void pause() { combo.pause(); }
    public void resume() { combo.resume(); }
    public int getComboCount() { return combo.count(); }
    public int getComboBonus() { return combo.lastBonus(); }

    public Level getLevel() { return level; }
    public int getLevelCoins() { return levelCoins; }

    public boolean isOver() { return snake.getDead() || game.gameWon(); }
    public Snake getSnake() { return snake; }
    public Coin getCoin() { return coin; }
    public Obstacle getObstacle() { return obstacle; }
    public double getSpeed() { return speed; }
}
