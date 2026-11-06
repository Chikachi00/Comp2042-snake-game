package com.comp2042.game;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.CsvSource;
import java.awt.Point;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import static org.junit.jupiter.api.Assertions.*;

class ShortcutGameplayTest {
    private static final long SECOND = 1_000_000_000L;

    @ParameterizedTest @EnumSource(value = Level.class, names = {"TWO", "THREE"})
    void firstCoinRevealsKeysAndOpeningPositionsRemainSafe(Level level) throws Exception {
        GameSession s = new GameSession(new Game(20, 20, false, 'N'), level);
        assertEquals(new Point(10, 3), s.getCoin().getPos());
        assertTrue(s.getShortcuts().stream().noneMatch(ShortcutSnapshot::keyAvailable));
        assertTrue(s.getShortcuts().stream().noneMatch(ShortcutSnapshot::open));
        assertEquals(CoinType.NORMAL, s.getCoin().getType());
        putCoin(s, new Coin(new Point(9, 10)));
        assertEquals(GameSession.StepResult.COIN_EATEN, s.advance(Direction.LEFT));
        assertTrue(s.getShortcuts().stream().allMatch(ShortcutSnapshot::keyAvailable));
        assertEquals(1, s.getLevelCoins());
        if (level == Level.THREE) assertEquals(new Point(10, 16), s.getCoin().getPos());
        assertFalse(s.getObstacle().getCoinExcludedCells().contains(s.getCoin().getPos()));
        assertThrows(UnsupportedOperationException.class, () -> s.getShortcuts().clear());
    }

    @ParameterizedTest @EnumSource(value = CoinType.class, names = {"RAINBOW", "BLUE"})
    void keyMovesTailWithoutChangingScoreComboOrTemporaryEffect(CoinType effect) throws Exception {
        AtomicLong clock = new AtomicLong();
        Game game = new Game(20, 20, false, 'N');
        GameSession s = new GameSession(game, Level.TWO, clock::get);
        putCoin(s, effect == CoinType.RAINBOW ? new RainbowCoin(new Point(9, 10)) : new BlueCoin(new Point(9, 10)));
        s.advance(Direction.LEFT);
        putCoin(s, new Coin(new Point(0, 0)));
        int length = s.getSnake().getBody().size();
        clock.set(2 * SECOND);
        assertEquals(GameSession.StepResult.SHORTCUT_OPENED, walk(s, new Point(15, 14)));
        assertEquals(length, s.getSnake().getBody().size());
        assertEquals(5, game.getScorePoints());
        assertEquals(1, s.getLevelCoins());
        assertEquals(1, s.getComboCount());
        assertEquals(3, s.getComboSecondsRemaining());
        assertEquals(effect == CoinType.RAINBOW ? 13.5 : 8.5, s.getSpeed());
        assertFalse(s.getObstacle().occupies(new Point(10, 6)));
        assertEquals("Shortcut A opened", s.getShortcutMessage());
        s.pause(); clock.set(102 * SECOND);
        assertEquals("Shortcut A opened", s.getShortcutMessage());
        assertEquals(3, s.getComboSecondsRemaining());
        s.resume(); clock.set(103 * SECOND + 500_000_000L);
        assertEquals("", s.getShortcutMessage());
        assertEquals(1.5, s.getComboSecondsRemaining());
        if (effect == CoinType.BLUE) {
            walk(s, new Point(19, 14)); s.advance(Direction.RIGHT);
            assertEquals(new Point(0, 14), s.getSnake().getHead());
            assertFalse(s.isOver());
        }
    }

    @Test void bothKeysAreIndependentAndRestartRestoresClosedHiddenState() throws Exception {
        Game game = new Game(20, 20, false, 'N');
        GameSession s = new GameSession(game, Level.THREE, () -> 0L);
        putCoin(s, new Coin(new Point(9, 10))); s.advance(Direction.LEFT);
        putCoin(s, new Coin(new Point(0, 0)));
        List<ShortcutSnapshot> before = s.getShortcuts();
        assertEquals(GameSession.StepResult.SHORTCUT_OPENED, walk(s, new Point(4, 11)));
        assertFalse(s.getShortcuts().get(0).open()); assertTrue(s.getShortcuts().get(1).open());
        assertEquals(GameSession.StepResult.SHORTCUT_OPENED, walk(s, new Point(15, 9)));
        assertTrue(s.getShortcuts().stream().allMatch(ShortcutSnapshot::open));
        assertTrue(before.stream().noneMatch(ShortcutSnapshot::open));
        assertEquals(5, game.getScorePoints());
        s.reset();
        assertEquals(Level.THREE, s.getLevel()); assertEquals(0, game.getScorePoints());
        assertTrue(s.getShortcuts().stream().noneMatch(v -> v.open() || v.keyAvailable()));
        assertTrue(s.getObstacle().occupies(new Point(10, 6)));
        assertTrue(s.getObstacle().occupies(new Point(10, 13)));
    }

