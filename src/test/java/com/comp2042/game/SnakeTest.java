package com.comp2042.game;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.awt.Point;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SnakeTest {
    private static final Point NO_COIN = new Point(-1, -1);

    @Test
    void startsAtTheCenterFacingLeft() {
        Snake snake = createSnake(false, new Obstacle(5, 5));

        assertEquals(new Point(2, 2), snake.getHead());
        assertEquals(List.of(new Point(3, 2)), snake.getBody());
        assertEquals(Direction.LEFT, snake.getDirection());
        assertFalse(snake.getDead());
    }

    @ParameterizedTest
    @CsvSource({"LEFT, 1, 2", "RIGHT, 3, 2", "UP, 2, 1", "DOWN, 2, 3"})
    void movesOneCellInEachDirection(Direction direction, int x, int y) {
        Snake snake = createSnake(false, new Obstacle(5, 5));

        snake.move(direction);

        assertEquals(new Point(x, y), snake.getHead());
        assertEquals(direction, snake.getDirection());
        assertFalse(snake.getDead());
    }

    @Test
    void advancesTheTailWhenNoCoinIsEaten() {
        Snake snake = createSnake(false, new Obstacle(5, 5));

        snake.move(Direction.LEFT);

        assertFalse(snake.ateCoin(NO_COIN));
        assertEquals(List.of(new Point(2, 2)), snake.getBody());
    }

    @Test
    void growsWhenTheHeadReachesACoin() {
        Snake snake = createSnake(false, new Obstacle(5, 5));

        snake.move(Direction.LEFT);

        assertTrue(snake.ateCoin(new Point(1, 2)));
        assertEquals(List.of(new Point(3, 2), new Point(2, 2)), snake.getBody());
    }

    @ParameterizedTest
    @CsvSource({"LEFT, 0, 2", "RIGHT, 4, 2", "UP, 2, 0", "DOWN, 2, 4"})
    void stopsAtTheWallWhenBordersAreEnabled(Direction direction, int edgeX, int edgeY) {
        Snake snake = createSnake(false, new Obstacle(5, 5));
        moveToEdge(snake, direction);

        snake.move(direction);

        assertTrue(snake.getDead());
        assertEquals(new Point(edgeX, edgeY), snake.getHead());
    }

    @ParameterizedTest
    @CsvSource({
            "LEFT, 4, 2, false", "RIGHT, 0, 2, false", "UP, 2, 4, false", "DOWN, 2, 0, false",
            "LEFT, 4, 2, true", "RIGHT, 0, 2, true", "UP, 2, 4, true", "DOWN, 2, 0, true"
    })
    void wrapsAcrossEveryEdgeWhenAllowed(Direction direction, int targetX, int targetY,
                                         boolean temporaryPowerUp) {
        Snake snake = createSnake(!temporaryPowerUp, new Obstacle(5, 5));
        snake.ignoreBorders(temporaryPowerUp);
        moveToEdge(snake, direction);

        snake.move(direction);

        assertFalse(snake.getDead());
        assertEquals(new Point(targetX, targetY), snake.getHead());
    }

    @ParameterizedTest
    @CsvSource({
            "LEFT, 0, 2, 4, 2, false", "RIGHT, 4, 2, 0, 2, false",
            "UP, 2, 0, 2, 4, false", "DOWN, 2, 4, 2, 0, false",
            "LEFT, 0, 2, 4, 2, true", "RIGHT, 4, 2, 0, 2, true",
            "UP, 2, 0, 2, 4, true", "DOWN, 2, 4, 2, 0, true"
    })
    void detectsAnObstacleAtTheWrappedDestination(Direction direction, int edgeX, int edgeY,
                                                   int targetX, int targetY,
                                                   boolean temporaryPowerUp) {
        Obstacle obstacle = new Obstacle(5, 5);
        ObstacleFixtures.place(obstacle, 5, 5, new Point(targetX, targetY));
        Snake snake = createSnake(!temporaryPowerUp, obstacle);
        snake.ignoreBorders(temporaryPowerUp);
        moveToEdge(snake, direction);

        snake.move(direction);

        assertTrue(snake.getDead());
        assertEquals(new Point(edgeX, edgeY), snake.getHead());
    }

    @Test
    void detectsAnObstacleWithoutWrapping() {
        Obstacle obstacle = new Obstacle(5, 5);
        ObstacleFixtures.place(obstacle, 5, 5, new Point(1, 2));
        Snake snake = createSnake(false, obstacle);

        snake.move(Direction.LEFT);

        assertTrue(snake.getDead());
        assertEquals(new Point(2, 2), snake.getHead());
    }

    @Test
    void detectsCollisionWithItsOwnBody() {
        Snake snake = new Snake(10, 10, false, new Obstacle(10, 10));
        for (Direction direction : new Direction[]{Direction.LEFT, Direction.UP, Direction.RIGHT}) {
            snake.move(direction);
            assertTrue(snake.ateCoin(snake.getHead()));
        }

        snake.move(Direction.DOWN);

        assertTrue(snake.getDead());
        assertTrue(snake.hitSelf());
    }

    private Snake createSnake(boolean noBorder, Obstacle obstacle) {
        return new Snake(5, 5, noBorder, obstacle);
    }

    private void moveToEdge(Snake snake, Direction direction) {
        for (int i = 0; i < 2; i++) {
            snake.move(direction);
            assertFalse(snake.getDead());
            assertFalse(snake.ateCoin(NO_COIN));
        }
    }
}