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

import java.util.List;

import kaaes.spotify.webapi.android.models.Track;


/**
 * A placeholder fragment containing a simple view.
 */
public class TopTracksFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final int TRACKS_LOADER = 0;

    private static final String[] TRACK_COLUMNS = {
            DataContract.TopTrackEntry.TABLE_NAME + "." + DataContract.TopTrackEntry._ID,
            DataContract.TopTrackEntry.COLUMN_TRACK_SPOTIFY_ID,
            DataContract.TopTrackEntry.COLUMN_TRACK_NAME,
            DataContract.TopTrackEntry.COLUMN_ARTIST_KEY,
            DataContract.TopTrackEntry.COLUMN_COUNTRY_KEY,
            DataContract.TopTrackEntry.COLUMN_ALBUM_NAME,
            DataContract.TopTrackEntry.COLUMN_ALBUM_IMAGE_URL,
            // This works because the DataProvider returns country data joined with
            // artist and track data, even though they're stored in three different tables.
            DataContract.CountryEntry.COLUMN_COUNTRY_SETTING
    };

    // These indices are tied to TRACK_COLUMNS.  If TRACK_COLUMNS changes, these
    // must change.
    public static final int COL_TRACK_ID = 0;
    public static final int COL_TRACK_SPOTIFY_ID = 1;
    public static final int COL_TRACK_NAME = 2;
    public static final int COL_ARTIST_ID = 3;
    public static final int COL_COUNTRY_ID = 4;
    public static final int COL_ALBUM_NAME = 5;
    public static final int COL_ALBUM_IMAGE_URL = 6;
    public static final int COL_COUNTRY_SETTING = 7;



    public static final String TRACK_ID_EXTRA = "track_id";
    public static final String TAG = "TopTracksFragment";


    TracksAdapter mTracksAdapter;
    long mArtistId;
    String mArtistSpotifyId;
    List<Track> mTopTracks;
    ListView mListView;


    public TopTracksFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mArtistId = getActivity().getIntent().getLongExtra(MainActivityFragment.EXTRA_ARTIST_ID, -1);
        mArtistSpotifyId = getActivity().getIntent().getStringExtra(MainActivityFragment.EXTRA_ARTIST_SPOTIFY_ID);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_top_tracks, container, false);

        mTracksAdapter = new TracksAdapter(getActivity(), null, 0);

        mListView = (ListView) rootView.findViewById(R.id.listview_top_tracks);
        mListView.setAdapter(mTracksAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Track track = mTracksAdapter.getItem(position);
//                Log.i(TAG, "got a Track: " + track.name + " " + track.id);
//                if (track.preview_url != null) {
//                    Intent i = new Intent(getActivity(), TrackPlayerActivity.class);
//                    i.putExtra(TRACK_ID_EXTRA, track.id);
//                    startActivity(i);
//                } else {
//                    Toast toast = Toast.makeText(getActivity(), getString(R.string.toast_track_not_playable), Toast.LENGTH_LONG);
//                    toast.show();
//                }
            }
        });

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        getLoaderManager().initLoader(TRACKS_LOADER, null, this);

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        Log.i(TAG, "onCreateLoader called for TACKS_LOADER");

        String countrySetting = Utility.getPreferredCountry(getActivity());

        String sortOrder = DataContract.TopTrackEntry.TABLE_NAME + "." + DataContract.TopTrackEntry._ID + " ASC";
        Uri tracksForArtistIdAndCountry = DataContract.TopTrackEntry
                .buildTrackWithCountryAndArtistId(countrySetting, mArtistId);

        return new CursorLoader(getActivity(),
                tracksForArtistIdAndCountry,
                TRACK_COLUMNS,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor.moveToFirst()) {
            Log.i(TAG, "onLoadFinished called and cursor.moveToFirst() is true. mTracksAdapter swaps with cursor");
            mTracksAdapter.swapCursor(cursor);
        } else {    //if the cursor is null, this means that the artists for mSearchTerm have not been added to the db.
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
            fetchTracksTask.execute(countrySetting, mArtistSpotifyId, Long.toString(mArtistId));
        } else {
            Toast toast = Toast.makeText(getActivity(), getString(R.string.toast_no_network_found), Toast.LENGTH_LONG);
            toast.show();
        }
    }

}

