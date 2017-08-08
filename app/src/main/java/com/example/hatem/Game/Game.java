package com.example.hatem.Game;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;

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

    public void onEndGamePopup(final int playerScore) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final Dialog dialog = new Dialog(Game.this);
                dialog.setContentView(R.layout.end_game_popup);
                dialog.show();

                // Set up resume button
                resumeButton = (Button) dialog.findViewById(R.id.end_game_popup_resume_button);
                resumeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        gamePanel.thread.onResume();
                    }
                });
                shareButton = (Button) dialog.findViewById(R.id.end_game_popup_share);
                shareButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent msgIntent = new Intent(Intent.ACTION_SEND);
                        msgIntent.setType("text/plain");
                        msgIntent.putExtra(Intent.EXTRA_TEXT, "Hey i just won in this sutpid fucking game  " + playerScore);
                        startActivity(Intent.createChooser(msgIntent, "Send a message via.."));
                    }
                });
                highScoreListView = (ListView) dialog.findViewById(R.id.end_game_popup_highscore_listview);
                highScoreListView.setAdapter(getScoresFromSharedPreferences());
            }
        });
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
            playerScoreList.add(playerScores.get(i));
            System.out.println("Player scores are saved: " + playerScores.get(i));
        }
        Collections.sort(playerScoreList, Collections.reverseOrder());
        ArrayAdapter<String> ad = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, playerScoreList);
        return ad;
    }
}
