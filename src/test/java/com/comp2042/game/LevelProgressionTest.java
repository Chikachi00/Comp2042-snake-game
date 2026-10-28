package com.comp2042.game;

import org.junit.jupiter.api.Test;
import java.awt.Point;
import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class LevelProgressionTest {
    private static GameSession slowSession(Game game, Level level) {
        java.util.concurrent.atomic.AtomicLong clock = new java.util.concurrent.atomic.AtomicLong();
        return new GameSession(game, level, () -> clock.getAndAdd(6_000_000_000L));
    }
    private static void coinAhead(GameSession session) throws Exception {
        Point head = session.getSnake().getHead();
        Field coin = GameSession.class.getDeclaredField("coin");
        coin.setAccessible(true);
        coin.set(session, new Coin(new Point(head.x - 1, head.y)));
    }
    private static GameSession.StepResult eatLeft(GameSession session) throws Exception {
        coinAhead(session);
        return session.advance(Direction.LEFT);
    }
    @Test void advancesThroughLevelsAndKeepsScore() throws Exception {
        Game game = new Game(20, 20, false, 'N');
        GameSession session = slowSession(game, Level.ONE);
        for (int i = 0; i < 4; i++) assertEquals(GameSession.StepResult.COIN_EATEN, eatLeft(session));
        assertEquals(GameSession.StepResult.LEVEL_COMPLETED, eatLeft(session));
        assertEquals(Level.TWO, session.getLevel());
        assertEquals(25, game.getScorePoints());
        assertEquals(0, session.getLevelCoins());
        assertEquals(new Point(10, 10), session.getSnake().getHead());
        for (int i = 0; i < 7; i++) assertEquals(GameSession.StepResult.COIN_EATEN, eatLeft(session));
        assertEquals(GameSession.StepResult.LEVEL_COMPLETED, eatLeft(session));
        assertEquals(Level.THREE, session.getLevel());
        assertEquals(65, game.getScorePoints());
        for (int i = 0; i < 9; i++) assertEquals(GameSession.StepResult.COIN_EATEN, eatLeft(session));
        assertEquals(GameSession.StepResult.WON, eatLeft(session));
        assertTrue(session.isOver());
        assertEquals(115, game.getScorePoints());
        assertEquals(GameSession.StepResult.ALREADY_ENDED, session.advance(Direction.LEFT));
    }
    @Test void selectedStartingLevelAndRestartArePreserved() throws Exception {
        Game game = new Game(20, 20, false, 'N');
        GameSession session = slowSession(game, Level.TWO);
        assertEquals(8, session.getObstacle().getObstacles().size());
        for (int i = 0; i < 8; i++) eatLeft(session);
        assertEquals(Level.THREE, session.getLevel());
        session.reset();
        assertEquals(Level.TWO, session.getLevel());
        assertEquals(0, session.getLevelCoins());
        assertEquals(0, game.getScorePoints());
        assertEquals(8, session.getObstacle().getObstacles().size());
        assertFalse(session.isOver());
    }
    @Test void startingAtFinalLevelWinsAfterItsObjective() throws Exception {
        Game game = new Game(20, 20, false, 'N');
        GameSession session = slowSession(game, Level.THREE);
        for (int i = 0; i < 9; i++) assertEquals(GameSession.StepResult.COIN_EATEN, eatLeft(session));
        assertEquals(GameSession.StepResult.WON, eatLeft(session));
        assertEquals(50, game.getScorePoints());
    }
    @Test void layoutsAreReachableAndDoNotCoverTheInitialSnakeOrCoin() {
        for (Level level : Level.values()) {
            GameSession session = new GameSession(new Game(20, 20, true, 'N'), level);
            Set<Point> blocks = new HashSet<>(session.getObstacle().getObstacles());
            Point start = session.getSnake().getHead();
            assertFalse(blocks.contains(start));
            assertTrue(session.getSnake().getBody().stream().noneMatch(blocks::contains));
            assertFalse(blocks.contains(session.getCoin().getPos()));
            Set<Point> visited = new HashSet<>();
            ArrayDeque<Point> queue = new ArrayDeque<>();
            visited.add(start); queue.add(start);
            while (!queue.isEmpty()) {
                Point p = queue.remove();
                for (int[] d : new int[][]{{0,1},{0,-1},{1,0},{-1,0}}) {
                    Point next = new Point(p.x+d[0], p.y+d[1]);
                    if (next.x>=0 && next.x<20 && next.y>=0 && next.y<20 && !blocks.contains(next) && visited.add(next)) queue.add(next);
                }
            }
            assertEquals(400-blocks.size(), visited.size());
        }
    }
    @Test void rejectsNonstandardBoardForDesignedLayouts() {
        assertThrows(IllegalArgumentException.class, () -> new GameSession(new Game(5,5,true,'N'), Level.TWO));
    }
}
