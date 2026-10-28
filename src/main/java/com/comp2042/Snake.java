package com.comp2042;

import java.awt.Point;
import java.util.ArrayList;

/** Maintains the snake's position, movement and collision state. */
public class Snake {
    private boolean dead = false;
    private final Point head = new Point();
    private final ArrayList<Point> body = new ArrayList<>();
    private final int gx;
    private final int gy;
    private boolean ignoreBorders = false;
    private boolean noBorder;
    private Obstacle obstacle;
    private int snakeDir = 270;

    public Snake(int x, int y, boolean noBorder, Obstacle obstacle) {
        this.obstacle = obstacle;
        this.noBorder = noBorder;
        this.head.setLocation(x / 2, y / 2);
        this.body.add(new Point(x / 2 + 1, y / 2));
        this.gx = x;
        this.gy = y;
    }

    public void move(char dir) {
        body.add(new Point(head));
        int previousX = head.x;
        int previousY = head.y;

        switch (dir) {
            case 'U':
                snakeDir = 0;
                head.translate(0, -1);
                break;
            case 'D':
                snakeDir = 180;
                head.translate(0, 1);
                break;
            case 'L':
                snakeDir = 270;
                head.translate(-1, 0);
                break;
            case 'R':
                snakeDir = 90;
                head.translate(1, 0);
                break;
        }

        if (borderControl()) {
            if (ignoreBorders || noBorder) {
                wrapHead();
            } else {
                head.setLocation(previousX, previousY);
                dead = true;
            }
        }

        if (!dead && obstacle.getObstacles().contains(head)) {
            head.setLocation(previousX, previousY);
            dead = true;
        }

        if (body.size() > 4 && hitSelf() && !body.get(0).equals(head)) {
            dead = true;
        }
    }

    private void wrapHead() {
        head.setLocation(Math.floorMod(head.x, gx), Math.floorMod(head.y, gy));
    }

    public int getBodyDir(int bodyCount) {
        if (bodyCount == 0) {
            return 0;
        } else if (body.get(bodyCount).getY() == body.get(bodyCount - 1).getY()) {
            return 270;
        } else {
            return 0;
        }
    }

    public boolean ateCoin(Coin coin) {
        return ateCoin(coin.getPos());
    }

    /**
     * Updates the tail after a move according to whether the snake reached the coin.
     *
     * @param coinPosition the position of the current coin
     * @return whether the snake reached the coin
     */
    boolean ateCoin(Point coinPosition) {
        boolean eaten = head.equals(coinPosition);
        if (!eaten) {
            body.remove(0);
        }
        return eaten;
    }

    private boolean borderControl() {
        return head.getX() < 0 || head.getY() < 0 || head.getX() >= gx || head.getY() >= gy;
    }

    public boolean hitSelf() {
        return body.contains(head);
    }

    public ArrayList<Point> getBody() {
        return new ArrayList<>(body);
    }

    public Point getHead() {
        return new Point(head);
    }

    public boolean getDead() {
        return dead;
    }

    public void ignoreBorders(boolean ign) {
        ignoreBorders = ign;
    }

    public int getSnakeDir() {
        return snakeDir;
    }
}
