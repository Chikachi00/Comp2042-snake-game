package com.comp2042.game;

/** Identifies classic endless survival or the progressive three-level challenge. */
public enum GameMode {
    /** Original survival rules, without level objectives, keys or combo bonuses. */
    ENDLESS("Endless"),
    /** Selectable starting level, sequential progression, combos and optional shortcuts. */
    CHALLENGE("Challenge");

    private final String title;

    /** @param title display name used by the mode selector and HUD */
    GameMode(String title) { this.title = title; }

    /** @return the mode's player-facing name */
    @Override public String toString() { return title; }
}
