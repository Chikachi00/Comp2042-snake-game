package com.comp2042.game;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GameTest {
    @ParameterizedTest
    @ValueSource(chars = {'?', 'X', 'n', '\0'})
    void rejectsUnknownDifficultyInsteadOfCreatingAStationaryGame(char difficulty) {
        assertThrows(IllegalArgumentException.class, () -> new Game(20, 20, true, difficulty));
    }

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
