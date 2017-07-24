package com.example.hatem.Game;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import java.util.Random;

/**
 * Created by hatem on 2016-08-02.
 */
public class Missile extends AbstractGameObject {
    private int score;
    private int speed;
    private Bitmap spriteSheet;
    private Animation animation = new Animation();

    public Missile(Bitmap res, int x, int y, int w, int h, int s, int framesNumber) {
        super.x = x;
        super.y = y;
        width = w;
        height = h;

        score = s;

        speed += 7;

        //Speed limit
        if(speed > 40)
            speed = 40;

        spriteSheet = res;

        Bitmap[] images  = new Bitmap[framesNumber];

        for (int i = 0; i < framesNumber; i++)
            images[i] = Bitmap.createBitmap(spriteSheet, 0, i * height, width, height);

        animation.setFrames(images);
        animation.setDelay(100 - speed);
    }

    public void update(){
        x -= speed;
        animation.update();
    }

    public void draw(Canvas canvas) {
        canvas.drawBitmap(animation.getImage(), x, y, null);
    }

    @Override
    public int getWidth() {
        return width - 10;      //slightly offset to make it more realistic regarding collisions
    }
}
