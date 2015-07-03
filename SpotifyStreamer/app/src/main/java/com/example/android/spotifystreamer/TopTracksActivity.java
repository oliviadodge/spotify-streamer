package com.example.android.spotifystreamer;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;


public class TopTracksActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_tracks);

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            String artistId = getIntent().getStringExtra(MainActivityFragment.ARTIST_ID_EXTRA);
            ab.setSubtitle(ArtistLab.get(this).getArtist(artistId).name);
        }
    }
}
