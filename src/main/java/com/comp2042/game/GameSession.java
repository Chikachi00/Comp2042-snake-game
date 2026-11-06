package com.comp2042.game;

import java.util.Optional;
import java.util.List;

/**
 * Coordinates endless survival or progressive levels independently of the UI timer.
 */
public final class GameSession {
    /** Describes the outcome of one session advance. */
    public enum StepResult {
        /** Moved without collecting a coin. */
        MOVED,
        /** Collected a coin within the current level. */
        COIN_EATEN,
        /** Collected a key and permanently opened its shortcut. */
        SHORTCUT_OPENED,
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
    private List<Shortcut> shortcuts = List.of();
    private String openedShortcut;
    private long openedAt;

    /**
     * Starts an endless round with the original special-coin and board-fill rules.
     * There are no level targets, keys or combo bonuses in this mode.
     *
     * @param game board settings and score state
     */
    public GameSession(Game game) {
        this(game, System::nanoTime);
        reset();
    }

    /**
     * Initializes common services for a fresh round of either mode.
     * @param game board settings and score state
     * @param clock monotonic clock returning nanoseconds
     */
    private GameSession(Game game, java.util.function.LongSupplier clock) {
        this.game = java.util.Objects.requireNonNull(game, "game");
        this.obstacle = new Obstacle(game.getWidth(), game.getHeight());
        this.coinFactory = new CoinFactory();
        this.coinEffects = new CoinEffectService();
        this.combo = new ComboTracker(clock);
    }

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
        this(game, clock);
        if (game.getWidth() != 20 || game.getHeight() != 20) {
            throw new IllegalArgumentException("Levels require a 20 by 20 board");
        }
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
            combo.pause();
            return StepResult.DIED;
        }
        boolean ateCoin = snake.ateCoin(coin);
        boolean opened = false;
        for (Shortcut shortcut : shortcuts) {
            if (shortcut.collect(snake.getHead())) {
                opened = true;
                openedShortcut = shortcut.snapshot().id();
                openedAt = combo.activeTimeNanos();
            }
        }
        if (opened) synchronizeShortcuts();
        if (!ateCoin) {
            return opened ? StepResult.SHORTCUT_OPENED : StepResult.MOVED;
        }

        speed = coinEffects.apply(coin, game, snake, obstacle);
        game.incScore();
        if (level != null) {
            game.addPoints(combo.recordCoin());
            levelCoins++;
            if (levelCoins == 1) {
                shortcuts.forEach(Shortcut::activate);
                synchronizeShortcuts();
            }
            if (levelCoins >= level.target() || game.gameWon()) {
                if (level.next() == null) {
                    game.markBoardFull();
                    combo.pause();
                    return StepResult.WON;
                }
                initializeLevel(level.next());
                return StepResult.LEVEL_COMPLETED;
            }
        }
        if (!game.gameWon()) {
            Optional<Coin> nextCoin = level == null ? coinFactory.createCoin(game, snake, obstacle)
                    : coinFactory.createCoin(game, snake, obstacle, level.preferredCoinPosition(levelCoins), false);
            if (nextCoin.isPresent()) {
                coin = nextCoin.get();
            } else {
                game.markBoardFull();
            }
        }
        if (game.gameWon()) combo.pause();
        return game.gameWon() ? StepResult.WON : StepResult.COIN_EATEN;
    }

    /**
     * Restarts the same mode with zero points, restoring its selected starting level if present.
     */
    public void reset() {
        if (startingLevel != null) {
            game.resetScore();
            initializeLevel(startingLevel);
            return;
        }
        obstacle.reset();
        combo.reset();
        levelCoins = 0;
        shortcuts = List.of();
        openedShortcut = null;
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
        openedShortcut = null;
        shortcuts = next.createShortcuts();
        obstacle.setLayout(next.obstaclePositions());
        obstacle.setShortcuts(getShortcuts());
        game.startLevel(obstacle.getBlockedCells().size());
        snake = new Snake(game.getWidth(), game.getHeight(), game.isNoBorder(), obstacle);
        if (obstacle.occupies(snake.getHead()) || snake.getBody().stream().anyMatch(obstacle::occupies)) {
            throw new IllegalStateException("Level obstacles overlap the starting snake");
        }
        coin = coinFactory.createCoin(game, snake, obstacle, next.preferredCoinPosition(0), true)
                .orElseThrow(() -> new IllegalStateException("No free cell for the opening coin"));
        speed = game.getSpeed();
    }

    /** Updates collision, spawn reservations and capacity after an immutable shortcut snapshot. */
    private void synchronizeShortcuts() {
        obstacle.setShortcuts(getShortcuts());
        game.updateBoardCapacity(obstacle.getBlockedCells().size());
    }

    /** @return an immutable list of independent, read-only shortcut states */
    public List<ShortcutSnapshot> getShortcuts() { return shortcuts.stream().map(Shortcut::snapshot).toList(); }

    /** @return remaining active combo time in seconds, zero when no window is active */
    public double getComboSecondsRemaining() { return combo.secondsRemaining(); }

    /** @return transient feedback text, excluding paused time, or an empty string */
    public String getShortcutMessage() {
        return openedShortcut != null && combo.activeTimeNanos() - openedAt < 1_500_000_000L
                ? "Shortcut " + openedShortcut + " opened" : "";
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

    /** @return endless survival when no level is configured, otherwise the challenge mode */
    public GameMode getMode() { return level == null ? GameMode.ENDLESS : GameMode.CHALLENGE; }

    /** @return total points earned in this run, retained between challenge levels */
    public int getScorePoints() { return game.getScorePoints(); }

    /**
     * Returns the currently active level.
     *
     * @return the active level, or null for endless survival
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
