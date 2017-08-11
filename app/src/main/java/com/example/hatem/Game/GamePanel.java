package com.example.hatem.Game;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.preference.PreferenceManager;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GamePanel extends SurfaceView implements SurfaceHolder.Callback {

    public static final int WIDTH = 856;
    public static final int HEIGHT = 480;
    public static final int MOVESPEED = -5;

    private Context context;

    private long smokeStartTime;
    private long missileStartTime;
    public MainThread thread;
    private Background bg;
    private PlayerGameObject player;
    private ArrayList<SmokePuff> smoke;
    private ArrayList<Missile> missiles;
    private ArrayList<TopBorder> topborder;
    private ArrayList<BottomBorder> botborder;
    private Random rand = new Random();
    private int maxBorderHeight;
    private int minBorderHeight;
    private boolean topDown = true;
    private boolean botDown = true;
    private boolean newGameCreated;

    //Sounds
    private MediaPlayer backgroundSound;
    private MediaPlayer helicopterSound;
    private MediaPlayer crashSound;
    private boolean playBckgrndMusic;
    private boolean playSoundEffects;

    //increase to slow down difficulty progression, decrease to speed up difficulty progression
    private int progressDenom = 40;

    private Explosion explosion;
    private long startReset;
    private boolean reset;
    private boolean disappear;
    private boolean hasDied;
    private boolean started;
    private int bestScore;
    private boolean pauseOccured;
    private boolean hasBeenCreated;

    public GamePanel(Context context) {
        super(context);
        this.context = context;

        hasDied = false;

        //add the callback to the surfaceholder to intercept events
        getHolder().addCallback(this);

        //make gamePanel focusable so it can handle events
        setFocusable(true);

        //Set values from the Settings
        updateSettings();

        bg = new Background(BitmapFactory.decodeResource(getResources(), R.drawable.grassbg1));
        player = new PlayerGameObject(BitmapFactory.decodeResource(getResources(), R.drawable.helicopter), 65, 25, 3);
        smoke = new ArrayList<SmokePuff>();
        missiles = new ArrayList<Missile>();
        topborder = new ArrayList<TopBorder>();
        botborder = new ArrayList<BottomBorder>();
        smokeStartTime = System.nanoTime();
        missileStartTime = System.nanoTime();

        pauseOccured = false;

        thread = new MainThread(getHolder(), this);
        thread.setRunning(true);
        thread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (pauseOccured && player.isPlaying()) {
            Game game = (Game) context;
            game.onPausePopup();
            pauseOccured = false;
        }
        else {
            thread.onResume();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        //Pauses the music
       // backgroundSound.release();
        backgroundSound.pause();
       // helicopterSound.release();
        helicopterSound.pause();
       // crashSound.release();
        crashSound.pause();
        synchronized (thread) {
            thread.onPause();
        }
        pauseOccured = true;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        //Start the background music
        backgroundSound = MediaPlayer.create(context, R.raw.background_music);
        backgroundSound.setLooping(true);
        if (playBckgrndMusic)
            backgroundSound.start();
        //Set up the sound for the Helicopter sound
        helicopterSound = MediaPlayer.create(context, R.raw.helicopter_sound);
        helicopterSound.setLooping(true);
        //Set up the sound for the Crash sound
        crashSound = MediaPlayer.create(context, R.raw.crash_sound);
        crashSound.setLooping(false);
        hasBeenCreated = true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            //Play the click sound
            if (playSoundEffects) {
                MediaPlayer clickSound = MediaPlayer.create(context, R.raw.button_press);
                clickSound.setLooping(false);
                clickSound.start();
            }

            if (!player.isPlaying() && newGameCreated && reset) {
                player.setPlaying(true);
                player.setUp(true);
                //Play the helicopter sound
                if (playSoundEffects)
                    helicopterSound.start();
            }
            if (player.isPlaying()) {
                if (!started)
                    started = true;
                reset = false;
                player.setUp(true);
            }
            return true;
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            player.setUp(false);
            return true;
        }
        return super.onTouchEvent(event);
    }

    public void update() {
        if (player.isPlaying()) {

            if (botborder.isEmpty()) {
                player.setPlaying(false);
                return;
            }
            if (topborder.isEmpty()) {
                player.setPlaying(false);
                return;
            }

            bg.update();
            player.update();

            //calculate the threshold of height the border can have based on the score
            //max and min border heart are updated, and the border switched direction when either max or
            //min is met
            maxBorderHeight = 30 + player.getScore() / progressDenom;
            //cap max border height so that borders can only take up a total of 1/2 the screen
            if (maxBorderHeight > HEIGHT / 4) maxBorderHeight = HEIGHT / 4;
            minBorderHeight = 5 + player.getScore() / progressDenom;

            //check bottom border collision
            for (int i = 0; i < botborder.size(); i++) {
                if (collided(botborder.get(i), player)) {
                    hasDied = true;
                    player.setPlaying(false);
                }
            }

            //check top border collision
            for (int i = 0; i < topborder.size(); i++) {
                if (collided(topborder.get(i), player)) {
                    hasDied = true;
                    player.setPlaying(false);
                }
            }

            //update top border
            this.updateTopBorder();

            //udpate bottom border
            this.updateBottomBorder();

            //add missiles on timer
            long missileElapsed = (System.nanoTime() - missileStartTime) / 1000000;
            if (missileElapsed > (2000 - player.getScore() / 4)) {
                //first missile always goes down the middle
                if (missiles.size() == 0)
                    missiles.add(new Missile(BitmapFactory.decodeResource(getResources(), R.drawable.
                            missile), WIDTH + 10, HEIGHT / 2, 45, 15, player.getScore(), 13));
                else
                    missiles.add(new Missile(BitmapFactory.decodeResource(getResources(), R.drawable.missile),
                            WIDTH + 10, (int) (rand.nextDouble() * (HEIGHT - (maxBorderHeight * 2)) + maxBorderHeight), 45, 15, player.getScore(), 13));

                //reset timer
                missileStartTime = System.nanoTime();
            }
            //loop through every missile and check collision and remove
            for (int i = 0; i < missiles.size(); i++) {
                //update missile
                missiles.get(i).update();

                if (collided(missiles.get(i), player)) {
                    missiles.remove(i);
                    player.setPlaying(false);
                    hasDied = true;
                    break;
                }
                //remove missile if it is way off the screen
                if (missiles.get(i).getX() < -100) {
                    missiles.remove(i);
                    break;
                }
            }

            //add smoke puffs on timer
            long elapsed = (System.nanoTime() - smokeStartTime) / 1000000;
            if (elapsed > 120) {
                smoke.add(new SmokePuff(player.getX(), player.getY() + 10));
                smokeStartTime = System.nanoTime();
            }

            for (int i = 0; i < smoke.size(); i++) {
                smoke.get(i).update();
                if (smoke.get(i).getX() < -10)
                    smoke.remove(i);
            }
        } else {
            player.resetDY();
            if (!reset) {
                newGameCreated = false;
                startReset = System.nanoTime();
                reset = true;
                disappear = true;
                explosion = new Explosion(BitmapFactory.decodeResource(getResources(), R.drawable.explosion), player.getX(),
                        player.getY() - 30, 100, 100, 25);
            }
            explosion.update();
            long resetElapsed = (System.nanoTime() - startReset) / 1000000;

            if (resetElapsed > 2500 && !newGameCreated)
                newGame();
        }
    }

    public boolean collided(AbstractGameObject a, AbstractGameObject b) {
        if (Rect.intersects(a.getRectangle(), b.getRectangle())) {
            //Add sound effects
            if (helicopterSound.isPlaying())
                helicopterSound.pause();
            if (playSoundEffects)
                crashSound.start();         //Play the crash sound
        }
        return Rect.intersects(a.getRectangle(), b.getRectangle());
    }

    @Override
    public void draw(Canvas canvas) {
        final float scaleFactorX = getWidth() / (WIDTH * 1.f);
        final float scaleFactorY = getHeight() / (HEIGHT * 1.f);

        if (canvas != null) {
            final int savedState = canvas.save();
            canvas.scale(scaleFactorX, scaleFactorY);
            bg.draw(canvas);
            if (!disappear)
                player.draw(canvas);

            //draw smokepuffs
            for (SmokePuff sp : smoke)
                sp.draw(canvas);

            //draw missiles
            for (Missile m : missiles)
                m.draw(canvas);

            //draw topborder
            for (TopBorder tb : topborder)
                tb.draw(canvas);

            //draw botborder
            for (BottomBorder bb : botborder)
                bb.draw(canvas);

            //draw explosion
            if (started)
                explosion.draw(canvas);
            drawText(canvas);
            canvas.restoreToCount(savedState);
        }
    }

    public void updateTopBorder() {
        //every 50 points, insert randomly placed top blocks that break the pattern
        if (player.getScore() % 50 == 0)
            topborder.add(new TopBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick
            ), topborder.get(topborder.size() - 1).getX() + 20, 0, (int) ((rand.nextDouble() * (maxBorderHeight
            )) + 1)));

        for (int i = 0; i < topborder.size(); i++) {
            topborder.get(i).update();
            if (topborder.get(i).getX() < -20) {
                topborder.remove(i);
                //remove element of arraylist, replace it by adding a new one

                //calculate topdown which determines the direction the border is moving (up or down)
                if (topborder.get(topborder.size() - 1).getHeight() >= maxBorderHeight)
                    topDown = false;
                if (topborder.get(topborder.size() - 1).getHeight() <= minBorderHeight)
                    topDown = true;
                //new border added will have larger height
                if (topDown)
                    topborder.add(new TopBorder(BitmapFactory.decodeResource(getResources(),
                            R.drawable.brick), topborder.get(topborder.size() - 1).getX() + 20,
                            0, topborder.get(topborder.size() - 1).getHeight() + 1));
                    //new border added wil have smaller height
                else
                    topborder.add(new TopBorder(BitmapFactory.decodeResource(getResources(),
                            R.drawable.brick), topborder.get(topborder.size() - 1).getX() + 20,
                            0, topborder.get(topborder.size() - 1).getHeight() - 1));
            }
        }
    }

    public void updateBottomBorder() {
        //every 40 points, insert randomly placed bottom blocks that break pattern
        if (player.getScore() % 40 == 0)
            botborder.add(new BottomBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                    botborder.get(botborder.size() - 1).getX() + 20, (int) ((rand.nextDouble()
                    * maxBorderHeight) + (HEIGHT - maxBorderHeight))));

        //update bottom border
        for (int i = 0; i < botborder.size(); i++) {
            botborder.get(i).update();

            //if border is moving off screen, remove it and add a corresponding new one
            if (botborder.get(i).getX() < -20) {
                botborder.remove(i);
                //determine if border will be moving up or down
                if (botborder.get(botborder.size() - 1).getY() <= HEIGHT - maxBorderHeight)
                    botDown = true;

                if (botborder.get(botborder.size() - 1).getY() >= HEIGHT - minBorderHeight)
                    botDown = false;

                if (botDown)
                    botborder.add(new BottomBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick
                    ), botborder.get(botborder.size() - 1).getX() + 20, botborder.get(botborder.size() - 1
                    ).getY() + 1));
                else
                    botborder.add(new BottomBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick
                    ), botborder.get(botborder.size() - 1).getX() + 20, botborder.get(botborder.size() - 1
                    ).getY() - 1));
            }
        }
    }

    public void newGame() {
        disappear = false;

        botborder.clear();
        topborder.clear();

        missiles.clear();
        smoke.clear();

        minBorderHeight = 5;
        maxBorderHeight = 30;

        player.resetDY();
        player.setY(HEIGHT / 2);

        //loading best score from shared prefs
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        bestScore = sharedPref.getInt("BestScore", 0) / 3;

        if (player.getScore() > bestScore) {
            bestScore = player.getScore();

            //saving best score
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt("BestScore", bestScore * 3);
            editor.apply();
        }
        player.resetScore();

        //create initial borders

        //initial top border
        for (int i = 0; i * 20 < WIDTH + 40; i++) {
            //first top border create
            if (i == 0)
                topborder.add(new TopBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick
                ), i * 20, 0, 10));
            else
                topborder.add(new TopBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick
                ), i * 20, 0, topborder.get(i - 1).getHeight() + 1));
        }
        //initial bottom border
        for (int i = 0; i * 20 < WIDTH + 40; i++) {
            //first border ever created
            if (i == 0)
                botborder.add(new BottomBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick)
                        , i * 20, HEIGHT - minBorderHeight));
                //adding borders until the initial screen is filed
            else
                botborder.add(new BottomBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                        i * 20, botborder.get(i - 1).getY() - 1));
        }
        newGameCreated = true;
    }

    public void drawText(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(30);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("SCORE: " + (player.getScore() * 3), 10, HEIGHT - 10, paint);
        canvas.drawText("BEST: " + bestScore * 3, WIDTH - 215, HEIGHT - 10, paint);

        if (!player.isPlaying() && newGameCreated && reset) {
            Paint paint1 = new Paint();
            paint1.setTextSize(40);
            paint1.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            canvas.drawText("PRESS TO START", WIDTH / 2 - 50, HEIGHT / 2, paint1);

            paint1.setTextSize(20);
            canvas.drawText("PRESS AND HOLD TO GO UP", WIDTH / 2 - 50, HEIGHT / 2 + 20, paint1);
            canvas.drawText("RELEASE TO GO DOWN", WIDTH / 2 - 50, HEIGHT / 2 + 40, paint1);
        }

        if (hasDied) {
            updateScoreToListOfScores(player.getScore() * 3);
            onEndGame();
        }
    }

    /*
     * Pauses the thread and calls the end game popup.
     * Resets player life status afterwards in order to resume the game to its normal state
     */
    private void onEndGame() {
        thread.onPause();
        Game game = (Game) context;
        int score = player.getScore() * 3;
        game.onEndGamePopup(score);
        // Make sure hasDied is reset so that the game continues and escapes the if statement.
        hasDied = false;
    }


    /*
     * Gets (if present) a PlayerScore list-like json object from shared prefs.
     * Converts said object to an ArrayList and adds the new player score.
     * The list is then converted back to json and stored in the shared prefs.
     */
    private void updateScoreToListOfScores(int playerScore) {
        Gson gson = new Gson();
        List scoresFromSharedPreferences;
        SharedPreferences sharedPref = context.getSharedPreferences("SharedPrefs", Context.MODE_PRIVATE);
        String jsonPreferences = sharedPref.getString("PlayerScores", "");

        Type type = new TypeToken<List>() {}.getType();
        scoresFromSharedPreferences = gson.fromJson(jsonPreferences, type);

        if (scoresFromSharedPreferences == null) {
            scoresFromSharedPreferences  = new ArrayList();
        }

        scoresFromSharedPreferences.add(playerScore);

        String jsonScoresFromSharedPreferences = gson.toJson(scoresFromSharedPreferences);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("PlayerScores", jsonScoresFromSharedPreferences);
        editor.commit();
    }

    /**
     * Update the gameplay according to the user's preference of the settings
     */
    public void updateSettings() {
        //Getting settings properties from shared preferences
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean backgroundMusic = sharedPrefs.getBoolean("background_music_chooser", true);           //false is the default value
        boolean soundEffects = sharedPrefs.getBoolean("sound_effects_chooser", true);
        String gameDifficulty = sharedPrefs.getString("game_difficulty_chooser", "Easy");

        //For background music
        playBckgrndMusic = backgroundMusic;

        //For Sound Effects
        playSoundEffects = soundEffects;

        //For Game Difficulty
        switch (gameDifficulty) {
            case "Easy":
                progressDenom = 20;
                break;
            case "Medium":
                progressDenom = 50;
                break;
            case "Hard":
                progressDenom = 100;
                break;
        }
    }
}