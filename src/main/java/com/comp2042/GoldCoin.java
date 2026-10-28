package com.comp2042;

import javafx.scene.image.Image;

import java.awt.Point;

public class GoldCoin extends Coin {
    private Image image = new Image("resources/views/res/goldcoin.png");

    public GoldCoin(int x, int y, Snake snek, Obstacle obstacle) {
        super(x, y, snek, obstacle);
    }

    GoldCoin(Point position) {
        super(position);
    }

    public Image getImage() {
        return image;
    }

    public int getPoints() {
        int points = 15;
        return points;
    }
}
