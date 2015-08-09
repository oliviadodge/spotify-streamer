package com.example.android.spotifystreamer;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.spotifystreamer.data.DataContract;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private  static final int ARTISTS_LOADER = 0;

    private static final String[] ARTIST_COLUMNS = {

            DataContract.ArtistEntry.TABLE_NAME + "." + DataContract.ArtistEntry._ID,
            DataContract.ArtistEntry.COLUMN_ARTIST_SPOTIFY_ID,
            DataContract.ArtistEntry.COLUMN_SEARCH_KEY,
            DataContract.SearchTermEntry.COLUMN_SEARCH_TERM,
            DataContract.ArtistEntry.COLUMN_ARTIST_NAME,
            DataContract.ArtistEntry.COLUMN_ARTIST_IMAGE_URL,
    };

    // These indices are tied to ARTIST_COLUMNS.  If ARTIST_COLUMNS changes, these
    // must change.
    static final int COL_ARTIST_ID = 0;
    static final int COL_ARTIST_SPOTIFY_ID = 1;
    static final int COL_SEARCH_ID = 2;
    static final int COL_SEARCH_TERM = 3;
    static final int COL_ARTIST_NAME = 4;
    static final int COL_ARTIST_IMAGE_URL = 5;


    ArtistsAdapter mArtistsAdapter;
    String mSearchTerm;
    ListView mListView;
    Context mContext;
    LoaderManager.LoaderCallbacks<Cursor> mLoaderCallbacks;

    public static final String EXTRA_ARTIST_ID = "artist_id";
    public static final String EXTRA_ARTIST_SPOTIFY_ID = "artist_spotify_id";

    private static final String TAG = MainActivityFragment.class.getSimpleName();

    // TODO add projection to get integers for database table columns to use in cursor adapter

    public MainActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mArtistsAdapter = new ArtistsAdapter(getActivity(), null, 0);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mListView = (ListView) rootView.findViewById(R.id.listview_artists);
        mListView.setAdapter(mArtistsAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                if (cursor != null) {
                    String countrySetting = Utility.getPreferredCountry(mContext);
                    long artistid = cursor.getLong(COL_ARTIST_ID);
                    String artistSpotifyId = cursor.getString(COL_ARTIST_SPOTIFY_ID);
                    Intent intent = new Intent(getActivity(), TopTracksActivity.class)
                            .setData(DataContract.TopTrackEntry
                                    .buildTrackWithCountryAndArtistId(countrySetting, cursor.getLong(COL_ARTIST_ID)));
                    intent.putExtra(EXTRA_ARTIST_ID, artistid);
                    intent.putExtra(EXTRA_ARTIST_SPOTIFY_ID, artistSpotifyId);
                    startActivity(intent);
                }
            }
        });

        final EditText editTextSearch = (EditText) rootView.findViewById(R.id.edittext_search_artist);
        editTextSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_SEARCH) {
                    mSearchTerm = textView.getText().toString();
                    SearchTermLab.get(mContext).setSearchTerm(mSearchTerm);

                    restartLoad();

                    InputMethodManager imm = (InputMethodManager) mContext.getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(editTextSearch.getWindowToken(), 0);
                }

                return true;
            }

        });
        return rootView;
    }

    public void restartLoad() {
        getLoaderManager().destroyLoader(ARTISTS_LOADER);
        getLoaderManager().initLoader(ARTISTS_LOADER, null, this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        mLoaderCallbacks = this;
        mSearchTerm = SearchTermLab.get(getActivity()).getSearchTerm();

        LoaderManager.enableDebugLogging(true); //for testing only TODO delete this line
        getLoaderManager().initLoader(ARTISTS_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        Log.i(TAG, "onCreateLoader called and mSearchTerm is " + mSearchTerm);

        String sortOrder = DataContract.ArtistEntry.TABLE_NAME + "." + DataContract.ArtistEntry._ID + " ASC";
        Uri artistsForSearchTermUri = DataContract.ArtistEntry.buildArtistWithSearchTermUri(mSearchTerm);

        return new CursorLoader(getActivity(),
                artistsForSearchTermUri,
                ARTIST_COLUMNS,
                null,
                null,
                sortOrder);

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        if (cursor.moveToFirst()) {
            Log.i(TAG, "onLoadFinished called and cursor.moveToFirst() is true. mArtistAdapter swaps with cursor");
            mArtistsAdapter.swapCursor(cursor);
        } else if (null != mSearchTerm) {    //if the cursor is null, this means that the artists for mSearchTerm have not been added to the db.
            Log.i(TAG, "onLoadFinished called, cursor is empty, and mSearchTerm is " + mSearchTerm + ". getArtists() called to start new FetchArtistsTask");
            getArtists();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.i(TAG, "onLoadReset called mArtistsAdapter is swaps with null");
        mArtistsAdapter.swapCursor(null);
    }

    private void getArtists() {

        ConnectivityManager connMgr = (ConnectivityManager)
                getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            //start a FetchArtistsTask and override onPostExecute to start a loader to get the Artist data so we can update the CursorAdapter.
            FetchArtistsTask fetchArtistsTask = new FetchArtistsTask(mContext);
            fetchArtistsTask.execute(mSearchTerm);
        } else {
            Toast toast = Toast.makeText(getActivity(), getString(R.string.toast_no_network_found), Toast.LENGTH_LONG);
            toast.show();
        }
    }
}

