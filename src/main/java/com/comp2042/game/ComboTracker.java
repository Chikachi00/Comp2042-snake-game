package com.comp2042.game;

import java.util.Objects;
import java.util.function.LongSupplier;

/** Counts coins eaten within five seconds of active play, excluding pauses. */
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

    ComboTracker(LongSupplier clock) { this.clock = Objects.requireNonNull(clock); }

    int recordCoin() {
        long now = activeTime();
        count = hasCoin && now - lastCoinAt <= WINDOW_NANOS ? count + 1 : 1;
        lastCoinAt = now;
        hasCoin = true;
        lastBonus = Math.min((count - 1) * 2, 10);
        return lastBonus;
    }
    int count() { return hasCoin && activeTime() - lastCoinAt <= WINDOW_NANOS ? count : 0; }
    int lastBonus() { return count() == 0 ? 0 : lastBonus; }
    void pause() { if (!paused) { pausedAt = clock.getAsLong(); paused = true; } }
    void resume() {
        if (paused) { pausedTotal += clock.getAsLong() - pausedAt; paused = false; }
    }
    void reset() { count = 0; lastBonus = 0; hasCoin = false; paused = false; pausedTotal = 0; }
    private long activeTime() {
        long now = clock.getAsLong();
        return now - pausedTotal - (paused ? now - pausedAt : 0);
    }
}
