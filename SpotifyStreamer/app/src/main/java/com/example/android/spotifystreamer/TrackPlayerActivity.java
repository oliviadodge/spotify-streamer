package com.example.android.spotifystreamer;

/**
 * Created by oliviadodge on 8/19/2015.
 *
 * This is an abstract activity class that will play top tracks from an artists and display a dialog fragment
 * for the currently playing track.
 *
 * Activities that subclass this activity should override the onCreate(Bundle savedInstanceState)
 * method, call super.onCreate(savedInstanceState) and call loadCursorFragment()
 * and handleSavedInstanceState(savedInstanceState)
 * when overriding the onCreate method depending on whether it will be a two
 * pane layout or one pane.
 */

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Toast;

import java.util.ArrayList;


public abstract class TrackPlayerActivity extends ActionBarActivity implements TopTracksFragment.Callback, TrackPlayerDialogFragment.TrackPlayerDialogListener {

    //Tags
    private static final String TAG = TrackPlayerActivity.class.getSimpleName();
    public static final String TRACK_PLAYER_DIALOG_FRAGMENT_TAG = "TrackPlayerDialogFragmentTag";
    public static final String CURSOR_FRAGMENT_TAG = "CursorFragmentTag";
    public static final String TOP_TRACKS_FRAGMENT_TAG = "TopTracksFragmentTag";

    //Keys
    public static final String KEY_TRACK_INFO_ARRAY_LIST = "track_info_array_list";
    public static final String KEY_IS_TRACK_PAUSED = "is_track_paused";
    public static final String KEY_CURRENT_PLAYBACK_POSITION = "current_position";
    public static final String KEY_NOW_PLAYING_URL = "now_playing_url";

    //Member Fields
    MediaPlayer mPlayer;
    Context mContext;
    String mNowPlayingUrl;
    int mCurrentPosition;
    boolean mIsPaused;
    Cursor mCursor;
    SeekBar mSeekBar;
    UpdateSeekBarTask mUpdateSeekBarTask;
    CursorFragment mCursorFragment;
    String mCountrySetting;
    MusicService mService;
    boolean mBound = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        mContext = this;
        mCountrySetting = Utility.getPreferredCountry(this);
        loadContentView();

