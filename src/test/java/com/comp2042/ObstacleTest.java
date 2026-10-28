package com.comp2042;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.awt.Point;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies obstacle placement, capacity, and crowded-board behavior without JavaFX windows.
 */
class ObstacleTest {

    @Test
    void keepsAllPositionsInsideTheBoard() {
        Obstacle obstacle = emptyBoard();
        generate(obstacle, 50);

        for (Point point : obstacle.getObstacles()) {
            assertTrue(point.x >= 0 && point.x < 20 && point.y >= 0 && point.y < 20,
                    () -> "Obstacle is outside the board: " + point);
        }
    }

    @Test
    void avoidsTheSnakeHeadBodyAndCoin() {
        Obstacle obstacle = emptyBoard();
        generate(obstacle, 30);
        Set<Point> occupied = Set.of(new Point(10, 10), new Point(11, 10),
                new Point(12, 10), new Point(0, 0));

        assertTrue(obstacle.getObstacles().stream().noneMatch(occupied::contains));
    }

    @Test
    void doesNotGenerateDuplicatePositions() {
        Obstacle obstacle = new Obstacle(3, 3, new Random(2042));
        obstacle.newObstacles(new Point(1, 1), List.of(new Point(2, 1)),
                new Point(0, 0), 20);

        assertEquals(obstacle.getObstacles().size(), new HashSet<>(obstacle.getObstacles()).size());
    }

    @Test
    void keepsNewObstaclesSeparateFromPreviousBatches() {
        Obstacle obstacle = new Obstacle(5, 5, new Random(2042));
        Point head = new Point(2, 2);
        List<Point> body = List.of(new Point(3, 2));
        Point coin = new Point(0, 0);
        obstacle.newObstacles(head, body, coin, 10);
        List<Point> previous = new ArrayList<>(obstacle.getObstacles());
        obstacle.newObstacles(head, body, coin, 10);

        assertTrue(obstacle.getObstacles().containsAll(previous));
        List<Point> added = obstacle.getObstacles().subList(previous.size(), obstacle.getObstacles().size());
        assertTrue(added.stream().noneMatch(previous::contains));
    }

    @Test
    void limitsOneLargeRequestToFiftyObstacles() {
        Obstacle obstacle = emptyBoard();
        generate(obstacle, 100);

        assertEquals(50, obstacle.getObstacles().size());
    }

    @Test
    void enforcesTheLimitAcrossSeveralRequests() {
        Obstacle obstacle = emptyBoard();
        generate(obstacle, 45);
        generate(obstacle, 20);

        assertEquals(50, obstacle.getObstacles().size());
    }

    @Test
    void addsNothingWhenAlreadyAtTheLimit() {
        Obstacle obstacle = emptyBoard();
        generate(obstacle, 50);
        List<Point> previous = new ArrayList<>(obstacle.getObstacles());
        generate(obstacle, 1);

        assertEquals(previous, obstacle.getObstacles());
    }

    @Test
    void addsTheRequestedNumberWhenEnoughSpaceExists() {
        Obstacle obstacle = emptyBoard();
        generate(obstacle, 7);

        assertEquals(7, obstacle.getObstacles().size());
    }

    @Test
    void fillsOnlyTheAvailableCellsWhenSpaceIsLimited() {
        Obstacle obstacle = new Obstacle(3, 3, new Random(2042));
        assertTimeoutPreemptively(Duration.ofSeconds(2), () ->
                obstacle.newObstacles(new Point(1, 1), List.of(new Point(2, 1)),
                        new Point(0, 0), 20));

        assertEquals(6, obstacle.getObstacles().size());
        assertEquals(6, new HashSet<>(obstacle.getObstacles()).size());
    }

