package com.comp2042;

import java.util.Optional;

/** Maintains the rules and state of a single game, independently of the UI timer. */
final class GameSession {
    enum StepResult { MOVED, COIN_EATEN, WON, DIED, ALREADY_ENDED }

    private final Game game;
    private final Obstacle obstacle;
    private final CoinFactory coinFactory;
    private final CoinEffectService coinEffects;
    private Snake snake;
    private Coin coin;
    private double speed;

    GameSession(Game game, Snake snake, Coin coin, Obstacle obstacle) {
        this(game, snake, coin, obstacle, new CoinFactory(), new CoinEffectService());
    }

    GameSession(Game game, Snake snake, Coin coin, Obstacle obstacle,
                CoinFactory coinFactory, CoinEffectService coinEffects) {
        this.game = game;
        this.snake = snake;
        this.coin = coin;
        this.obstacle = obstacle;
        this.coinFactory = coinFactory;
        this.coinEffects = coinEffects;
        this.speed = game.getSpeed();
    }

    /** Advances one movement and applies any resulting coin, collision or win rules. */
    StepResult advance(char direction) {
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
    void reset() {
        obstacle.reset();
        snake = new Snake(game.getWidth(), game.getHeight(), game.isNoBorder(), obstacle);
        coin = new Coin(game.getWidth(), game.getHeight(), snake, obstacle);
        speed = game.getSpeed();
        game.resetScore();
    }

    boolean isOver() { return snake.getDead() || game.gameWon(); }
    Snake getSnake() { return snake; }
    Coin getCoin() { return coin; }
    Obstacle getObstacle() { return obstacle; }
    double getSpeed() { return speed; }
}
