package com.comp2042;

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
        assertEquals(270, snake.getSnakeDir());
        assertFalse(snake.getDead());
    }

    @ParameterizedTest
    @CsvSource({"L, 1, 2, 270", "R, 3, 2, 90", "U, 2, 1, 0", "D, 2, 3, 180"})
    void movesOneCellInEachDirection(char direction, int x, int y, int rotation) {
        Snake snake = createSnake(false, new Obstacle(5, 5));

        snake.move(direction);

        assertEquals(new Point(x, y), snake.getHead());
        assertEquals(rotation, snake.getSnakeDir());
        assertFalse(snake.getDead());
    }

    @Test
    void advancesTheTailWhenNoCoinIsEaten() {
        Snake snake = createSnake(false, new Obstacle(5, 5));

        snake.move('L');

        assertFalse(snake.ateCoin(NO_COIN));
        assertEquals(List.of(new Point(2, 2)), snake.getBody());
    }

    @Test
    void growsWhenTheHeadReachesACoin() {
        Snake snake = createSnake(false, new Obstacle(5, 5));

        snake.move('L');

        assertTrue(snake.ateCoin(new Point(1, 2)));
        assertEquals(List.of(new Point(3, 2), new Point(2, 2)), snake.getBody());
    }

    @ParameterizedTest
    @CsvSource({"L, 0, 2", "R, 4, 2", "U, 2, 0", "D, 2, 4"})
    void stopsAtTheWallWhenBordersAreEnabled(char direction, int edgeX, int edgeY) {
        Snake snake = createSnake(false, new Obstacle(5, 5));
        moveToEdge(snake, direction);

        snake.move(direction);

        assertTrue(snake.getDead());
        assertEquals(new Point(edgeX, edgeY), snake.getHead());
    }

    @ParameterizedTest
    @CsvSource({
            "L, 4, 2, false", "R, 0, 2, false", "U, 2, 4, false", "D, 2, 0, false",
            "L, 4, 2, true", "R, 0, 2, true", "U, 2, 4, true", "D, 2, 0, true"
    })
    void wrapsAcrossEveryEdgeWhenAllowed(char direction, int targetX, int targetY,
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
            "L, 0, 2, 4, 2, false", "R, 4, 2, 0, 2, false",
            "U, 2, 0, 2, 4, false", "D, 2, 4, 2, 0, false",
            "L, 0, 2, 4, 2, true", "R, 4, 2, 0, 2, true",
            "U, 2, 0, 2, 4, true", "D, 2, 4, 2, 0, true"
    })
    void detectsAnObstacleAtTheWrappedDestination(char direction, int edgeX, int edgeY,
                                                   int targetX, int targetY,
                                                   boolean temporaryPowerUp) {
        Obstacle obstacle = new Obstacle(5, 5);
        obstacle.getObstacles().add(new Point(targetX, targetY));
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
        obstacle.getObstacles().add(new Point(1, 2));
        Snake snake = createSnake(false, obstacle);

        snake.move('L');

        assertTrue(snake.getDead());
        assertEquals(new Point(2, 2), snake.getHead());
    }

    @Test
    void detectsCollisionWithItsOwnBody() {
        Snake snake = new Snake(10, 10, false, new Obstacle(10, 10));
        for (char direction : new char[]{'L', 'U', 'R'}) {
            snake.move(direction);
            assertTrue(snake.ateCoin(snake.getHead()));
        }

        snake.move('D');

        assertTrue(snake.getDead());
        assertTrue(snake.hitSelf());
    }

    private Snake createSnake(boolean noBorder, Obstacle obstacle) {
        return new Snake(5, 5, noBorder, obstacle);
    }

    private void moveToEdge(Snake snake, char direction) {
        for (int i = 0; i < 2; i++) {
            snake.move(direction);
            assertFalse(snake.getDead());
            assertFalse(snake.ateCoin(NO_COIN));
        }
    }
}