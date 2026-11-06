package com.comp2042.game;

/**
 * Immutable drawing state of one optional shortcut, without mutable coordinate references.
 *
 * @param id matching letter on the key and gate
 * @param keyX key column
 * @param keyY key row
 * @param gateX gate column
 * @param gateY gate row
 * @param keyAvailable whether the uncollected key is visible
 * @param open whether the gate is permanently open for this level
 */
public record ShortcutSnapshot(String id, int keyX, int keyY, int gateX, int gateY,
                               boolean keyAvailable, boolean open) { }
