package com.example.android.spotifystreamer;

import android.content.Context;

import java.util.List;

import kaaes.spotify.webapi.android.models.Artist;

/**
 * Created by oliviadodge on 6/30/2015.
 */
public class ArtistLab {

    private List<Artist> mArtists;

    private static ArtistLab sArtistLab;
    private Context mAppContext;

    private ArtistLab(Context appContext){
        mAppContext = appContext;
    }

    public static ArtistLab get(Context context) {
        if (sArtistLab == null) {
            sArtistLab = new ArtistLab(context.getApplicationContext());
        }
        return sArtistLab;
    }

    public List<Artist> getArtists() {
        return mArtists;
    }

    public Artist getArtist(String spotifyId) {
        for (Artist a : mArtists) {
            if (a.id.equals(spotifyId)) {
                return a;
            }
        }
        return null;
    }

    public void setArtists(List<Artist> artists) {
        mArtists = artists;
    }
}
