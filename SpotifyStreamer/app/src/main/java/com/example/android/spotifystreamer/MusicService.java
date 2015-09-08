package com.example.android.spotifystreamer;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

/**
 * Created by FashyunGod on 9/7/2015.
 */
public class MusicService extends Service implements MediaPlayer.OnErrorListener {
    private static final String TAG = MusicService.class.getSimpleName();

    public static String MAIN_ACTION = "com.example.android.spotifystreamer.action.main";
    public static String PREV_ACTION = "com.example.android.spotifystreamer.action.prev";
    public static String PLAY_ACTION = "com.example.android.spotifystreamer.action.play";
    public static String NEXT_ACTION = "com.example.android.spotifystreamer.action.next";
    public static String STARTFOREGROUND_ACTION = "com.example.android.spotifystreamer.startforeground";
    public static String STOPFOREGROUND_ACTION = "com.example.android.spotifystreamer.stopforeground";

    public static int MUSIC_SERVICE = 101;


    MediaPlayer mMediaPlayer;
    //Binder given to clients
    private final IBinder mBinder = new MusicBinder();

    /**
     * Class used for the client Binder.
     */
    public class MusicBinder extends Binder {
        MusicService getService() {
            // Return this instance of LocalService so clients can call public methods
            return MusicService.this;
        }
    }

    /** Method for clients */
    public MediaPlayer playTrack(String trackPreviewUrl, final int currentPosition, final boolean isPaused) {

        if ((mMediaPlayer != null)) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }

        mMediaPlayer =  new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnErrorListener(this);

        //If there is no available track preview url, display a toast.
        if (trackPreviewUrl == null) {
            Toast toast = Toast.makeText(getApplicationContext(), getString(R.string.toast_track_not_playable), Toast.LENGTH_LONG);
            toast.show();
        }
        try {
            mMediaPlayer.setDataSource(trackPreviewUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //mPlayer will be prepared on a background thread. So we
        //set a listener on it here to start playing once it is prepared.
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                if (currentPosition > 0) {
                    mediaPlayer.seekTo(currentPosition);
                }
                if (checkNetworkConnection() && !isPaused) {
                    mMediaPlayer.start();
                    Log.i(TAG, "mPlayer prepared and started");
                } else if (!checkNetworkConnection()) {
                    Toast toast = Toast.makeText(getApplicationContext(), getString(R.string.toast_no_network_found), Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        });
        //Prepare MediaPlayer if there is a network connection
        if (checkNetworkConnection()) {
            mMediaPlayer.prepareAsync();
        } else {
            Toast toast = Toast.makeText(getApplicationContext(), getString(R.string.toast_no_network_found), Toast.LENGTH_LONG);
            toast.show();
        }

        return mMediaPlayer;
    }

    public MediaPlayer getMediaPlayer() {
        return mMediaPlayer;
    }

    //Helper method to check if there is an internet connection for playing tracks
    //and loading album artwork
    private boolean checkNetworkConnection() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        return (networkInfo != null && networkInfo.isConnected());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    //The following code in the onStartCommand method was taken and modified
    // from http://www.truiton.com/2014/10/android-foreground-service-example/
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().equals(STARTFOREGROUND_ACTION)) {
            Log.i(TAG, "Received Start Foreground Intent ");
            Intent notificationIntent = new Intent(this, MainActivity.class);
            notificationIntent.setAction(MAIN_ACTION);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                    notificationIntent, 0);

            Intent previousIntent = new Intent(this, MusicService.class);
            previousIntent.setAction(PREV_ACTION);
            PendingIntent ppreviousIntent = PendingIntent.getService(this, 0,
                    previousIntent, 0);

            Intent playIntent = new Intent(this, MusicService.class);
            playIntent.setAction(PLAY_ACTION);
            PendingIntent pplayIntent = PendingIntent.getService(this, 0,
                    playIntent, 0);

            Intent nextIntent = new Intent(this, MusicService.class);
            nextIntent.setAction(NEXT_ACTION);
            PendingIntent pnextIntent = PendingIntent.getService(this, 0,
                    nextIntent, 0);

            Bitmap icon = BitmapFactory.decodeResource(getResources(),
                    R.drawable.default_placeholder);

            Notification notification = new NotificationCompat.Builder(this)
                    .setContentTitle("Spotify Streamer")
                    .setTicker("Spotify Streamer")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(
                            Bitmap.createScaledBitmap(icon, 128, 128, false))
                    .setContentIntent(pendingIntent)
                    .setOngoing(true)
                    .addAction(android.R.drawable.ic_media_previous,
                            "Previous", ppreviousIntent)
                    .addAction(android.R.drawable.ic_media_play, "Play",
                            pplayIntent)
                    .addAction(android.R.drawable.ic_media_next, "Next",
                            pnextIntent).build();
            startForeground(MUSIC_SERVICE,
                    notification);
        } else if (intent.getAction().equals(PREV_ACTION)) {
            Log.i(TAG, "Clicked Previous");
        } else if (intent.getAction().equals(PLAY_ACTION)) {
            Log.i(TAG, "Clicked Play");
        } else if (intent.getAction().equals(NEXT_ACTION)) {
            Log.i(TAG, "Clicked Next");
        } else if (intent.getAction().equals(
                STOPFOREGROUND_ACTION)) {
            Log.i(TAG, "Received Stop Foreground Intent");
            stopForeground(true);
            stopSelf();
        }
        return START_STICKY;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return true;
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        // ... react appropriately ...
        // The MediaPlayer has moved to the Error state, must be reset!

        return true;
    }
}
