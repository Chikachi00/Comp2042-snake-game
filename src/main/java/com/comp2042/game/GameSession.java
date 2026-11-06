package com.comp2042.game;

import java.util.Optional;

/**
 * Coordinates movement, coins, scoring and level progression independently of the UI timer.
 */
public final class GameSession {
    /** Describes the outcome of one session advance. */
    public enum StepResult {
        /** Moved without collecting a coin. */
        MOVED,
        /** Collected a coin within the current level. */
        COIN_EATEN,
        /** Initialized the next level. */
        LEVEL_COMPLETED,
        /** Reached a winning condition. */
        WON,
        /** Collided during movement. */
        DIED,
        /** Ignored a step after the round ended. */
        ALREADY_ENDED
    }

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

    /**
     * Creates a legacy round with supplied game objects and default coin services.
     *
     * @param game game settings and score state
     * @param snake current snake
     * @param coin current or collected coin
     * @param obstacle board obstacle collection
     */
    public GameSession(Game game, Snake snake, Coin coin, Obstacle obstacle) {
        this(game, snake, coin, obstacle, new CoinFactory(), new CoinEffectService());
    }

    /**
     * Starts a progressive run at the selected level on the standard twenty by twenty board.
     *
     * @param game game settings and score state
     * @param startingLevel first level to play and the level to restore on restart
     */
    public GameSession(Game game, Level startingLevel) {
        this(game, startingLevel, System::nanoTime);
    }

    /**
     * Starts a progressive run with an injectable clock for deterministic combo timing.
     *
     * @param game game settings and score state
     * @param startingLevel first level to play and the level to restore on restart
     * @param clock monotonic clock returning nanoseconds
     */
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

    /**
     * Creates a legacy round with injectable coin services for deterministic tests.
     *
     * @param game game settings and score state
     * @param snake current snake
     * @param coin current or collected coin
     * @param obstacle board obstacle collection
     * @param coinFactory factory used to place subsequent coins
     * @param coinEffects service used to apply effects and base points
     */
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

    /**
     * Advances one move and resolves collision, growth, effects, scoring and level completion. The
     * controller is responsible for withholding calls while paused.
     *
     * @param direction requested movement direction
     * @return the event produced by this move, or ALREADY_ENDED if the round is over
     */
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

    /**
     * Restarts from the selected starting level with zero run points, or resets a legacy round.
     */
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

    /**
     * Resets the snake, layout, temporary effects and combo while retaining run points.
     *
     * @param next level to initialize
     */
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

    /**
     * Freezes combo time; movement is stopped separately by the controller.
     */
    public void pause() { combo.pause(); }
    /**
     * Resumes combo time after excluding the paused duration.
     */
    public void resume() { combo.resume(); }
    /**
     * Returns the live combo count.
     *
     * @return the consecutive coin count, or zero when expired
     */
    public int getComboCount() { return combo.count(); }
    /**
     * Returns the live combo's latest awarded bonus.
     *
     * @return the latest bonus, or zero when expired
     */
    public int getComboBonus() { return combo.lastBonus(); }

    /**
     * Returns the currently active level.
     *
     * @return the active level, or null for a legacy round
     */
    public Level getLevel() { return level; }
    /**
     * Returns progress toward the active level objective.
     *
     * @return the number of coins collected in this level
     */
    public int getLevelCoins() { return levelCoins; }

    /**
     * Checks whether collision or a win condition has ended the round.
     *
     * @return whether further movement should be stopped
     */
    public boolean isOver() { return snake.getDead() || game.gameWon(); }
    /**
     * Returns the snake owned by this session.
     *
     * @return the current snake, which is replaced on level changes and restarts
     */
    public Snake getSnake() { return snake; }
    /**
     * Returns the current pickup.
     *
     * @return the current coin
     */
    public Coin getCoin() { return coin; }
    /**
     * Returns the obstacle collection used by this session.
     *
     * @return the current obstacle collection
     */
    public Obstacle getObstacle() { return obstacle; }
    /**
     * Returns the movement speed after the active coin effect.
     *
     * @return the number of moves per second
     */
    public double getSpeed() { return speed; }
}
