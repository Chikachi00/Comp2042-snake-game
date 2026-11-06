package com.comp2042.game;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Maintains snake coordinates, movement, growth and collision state without sprite images.
 */
public class Snake {
    private boolean dead = false;
    private final Point head = new Point();
    private final ArrayList<Point> body = new ArrayList<>();
    private final int gx;
    private final int gy;
    private boolean ignoreBorders = false;
    private boolean noBorder;
    private Obstacle obstacle;
    private Direction direction = Direction.LEFT;

    /**
     * Creates a left-facing snake in the board center with one tail cell.
     *
     * @param x board width in cells
     * @param y board height in cells
     * @param noBorder whether permanent border wrapping is enabled
     * @param obstacle board obstacle collection
     */
    public Snake(int x, int y, boolean noBorder, Obstacle obstacle) {
        this.obstacle = obstacle;
        this.noBorder = noBorder;
        this.head.setLocation(x / 2, y / 2);
        this.body.add(new Point(x / 2 + 1, y / 2));
        this.gx = x;
        this.gy = y;
    }

    /**
     * Moves the head and records its previous position, checking walls, obstacles and self collision.
     * The caller must then resolve the tail with {@code ateCoin}.
     *
     * @param direction requested movement direction
     */
    public void move(Direction direction) {
        Objects.requireNonNull(direction, "direction");
        body.add(new Point(head));
        int previousX = head.x;
        int previousY = head.y;
        this.direction = direction;
        head.translate(direction.dx(), direction.dy());

        if (borderControl()) {
            if (ignoreBorders || noBorder) {
                wrapHead();
            } else {
                head.setLocation(previousX, previousY);
                dead = true;
            }
        }

        if (!dead && obstacle.occupies(head)) {
            head.setLocation(previousX, previousY);
            dead = true;
        }

        if (body.size() > 4 && hitSelf() && !body.get(0).equals(head)) {
            dead = true;
        }
    }

    /**
     * Wraps an out-of-range head coordinate to the opposite board edge.
     */
    private void wrapHead() {
        head.setLocation(Math.floorMod(head.x, gx), Math.floorMod(head.y, gy));
    }

    /**
     * Chooses the axis used to draw a straight body segment.
     *
     * @param bodyCount index of the body segment
     * @return UP for a vertical segment, LEFT for a horizontal segment
     */
    public Direction getBodyDirection(int bodyCount) {
        if (bodyCount == 0) {
            return Direction.UP;
        } else if (body.get(bodyCount).getY() == body.get(bodyCount - 1).getY()) {
            return Direction.LEFT;
        } else {
            return Direction.UP;
        }
    }

    /**
     * Resolves growth after a move: retains the tail when the head reached the coin, otherwise removes
     * the oldest body cell.
     *
     * @param coin current or collected coin
     * @return whether the head occupies the coin position
     */
    public boolean ateCoin(Coin coin) {
        return ateCoin(coin.getPos());
    }

    /**
     * Resolves growth after a move: retains the tail when the head reached the coin, otherwise removes
     * the oldest body cell.
     *
     * @param coinPosition position of the current coin
     * @return whether the head occupies the coin position
     */
    boolean ateCoin(Point coinPosition) {
        boolean eaten = head.equals(coinPosition);
        if (!eaten) {
            body.remove(0);
        }
        return eaten;
    }

    /**
     * Checks whether the head is outside the board.
     *
     * @return whether either coordinate is out of bounds
     */
    private boolean borderControl() {
        return head.getX() < 0 || head.getY() < 0 || head.getX() >= gx || head.getY() >= gy;
    }

    /**
     * Checks whether any stored body coordinate equals the head.
     *
     * @return whether the body contains the head position
     */
    public boolean hitSelf() {
        return body.contains(head);
    }

    /**
     * Returns an unmodifiable snapshot with independent coordinate objects.
     *
     * @return tail-to-head body coordinates, excluding the head
     */
    public List<Point> getBody() {
        return body.stream().map(Point::new).toList();
    }

    /**
     * Returns an independent copy of the head coordinate.
     *
     * @return the current head position
     */
    public Point getHead() {
        return new Point(head);
    }

    /**
     * Checks the collision result stored by the last movement.
     *
     * @return whether the snake has died
     */
    public boolean getDead() {
        return dead;
    }

    /**
     * Enables or clears temporary border wrapping from a blue coin.
     *
     * @param ign whether temporary border wrapping is enabled
     */
    public void ignoreBorders(boolean ign) {
        ignoreBorders = ign;
    }

    /**
     * Returns the direction used for the latest movement.
     *
     * @return the current snake direction
     */
    public Direction getDirection() {
        return direction;
    }
}
