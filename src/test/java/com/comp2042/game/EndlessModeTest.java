package com.comp2042.game;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.awt.Point;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class EndlessModeTest {
    @Test
    void startsAFreshClassicRoundWithoutChallengeState() {
        Game game = new Game(20, 20, true, 'N');
        game.addPoints(100);
        game.markBoardFull();
        GameSession session = new GameSession(game);

        assertEquals(GameMode.ENDLESS, session.getMode());
        assertNull(session.getLevel());
        assertEquals(0, game.getScorePoints());
        assertFalse(session.isOver());
        assertTrue(session.getObstacle().getObstacles().isEmpty());
        assertTrue(session.getShortcuts().isEmpty());
        assertEquals(CoinType.NORMAL, session.getCoin().getType());
        assertFreeCoin(session);
    }

    @Test
    void continuesPastAllChallengeTargetsWithOriginalScoringAndNoCombo() throws Exception {
        Game game = new Game(20, 20, false, 'N');
        GameSession session = new GameSession(game);
        Snake original = session.getSnake();
        for (int i = 0; i < 16; i++) {
            Direction direction = i < 9 ? Direction.LEFT : Direction.UP;
            Point head = session.getSnake().getHead();
            putCoin(session, new Coin(new Point(head.x + direction.dx(), head.y + direction.dy())));
            assertEquals(GameSession.StepResult.COIN_EATEN, session.advance(direction));
            assertSame(original, session.getSnake(), "Endless play must not replace the snake at 5, 8 or 10 coins");
            assertFalse(session.isOver());
            assertNull(session.getLevel());
            assertEquals(5 * (i + 1), game.getScorePoints());
            assertEquals(0, session.getComboCount());
            assertEquals(0, session.getComboBonus());
            assertEquals(0, session.getComboSecondsRemaining());
            assertTrue(session.getShortcuts().isEmpty());
            assertFreeCoin(session);
        }
        assertEquals(17, session.getSnake().getBody().size());
    }

    @ParameterizedTest
    @EnumSource(CoinType.class)
    void preservesOriginalCoinEffectsAndRestartsInTheSameMode(CoinType type) throws Exception {
        Game game = new Game(20, 20, true, 'N');
        GameSession session = new GameSession(game);
        Point pickup = new Point(9, 10);
        Coin coin = switch (type) {
            case NORMAL -> new Coin(pickup);
            case GOLD -> new GoldCoin(pickup);
            case BLUE -> new BlueCoin(pickup);
            case RAINBOW -> new RainbowCoin(pickup);
            case SILVER -> new SilverCoin(pickup);
        };
        putCoin(session, coin);
        assertEquals(GameSession.StepResult.COIN_EATEN, session.advance(Direction.LEFT));
        assertEquals(type == CoinType.GOLD ? 15 : 5, game.getScorePoints());
        assertEquals(type == CoinType.RAINBOW ? 13.5 : 8.5, session.getSpeed());
        assertEquals(type == CoinType.SILVER, !session.getObstacle().getObstacles().isEmpty());
        assertFreeCoin(session);
        if (type == CoinType.BLUE) {
            putCoin(session, new Coin(new Point(1, 1)));
            for (int i = 0; i < 10; i++) session.advance(Direction.LEFT);
            assertFalse(session.isOver());
            assertEquals(new Point(19, 10), session.getSnake().getHead());
        }

        Snake previous = session.getSnake();
        session.pause();
        session.reset();
        assertNotSame(previous, session.getSnake());
        assertEquals(GameMode.ENDLESS, session.getMode());
        assertNull(session.getLevel());
        assertEquals(0, game.getScorePoints());
        assertFalse(session.isOver());
        assertEquals(new Point(10, 10), session.getSnake().getHead());
        assertEquals(1, session.getSnake().getBody().size());
        assertEquals(game.getSpeed(), session.getSpeed());
        assertTrue(session.getObstacle().getObstacles().isEmpty());
        assertTrue(session.getShortcuts().isEmpty());
        assertEquals(0, session.getComboCount());
        assertEquals(CoinType.NORMAL, session.getCoin().getType());
        putCoin(session, new Coin(new Point(1, 1)));
        for (int i = 0; i < 10; i++) session.advance(Direction.LEFT);
        assertEquals(GameSession.StepResult.DIED, session.advance(Direction.LEFT),
                "Restart must clear temporary wrapping and restore normal walls");
    }

    private static void putCoin(GameSession session, Coin coin) throws Exception {
        Field field = GameSession.class.getDeclaredField("coin");
        field.setAccessible(true);
        field.set(session, coin);
    }

    private static void assertFreeCoin(GameSession session) {
        Point position = session.getCoin().getPos();
        assertNotEquals(session.getSnake().getHead(), position);
        assertFalse(session.getSnake().getBody().contains(position));
        assertFalse(session.getObstacle().occupies(position));
    }
}
