package com.example.android.spotifystreamer;

import android.media.AudioManager;
import android.media.MediaPlayer;

import java.io.IOException;

import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by oliviadodge on 7/27/2015.
 */
public class TrackPreviewPlayer extends MediaPlayer {

    private MediaPlayer mPlayer;
    private Track mTrack;

    public TrackPreviewPlayer(Track track){
        mTrack = track;
        mPlayer = new MediaPlayer();
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mPlayer.setDataSource(mTrack.preview_url);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mPlayer.prepareAsync();

//        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//            public void onCompletion(MediaPlayer mp) {
//                stop();
//            }
//        });
    }


    public void stop() {
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }
}
