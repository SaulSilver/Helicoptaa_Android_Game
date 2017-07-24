package com.example.hatem.Game;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * Created by hatem on 2016-08-14.
 *
 */
public class Explosion {
    private int x;
    private int y;
    private int width;
    private int height;
    private int row;
    private Animation animation = new Animation();
    private Bitmap spriteSheet;

    public Explosion(Bitmap res, int x, int y, int w, int h, int framesNumber) {
        this.x = x;
        this.y = y;
        width = w;
        height = h;

        Bitmap[] images = new Bitmap[framesNumber];

        spriteSheet = res;

        for(int i = 0; i < framesNumber; i++) {
            if(i % 5 == 0 && i > 0)
                row++;
            images[i] = Bitmap.createBitmap(spriteSheet, (i-(5 * row)) * width, row * height, width, height);
        }

        animation.setFrames(images);
        animation.setDelay(10);

    }

    public void draw(Canvas canvas) {
        if(!animation.isPlayedOnce())
            canvas.drawBitmap(animation.getImage(), x, y, null);
    }

    public void update() {
        if (!animation.isPlayedOnce())
            animation.update();
    }

    public int getHeight() { return height;}

}
