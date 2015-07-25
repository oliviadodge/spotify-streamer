package com.example.android.spotifystreamer;

import android.content.Context;

import java.util.List;

import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by oliviadodge on 6/30/2015.
 */
public class TopTracksLab {

    private static final String TAG = "TopTracksLab";

    private List<Track> mToptracks;
    private String mArtistId;

    private static TopTracksLab sTopTracksLab;
    private Context mAppContext;

    private TopTracksLab(Context appContext){
        mAppContext = appContext;
    }

    public static TopTracksLab get(Context context) {
        if (sTopTracksLab == null) {
            sTopTracksLab = new TopTracksLab(context.getApplicationContext());
        }
        return sTopTracksLab;
    }

    public String getArtistId() {
        return mArtistId;
    }

    public void setArtistId(String artistId) {
        mArtistId = artistId;
    }

    public List<Track> getTopTracks() {
        return mToptracks;
    }

    public void setTopTracks(List<Track> toptracks) {
        mToptracks = toptracks;
    }

    public Track getTrack(String spotifyId) {
        for (Track t : mToptracks) {
            if (t.id.equals(spotifyId)) {
                return t;
            }
        }
        return null;
    }
}
