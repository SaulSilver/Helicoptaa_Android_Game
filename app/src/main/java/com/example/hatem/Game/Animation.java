package com.example.hatem.Game;

import android.graphics.Bitmap;

/**
 * Created by hatem on 2016-07-10.
 */
public class Animation {
    private Bitmap[] frames;
    private int currentFrame;
    private long startTime;
    private long delay;
    private boolean playedOnce;

    public void setFrames(Bitmap[] frames){
        this.frames = frames;
        currentFrame = 0;
        startTime = System.nanoTime();
    }

    public void setDelay(long d){
        delay = d;
    }

    public void setFrame(int i) {
        currentFrame = i;
    }

    public void update() {
        long elapsed = (System.nanoTime() - startTime) / MainThread.TO_ONE_MILLISECOND;

        if(elapsed > delay)
            currentFrame++;

        if(currentFrame == frames.length) {
            currentFrame = 0;
            playedOnce = true;
        }
    }

    public Bitmap getImage() {
        return frames[currentFrame];
    }

    public int getFrame() { return currentFrame;}

    public boolean isPlayedOnce() {
        return playedOnce;
    }
}
