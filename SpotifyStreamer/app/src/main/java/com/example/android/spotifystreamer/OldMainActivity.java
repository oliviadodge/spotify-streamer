package com.example.android.spotifystreamer;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;


public class OldMainActivity extends ActionBarActivity implements ArtistsFragment.Callback, TopTracksFragment.Callback, TrackPlayerDialogFragment.TrackPlayerDialogListener {

    //Tags
    private static final String TAG = OldMainActivity.class.getSimpleName();
    public static final String TOP_TRACKS_FRAGMENT_TAG = "TopTracksFragmentTag";
    public static final String TRACK_PLAYER_DIALOG_FRAGMENT_TAG = "TrackPlayerDialogFragmentTag";
    public static final String CURSOR_FRAGMENT_TAG = "CursorFragmentTag";

    //Keys
    public static final String KEY_TRACK_INFO_ARRAY_LIST = "track_info_array_list";
    public static final String KEY_IS_TRACK_PAUSED = "is_track_paused";
    public static final String KEY_CURRENT_PLAYBACK_POSITION = "current_position";
    public static final String KEY_NOW_PLAYING_URL = "now_playing_url";

    //Member Fields
    private boolean mTwoPane;
    String mCountrySetting;
    MediaPlayer mPlayer;
    Context mContext;
    String mNowPlayingUrl;
    boolean mIsPaused;
    Cursor mCursor;
    SeekBar mSeekBar;
    UpdateSeekBarTask mUpdateSeekBarTask;
    CursorFragment mCursorFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCountrySetting = Utility.getPreferredCountry(this);
        mContext = this;

