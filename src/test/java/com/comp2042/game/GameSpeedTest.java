package com.comp2042.game;

import java.awt.Point;
import java.lang.reflect.Field;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

/** Verifies selected speeds through real endless-session movement, effects and resets. */
class GameSpeedTest {
    @ParameterizedTest
    @CsvSource({"SLOW, 5.0", "MEDIUM, 8.5", "FAST, 13.0"})
    void selectedSpeedStartsWithNormalWallsAndNoChallengeState(GameSpeed speed, double expected) {
        Game game = new Game(20, 20, true, speed);
        GameSession session = new GameSession(game);
        assertEquals(expected, session.getSpeed());
        assertEquals(GameMode.ENDLESS, session.getMode());
        assertNull(session.getLevel());
        assertFalse(game.isNoBorder(), "Slow changes movement rate, not wall collisions");
        assertTrue(game.isPowerUp());
        assertEquals(0, session.getScorePoints());
    }

    @ParameterizedTest
    @EnumSource(GameSpeed.class)
    void rainbowAddsFiveThenTheNextCoinAndRestartRestoreTheChosenSpeed(GameSpeed speed)
            throws Exception {
        Game game = new Game(20, 20, true, speed);
        GameSession session = new GameSession(game);
        putCoin(session, new RainbowCoin(new Point(9, 10)));
        assertEquals(GameSession.StepResult.COIN_EATEN, session.advance(Direction.LEFT));
        assertEquals(speed.movesPerSecond() + 5, session.getSpeed());
        assertEquals(5, session.getScorePoints());

        putCoin(session, new Coin(new Point(8, 10)));
        assertEquals(GameSession.StepResult.COIN_EATEN, session.advance(Direction.LEFT));
        assertEquals(speed.movesPerSecond(), session.getSpeed());
        assertEquals(10, session.getScorePoints());

        putCoin(session, new RainbowCoin(new Point(7, 10)));
        session.advance(Direction.LEFT);
        session.pause();
        session.reset();
        assertEquals(speed.movesPerSecond(), session.getSpeed());
        assertEquals(0, session.getScorePoints());
        assertEquals(new Point(10, 10), session.getSnake().getHead());
        assertEquals(CoinType.NORMAL, session.getCoin().getType());
        assertFalse(session.isOver());
    }

    @ParameterizedTest
    @EnumSource(GameSpeed.class)
    void everySpeedRetainsNormalWallCollisions(GameSpeed speed) throws Exception {
        GameSession session = new GameSession(new Game(20, 20, true, speed));
        putCoin(session, new Coin(new Point(19, 19)));
        for (int move = 0; move < 10; move++) {
            assertEquals(GameSession.StepResult.MOVED, session.advance(Direction.LEFT));
        }
        assertEquals(GameSession.StepResult.DIED, session.advance(Direction.LEFT));
    }

    @Test
    void mediumMatchesTheExistingNormalSpeedAndNullIsRejected() {
        assertEquals(new Game(20, 20, true, 'N').getSpeed(),
                new Game(20, 20, true, GameSpeed.MEDIUM).getSpeed());
        assertThrows(NullPointerException.class, () -> new Game(20, 20, true, (GameSpeed) null));
    }

    private static void putCoin(GameSession session, Coin coin) throws Exception {
        Field field = GameSession.class.getDeclaredField("coin");
        field.setAccessible(true);
        field.set(session, coin);
    }
}
