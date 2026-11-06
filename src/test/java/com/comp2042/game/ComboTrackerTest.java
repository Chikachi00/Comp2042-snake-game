package com.comp2042.game;

import org.junit.jupiter.api.Test;
import java.awt.Point;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicLong;
import static org.junit.jupiter.api.Assertions.*;

class ComboTrackerTest {
    private static final long SECOND = 1_000_000_000L;
    @Test void firstCoinAndBoundaryAwardExpectedBonuses() {
        AtomicLong clock = new AtomicLong(); ComboTracker combo = new ComboTracker(clock::get);
        assertEquals(0, combo.recordCoin());
        clock.set(5 * SECOND);
        assertEquals(2, combo.recordCoin());
        assertEquals(2, combo.count());
        clock.addAndGet(5 * SECOND + 1);
        assertEquals(0, combo.count());
        assertEquals(0, combo.recordCoin());
        assertEquals(1, combo.count());
    }
    @Test void bonusIsCappedAtTenPoints() {
        ComboTracker combo = new ComboTracker(() -> 0L);
        int[] expected = {0, 2, 4, 6, 8, 10, 10, 10};
        for (int points : expected) assertEquals(points, combo.recordCoin());
        assertEquals(8, combo.count());
        assertEquals(10, combo.lastBonus());
    }
    @Test void pauseTimeDoesNotExpireTheCombo() {
        AtomicLong clock = new AtomicLong(); ComboTracker combo = new ComboTracker(clock::get);
        combo.recordCoin(); clock.set(3 * SECOND); combo.pause();
        clock.set(103 * SECOND); assertEquals(1, combo.count()); combo.resume();
        clock.set(105 * SECOND); assertEquals(2, combo.recordCoin());
        clock.set(111 * SECOND); assertEquals(0, combo.recordCoin());
    }
    @Test void repeatedPauseAndResumeDoNotDoubleCountTime() {
        AtomicLong clock = new AtomicLong(); ComboTracker combo = new ComboTracker(clock::get);
        combo.recordCoin(); clock.set(2 * SECOND); combo.pause(); combo.pause();
        clock.set(12 * SECOND); combo.resume(); combo.resume(); clock.set(15 * SECOND);
        assertEquals(2, combo.recordCoin());
    }
    @Test void resetClearsComboAndPauseState() {
        AtomicLong clock = new AtomicLong(); ComboTracker combo = new ComboTracker(clock::get);
        combo.recordCoin(); combo.pause(); clock.set(100 * SECOND); combo.reset();
        assertEquals(0, combo.count()); assertEquals(0, combo.recordCoin());
        clock.addAndGet(SECOND); assertEquals(2, combo.recordCoin());
    }
    @Test void sessionAwardsComboAndClearsItOnRestart() throws Exception {
        AtomicLong clock = new AtomicLong(); Game game = new Game(20, 20, false, 'N');
        GameSession session = new GameSession(game, Level.ONE, clock::get);
        Field coin = GameSession.class.getDeclaredField("coin"); coin.setAccessible(true);
        Point head = session.getSnake().getHead(); coin.set(session, new Coin(new Point(head.x-1, head.y)));
        session.advance(Direction.LEFT);
        clock.set(SECOND); head = session.getSnake().getHead(); coin.set(session, new GoldCoin(new Point(head.x-1, head.y)));
        session.advance(Direction.LEFT);
        assertEquals(22, game.getScorePoints());
        assertEquals(2, session.getComboCount()); assertEquals(2, session.getComboBonus());
        session.reset();
        assertEquals(0, session.getComboCount()); assertEquals(0, game.getScorePoints());
    }
    @Test void advancingLevelClearsComboButKeepsBonusPoints() throws Exception {
        AtomicLong clock = new AtomicLong(); Game game = new Game(20, 20, false, 'N');
        GameSession session = new GameSession(game, Level.ONE, clock::get);
        Field coin = GameSession.class.getDeclaredField("coin"); coin.setAccessible(true);
        for (int i=0;i<5;i++) {
            Point head = session.getSnake().getHead(); coin.set(session, new Coin(new Point(head.x-1, head.y)));
            session.advance(Direction.LEFT); clock.addAndGet(SECOND);
        }
        assertEquals(Level.TWO, session.getLevel());
        assertEquals(45, game.getScorePoints());
        assertEquals(0, session.getComboCount());
    }
}
