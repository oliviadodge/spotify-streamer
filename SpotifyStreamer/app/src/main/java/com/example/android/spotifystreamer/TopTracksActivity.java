package com.example.android.spotifystreamer;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.util.Log;

public class TopTracksActivity extends TrackPlayerActivity {

    //Tags
    private static final String TAG = TopTracksActivity.class.getSimpleName();
    public static final String TOP_TRACKS_FRAGMENT_TAG = "TopTracksFragmentTag";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadCursorFragment();

        handleSavedInstanceState(savedInstanceState);

        //Set subtitle on the ActionBar to be the name of the artist
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            String artistName = getIntent().getStringExtra(ArtistsFragment.EXTRA_ARTIST_NAME);
            ab.setSubtitle(artistName);
        }
    }

    @Override
    public void loadContentView() {
        setContentView(R.layout.activity_top_tracks);
    }

    @Override
    public void onSavedInstanceStateIsNull() {
        //Start up TopTracksFragment using the uri sent with the intent
        Log.i(TAG, "onCreate() called and savedInstanceState is null. Putting TRACKS_URI " +
                "and EXTRA_ARTIST_SPOTIFY_ID into arguments. Uri is " + getIntent().getData());
        Bundle topTracksFragmentArgs = new Bundle();
        topTracksFragmentArgs.putParcelable(TopTracksFragment.TRACKS_URI, getIntent().getData());
        topTracksFragmentArgs.putString(ArtistsFragment.EXTRA_ARTIST_SPOTIFY_ID, getIntent().getStringExtra(ArtistsFragment.EXTRA_ARTIST_SPOTIFY_ID));

        TopTracksFragment fragment = new TopTracksFragment();
        fragment.setArguments(topTracksFragmentArgs);

        getFragmentManager().beginTransaction()
                .add(R.id.fragment_top_tracks, fragment, TOP_TRACKS_FRAGMENT_TAG)
                .commit();
    }

}