        //Activities that subclass this Activity should
        //call loadCursorFragment() and handleSavedInstanceState(savedInstanceState)
        //when overriding the onCreate method depending on whether it will be a two
        //pane layout or one pane.
    }

    //Abstract method to be implemented by subclassing
    //Activities to load their custom views
    public abstract void loadContentView();


    //Abstract method to be implemented by subclassing
    //Activity to determine what to do when
    //starting the Activity from scratch
    public abstract void onSavedInstanceStateIsNull();

    public void loadCursorFragment() {
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
    }

    public void handleSavedInstanceState(Bundle savedInstanceState) {

        if (savedInstanceState == null) {
            onSavedInstanceStateIsNull();
        } else {
            //See if there was a TrackPlayerDialogFragment showing when the activity
            //was recreated or if there was a Url to a track currently streaming.
            TrackPlayerDialogFragment dialogFragment = (TrackPlayerDialogFragment) getFragmentManager()
                    .findFragmentByTag(TRACK_PLAYER_DIALOG_FRAGMENT_TAG);

            mNowPlayingUrl = savedInstanceState.getString(KEY_NOW_PLAYING_URL);

            //If there was a dialog showing, get the arguments.
            Bundle dialogFragmentArgs = new Bundle();
            if (dialogFragment != null) {
                Log.i(TAG, "onCreate() called and savedInstanceState and dialogFragment are not null ");
                dialogFragmentArgs = dialogFragment.getArguments();
            }

            //We need to address two scenarios that could have been the case when the device was rotated:
            //1. The dialog was showing (ie the dialog args are not null) so we need to restart the track player
            //2. The dialog had been dismissed but a track was playing or had been paused (ie mNowPlayingUrl is not null)
            if ((dialogFragmentArgs != null) || (mNowPlayingUrl != null)) {
                //Get a reference to the url, position, and state of the track that was playing when the activity was recreated.
                mCurrentPosition = savedInstanceState.getInt(KEY_CURRENT_PLAYBACK_POSITION, 0);
                mIsPaused = savedInstanceState.getBoolean(KEY_IS_TRACK_PAUSED, false);

                //If the track dialog was showing and paused make sure the dialog gets this info so it can update the playback button
                //to show the "play" icon.
                if ((dialogFragmentArgs != null) && (mIsPaused)) {
                    dialogFragmentArgs.putBoolean(KEY_IS_TRACK_PAUSED, true);
                }

                //If the track hadn't finished playing or there was a dialog showing, mNowPlayingUrl will not be null.
                //Restart the track player with the info we have. So if mNowPlayingUrl is not null,
                //when the service connection is established, restart the media player.
                //See the mConnection implementation for this logiic.

            }
        }

        // Bind to MusicService
        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }


    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Log.i(TAG, "onServiceConnected() called and mUrlNowPlaying is " + mNowPlayingUrl);
            // We've bound to MusicService, cast the IBinder and get MusicService instance
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            mService = binder.getService();
            mBound = true;

            if (mNowPlayingUrl != null) {
                mPlayer = mService.getMediaPlayer();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };


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

        //Make a new bundle of dialog fragment arguments
        Bundle dialogFragmentArgs = new Bundle();
        dialogFragmentArgs.putStringArrayList(KEY_TRACK_INFO_ARRAY_LIST, trackInfo);

        //If the last selected track is also the current one and it
        //was paused before it was dismissed, make sure this is info
        //is loaded into the arguments so the "play" icon can be shown.
        if (isTrackSelectedTrackPlaying(trackInfo) && (!mPlayer.isPlaying())) {
            dialogFragmentArgs.putBoolean(KEY_IS_TRACK_PAUSED, true);
        }

        //A few different scenarios could be the case. If any of these cases are true,
        // we need to start the player again:
        //1. A track had not yet been selected or had completed playback and the dialog was dismissed
                //(mNowPlayingUrl will be null)
        //2. The device was rotated (mPlayer is null)
        //3. The track selected is other than the track that is currently playing.
        if ((mNowPlayingUrl == null) || (mPlayer == null) || (!isTrackSelectedTrackPlaying(trackInfo))){
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

//        //Start the task to update the progress bar every second.
//        startUpdatingSeekBar();
    }

    //Helper method to start updating the seek bar as the song plays
    private void startUpdatingSeekBar() {
        //Make sure you are not creating a second AsyncTask.
        //If there was already one running, cancel it and start a new one so that
        //it doesn't call mPlayer.getCurrentPosition() when the MediaPlayer is not
        //in a valid state.
        if ((mUpdateSeekBarTask == null) || (mUpdateSeekBarTask.isCancelled())) {
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

        //Restart the UpdateSeekBarTask now that we have a seek bar to update the progress bar
        // unless of course mPlayer is null.
        if (mPlayer != null) {
            startUpdatingSeekBar();
            Log.i(TAG, "UpdateSeekBarTask executed from onDialogViewCreated");
        }
    }

    public void startTrackPlayer(String trackPreviewUrl, final int currentPosition, final boolean isPaused) {

        //We must cancel the UpdateSeekBarTask if it is running.
        //Otherwise we might get an IllegalStateException when we are
        //preparing the MediaPlayer and the Task calls getCurrentPosition().
        if ((mUpdateSeekBarTask != null) && (!mUpdateSeekBarTask.isCancelled())) {
            mUpdateSeekBarTask.cancel(true);
        }

        if (mBound) {
            mPlayer = mService.playTrack(trackPreviewUrl, currentPosition, isPaused);
        } else {
            Log.i(TAG, "Activity not bound to service");
        }

        // Now that mPlayer is not null, restart the UpdateSeekBarTask, unless of
        //course we don't have a seek bar.
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
        if ((mUpdateSeekBarTask != null) && (!mUpdateSeekBarTask.isCancelled())) {
            mUpdateSeekBarTask.cancel(true);
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

        //If there is only one song to play, show a toast and continue business
        //as usual
        if (mCursor.getCount() == 1) {
            Toast toast = Toast.makeText(this, getString(R.string.toast_no_more_tracks), Toast.LENGTH_LONG);
            toast.show();
            return;
        }

        if (mCursor.moveToNext()) {
            onItemSelected(addTrackInfoFromCursor(mCursor), mCursor);
        } else if (mCursor.moveToFirst()) {
            onItemSelected(addTrackInfoFromCursor(mCursor), mCursor);
        }
    }

    //Method required of the TrackPlayerDialogFragment listener
    //to play the previous song
    @Override
    public void onPreviousClick(DialogFragment dialog) {

        //If there is only one song to play, show a toast and continue business
        //as usual
        if (mCursor.getCount() == 1) {
            Toast toast = Toast.makeText(this, getString(R.string.toast_no_more_tracks), Toast.LENGTH_LONG);
            toast.show();
            return;
        }

        if (mCursor.moveToPrevious()) {
            onItemSelected(addTrackInfoFromCursor(mCursor), mCursor);
        } else if (mCursor.moveToLast()) {
            onItemSelected(addTrackInfoFromCursor(mCursor), mCursor);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();


        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    //Standard getters and setters for member fields in case the sub-classing activites
    //need to access them.

    public MediaPlayer getPlayer() {
        return mPlayer;
    }

    public void setPlayer(MediaPlayer player) {
        mPlayer = player;
    }

    public String getNowPlayingUrl() {
        return mNowPlayingUrl;
    }

    public void setNowPlayingUrl(String nowPlayingUrl) {
        mNowPlayingUrl = nowPlayingUrl;
    }

    public boolean isPaused() {
        return mIsPaused;
    }

    public void setIsPaused(boolean isPaused) {
        mIsPaused = isPaused;
    }

    public Cursor getCursor() {
        return mCursor;
    }

    public void setCursor(Cursor cursor) {
        mCursor = cursor;
    }

    public SeekBar getSeekBar() {
        return mSeekBar;
    }

    public void setSeekBar(SeekBar seekBar) {
        mSeekBar = seekBar;
    }

    public UpdateSeekBarTask getUpdateSeekBarTask() {
        return mUpdateSeekBarTask;
    }

    public void setUpdateSeekBarTask(UpdateSeekBarTask updateSeekBarTask) {
        mUpdateSeekBarTask = updateSeekBarTask;
    }

    public CursorFragment getCursorFragment() {
        return mCursorFragment;
    }

    public void setCursorFragment(CursorFragment cursorFragment) {
        mCursorFragment = cursorFragment;
    }

    //Class to poll for track preview progress and update the UI accordingly
    protected class UpdateSeekBarTask extends AsyncTask<Void, Integer, Void> {

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
