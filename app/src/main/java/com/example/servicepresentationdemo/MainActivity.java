package com.example.servicepresentationdemo;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity implements MusicService.MusicServiceCallback {
    TextView statusText;
    private MusicService musicService;
    private boolean isBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button startMusicButton = findViewById(R.id.btn_startmusic);
        Button stopMusicButton = findViewById(R.id.btn_stopmusic);
        statusText = findViewById(R.id.tv_status);

        startMusicButton.setOnClickListener(v -> {
//            Context context = getApplicationContext();
//            Intent startServiceIntent = new Intent(MainActivity.this, MusicService.class);
//            statusText.setText("Sample music: Is playing");
//            startServiceIntent.setAction(MusicService.ACTION_PLAY);
//
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                context.startForegroundService(startServiceIntent);
//            }else{
//                startService(startServiceIntent);
//            }

            if (isBound) {
                musicService.playMusic();
            }
        });

        stopMusicButton.setOnClickListener(v -> {
//            Intent stopServiceIntent = new Intent(MainActivity.this, MusicService.class);
//            statusText.setText("Sample music: Not playing");
//            stopService(stopServiceIntent);
            if (isBound) {
                musicService.stopMusic();
            }
        });
    }

    private ServiceConnection musicConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder)service;

            musicService = binder.getService();
            binder.setCallback(MainActivity.this);
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, musicConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isBound) {
            unbindService(musicConnection);
            isBound = false;
        }
    }

    @Override
    public void onMusicStatusChanged(String status) {
        runOnUiThread(() -> statusText.setText(status));
    }
}