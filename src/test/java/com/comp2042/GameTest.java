package com.comp2042;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameTest {
    @Test
    void accountsForEveryAddedObstacleInTheWinThreshold() {
        Game game = new Game(3, 3, true, 'N');
        game.decMaxScore(2);
        advanceScore(game, 4);
        assertFalse(game.gameWon());
        game.incScore();
        assertTrue(game.gameWon());
    }

    @Test
    void noNewObstaclesDoNotChangeTheWinThreshold() {
        Game game = new Game(3, 3, true, 'N');
        game.decMaxScore(0);
        advanceScore(game, 7);
        assertTrue(game.gameWon());
    }

    @Test
    void countsSeparateBatchesOfNewObstacles() {
        Game game = new Game(3, 3, true, 'N');
        game.decMaxScore(1);
        game.decMaxScore(2);
        advanceScore(game, 4);
        assertTrue(game.gameWon());
    }

    @Test
    void resetRestoresOriginalWinThresholdAndPoints() {
        Game game = new Game(3, 3, true, 'N');
        game.decMaxScore(2);
        advanceScore(game, 3);
        game.addPoints(15);
        game.resetScore();
        assertEquals(0, game.getScorePoints());
        advanceScore(game, 6);
        assertFalse(game.gameWon());
        game.incScore();
        assertTrue(game.gameWon());
    }

    @Test
    void fullBoardWinStateIsClearedOnReset() {
        Game game = new Game(3, 3, true, 'N');
        game.markBoardFull();
        assertTrue(game.gameWon());

        game.resetScore();
        assertFalse(game.gameWon());
    }

    @Test
    void tenObstaclesOnTwentyByTwentyBoardRequire388Coins() {
        Game game = new Game(20, 20, true, 'N');
        game.decMaxScore(10);
        advanceScore(game, 387);
        assertFalse(game.gameWon());
        game.incScore();
        assertTrue(game.gameWon());
    }

    private void advanceScore(Game game, int count) {
        for (int i = 0; i < count; i++) {
            game.incScore();
        }
    }
}