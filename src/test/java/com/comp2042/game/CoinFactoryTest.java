package com.comp2042.game;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.awt.Point;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CoinFactoryTest {
    @ParameterizedTest
    @CsvSource({"0, NORMAL", "1, GOLD", "2, RAINBOW", "3, BLUE", "4, SILVER"})
    void createsTheSelectedCoinOnAnAvailableCell(int roll, CoinType expectedType) {
        Game game = new Game(20, 20, true, 'N');
        Obstacle obstacle = new Obstacle(20, 20);
        Snake snake = new Snake(20, 20, false, obstacle);
        CoinFactory factory = new CoinFactory(new SequenceRandom(0, roll));

        Coin coin = factory.createCoin(game, snake, obstacle).orElseThrow();

        assertEquals(expectedType, coin.getType());
        assertFalse(coin.getPos().equals(snake.getHead()));
        assertFalse(snake.getBody().contains(coin.getPos()));
        assertFalse(obstacle.getObstacles().contains(coin.getPos()));
    }

    @Test
    void createsOnlyNormalCoinsWhenPowerUpsAreDisabled() {
        Game game = new Game(20, 20, false, 'N');
        Obstacle obstacle = new Obstacle(20, 20);
        Snake snake = new Snake(20, 20, false, obstacle);
        CoinFactory factory = new CoinFactory(new SequenceRandom(0));

        assertEquals(CoinType.NORMAL, factory.createCoin(game, snake, obstacle).orElseThrow().getType());
    }

    @Test
    void doesNotCreateSilverCoinsWhenObstaclesExist() {
        Game game = new Game(20, 20, true, 'N');
        Obstacle obstacle = new Obstacle(20, 20);
        Snake snake = new Snake(20, 20, false, obstacle);
        ObstacleFixtures.place(obstacle, 20, 20, new Point(0, 0));
        CoinFactory factory = new CoinFactory(new SequenceRandom(0, 4));

        Coin coin = factory.createCoin(game, snake, obstacle).orElseThrow();

        assertEquals(CoinType.NORMAL, coin.getType());
        assertFalse(obstacle.getObstacles().contains(coin.getPos()));
    }

    @Test
    void returnsEmptyWhenTheBoardHasNoFreeCell() {
        Game game = new Game(3, 3, true, 'N');
        Obstacle obstacle = new Obstacle(3, 3);
        Snake snake = new Snake(3, 3, false, obstacle);
        ObstacleFixtures.place(obstacle, 3, 3, new Point(0, 0), new Point(0, 1),
                new Point(0, 2), new Point(1, 0), new Point(1, 2), new Point(2, 0), new Point(2, 2));
        CoinFactory factory = new CoinFactory(new SequenceRandom());

        Optional<Coin> result = factory.createCoin(game, snake, obstacle);

        assertTrue(result.isEmpty());
    }

    private static final class SequenceRandom extends Random {
        private final int[] values;
        private int index;

        private SequenceRandom(int... values) {
            this.values = values;
        }

        @Override
        public int nextInt(int bound) {
            if (index >= values.length) {
                throw new AssertionError("Unexpected random draw");
            }
            int value = values[index++];
            if (value < 0 || value >= bound) {
                throw new AssertionError("Value outside random bound");
            }
            return value;
        }
    }
}