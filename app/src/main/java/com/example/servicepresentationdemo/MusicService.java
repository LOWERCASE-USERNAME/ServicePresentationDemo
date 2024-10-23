package com.example.servicepresentationdemo;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class MusicService extends Service {
    public interface MusicServiceCallback {
        void onMusicStatusChanged(String status);
    }

    public static final String ACTION_PLAY = "servicepresentationdemo.ACTION_PLAY";
    public static final String ACTION_PAUSE = "servicepresentationdemo.ACTION_PAUSE";
    public static final String ACTION_STOP = "servicepresentationdemo.ACTION_STOP";

    private final IBinder binder = new MusicBinder();
    private MusicServiceCallback callback;
    private MediaPlayer mediaPlayer;
    private static final String CHANNEL_ID = "MusicPlayerChannel";

    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }

        public void setCallback(MusicServiceCallback callback) {
            MusicService.this.callback = callback; // Set the callback instance
        }

    }


    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = MediaPlayer.create(this, R.raw.sample);
        mediaPlayer.setLooping(true);
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground( 1, getNotification());

        String action = intent.getAction();

        if (ACTION_PLAY.equals(action)) {
            playMusic();
        } else if (ACTION_PAUSE.equals(action)) {
            pauseMusic();
        } else if (ACTION_STOP.equals(action)) {
            stopMusic();
            stopForeground(true);
            stopSelf();
        }

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent){
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        return false;
    }

    public void playMusic() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            updateNotification();
            if (callback != null) {
                callback.onMusicStatusChanged("Sample Music: Is playing");
            }
        }
    }

    public void pauseMusic() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            updateNotification();
            if (callback != null) {
                callback.onMusicStatusChanged("Sample Music: Is pausing");
            }
        }
    }

    public void stopMusic() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            if (callback != null) {
                callback.onMusicStatusChanged("Sample Music: Not playing");
            }
        }
    }

    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    private Notification getNotification() {
        Log.d("MusicService", "get-notification");
        // Play Intent
        Intent playIntent = new Intent(this, MusicService.class);
        playIntent.setAction(ACTION_PLAY);
        PendingIntent playPendingIntent = PendingIntent.getService(this, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);

        // Pause Intent
        Intent pauseIntent = new Intent(this, MusicService.class);
        pauseIntent.setAction(ACTION_PAUSE);
        PendingIntent pausePendingIntent = PendingIntent.getService(this, 1, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);

        // Stop Intent
        Intent stopIntent = new Intent(this, MusicService.class);
        stopIntent.setAction(ACTION_STOP);
        PendingIntent stopPendingIntent = PendingIntent.getService(this, 2, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Music Playing")
                .setContentText("Control the music from here")
                .setSmallIcon(R.drawable.ic_music_note_24)
                .addAction(R.drawable.ic_play_arrow_24, "Play", playPendingIntent)
                .addAction(R.drawable.ic_pause_24, "Pause", pausePendingIntent)
                .addAction(R.drawable.ic_stop_24, "Stop", stopPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(playPendingIntent)  // Intent to launch when tapping notification
                .build();
    }

    private void updateNotification() {
        Log.d("MusicService", "update-notification");
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, getNotification());
    }

    private void createNotificationChannel() {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID, "Music Player Channel", NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    @Override
    public void onDestroy() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        super.onDestroy();
    }
}