    @Test
    void returnsWithoutAddingObstaclesWhenTheBoardIsFull() {
        Obstacle obstacle = new Obstacle(2, 2, new Random(2042));
        assertTimeoutPreemptively(Duration.ofSeconds(2), () ->
                obstacle.newObstacles(new Point(0, 0),
                        List.of(new Point(1, 0), new Point(1, 1)), new Point(0, 1), 10));

        assertTrue(obstacle.getObstacles().isEmpty());
    }

    @Test
    void usesTheLastFreeCellOnlyOnce() {
        Obstacle obstacle = new Obstacle(2, 3, new Random(2042));
        assertTimeoutPreemptively(Duration.ofSeconds(2), () ->
                obstacle.newObstacles(new Point(0, 0),
                        List.of(new Point(1, 0), new Point(0, 1), new Point(1, 1)),
                        new Point(0, 2), 10));

        assertEquals(List.of(new Point(1, 2)), obstacle.getObstacles());
    }

    @ParameterizedTest(name = "narrow board: {0} by {1}")
    @CsvSource({"1, 6", "6, 1"})
    void supportsSingleRowAndSingleColumnBoards(int width, int height) {
        Obstacle obstacle = new Obstacle(width, height, new Random(2042));
        boolean vertical = width == 1;
        Point body = vertical ? new Point(0, 1) : new Point(1, 0);
        Point coin = vertical ? new Point(0, 2) : new Point(2, 0);
        obstacle.newObstacles(new Point(0, 0), List.of(body), coin, 3);

        assertEquals(3, obstacle.getObstacles().size());
        for (Point point : obstacle.getObstacles()) {
            assertTrue(point.x >= 0 && point.x < width && point.y >= 0 && point.y < height);
        }
        assertEquals(3, new HashSet<>(obstacle.getObstacles()).size());
    }

    @ParameterizedTest(name = "request {0} leaves the board unchanged")
    @ValueSource(ints = {0, -1, -50})
    void ignoresNonPositiveRequests(int amount) {
        Obstacle obstacle = emptyBoard();
        generate(obstacle, 5);
        List<Point> previous = new ArrayList<>(obstacle.getObstacles());
        generate(obstacle, amount);

        assertEquals(previous, obstacle.getObstacles());
    }

    @Test
    void resetRestoresTheFullObstacleAllowance() {
        Obstacle obstacle = emptyBoard();
        generate(obstacle, 50);
        obstacle.reset();
        assertTrue(obstacle.getObstacles().isEmpty());
        generate(obstacle, 50);

        assertEquals(50, obstacle.getObstacles().size());
    }

    @Test
    void leavesTheSuppliedPositionsUnchanged() {
        Obstacle obstacle = emptyBoard();
        Point head = new Point(10, 10);
        List<Point> body = new ArrayList<>(List.of(new Point(11, 10), new Point(12, 10)));
        Point coin = new Point(0, 0);
        obstacle.newObstacles(head, body, coin, 10);

        assertEquals(new Point(10, 10), head);
        assertEquals(List.of(new Point(11, 10), new Point(12, 10)), body);
        assertEquals(new Point(0, 0), coin);
    }

    @Test
    void addsNothingAfterExistingObstaclesFillTheRemainingSpace() {
        Obstacle obstacle = new Obstacle(3, 3, new Random(2042));
        obstacle.newObstacles(new Point(1, 1), List.of(new Point(2, 1)), new Point(0, 0), 6);
        List<Point> previous = new ArrayList<>(obstacle.getObstacles());
        assertTimeoutPreemptively(Duration.ofSeconds(2), () ->
                obstacle.newObstacles(new Point(1, 1), List.of(new Point(2, 1)),
                        new Point(0, 0), 5));

        assertFalse(previous.isEmpty());
        assertEquals(previous, obstacle.getObstacles());
    }

    private Obstacle emptyBoard() {
        return new Obstacle(20, 20, new Random(2042));
    }

    private void generate(Obstacle obstacle, int amount) {
        obstacle.newObstacles(new Point(10, 10),
                List.of(new Point(11, 10), new Point(12, 10)), new Point(0, 0), amount);
    }
}
