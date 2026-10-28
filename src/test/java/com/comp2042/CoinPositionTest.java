package com.comp2042;

import org.junit.jupiter.api.Test;

import java.awt.Point;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CoinPositionTest {
    @Test
    void returnsEmptyPromptlyWhenBoardIsFull() {
        Optional<Point> result = assertTimeoutPreemptively(Duration.ofSeconds(2), () ->
                Coin.findFreePosition(2, 2, new Point(0, 0), List.of(new Point(1, 0)),
                        List.of(new Point(0, 1), new Point(1, 1)), new Random(1)));

        assertTrue(result.isEmpty());
    }

    @Test
    void selectsTheOnlyAvailableCell() {
        Optional<Point> result = Coin.findFreePosition(2, 2, new Point(0, 0),
                List.of(new Point(1, 0)), List.of(new Point(0, 1)), new Random(1));

        assertEquals(Optional.of(new Point(1, 1)), result);
    }

    @Test
    void selectsOnlyUnoccupiedCellsInsideTheBoard() {
        Point head = new Point(1, 1);
        List<Point> body = List.of(new Point(1, 2), new Point(1, 3));
        List<Point> obstacles = List.of(new Point(0, 0), new Point(3, 3));
        Set<Point> occupied = new HashSet<>(body);
        occupied.add(head);
        occupied.addAll(obstacles);
        Random random = new Random(2042);

        for (int i = 0; i < 100; i++) {
            Point position = Coin.findFreePosition(4, 4, head, body, obstacles, random).orElseThrow();
            assertTrue(position.x >= 0 && position.x < 4 && position.y >= 0 && position.y < 4);
            assertFalse(occupied.contains(position));
        }
    }

    @Test
    void leavesSnakeAndObstaclePositionsUnchanged() {
        Point head = new Point(1, 1);
        List<Point> body = new ArrayList<>(List.of(new Point(1, 2)));
        List<Point> obstacles = new ArrayList<>(List.of(new Point(0, 0)));

        Coin.findFreePosition(3, 3, head, body, obstacles, new Random(1));

        assertEquals(new Point(1, 1), head);
        assertEquals(List.of(new Point(1, 2)), body);
        assertEquals(List.of(new Point(0, 0)), obstacles);
    }

    @Test
    void returnsEmptyForAnEmptyBoard() {
        assertTrue(Coin.findFreePosition(0, 2, new Point(0, 0),
                List.of(), List.of(), new Random(1)).isEmpty());
    }
}