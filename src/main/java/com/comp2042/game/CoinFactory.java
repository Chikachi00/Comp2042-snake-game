package com.comp2042.game;

import java.awt.Point;
import java.util.Optional;
import java.util.Random;

/** Selects a coin type and places it on an available board cell. */
final class CoinFactory {
    private final Random random;

    CoinFactory() {
        this(new Random());
    }

    CoinFactory(Random random) {
        this.random = random;
    }

    /**
     * Creates the next coin, or returns empty when no board cell is free.
     */
    Optional<Coin> createCoin(Game game, Snake snake, Obstacle obstacle) {
        Optional<Point> position = Coin.findFreePosition(game.getWidth(), game.getHeight(),
                snake.getHead(), snake.getBody(), obstacle.getObstacles(), random);
        if (position.isEmpty()) {
            return Optional.empty();
        }

        int roll = game.isPowerUp() ? random.nextInt(12) : 0;
        CoinType type = selectCoinType(roll, game.isPowerUp(), !obstacle.getObstacles().isEmpty());
        Point nextPosition = position.get();
        Coin coin = switch (type) {
            case GOLD -> new GoldCoin(nextPosition);
            case RAINBOW -> new RainbowCoin(nextPosition);
            case BLUE -> new BlueCoin(nextPosition);
            case SILVER -> new SilverCoin(nextPosition);
            case NORMAL -> new Coin(nextPosition);
        };
        return Optional.of(coin);
    }

    /**
     * Chooses a coin category for a random roll and the current game settings.
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
}
