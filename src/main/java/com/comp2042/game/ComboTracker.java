package com.comp2042.game;

import java.util.Objects;
import java.util.function.LongSupplier;

/**
 * Awards consecutive-coin bonuses within five seconds of active play, excluding pauses.
 */
final class ComboTracker {
    private static final long WINDOW_NANOS = 5_000_000_000L;
    private final LongSupplier clock;
    private long pausedAt;
    private long pausedTotal;
    private long lastCoinAt;
    private int count;
    private int lastBonus;
    private boolean paused;
    private boolean hasCoin;

    /**
     * Creates a tracker using an injectable monotonic nanosecond clock.
     *
     * @param clock monotonic clock returning nanoseconds
     */
    ComboTracker(LongSupplier clock) { this.clock = Objects.requireNonNull(clock); }

    /**
     * Records a pickup; intervals of at most five active seconds continue the combo.
     *
     * @return the bonus for this pickup, from zero to ten points
     */
    int recordCoin() {
        long now = activeTime();
        count = hasCoin && now - lastCoinAt <= WINDOW_NANOS ? count + 1 : 1;
        lastCoinAt = now;
        hasCoin = true;
        lastBonus = Math.min((count - 1) * 2, 10);
        return lastBonus;
    }
    /**
     * Returns the combo count while its five-second window remains active.
     *
     * @return the consecutive pickup count, or zero after expiry
     */
    int count() { return hasCoin && activeTime() - lastCoinAt <= WINDOW_NANOS ? count : 0; }
    /**
     * Returns the most recent bonus while the combo remains active.
     *
     * @return the last bonus, or zero after expiry
     */
    int lastBonus() { return count() == 0 ? 0 : lastBonus; }
    /**
     * Freezes active time; repeated calls do not add another pause.
     */
    void pause() { if (!paused) { pausedAt = clock.getAsLong(); paused = true; } }
    /**
     * Excludes the completed pause duration from active time.
     */
    void resume() {
        if (paused) { pausedTotal += clock.getAsLong() - pausedAt; paused = false; }
    }
    /**
     * Clears the combo and all pause bookkeeping.
     */
    void reset() { count = 0; lastBonus = 0; hasCoin = false; paused = false; pausedTotal = 0; }
    /**
     * Subtracts accumulated and current pause durations from the clock.
     *
     * @return active play time in nanoseconds
     */
    private long activeTime() {
        long now = clock.getAsLong();
        return now - pausedTotal - (paused ? now - pausedAt : 0);
    }
}
