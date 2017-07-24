package com.example.hatem.Game;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

/**
 * A thread to start the game
 * Created by hatem on 2017-07-07.
 */
public class MainThread extends Thread {

    private int FPS = 30;
    private double averageFPS;
    private SurfaceHolder surfaceHolder;
    private GamePanel gamePanel;
    private boolean running;
    public static Canvas canvas;

    public static final int TO_ONE_MILLISECOND = 1000000;

    public MainThread(SurfaceHolder surfaceHolder, GamePanel gamePanel){
        super();
        this.surfaceHolder = surfaceHolder;
        this.gamePanel = gamePanel;
    }

    @Override
    public void run(){
        long startTime;
        long timeMillis;
        long waitTime;
        long totalTime = 0;
        int frameCount = 0;
        long targetTime = 1000/FPS;         //time for each gameloop

        while(running){
            startTime = System.nanoTime();
            canvas = null;

            //try locking the canvas for pixel editing
            try{
                canvas = this.surfaceHolder.lockCanvas();
                synchronized (surfaceHolder){
                    this.gamePanel.update();
                    this.gamePanel.draw(canvas);
                }
            }catch (Exception f) {
                f.printStackTrace();
            }
            finally {
                if (canvas != null){
                    try {
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    } catch (Exception e) {e.printStackTrace();}
                }
            }

            timeMillis = (System.nanoTime() - startTime) / TO_ONE_MILLISECOND;
            waitTime = targetTime - timeMillis;

            try{
                this.sleep(waitTime);
            }catch (Exception e) {}

            totalTime += System.nanoTime() - startTime;
            frameCount++;

            if(frameCount == FPS) {
                averageFPS = 1000/((totalTime/frameCount)/TO_ONE_MILLISECOND);
                frameCount = 0;
                totalTime = 0;
                System.out.println("average FPS: " + averageFPS);

            }
        }
    }

    public void setRunning(boolean start){
        running = start;
    }
}
