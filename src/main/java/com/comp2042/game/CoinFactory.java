package com.comp2042.game;

import java.awt.Point;
import java.util.Optional;
import java.util.Random;

/**
 * Selects a coin category and places it in an available board cell.
 */
final class CoinFactory {
    private final Random random;

    /**
     * Creates a helper with an independent default random source.
     */
    CoinFactory() {
        this(new Random());
    }

    /**
     * Creates a helper with the supplied random source.
     *
     * @param random random source for selection or effects
     */
    CoinFactory(Random random) {
        this.random = random;
    }

    /**
     * Selects the next coin type and a free cell, without changing the snake or obstacles.
     *
     * @param game game settings and score state
     * @param snake current snake
     * @param obstacle board obstacle collection
     * @return the next coin, or an empty optional when no cell is free
     */
    Optional<Coin> createCoin(Game game, Snake snake, Obstacle obstacle) {
        return createCoin(game, snake, obstacle, null, false);
    }

    /**
     * Creates a coin with an optional safe placement preference and ordinary opening category.
     * @param game game settings
     * @param snake current snake
     * @param obstacle walls, gates and reserved keys
     * @param preferred desired position, or null for random placement
     * @param ordinary whether this must be an ordinary opening coin
     * @return the placed coin, or empty when no cell is available
     */
    Optional<Coin> createCoin(Game game, Snake snake, Obstacle obstacle, Point preferred, boolean ordinary) {
        Optional<Point> position = Coin.findFreePosition(game.getWidth(), game.getHeight(),
                snake.getHead(), snake.getBody(), obstacle.getCoinExcludedCells(), random, preferred);
        if (position.isEmpty()) {
            return Optional.empty();
        }

        int roll = game.isPowerUp() && !ordinary ? random.nextInt(12) : 0;
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
     * Maps the random roll to a category; silver is eligible only without existing obstacles.
     *
     * @param roll random selection value, normally zero through eleven
     * @param powerUpsEnabled whether special coin categories are enabled
     * @param hasObstacles whether the board already contains obstacles
     * @return the selected coin category
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