    @Test void closedGateKillsWhileAnOpenedGateAllowsNormalMovement() throws Exception {
        GameSession closed = new GameSession(new Game(20, 20, false, 'N'), Level.TWO);
        walk(closed, new Point(10, 7));
        assertEquals(GameSession.StepResult.DIED, closed.advance(Direction.UP));
        assertTrue(closed.isOver());
        GameSession open = new GameSession(new Game(20, 20, false, 'N'), Level.TWO);
        putCoin(open, new Coin(new Point(9, 10))); open.advance(Direction.LEFT);
        putCoin(open, new Coin(new Point(0, 0))); walk(open, new Point(15, 14));
        walk(open, new Point(10, 7));
        assertEquals(GameSession.StepResult.MOVED, open.advance(Direction.UP));
        assertEquals(new Point(10, 6), open.getSnake().getHead());
        assertFalse(open.isOver());
    }

    @Test void advancingAfterOpeningGateResetsMechanismsAndKeepsRunPoints() throws Exception {
        Game game = new Game(20, 20, false, 'N');
        GameSession s = new GameSession(game, Level.TWO, () -> 0L);
        putCoin(s, new Coin(new Point(9, 10))); s.advance(Direction.LEFT);
        putCoin(s, new Coin(new Point(0, 0))); walk(s, new Point(15, 14));
        // Use a safe straight row to finish the level after the key detour.
        walk(s, new Point(15, 17));
        for (int i = 0; i < 7; i++) {
            Point p = s.getSnake().getHead(); putCoin(s, new Coin(new Point(p.x-1, p.y)));
            s.advance(Direction.LEFT);
        }
        assertEquals(Level.THREE, s.getLevel()); assertTrue(game.getScorePoints() > 40);
        assertEquals(0, s.getLevelCoins());
        assertTrue(s.getShortcuts().stream().noneMatch(v -> v.open() || v.keyAvailable()));
        assertEquals(0, s.getComboSecondsRemaining());
        assertEquals(new Point(10, 3), s.getCoin().getPos());
    }

    @Test void coinPlacementExcludesKeysAndClosedGatesButAllowsOpenedCells() {
        Game game = new Game(20, 20, false, 'N');
        GameSession s = new GameSession(game, Level.THREE);
        CoinFactory factory = new CoinFactory(new Random(42));
        for (int i = 0; i < 100; i++) {
            Coin coin = factory.createCoin(game, s.getSnake(), s.getObstacle()).orElseThrow();
            assertFalse(s.getObstacle().getCoinExcludedCells().contains(coin.getPos()));
        }
        Point occupied = s.getSnake().getHead();
        assertNotEquals(occupied, factory.createCoin(game, s.getSnake(), s.getObstacle(), occupied, false).orElseThrow().getPos());
        List<Shortcut> model = Level.THREE.createShortcuts();
        model.get(0).activate(); model.get(0).collect(new Point(15, 9));
        s.getObstacle().setShortcuts(model.stream().map(Shortcut::snapshot).toList());
        Point gate = new Point(10, 6);
        assertEquals(gate, factory.createCoin(game, s.getSnake(), s.getObstacle(), gate, false).orElseThrow().getPos());
        assertEquals(new Point(15, 9), factory.createCoin(game, s.getSnake(), s.getObstacle(), new Point(15, 9), false).orElseThrow().getPos());
    }

    @ParameterizedTest @EnumSource(Level.class)
    void everyGateCombinationKeepsAllWalkableCellsConnected(Level level) {
        List<Shortcut> shortcuts = level.createShortcuts();
        for (int mask = 0; mask < (1 << shortcuts.size()); mask++) {
            Obstacle obstacle = new Obstacle(20, 20); obstacle.setLayout(level.obstaclePositions());
            List<Shortcut> fresh = level.createShortcuts();
            for (int i = 0; i < fresh.size(); i++) if ((mask & (1 << i)) != 0) {
                ShortcutSnapshot v = fresh.get(i).snapshot(); fresh.get(i).activate(); fresh.get(i).collect(new Point(v.keyX(), v.keyY()));
            }
            obstacle.setShortcuts(fresh.stream().map(Shortcut::snapshot).toList());
            Set<Point> visited = reachable(new Point(10, 10), new HashSet<>(obstacle.getBlockedCells()));
            assertEquals(400 - obstacle.getBlockedCells().size(), visited.size());
            for (Shortcut shortcut : fresh) assertTrue(visited.contains(new Point(shortcut.snapshot().keyX(), shortcut.snapshot().keyY())));
        }
    }