        setContentView(R.layout.activity_main);
        if (findViewById(R.id.fragment_top_tracks) != null){
            mTwoPane = true;

            //Find the retained fragment to load the cursor of tracks for media playback after screen
            // rotation
            FragmentManager fm = getFragmentManager();
            mCursorFragment = (CursorFragment) fm.findFragmentByTag(CURSOR_FRAGMENT_TAG);

            //If the CursorFragment is null, create it for the first time and add it to FragmentManager
            //Otherwise get the cursor from the retained fragment and give it to mCursor.
            if (mCursorFragment == null) {
                mCursorFragment = new CursorFragment();
                fm.beginTransaction().add(mCursorFragment, CURSOR_FRAGMENT_TAG).commit();
                Log.i(TAG, "CursorFragment created and committed");
            } else {
                mCursor = mCursorFragment.getCursor();
                Log.i(TAG, "mCursor recovered from CursorFragment");
            }

            Log.i(TAG, "onCreate called and mTwoPane is " + mTwoPane);

            if (savedInstanceState == null) {
                getFragmentManager().beginTransaction()
                        .replace(R.id.fragment_top_tracks, new TopTracksFragment(), TOP_TRACKS_FRAGMENT_TAG)
                        .commit();
            } else {
                //See if there was a TrackPlayerDialogFragment showing when the activity
                //was recreated or if there was a Url to a track currently streaming.
                TrackPlayerDialogFragment dialogFragment = (TrackPlayerDialogFragment)getFragmentManager()
                        .findFragmentByTag(TRACK_PLAYER_DIALOG_FRAGMENT_TAG);

                mNowPlayingUrl = savedInstanceState.getString(KEY_NOW_PLAYING_URL);

                //If there was a dialog showing, get the arguments and put them into
                //a new ArrayList<String> trackInfo
                ArrayList<String> trackInfo = new ArrayList<>();
                Bundle dialogFragmentArgs = new Bundle();
                if (dialogFragment != null) {
                    Log.i(TAG, "onCreate() called and savedInstanceState and dialogFragment are not null ");
                    dialogFragmentArgs = dialogFragment.getArguments();
                    ArrayList<String> trackAttr = dialogFragmentArgs.getStringArrayList(KEY_TRACK_INFO_ARRAY_LIST);
                    if (trackAttr != null) {
                        trackInfo = trackAttr;
                    }
                }

                //We need to address two scenarios that could have been the case when the device was rotated:
                //1. The dialog was showing (ie trackInfo is greater than 0) so we need to restart the track player
                //2. The dialog had been dismissed but a track was playing or had been paused (ie mNowPlayingUrl is not null)
                if ((trackInfo.size() > 0) || (mNowPlayingUrl != null)) {
                    //Get a reference to the url, position, and state of the track that was playing when the activity was recreated.
                    int currentPosition = savedInstanceState.getInt(KEY_CURRENT_PLAYBACK_POSITION, 0);
                    mIsPaused = savedInstanceState.getBoolean(KEY_IS_TRACK_PAUSED, false);

                    //If the track was paused make sure the dialog gets this info so it can update the playback button
                    //to show the "play" icon.
                    if (mIsPaused) {
                        dialogFragmentArgs.putBoolean(KEY_IS_TRACK_PAUSED, true);
                    }

                    if ((trackInfo.size() > 0) && (mNowPlayingUrl == null)) {
                        mNowPlayingUrl = trackInfo.get(TopTracksFragment.COL_TRACK_PREVIEW_URL);
                    }

                    if (mNowPlayingUrl != null) {
                        startTrackPlayer(mNowPlayingUrl, currentPosition, mIsPaused);
                    }
                }
            }
        } else{
            //fragment_top_tracks could not be found so this must be a smaller device.
            //Set mTwoPane to false
            mTwoPane = false;
             Log.i(TAG, "onCreate called and mTwoPane is " + mTwoPane);
        }
    }

    //Method required by ArtistsFragment.Callback when an artist list item is selected
    @Override
    public void onItemSelected(Uri contentUri, String artistSpotifyId, String artistName){
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
            Log.i(TAG, "onItemSelected called, uri is " + contentUri);
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
        if (mUpdateSeekBarTask != null) {
            mUpdateSeekBarTask.cancel(true);
        }
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
        mNowPlayingUrl = null;
        mIsPaused = false;
        mCursor = null;
        mCursorFragment.setCursor(null);
        mSeekBar = null;
    }
    //Method required by TopTracksFragment.Callback to start up the track player dialog
    //when a track is selected from the list
    @Override
    public void onItemSelected(ArrayList<String> trackInfo, Cursor cursor) {

        Log.i(TAG, "onItemSelected called, trackinfo is " + trackInfo + " and cursor count is" + cursor.getCount());

        //Get a reference to the top tracks cursor so that we can implement onNextClick
        //and onPreviousClick for the track player fragment.
        mCursor = cursor;
        //Make sure mCursor is retained across rotation by saving it in a fragment
        mCursorFragment.setCursor(mCursor);
        Bundle dialogFragmentArgs = new Bundle();
        dialogFragmentArgs.putStringArrayList(KEY_TRACK_INFO_ARRAY_LIST, trackInfo);
        if (isTrackSelectedTrackPlaying(trackInfo) && (!mPlayer.isPlaying())) {
            dialogFragmentArgs.putBoolean(KEY_IS_TRACK_PAUSED, true);
        }

        //Check if there is a track playing and if it is also the track that was selected. If not start the track player.
        if ((mNowPlayingUrl == null) || (mPlayer == null)) {
            //There is no track currently playing. Start up the MediaPlayer.
            mNowPlayingUrl = trackInfo.get(TopTracksFragment.COL_TRACK_PREVIEW_URL);
            startTrackPlayer(trackInfo.get(TopTracksFragment.COL_TRACK_PREVIEW_URL), 0, false);
        } else if (!isTrackSelectedTrackPlaying(trackInfo)) {
            //A different track was selected. Start up the MediaPlayer.
            mNowPlayingUrl = trackInfo.get(TopTracksFragment.COL_TRACK_PREVIEW_URL);
            startTrackPlayer(trackInfo.get(TopTracksFragment.COL_TRACK_PREVIEW_URL), 0, false);
        }

        showDialog(dialogFragmentArgs);
    }

    //Method to show the DialogFragment with all the track information and the
    //playback buttons.
    public void showDialog(Bundle args) {
        TrackPlayerDialogFragment dialogFragment = new TrackPlayerDialogFragment();
        dialogFragment.setArguments(args);

        // Remove any currently showing track player dialog.
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment previousDialog = getFragmentManager().findFragmentByTag(TRACK_PLAYER_DIALOG_FRAGMENT_TAG);
        if (previousDialog != null) {
            ft.remove(previousDialog);
        }

        // Create and show the dialog.
        dialogFragment.show(ft, TRACK_PLAYER_DIALOG_FRAGMENT_TAG);

        //Start the task to update the progress bar every second.
        startUpdatingSeekBar();
    }

    //Helper method to start updating the seek bar as the song plays
    private void startUpdatingSeekBar() {
        if (mUpdateSeekBarTask == null) {
            mUpdateSeekBarTask = new UpdateSeekBarTask();
            mUpdateSeekBarTask.execute();
        } else if (mUpdateSeekBarTask.isCancelled()) {
            mUpdateSeekBarTask = new UpdateSeekBarTask();
            mUpdateSeekBarTask.execute();
        } else {
            mUpdateSeekBarTask.cancel(true);
            mUpdateSeekBarTask = new UpdateSeekBarTask();
            mUpdateSeekBarTask.execute();
        }
    }

    @Override
    public void onDialogViewCreated(View view) {
        Log.i(TAG, "onDialogViewCreated called");
        mSeekBar = (SeekBar) view.findViewById(R.id.track_player_seek_bar);

        //Restart the UpdateSeekBarTask to update the progress bar every second.
        if (mPlayer != null) {
            startUpdatingSeekBar();
            Log.i(TAG, "UpdateSeekBarTask executed from onDialogViewCreated");
        }
    }

    public void startTrackPlayer(String trackPreviewUrl, final int currentPosition, final boolean isPaused) {

        if ((mUpdateSeekBarTask != null) && (!mUpdateSeekBarTask.isCancelled())) {
            mUpdateSeekBarTask.cancel(true);
        }

        if ((mPlayer != null)) {
            mPlayer.release();
            mPlayer = null;
        }

        mPlayer = new MediaPlayer();
        //Give the retained fragment a reference to mPlayer so it can be saved on rotation.
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        //If there is no available track preview url, display a toast.
        if (trackPreviewUrl == null) {
            Toast toast = Toast.makeText(mContext, getString(R.string.toast_track_not_playable), Toast.LENGTH_LONG);
            toast.show();
        }
        try {
            mPlayer.setDataSource(trackPreviewUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //mPlayer will be prepared on a background thread. So we
        //set a listener on it here to start playing once it is prepared.
        mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mPlayer = mediaPlayer;
                if (currentPosition > 0) {
                    mediaPlayer.seekTo(currentPosition);
                }
                if (checkNetworkConnection() && !isPaused) {
                    mPlayer.start();
                    mIsPaused = false;
                    Log.i(TAG, "mPlayer prepared and started");
                } else if (!checkNetworkConnection()) {
                    Toast toast = Toast.makeText(mContext, getString(R.string.toast_no_network_found), Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        });

        //Prepare MediaPlayer if there is a network connection
        if (checkNetworkConnection()) {
            mPlayer.prepareAsync();
        } else {
            Toast toast = Toast.makeText(mContext, getString(R.string.toast_no_network_found), Toast.LENGTH_LONG);
            toast.show();
        }

        //Set the onCompletionListener here so that we can show the play button (for the user to replay the track) after it finishes.
        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                DialogFragment dialogFragment = (DialogFragment) getFragmentManager().findFragmentByTag(TRACK_PLAYER_DIALOG_FRAGMENT_TAG);
                if (dialogFragment != null) {
                    ImageButton playButton = (ImageButton) dialogFragment.getDialog().findViewById(R.id.track_player_pause);
                    playButton.setSelected(true);
                    mIsPaused = true;
                } else {
                    mPlayer.reset();
                    mNowPlayingUrl = null;
                }
            }
        });

        //Restart the UpdateSeekBarTask to update the progress bar every second.
        if (mSeekBar != null) {
            startUpdatingSeekBar();
            Log.i(TAG, "UpdateSeekBarTask executed from startTrackPlayer");
        }
    }

    //Helper method to determine if the track that was selected is the
    //track that is currently playing
    private boolean isTrackSelectedTrackPlaying(ArrayList<String> trackInfo) {
        String selectedTrackUrl = trackInfo.get(TopTracksFragment.COL_TRACK_PREVIEW_URL);
        return ((mNowPlayingUrl != null) && (mNowPlayingUrl.equals(selectedTrackUrl)));
    }

    //Helper method to check if there is an internet connection for playing tracks
    //and loading album artwork
    private boolean checkNetworkConnection() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        return (networkInfo != null && networkInfo.isConnected());
    }

    @Override
    public void onStop() {
        Log.i(TAG, "onStop() called! Cancelling tasks and releasing mediaplayer");
        if (mUpdateSeekBarTask != null) {
            mUpdateSeekBarTask.cancel(true);
        }

        if (mPlayer != null) {

            mPlayer.release();
            mPlayer = null;
        }

        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //Method required of the TrackPlayerDialogFragment listener
    //to play the next song in the list
    @Override
    public void onNextClick(DialogFragment dialog) {
        if (mUpdateSeekBarTask != null) {
            mUpdateSeekBarTask.cancel(true);
        }
        if (mCursor != null) {
            if (mCursor.moveToNext()) {
                onItemSelected(addTrackInfoFromCursor(mCursor), mCursor);
            } else if (mCursor.moveToFirst()) {
                onItemSelected(addTrackInfoFromCursor(mCursor), mCursor);
            }
        }
    }

    //Method required of the TrackPlayerDialogFragment listener
    //to play the previous song
    @Override
    public void onPreviousClick(DialogFragment dialog) {
        if (mUpdateSeekBarTask != null) {
            mUpdateSeekBarTask.cancel(true);
        }
        if (mCursor != null) {
            if (mCursor.moveToPrevious()) {
                onItemSelected(addTrackInfoFromCursor(mCursor), mCursor);
            } else if (mCursor.moveToLast()) {
                onItemSelected(addTrackInfoFromCursor(mCursor), mCursor);
            }
        }
    }

    //Helper method for onPreviousClick and onNextClick so that the previous or next
    //track info can be loaded
    private ArrayList<String> addTrackInfoFromCursor(Cursor cursor) {
        ArrayList<String> trackInfo = new ArrayList<>();
        trackInfo.add(TopTracksFragment.COL_TRACK_NAME, cursor.getString(TopTracksFragment.COL_TRACK_NAME));
        trackInfo.add(TopTracksFragment.COL_ARTIST_NAME, cursor.getString(TopTracksFragment.COL_ARTIST_NAME));
        trackInfo.add(TopTracksFragment.COL_ALBUM_NAME, cursor.getString(TopTracksFragment.COL_ALBUM_NAME));
        trackInfo.add(TopTracksFragment.COL_ALBUM_IMAGE_URL, cursor.getString(TopTracksFragment.COL_ALBUM_IMAGE_URL));
        trackInfo.add(TopTracksFragment.COL_TRACK_PREVIEW_URL, cursor.getString(TopTracksFragment.COL_TRACK_PREVIEW_URL));

        return trackInfo;
    }


    //Method required of the TrackPlayerDialogFragment listener
    //to play or pause the currently selected song
    @Override
    public void onPlayPauseClick(DialogFragment dialog) {
        Dialog d = dialog.getDialog();
        ImageButton pauseButton = (ImageButton) d.findViewById(R.id.track_player_pause);

        //If the track had been playing, pause it and set the button to show the "play" icon.
        if (mPlayer.isPlaying()) {
            pauseButton.setSelected(true);
            Log.i(TAG, "Pause button clicked and the value of isPlaying() is  " + mPlayer.isPlaying() +
                    ", and the value of isSelected is " + d.findViewById(R.id.track_player_pause).isSelected());
            mPlayer.pause();
            mIsPaused = true;
            Log.i(TAG, "mPlayer.pause() called and the value of isPlaying() is  " + mPlayer.isPlaying());
        }
        //If the track had NOT been playing, play it and set the button to show the "pause" icon.
        else if (checkNetworkConnection()) {
            Log.i(TAG, "Play button pressed and value of mNowPlayingUrl is " + mNowPlayingUrl);
            mPlayer.start();
            pauseButton.setSelected(false);
            mIsPaused = false;
        } else {
            Toast toast = Toast.makeText(this, getString(R.string.toast_no_network_found), Toast.LENGTH_LONG);
            toast.show();
        }
    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            mPlayer.seekTo(progress);
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        String country = Utility.getPreferredCountry(this);
        // update the location in our second pane using the fragment manager
            if (country != null && !country.equals(mCountrySetting)) {
            TopTracksFragment ff = (TopTracksFragment)getFragmentManager().findFragmentByTag(TOP_TRACKS_FRAGMENT_TAG);
            if ( null != ff ) {
                ff.onCountryChanged(country);
            }
            mCountrySetting = country;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.i(TAG, "onSaveInstanceState called");

        //If a track had been playing, get the current position and whether it was paused so
        //this can be rebuilt once the activity restarts
        if (mPlayer != null) {
            outState.putInt(KEY_CURRENT_PLAYBACK_POSITION, mPlayer.getCurrentPosition());
            outState.putBoolean(KEY_IS_TRACK_PAUSED, !mPlayer.isPlaying());
            outState.putString(KEY_NOW_PLAYING_URL, mNowPlayingUrl);
        }
    }

    //Class to poll for track preview progress and update the UI accordingly
    private class UpdateSeekBarTask extends AsyncTask<Void, Integer, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            int progress = 0;
            Log.i(TAG, "UpdateSeekBarTask started and running ");
            while (mPlayer != null) {
                progress = mPlayer.getCurrentPosition();
                publishProgress(progress);

                if (isCancelled()) break;

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onCancelled() {
            Log.i(TAG, "UpdateSeekBarTask is being cancelled! ");
            super.onCancelled();

        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            if (mSeekBar != null) {
                mSeekBar.setProgress(values[0]);
            } else {
                Log.i(TAG, "mSeekBar == null!!!");
                //This will occur when the dialog has been dismissed but the track is still playing.
                //Cancel the task.
                cancel(true);
            }
        }
    }
}
