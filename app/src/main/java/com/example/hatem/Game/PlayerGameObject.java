package com.example.hatem.Game;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * The class is responsible for the player and extends the abstract class AbstractGameObject
 * Created by hatem on 2016-07-09.
 */
public class PlayerGameObject extends AbstractGameObject{
    private Bitmap spriteSheet;
    private int score;
    private boolean up;
    private boolean playing;
    private Animation animation = new Animation();
    private long startTime;

    public PlayerGameObject(Bitmap res, int w, int h, int framesNumber) {
        spriteSheet = res;
        x = 100;
        y = GamePanel.HEIGHT / 2;
        dy = 0;
        score = 0;
        height = h;
        width = w;

        Bitmap[] image = new Bitmap[framesNumber];

        for(int i = 0; i < image.length; i++)
            image[i] = Bitmap.createBitmap(spriteSheet, i * width, 0, width, height);

        animation.setFrames(image);
        animation.setDelay(10);

        startTime = System.nanoTime();
    }

    public void setUp(boolean b) { up = b;    }

    public void update() {
        long elapsed = (System.nanoTime() - startTime)/ MainThread.TO_ONE_MILLISECOND;

        if(elapsed > 100) {
            score++;
            startTime = System.nanoTime();
        }
        animation.update();

        if (up)
            dy -= 1.1;
        else dy += 1.1;

        if(dy > 14)
            dy = 14;

        if (dy < -14)
            dy = -14;

        y += dy*1.1;
    }

    public void draw(Canvas canvas){
        canvas.drawBitmap(animation.getImage(), x, y, null);
    }

    public boolean isPlaying() {
        return playing;
    }

    public int getScore() {
        return score;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    public void resetScore() {
        score = 0;
    }

    public void resetDY(){
        dy = 0;
    }
}