    @Test void sharedCountdownUsesExactlyTheScoringClockAndPauseBoundary() {
        AtomicLong clock = new AtomicLong(); ComboTracker tracker = new ComboTracker(clock::get);
        assertEquals(0, tracker.secondsRemaining()); tracker.recordCoin();
        assertEquals(5, tracker.secondsRemaining()); clock.set(2 * SECOND); tracker.pause();
        clock.set(102 * SECOND); assertEquals(3, tracker.secondsRemaining()); tracker.resume();
        clock.set(105 * SECOND); assertEquals(0, tracker.secondsRemaining()); assertEquals(1, tracker.count());
        clock.incrementAndGet(); assertEquals(0, tracker.count()); tracker.reset();
        assertEquals(0, tracker.secondsRemaining());
    }

    @ParameterizedTest
    @CsvSource({"ONE, false, 255", "ONE, true, 255", "TWO, false, 210", "TWO, true, 210", "THREE, false, 120", "THREE, true, 120"})
    void entireRunsWorkBothWithAllKeysAndWithoutAnyKeys(Level starting, boolean takeKeys, int expectedScore) throws Exception {
        Game game = new Game(20, 20, false, 'N');
        GameSession s = new GameSession(game, starting, () -> 0L);
        Field factoryField = GameSession.class.getDeclaredField("coinFactory"); factoryField.setAccessible(true);
        Field randomField = CoinFactory.class.getDeclaredField("random"); randomField.setAccessible(true);
        ((Random) randomField.get(factoryField.get(s))).setSeed(42);
        if (starting == Level.ONE) putCoin(s, new Coin(new Point(2, 2)));
        for (int objectives = 0; !s.isOver(); objectives++) {
            assertTrue(objectives < 40);
            Optional<ShortcutSnapshot> key = takeKeys ? s.getShortcuts().stream().filter(ShortcutSnapshot::keyAvailable).findFirst() : Optional.empty();
            Point target = key.map(v -> new Point(v.keyX(), v.keyY())).orElseGet(() -> s.getCoin().getPos());
            walk(s, target, !takeKeys);
        }
        assertFalse(s.getSnake().getDead()); assertTrue(game.gameWon()); assertEquals(Level.THREE, s.getLevel());
        assertEquals(expectedScore, game.getScorePoints());
        assertTrue(s.getShortcuts().stream().allMatch(v -> v.open() == takeKeys));
    }

    static void putCoin(GameSession s, Coin coin) throws Exception {
        Field f = GameSession.class.getDeclaredField("coin"); f.setAccessible(true); f.set(s, coin);
    }

    static GameSession.StepResult walk(GameSession s, Point target) {
        return walk(s, target, false);
    }

    static GameSession.StepResult walk(GameSession s, Point target, boolean avoidKeys) {
        GameSession.StepResult result = GameSession.StepResult.MOVED;
        for (int steps = 0; !s.getSnake().getHead().equals(target); steps++) {
            assertTrue(steps < 500, "Route exceeded bound");
            Point head = s.getSnake().getHead();
            Set<Point> blocked = new HashSet<>(s.getObstacle().getBlockedCells()); blocked.addAll(s.getSnake().getBody());
            if (avoidKeys) s.getShortcuts().stream().filter(v -> !v.open())
                    .forEach(v -> blocked.add(new Point(v.keyX(), v.keyY())));
            Map<Point, Point> previous = previous(head, blocked);
            assertTrue(previous.containsKey(target), "No route to " + target);
            Point next = target;
            while (!head.equals(previous.get(next))) next = previous.get(next);
            final Point step = next;
            Direction direction = Arrays.stream(Direction.values()).filter(d -> head.x+d.dx()==step.x && head.y+d.dy()==step.y).findFirst().orElseThrow();
            result = s.advance(direction); assertFalse(s.getSnake().getDead());
            if (result == GameSession.StepResult.LEVEL_COMPLETED || result == GameSession.StepResult.WON) return result;
        }
        return result;
    }

    static Set<Point> reachable(Point start, Set<Point> blocked) { return previous(start, blocked).keySet(); }
    static Map<Point, Point> previous(Point start, Set<Point> blocked) {
        Map<Point, Point> previous = new HashMap<>(); previous.put(start, null);
        ArrayDeque<Point> queue = new ArrayDeque<>(); queue.add(start);
        while (!queue.isEmpty()) {
            Point p = queue.remove();
            for (Direction d : Direction.values()) {
                Point n = new Point(p.x+d.dx(), p.y+d.dy());
                if (n.x>=0 && n.x<20 && n.y>=0 && n.y<20 && !blocked.contains(n) && !previous.containsKey(n)) {
                    previous.put(n, p); queue.add(n);
                }
            }
        }
        return previous;
    }
}
