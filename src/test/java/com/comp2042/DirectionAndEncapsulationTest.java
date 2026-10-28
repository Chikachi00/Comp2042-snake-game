package com.comp2042;

import javafx.scene.input.KeyCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import java.awt.Point;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class DirectionAndEncapsulationTest {
    @ParameterizedTest
    @CsvSource({"LEFT, LEFT", "RIGHT, RIGHT", "UP, UP", "DOWN, DOWN"})
    void arrowKeysMapToTheSameMovementDirection(KeyCode key, Direction direction) {
        assertEquals(direction, GameController.directionForKey(key));
    }

    @Test
    void modifyingTheReturnedSnakePointsCannotMoveTheSnake() {
        Obstacle obstacles = new Obstacle(5, 5);
        Snake snake = new Snake(5, 5, false, obstacles);
        Point head = snake.getHead();
        List<Point> body = snake.getBody();
        head.setLocation(0, 0);
        body.get(0).setLocation(0, 0);
        assertThrows(UnsupportedOperationException.class, () -> body.clear());
        assertEquals(new Point(2, 2), snake.getHead());
        assertEquals(List.of(new Point(3, 2)), snake.getBody());
        snake.move(Direction.LEFT);
        assertFalse(snake.getDead());
        assertEquals(new Point(1, 2), snake.getHead());
    }

    @Test
    void modifyingReturnedObstaclePointsCannotChangeCollisions() {
        Obstacle obstacles = new Obstacle(5, 5);
        ObstacleFixtures.place(obstacles, 5, 5, new Point(1, 2));
        List<Point> snapshot = obstacles.getObstacles();
        snapshot.get(0).setLocation(0, 0);
        assertThrows(UnsupportedOperationException.class, () -> snapshot.clear());
        assertEquals(List.of(new Point(1, 2)), obstacles.getObstacles());
        Snake snake = new Snake(5, 5, false, obstacles);
        snake.move(Direction.LEFT);
        assertTrue(snake.getDead());
    }

    @Test
    void nullDirectionIsRejectedBeforeSnakeStateChanges() {
        Snake snake = new Snake(5, 5, false, new Obstacle(5, 5));
        assertThrows(NullPointerException.class, () -> snake.move(null));
        assertEquals(new Point(2, 2), snake.getHead());
        assertEquals(List.of(new Point(3, 2)), snake.getBody());
        assertEquals(Direction.LEFT, snake.getDirection());
    }
}
