package com.example.hatem.Game;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Game extends Activity {

    private static GamePanel gamePanel;

    // End game popup specific UI components
    private Button resumeButton;
    private Button shareButton;
    private Button settingsButton;
    private Button pauseResumeButton;
    private Button pauseSettingsButton;
    private Button pauseExitButton;
    private ListView highScoreListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //turn title off
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        //set to full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        gamePanel = new GamePanel(this);
        setContentView(gamePanel);
    }

    @Override
    public void onPause(){
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    public void onEndGamePopup(final int playerScore) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final Dialog dialog = new Dialog(Game.this);
                dialog.setContentView(R.layout.end_game_popup);
                dialog.setCancelable(false);
                dialog.show();

                //Setup the share button
                shareButton = (Button) dialog.findViewById(R.id.end_game_popup_share);
                shareButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent msgIntent = new Intent(Intent.ACTION_SEND);
                        msgIntent.setType("text/plain");
                        msgIntent.putExtra(Intent.EXTRA_TEXT, "Hey, I just won in the Helicopty game with score  " + playerScore + "\nTry your best to beat my score, bitch!");
                        startActivity(Intent.createChooser(msgIntent, "Send a message via.."));
                    }
                });
                //Setup the settings button
                settingsButton = (Button) dialog.findViewById(R.id.end_game_popup_settings_btn);
                settingsButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent settingsIntent = new Intent(getBaseContext(), SettingsActivity.class);
                        startActivityForResult(settingsIntent, 1);
                    }
                });

                // Set up resume button
                resumeButton = (Button) dialog.findViewById(R.id.end_game_popup_resume_button);
                resumeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        gamePanel.resumeThread();
                    }
                });
                highScoreListView = (ListView) dialog.findViewById(R.id.end_game_popup_highscore_listview);
                highScoreListView.setAdapter(getScoresFromSharedPreferences());
            }
        });
    }

    public void onPausePopup() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final Dialog dialog = new Dialog(Game.this);
                dialog.setContentView(R.layout.pause_menu);
                dialog.setCancelable(false);
                dialog.show();

                //Resume Game
                pauseResumeButton = (Button) dialog.findViewById(R.id.pause_menu_resume_button);
                pauseResumeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        gamePanel.resumeThread();
                    }
                });

                //Go to Settings
                pauseSettingsButton = (Button) dialog.findViewById(R.id.pause_menu_settings_button);
                pauseSettingsButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        Intent settingsIntent = new Intent(getBaseContext(), SettingsActivity.class);
                        startActivityForResult(settingsIntent, 1);
                    }
                });

                //Exit application
                pauseExitButton = (Button) dialog.findViewById(R.id.pause_menu_exit_button);
                pauseExitButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                        System.exit(0);
                    }
                });
            }
        });
    }

    @Override
    public void  onActivityResult(int requestCode, int resultCode, Intent result) {
        switch (requestCode) {
            case 1:     //Back from the Settings activity
                gamePanel.updateSettings();
        }
    }

    private ArrayAdapter<String> getScoresFromSharedPreferences() {
        Gson gson = new Gson();
        List scoresFromSharedPreferences;
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("SharedPrefs", Context.MODE_PRIVATE);
        String jsonPreferences = sharedPref.getString("PlayerScores", "");

        Type type = new TypeToken<List>() {}.getType();
        scoresFromSharedPreferences = gson.fromJson(jsonPreferences, type);
        List playerScores = scoresFromSharedPreferences;
        ArrayList playerScoreList = new ArrayList();
        for (int i = 0; i < playerScores.size(); i++) {
            if(i > 9)
                break;
            int score = ((Double) playerScores.get(i)).intValue();      //Changing double value to int for better look
            playerScoreList.add(score);
        }
        Collections.sort(playerScoreList, Collections.reverseOrder());
        ArrayAdapter<String> ad = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, playerScoreList);
        return ad;
    }
}
