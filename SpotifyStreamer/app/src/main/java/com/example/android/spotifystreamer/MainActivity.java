package com.example.android.spotifystreamer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;


public class MainActivity extends TrackPlayerActivity implements ArtistsFragment.Callback {

    //Tags
    private static final String TAG = MainActivity.class.getSimpleName();
    public static final String TOP_TRACKS_FRAGMENT_TAG = "TopTracksFragmentTag";

    //Member Fields
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (findViewById(R.id.fragment_top_tracks) != null){
            //The application is launched on a tablet. Set mTwoPane true.
            mTwoPane = true;

            //Load the fragment that will retain on rotation an instance
            //of the top tracks cursor
            loadCursorFragment();

            handleSavedInstanceState(savedInstanceState);

        } else{
            //fragment_top_tracks could not be found so this must be a smaller device.
            //Set mTwoPane to false
            mTwoPane = false;
        }
    }

    @Override
    public void loadContentView() {
        setContentView(R.layout.activity_main);
    }

    @Override
    public void onSavedInstanceStateIsNull() {
        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_top_tracks, new TopTracksFragment(), TOP_TRACKS_FRAGMENT_TAG)
                .commit();
    }

    //Method required by ArtistsFragment.Callback when an artist list item is selected
    @Override
    public void onItemSelected(android.net.Uri contentUri, String artistSpotifyId, String artistName){
        if (mTwoPane){
            Log.i(TAG, "onItemSelected called, uri is " + contentUri);

            TopTracksFragment ttf = (TopTracksFragment)getFragmentManager().findFragmentByTag(TOP_TRACKS_FRAGMENT_TAG);
            Bundle oldArgs = new Bundle();
            if (ttf != null) {
                oldArgs = ttf.getArguments();
            }

            //Check whether the old arguments are the same as the new ones
            if (oldArgs != null) {
                Uri oldUri = oldArgs.getParcelable(TopTracksFragment.TRACKS_URI);
                if ((oldUri != null) && (oldUri.equals(contentUri))) {
                    return;
                } else {
                    removeTrackPlayerComponents();
                }
            }

            Bundle args = new Bundle();
            args.putParcelable(TopTracksFragment.TRACKS_URI, contentUri);
            args.putString(ArtistsFragment.EXTRA_ARTIST_SPOTIFY_ID, artistSpotifyId);
            args.putString(ArtistsFragment.EXTRA_ARTIST_NAME, artistName);

            TopTracksFragment fragment = new TopTracksFragment();
            fragment.setArguments(args);

            getFragmentManager().beginTransaction()
            .replace(R.id.fragment_top_tracks, fragment, TOP_TRACKS_FRAGMENT_TAG)
            .commit();
        } else{
            android.util.Log.i(TAG, "onItemSelected called, uri is " + contentUri);
            Intent intent = new Intent(this, TopTracksActivity.class)
            .setData(contentUri);
            intent.putExtra(ArtistsFragment.EXTRA_ARTIST_SPOTIFY_ID, artistSpotifyId);
            intent.putExtra(ArtistsFragment.EXTRA_ARTIST_NAME, artistName);
            startActivity(intent);
        }
    }

    //Method required by ArtistsFragment.Callback
    @Override
    public void onSearchTermChanged() {
        //If there are two panes, remove the TopTracksFragment when the search term changes (ie a new artist is loaded)
        if (mTwoPane) {
            TopTracksFragment topTracksFragment = (TopTracksFragment) getFragmentManager().findFragmentByTag(TOP_TRACKS_FRAGMENT_TAG);
            if ((topTracksFragment != null) && (topTracksFragment.getArguments()!= null)) {
                getFragmentManager().beginTransaction().remove(topTracksFragment).commit();

                removeTrackPlayerComponents();
                Log.i(TAG, "Track player components removed");
            }
        }
    }

    //Helper method to reset the track player components of this activity once
    //a new search term has been entered (ie. the artist is changed)
    private void removeTrackPlayerComponents() {
        if (getUpdateSeekBarTask() != null) {
            getUpdateSeekBarTask().cancel(true);
        }
        if (getPlayer() != null) {
            getPlayer().release();
            setPlayer(null);
        }
        setNowPlayingUrl(null);
        setIsPaused(false);
        setCursor(null);
        getCursorFragment().setCursor(null);
        setSeekBar(null);
    }
}
