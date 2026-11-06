package com.comp2042.persistence;

import com.comp2042.game.GameMode;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.EnumMap;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Stores one personal best per mode, independently of game rules and JavaFX controls. */
public final class HighScoreStore {
    private static final Logger LOG = Logger.getLogger(HighScoreStore.class.getName());
    private final EnumMap<GameMode, Integer> scores = new EnumMap<>(GameMode.class);
    private final Path file;
    private boolean dirty;

    /**
     * Loads records from a file. Missing or invalid entries start at zero; I/O failure never
     * prevents play. New records are also retained in memory if saving fails.
     * @param file destination for local properties, created on the first new record
     */
    public HighScoreStore(Path file) {
        this.file = Objects.requireNonNull(file, "file").toAbsolutePath().normalize();
        load();
    }

    /** Creates a store without disk I/O for isolated rounds and tests. */
    private HighScoreStore() { file = null; }

    /** @return an isolated, non-persistent store */
    public static HighScoreStore inMemory() { return new HighScoreStore(); }

    /** @return personal records shared by IDE and packaged launches under the user's home */
    public static HighScoreStore forCurrentUser() {
        return new HighScoreStore(Path.of(System.getProperty("user.home"),
                ".comp2042-snake", "high-scores.properties"));
    }

    /**
     * Returns the best score in one mode, or zero before a record has been set.
     * @param mode mode to query
     * @return the non-negative personal best
     */
    public int getBest(GameMode mode) { return scores.getOrDefault(Objects.requireNonNull(mode), 0); }

    /**
     * Immediately saves a strictly improved record; equal and lower scores leave it unchanged.
     * @param mode mode played
     * @param score current total run points, including challenge combo bonuses
     * @return whether this score improved the in-memory record
     */
    public boolean record(GameMode mode, int score) {
        Objects.requireNonNull(mode, "mode");
        if (score < 0) throw new IllegalArgumentException("A score cannot be negative");
        if (score <= getBest(mode)) return false;
        scores.put(mode, score);
        dirty = true;
        flush();
        return true;
    }

    /** Retries a pending save on restart, navigation or window close, without interrupting play. */
    public void flush() {
        if (!dirty || file == null) return;
        Path temporary = null;
        try {
            Files.createDirectories(file.getParent());
            temporary = Files.createTempFile(file.getParent(), "high-scores-", ".tmp");
            Properties properties = new Properties();
            for (GameMode mode : GameMode.values()) properties.setProperty(mode.name(), Integer.toString(getBest(mode)));
            try (OutputStream output = Files.newOutputStream(temporary)) {
                properties.store(output, "COMP2042 Snake personal bests by mode");
            }
            try {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException unsupported) {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
            }
            dirty = false;
        } catch (IOException | SecurityException failure) {
            LOG.log(Level.WARNING, "Could not save personal bests; records remain available for this session", failure);
        } finally {
            if (temporary != null) {
                try { Files.deleteIfExists(temporary); }
                catch (IOException | SecurityException failure) {
                    LOG.log(Level.FINE, "Could not remove temporary score file", failure);
                }
            }
        }
    }

    /** Reads each mode independently so a malformed value cannot discard the other record. */
    private void load() {
        if (Files.notExists(file)) return;
        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(file)) {
            properties.load(input);
        } catch (IOException | IllegalArgumentException | SecurityException failure) {
            LOG.log(Level.WARNING, "Could not read personal bests; starting with zero records", failure);
            return;
        }
        for (GameMode mode : GameMode.values()) {
            try {
                int score = Integer.parseInt(properties.getProperty(mode.name(), "0").trim());
                if (score >= 0) scores.put(mode, score);
            } catch (NumberFormatException ignored) {
                // A missing, malformed or overflowing value cannot become a personal best.
            }
        }
    }
}
