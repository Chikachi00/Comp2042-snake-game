package com.comp2042.game;

import java.awt.Point;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ShortcutTest {
    @Test void keyMustBeActiveAndCanOnlyOpenItsGateOnce() {
        Shortcut shortcut = new Shortcut("A", new Point(3, 4), new Point(8, 6));
        assertFalse(shortcut.collect(new Point(3, 4)));
        shortcut.activate();
        assertFalse(shortcut.collect(new Point(4, 4)));
        assertTrue(shortcut.snapshot().keyAvailable());
        assertTrue(shortcut.collect(new Point(3, 4)));
        assertTrue(shortcut.snapshot().open());
        assertFalse(shortcut.snapshot().keyAvailable());
        shortcut.activate();
        assertFalse(shortcut.collect(new Point(3, 4)));
        assertTrue(shortcut.snapshot().open());
    }

    @Test void constructorAndSnapshotsDoNotExposeMutableCoordinates() {
        Point key = new Point(3, 4), gate = new Point(8, 6);
        Shortcut shortcut = new Shortcut("A", key, gate);
        ShortcutSnapshot before = shortcut.snapshot();
        key.setLocation(0, 0); gate.setLocation(0, 0);
        shortcut.activate(); shortcut.collect(new Point(3, 4));
        assertEquals(3, shortcut.snapshot().keyX());
        assertEquals(8, shortcut.snapshot().gateX());
        assertFalse(before.open());
        assertFalse(before.keyAvailable());
        assertThrows(IllegalArgumentException.class,
                () -> new Shortcut("A", new Point(1, 1), new Point(1, 1)));
    }
}
