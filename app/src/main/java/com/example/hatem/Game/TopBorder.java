package com.example.hatem.Game;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * Created by hatem on 2016-08-11.
 * Top border for the game
 */
public class TopBorder extends AbstractGameObject {
    Bitmap image;

    public TopBorder(Bitmap res, int x, int y, int h) {
        this.x = x;
        this.y = y;

        height = h;
        width = 20;

        dx = GamePanel.MOVESPEED;
        image = Bitmap.createBitmap(res, 0, 0, width, height);
    }

    public void update() {
        x += dx;
    }

    public void draw(Canvas canvas) {
        canvas.drawBitmap(image, x, y, null);
    }
}
