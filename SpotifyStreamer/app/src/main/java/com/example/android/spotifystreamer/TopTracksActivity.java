package com.example.android.spotifystreamer;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.util.Log;

public class TopTracksActivity extends TrackPlayerActivity {

    //Tags
    private static final String TAG = TopTracksActivity.class.getSimpleName();
    public static final String TOP_TRACKS_FRAGMENT_TAG = "TopTracksFragmentTag";

    //Member fields
    String mCountrySetting;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Initialize member fields
        mCountrySetting = Utility.getPreferredCountry(this);
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

    @Override
    protected void onResume() {
        super.onResume();
        String preferredCountry = Utility.getPreferredCountry(this);
        // update the country using the fragment manager
        if (preferredCountry != null && !preferredCountry.equals(mCountrySetting)) {
            TopTracksFragment ff = (TopTracksFragment) getFragmentManager().findFragmentByTag(TOP_TRACKS_FRAGMENT_TAG);
            if (null != ff) {
                ff.onCountryChanged(preferredCountry);
                Log.i(TAG, "TopTracksFragment.onCountryChanged called");
            } else {
                Log.i(TAG, "attempted, but failed to get TopTracksFragment by tag.");
            }
            mCountrySetting = preferredCountry;
        }
    }
}
