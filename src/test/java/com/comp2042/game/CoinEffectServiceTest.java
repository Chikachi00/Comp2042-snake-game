package com.comp2042.game;

import org.junit.jupiter.api.Test;

import java.awt.Point;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CoinEffectServiceTest {
    private static final Point NO_COIN = new Point(-1, -1);

    @Test
    void normalAndGoldCoinsAwardPointsAtBaseSpeed() {
        Game game = new Game(20, 20, true, 'N');
        Obstacle obstacle = new Obstacle(20, 20);
        Snake snake = new Snake(20, 20, false, obstacle);
        CoinEffectService effects = new CoinEffectService(new Random(1));

        assertEquals(game.getSpeed(), effects.apply(new Coin(new Point(0, 0)), game, snake, obstacle));
        assertEquals(5, game.getScorePoints());
        assertEquals(game.getSpeed(), effects.apply(new GoldCoin(new Point(0, 0)), game, snake, obstacle));
        assertEquals(20, game.getScorePoints());
        assertTrue(obstacle.getObstacles().isEmpty());
    }

    @Test
    void rainbowIncreasesSpeedUntilTheNextCoin() {
        Game game = new Game(20, 20, true, 'N');
        Obstacle obstacle = new Obstacle(20, 20);
        Snake snake = new Snake(20, 20, false, obstacle);
        CoinEffectService effects = new CoinEffectService(new Random(1));

        assertEquals(game.getSpeed() + 5, effects.apply(new RainbowCoin(new Point(0, 0)), game, snake, obstacle));
        assertEquals(game.getSpeed(), effects.apply(new GoldCoin(new Point(0, 0)), game, snake, obstacle));
        assertEquals(20, game.getScorePoints());
    }

    @Test
    void blueAllowsWrappingUntilTheNextCoin() {
        Game game = new Game(5, 5, true, 'N');
        Obstacle obstacle = new Obstacle(5, 5);
        Snake snake = new Snake(5, 5, false, obstacle);
        CoinEffectService effects = new CoinEffectService(new Random(1));

        assertEquals(game.getSpeed(), effects.apply(new BlueCoin(new Point(0, 0)), game, snake, obstacle));
        for (int i = 0; i < 3; i++) {
            snake.move(Direction.LEFT);
            assertFalse(snake.getDead());
            assertFalse(snake.ateCoin(NO_COIN));
        }
        assertEquals(new Point(4, 2), snake.getHead());

        effects.apply(new Coin(new Point(0, 0)), game, snake, obstacle);
        snake.move(Direction.RIGHT);

        assertTrue(snake.getDead());
        assertEquals(10, game.getScorePoints());
    }

    @Test
    void silverCreatesObstaclesAndAdjustsTheWinThreshold() {
        Game game = new Game(20, 20, true, 'N');
        Obstacle obstacle = new Obstacle(20, 20, new Random(2042));
        Snake snake = new Snake(20, 20, false, obstacle);
        Coin coin = new SilverCoin(new Point(0, 0));
        CoinEffectService effects = new CoinEffectService(new Random(42));

        assertEquals(game.getSpeed(), effects.apply(coin, game, snake, obstacle));

        int count = obstacle.getObstacles().size();
        assertTrue(count >= 3 && count <= 22);
        assertFalse(obstacle.getObstacles().contains(snake.getHead()));
        assertTrue(snake.getBody().stream().noneMatch(obstacle.getObstacles()::contains));
        assertFalse(obstacle.getObstacles().contains(coin.getPos()));
        assertEquals(5, game.getScorePoints());
        for (int i = 0; i < 398 - count - 1; i++) {
            game.incScore();
        }
        assertFalse(game.gameWon());
        game.incScore();
        assertTrue(game.gameWon());
    }

    @Test
    void silverDoesNotChangeTheThresholdWhenObstacleLimitIsReached() {
        Game game = new Game(20, 20, true, 'N');
        Obstacle obstacle = new Obstacle(20, 20, new Random(2042));
        Snake snake = new Snake(20, 20, false, obstacle);
        Coin coin = new SilverCoin(new Point(0, 0));
        obstacle.newObstacles(snake, coin, 50);
        game.decMaxScore(50);
        CoinEffectService effects = new CoinEffectService(new Random(42));

        effects.apply(coin, game, snake, obstacle);

        assertEquals(50, obstacle.getObstacles().size());
        for (int i = 0; i < 347; i++) {
            game.incScore();
        }
        assertFalse(game.gameWon());
        game.incScore();
        assertTrue(game.gameWon());
    }

    @Test
    void appliesAnExtendedCoinEffectWithoutCheckingItsConcreteClass() {
        Game game = new Game(20, 20, true, 'N');
        Obstacle obstacle = new Obstacle(20, 20);
        Snake snake = new Snake(20, 20, false, obstacle);
        Coin special = new Coin(new Point(0, 0)) {
            @Override
            double applyEffect(Game game, Snake snake, Obstacle obstacle, Random random) {
                return game.getSpeed() + 3;
            }

            @Override
            public int getPoints() {
                return 9;
            }
        };
        CoinEffectService effects = new CoinEffectService(new Random(1));

        assertEquals(11.5, effects.apply(special, game, snake, obstacle));
        assertEquals(9, game.getScorePoints());
    }
}