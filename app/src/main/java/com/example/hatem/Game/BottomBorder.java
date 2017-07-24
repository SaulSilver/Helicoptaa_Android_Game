package com.example.hatem.Game;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * Created by hatem on 2016-08-11.
 * Bottom border for the game
 */
public class BottomBorder extends AbstractGameObject {
    Bitmap image;

    public BottomBorder(Bitmap res, int x, int y){
        height = 200;
        width = 20;

        this.x = x;
        this.y = y;

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
