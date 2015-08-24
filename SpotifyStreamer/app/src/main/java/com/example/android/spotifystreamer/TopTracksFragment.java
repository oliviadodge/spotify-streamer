package com.example.android.spotifystreamer;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.spotifystreamer.data.DataContract;

import java.util.ArrayList;


/**
 * The TopTracksFragment will load the top 10 tracks for a selected artist and display them in a list view.
 */
public class TopTracksFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final int TRACKS_LOADER = 0;
    public static final String TRACKS_URI = "tracks_uri";
    private static final String KEY_SELECTED = "selected_position";

    private static final String[] TRACK_COLUMNS = {
            DataContract.TopTrackEntry.COLUMN_TRACK_NAME,
            DataContract.ArtistEntry.COLUMN_ARTIST_NAME,
            DataContract.TopTrackEntry.COLUMN_ALBUM_NAME,
            DataContract.TopTrackEntry.COLUMN_ALBUM_IMAGE_URL,
            DataContract.TopTrackEntry.COLUMN_TRACK_PREVIEW_URL,
            DataContract.TopTrackEntry.COLUMN_TRACK_SPOTIFY_ID,
            DataContract.TopTrackEntry.TABLE_NAME + "." + DataContract.TopTrackEntry._ID,
            DataContract.TopTrackEntry.COLUMN_ARTIST_KEY,
            DataContract.TopTrackEntry.COLUMN_COUNTRY_KEY,

            // This works because the DataProvider returns country data joined with
            // artist and track data, even though they're stored in three different tables.
            DataContract.CountryEntry.COLUMN_COUNTRY_SETTING
    };

    // These indices are tied to TRACK_COLUMNS.  If TRACK_COLUMNS changes, these
    // must change.
    public static final int COL_TRACK_NAME = 0;
    public static final int COL_ARTIST_NAME = 1;
    public static final int COL_ALBUM_NAME = 2;
    public static final int COL_ALBUM_IMAGE_URL = 3;
    public static final int COL_TRACK_PREVIEW_URL = 4;
    public static final int COL_TRACK_SPOTIFY_ID = 5;


    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * Callback for when an item has been selected.
         */
        void onItemSelected(ArrayList<String> trackInfo, Cursor cursor);
    }


    public static final String TAG = "TopTracksFragment";


    TracksAdapter mTracksAdapter;
    String mArtistSpotifyId;
    Uri mUri;
    ListView mListView;
    private int mPosition = ListView.INVALID_POSITION;


    public TopTracksFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(TopTracksFragment.TRACKS_URI);
            Log.i(TAG, "arguments are not null and uri is " + mUri);
            mArtistSpotifyId = arguments.getString(ArtistsFragment.EXTRA_ARTIST_SPOTIFY_ID);
        } else{
            Log.i(TAG, "onCreateView called and arguments are null!");
        }

        View rootView = inflater.inflate(R.layout.fragment_top_tracks, container, false);

        mTracksAdapter = new TracksAdapter(getActivity(), null, 0);

        mListView = (ListView) rootView.findViewById(R.id.listview_top_tracks);
        mListView.setAdapter(mTracksAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                Log.i(TAG, "onItemClick called. Position, ID, and nextFocusDn are  " + position + id + parent.getNextFocusDownId());
                if (cursor != null) {
                    ArrayList<String> trackInfo = new ArrayList<>();
                    trackInfo.add(COL_TRACK_NAME, cursor.getString(COL_TRACK_NAME));
                    trackInfo.add(COL_ARTIST_NAME, cursor.getString(COL_ARTIST_NAME));
                    trackInfo.add(COL_ALBUM_NAME, cursor.getString(COL_ALBUM_NAME));
                    trackInfo.add(COL_ALBUM_IMAGE_URL, cursor.getString(COL_ALBUM_IMAGE_URL));
                    trackInfo.add(COL_TRACK_PREVIEW_URL, cursor.getString(COL_TRACK_PREVIEW_URL));

                    ((Callback) getActivity()).onItemSelected(trackInfo, cursor);
                }
                mPosition = position;
            }
        });

        // If there's instance state, get the last position
        if (savedInstanceState != null && savedInstanceState.containsKey(KEY_SELECTED)) {
            // The listview probably hasn't been populated yet. Perform the
            // swapout in onLoadFinished.
            mPosition = savedInstanceState.getInt(KEY_SELECTED);
        }

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        getLoaderManager().initLoader(TRACKS_LOADER, null, this);

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        Log.i(TAG, "onCreateLoader called for TRACKS_LOADER and mUri is " + mUri);

        if (null != mUri){

            String sortOrder = DataContract.TopTrackEntry.TABLE_NAME + "." + DataContract.TopTrackEntry._ID + " ASC";

            return new CursorLoader(getActivity(),
                    mUri,
                    TRACK_COLUMNS,
                    null,
                    null,
                    sortOrder)
            ;
        }

        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if ((cursor.moveToFirst()) && (cursor.getCount() == 1)) {
            // Check to see if there is a no tracks flag in the cursor indicating that
            // there are no top tracks for the requested artist.
            String flag = cursor.getString(COL_TRACK_SPOTIFY_ID);
            if (flag.equals(DataContract.TopTrackEntry.FLAG_NO_TRACKS_FOUND)) {
                Toast toast = Toast.makeText(getActivity(), getString(R.string.toast_no_track_found), Toast.LENGTH_LONG);
                toast.show();
            } else {
                mTracksAdapter.swapCursor(cursor);
            }
        } else if (cursor.moveToFirst()) {
            Log.i(TAG, "onLoadFinished called and cursor.moveToFirst() is true. mTracksAdapter swaps with cursor");
            mTracksAdapter.swapCursor(cursor);
            if (mPosition != ListView.INVALID_POSITION) {
                mListView.smoothScrollToPosition(mPosition);
            }
        } else {    //if the cursor is null, this means that the data has not been added to the db.
            Log.i(TAG, "onLoadFinished called, and cursor is empty. getTracks() called to start new FetchTracksTask");
            getTracks();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mTracksAdapter.swapCursor(null);
    }

    private void getTracks() {

        String countrySetting = Utility.getPreferredCountry(getActivity());

        ConnectivityManager connMgr = (ConnectivityManager)
                getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            FetchTracksTask fetchTracksTask = new FetchTracksTask(getActivity());
            fetchTracksTask.execute(countrySetting, mArtistSpotifyId, Long.toString(DataContract.TopTrackEntry.getArtistIdFromUri(mUri)));
        } else {
            Toast toast = Toast.makeText(getActivity(), getString(R.string.toast_no_network_found), Toast.LENGTH_LONG);
            toast.show();
        }
    }

    public void onCountryChanged(String newCountry) {
        Log.i(TAG, "onCountryChanged() called and data is being reloaded");
                Uri uri = mUri;
        if (null != uri) {
            long dateartistId = DataContract.TopTrackEntry.getArtistIdFromUri(uri);
            Uri updatedUri = DataContract.TopTrackEntry.buildTrackWithCountryAndArtistId(newCountry, dateartistId);
            mUri = updatedUri;
            getLoaderManager().restartLoader(TRACKS_LOADER, null, this);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mPosition != ListView.INVALID_POSITION) {
            outState.putInt(KEY_SELECTED, mPosition);
        }
        super.onSaveInstanceState(outState);
    }
}

