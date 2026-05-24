package com.example.snackgame;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {
    private SnakeView snakeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        snakeView = new SnakeView(this);
        setContentView(snakeView);
    }

    @Override
    protected void onPause() {
        super.onPause();
        snakeView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        snakeView.resume();
    }
}
