package com.comp2042;

import org.junit.jupiter.api.Test;
import java.awt.Point;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;

class GameSessionTest {
    private GameSession session(Game game, Snake snake, Coin coin, Obstacle obstacle) {
        return new GameSession(game, snake, coin, obstacle,
                new CoinFactory(new Random(1)), new CoinEffectService(new Random(1)));
    }

    @Test
    void ordinaryMovementKeepsScoreAndLength() {
        Game game = new Game(20, 20, true, 'N');
        Obstacle obstacle = new Obstacle(20, 20);
        Snake snake = new Snake(20, 20, false, obstacle);
        GameSession session = session(game, snake, new Coin(new Point(0, 0)), obstacle);

        assertEquals(GameSession.StepResult.MOVED, session.advance(Direction.LEFT));
        assertEquals(new Point(9, 10), snake.getHead());
        assertEquals(1, snake.getBody().size());
        assertEquals(0, game.getScorePoints());
        assertEquals(8.5, session.getSpeed());
    }

    @Test
    void eatingGoldAwardsPointsAndCreatesAFreeNextCoin() {
        Game game = new Game(20, 20, false, 'N');
        Obstacle obstacle = new Obstacle(20, 20);
        Snake snake = new Snake(20, 20, false, obstacle);
        GameSession session = session(game, snake, new GoldCoin(new Point(9, 10)), obstacle);

        assertEquals(GameSession.StepResult.COIN_EATEN, session.advance(Direction.LEFT));
        assertEquals(15, game.getScorePoints());
        assertEquals(2, snake.getBody().size());
        assertFalse(snake.getHead().equals(session.getCoin().getPos()));
        assertFalse(snake.getBody().contains(session.getCoin().getPos()));
        assertEquals(CoinType.NORMAL, session.getCoin().getType());
    }

    @Test
    void rainbowEffectIsClearedAfterEatingTheNextCoin() {
        Game game = new Game(20, 20, false, 'N');
        Obstacle obstacle = new Obstacle(20, 20);
        Snake snake = new Snake(20, 20, false, obstacle);
        // Place the next coin deterministically so the snake can reach it.
        CoinFactory factory = new CoinFactory(new Random() {
            @Override public int nextInt(int bound) { return 0; }
        });
        GameSession session = new GameSession(game, snake, new RainbowCoin(new Point(9, 10)), obstacle,
                factory, new CoinEffectService(new Random(1)));

        assertEquals(GameSession.StepResult.COIN_EATEN, session.advance(Direction.LEFT));
        assertEquals(13.5, session.getSpeed());
        assertEquals(new Point(0, 0), session.getCoin().getPos());
        for (int i = 0; i < 9; i++) session.advance(Direction.LEFT);
        for (int i = 0; i < 9; i++) session.advance(Direction.UP);
        assertEquals(GameSession.StepResult.COIN_EATEN, session.advance(Direction.UP));
        assertEquals(8.5, session.getSpeed());
        assertEquals(10, game.getScorePoints());
    }

    @Test
    void collisionEndsTheRoundAndLaterStepsDoNothing() {
        Game game = new Game(5, 5, true, 'N');
        Obstacle obstacle = new Obstacle(5, 5);
        ObstacleFixtures.place(obstacle, 5, 5, new Point(1, 2));
        Snake snake = new Snake(5, 5, false, obstacle);
        GameSession session = session(game, snake, new Coin(new Point(0, 0)), obstacle);

        assertEquals(GameSession.StepResult.DIED, session.advance(Direction.LEFT));
        assertTrue(session.isOver());
        assertEquals(new Point(2, 2), snake.getHead());
        assertEquals(GameSession.StepResult.ALREADY_ENDED, session.advance(Direction.UP));
        assertEquals(new Point(2, 2), snake.getHead());
        assertEquals(0, game.getScorePoints());
    }

    @Test
    void eatingOnAFullBoardWinsAndResetStartsACleanRound() {
        Game game = new Game(3, 3, true, 'N');
        Obstacle obstacle = new Obstacle(3, 3);
        Snake snake = new Snake(3, 3, false, obstacle);
        GameSession session = session(game, snake, new Coin(new Point(0, 1)), obstacle);
        ObstacleFixtures.place(obstacle, 3, 3, new Point(0, 0), new Point(1, 0),
                new Point(2, 0), new Point(0, 2), new Point(1, 2), new Point(2, 2));

        assertEquals(GameSession.StepResult.WON, session.advance(Direction.LEFT));
        assertTrue(game.gameWon());
        assertEquals(5, game.getScorePoints());
        assertEquals(GameSession.StepResult.ALREADY_ENDED, session.advance(Direction.LEFT));
        session.reset();
        assertFalse(game.gameWon());
        assertFalse(session.isOver());
        assertEquals(0, game.getScorePoints());
        assertTrue(obstacle.getObstacles().isEmpty());
        assertEquals(new Point(1, 1), session.getSnake().getHead());
        assertEquals(1, session.getSnake().getBody().size());
        assertEquals(CoinType.NORMAL, session.getCoin().getType());
        assertEquals(game.getSpeed(), session.getSpeed());
    }

    @Test
    void reachingScoreThresholdWinsWithoutGeneratingAnotherCoin() {
        Game game = new Game(3, 3, true, 'N');
        Obstacle obstacle = new Obstacle(3, 3);
        Snake snake = new Snake(3, 3, false, obstacle);
        Coin lastCoin = new GoldCoin(new Point(0, 1));
        GameSession session = session(game, snake, lastCoin, obstacle);
        for (int i = 0; i < 6; i++) game.incScore();

        assertEquals(GameSession.StepResult.WON, session.advance(Direction.LEFT));
        assertTrue(session.isOver());
        assertSame(lastCoin, session.getCoin());
        assertEquals(15, game.getScorePoints());
    }
}
