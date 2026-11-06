package com.comp2042.persistence;

import com.comp2042.game.GameMode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

class HighScoreStoreTest {
    @TempDir Path directory;

    @Test void missingFileStartsAtZeroWithoutCreatingData() {
        Path file = directory.resolve("nested/scores.properties");
        HighScoreStore scores = new HighScoreStore(file);
        assertEquals(0, scores.getBest(GameMode.ENDLESS));
        assertEquals(0, scores.getBest(GameMode.CHALLENGE));
        assertFalse(Files.exists(file.getParent()));
    }

    @Test void modeRecordsSurviveReopeningIndependently() {
        Path file = directory.resolve("nested/scores.properties");
        HighScoreStore scores = new HighScoreStore(file);
        assertTrue(scores.record(GameMode.ENDLESS, 125));
        assertTrue(scores.record(GameMode.CHALLENGE, 255));
        HighScoreStore reopened = new HighScoreStore(file);
        assertEquals(125, reopened.getBest(GameMode.ENDLESS));
        assertEquals(255, reopened.getBest(GameMode.CHALLENGE));
        assertTrue(reopened.record(GameMode.ENDLESS, 200));
        assertEquals(255, new HighScoreStore(file).getBest(GameMode.CHALLENGE));
    }

    @Test void lowerAndEqualScoresNeitherReplaceNorRewriteARecord() throws Exception {
        Path file = directory.resolve("scores.properties");
        HighScoreStore scores = new HighScoreStore(file);
        scores.record(GameMode.ENDLESS, 100);
        byte[] original = Files.readAllBytes(file);
        assertFalse(scores.record(GameMode.ENDLESS, 99));
        assertFalse(scores.record(GameMode.ENDLESS, 100));
        assertFalse(scores.record(GameMode.CHALLENGE, 0));
        assertArrayEquals(original, Files.readAllBytes(file));
        assertEquals(100, new HighScoreStore(file).getBest(GameMode.ENDLESS));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "-1", "not-a-number", "2147483648"})
    void invalidEntryDoesNotDiscardTheOtherMode(String invalid) throws Exception {
        Path file = directory.resolve("scores.properties");
        Files.writeString(file, "ENDLESS=" + invalid + "\nCHALLENGE=255\nUNKNOWN=999\n");
        HighScoreStore scores = new HighScoreStore(file);
        assertEquals(0, scores.getBest(GameMode.ENDLESS));
        assertEquals(255, scores.getBest(GameMode.CHALLENGE));
        scores.record(GameMode.ENDLESS, 5);
        assertEquals(255, new HighScoreStore(file).getBest(GameMode.CHALLENGE));
    }

    @Test void malformedFileCanBeReplacedByANewValidRecord() throws Exception {
        Path file = directory.resolve("scores.properties");
        Files.writeString(file, "ENDLESS=\\uGGGG");
        quietly(() -> {
            HighScoreStore scores = assertDoesNotThrow(() -> new HighScoreStore(file));
            assertEquals(0, scores.getBest(GameMode.ENDLESS));
            scores.record(GameMode.ENDLESS, 15);
            assertEquals(15, new HighScoreStore(file).getBest(GameMode.ENDLESS));
        });
    }

    @Test void failedSaveKeepsTheRecordInMemoryAndFlushCanRecover() throws Exception {
        Path blocker = directory.resolve("blocked");
        Files.writeString(blocker, "A file cannot be used as a parent directory");
        Path file = blocker.resolve("scores.properties");
        quietly(() -> {
            HighScoreStore scores = new HighScoreStore(file);
            assertTrue(scores.record(GameMode.CHALLENGE, 100));
            assertEquals(100, scores.getBest(GameMode.CHALLENGE));
            Files.delete(blocker);
            scores.flush();
            assertEquals(100, new HighScoreStore(file).getBest(GameMode.CHALLENGE));
            try (var children = Files.list(blocker)) {
                assertEquals(1, children.count(), "Temporary save files must be cleaned up");
            }
        });
    }

    @Test void inMemoryRoundsAreIsolatedAndRejectInvalidScores() {
        HighScoreStore first = HighScoreStore.inMemory();
        first.record(GameMode.ENDLESS, 20);
        first.flush();
        assertEquals(20, first.getBest(GameMode.ENDLESS));
        assertEquals(0, HighScoreStore.inMemory().getBest(GameMode.ENDLESS));
        assertThrows(IllegalArgumentException.class, () -> first.record(GameMode.ENDLESS, -1));
        assertThrows(NullPointerException.class, () -> first.record(null, 5));
    }

    private static void quietly(CheckedAction action) throws Exception {
        Logger logger = Logger.getLogger(HighScoreStore.class.getName());
        Level previous = logger.getLevel();
        logger.setLevel(Level.OFF);
        try { action.run(); } finally { logger.setLevel(previous); }
    }

    @FunctionalInterface private interface CheckedAction { void run() throws Exception; }
}
