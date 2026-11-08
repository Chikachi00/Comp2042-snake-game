package com.comp2042.game;

/** Defines the three base movement speeds offered by the endless-mode menu. */
public enum GameSpeed {
    /** Slower movement for learning and planning a route. */
    SLOW("Slow", 5),
    /** The original normal movement speed. */
    MEDIUM("Medium (Default)", 8.5),
    /** Faster movement for a greater reaction-time challenge. */
    FAST("Fast", 13);

    private final String title;
    private final double movesPerSecond;

    /**
     * Stores a menu label and its base movement rate.
     * @param title player-facing speed name
     * @param movesPerSecond number of board cells moved per second
     */
    GameSpeed(String title, double movesPerSecond) {
        this.title = title;
        this.movesPerSecond = movesPerSecond;
    }

    /** @return the base movement rate before temporary coin effects */
    public double movesPerSecond() { return movesPerSecond; }

    /** @return the label displayed in the speed selector */
    @Override public String toString() { return title; }
}
