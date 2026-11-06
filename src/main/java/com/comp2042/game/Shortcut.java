package com.comp2042.game;

import java.awt.Point;
import java.util.Objects;

/** Owns the one-way hidden-key, available-key and open-gate lifecycle of a shortcut. */
final class Shortcut {
    private enum State { HIDDEN, AVAILABLE, OPEN }
    private final String id;
    private final Point key;
    private final Point gate;
    private State state = State.HIDDEN;

    /**
     * Creates a closed gate with its initially hidden key.
     * @param id matching display letter
     * @param key key cell, copied on construction
     * @param gate gate cell, copied on construction
     */
    Shortcut(String id, Point key, Point gate) {
        this.id = Objects.requireNonNull(id);
        this.key = new Point(key);
        this.gate = new Point(gate);
        if (key.equals(gate)) throw new IllegalArgumentException("Key and gate must occupy different cells");
    }

    /** Reveals this key without closing a gate that has already opened. */
    void activate() { if (state == State.HIDDEN) state = State.AVAILABLE; }

    /**
     * Collects the available key and permanently opens its gate.
     * @param head current snake head
     * @return whether this call opened the gate
     */
    boolean collect(Point head) {
        if (state != State.AVAILABLE || !key.equals(head)) return false;
        state = State.OPEN;
        return true;
    }

    /** @return an immutable copy of the current drawing state */
    ShortcutSnapshot snapshot() {
        return new ShortcutSnapshot(id, key.x, key.y, gate.x, gate.y,
                state == State.AVAILABLE, state == State.OPEN);
    }
}
