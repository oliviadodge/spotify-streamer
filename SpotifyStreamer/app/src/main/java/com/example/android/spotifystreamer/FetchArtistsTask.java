package com.example.android.spotifystreamer;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.example.android.spotifystreamer.data.DataContract;

import java.util.List;
import java.util.Vector;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.RetrofitError;

/**
 * Created by oliviadodge on 8/4/2015.
 */

public class FetchArtistsTask extends AsyncTask<String, Void, Void> {

    private final String LOG_TAG = FetchArtistsTask.class.getSimpleName();

    private final Context mContext;

    public FetchArtistsTask(Context context) {
        mContext = context;
    }

    long addSearchTerm(String searchTerm) {
        long searchTermId;
            ContentValues values = new ContentValues();


            values.put(DataContract.SearchTermEntry.COLUMN_SEARCH_TERM, searchTerm);

            Uri insertedUri = mContext.getContentResolver().insert(DataContract.SearchTermEntry.CONTENT_URI,
                    values);

            searchTermId = ContentUris.parseId(insertedUri);

        return searchTermId;
    }

    private void addArtists(List<Artist> artists, String searchTerm) {

        long searchTermId = addSearchTerm(searchTerm);

        // Insert the new artists information into the database
        Vector<ContentValues> cVVector = new Vector<>(artists.size());

        for (int i = 0; i < artists.size(); i++) {
            // These are the values that will be collected.
            String artistSpotifyId;
            String artistName;
            String artistImageUrl;

            artistSpotifyId = artists.get(i).id;
            artistName = artists.get(i).name;

            int numOfImages = 0;
            numOfImages = artists.get(i).images.size();
            if (numOfImages > 0) {
                artistImageUrl = artists.get(i).images.get((numOfImages - 1) / 2).url;
            } else {
                artistImageUrl = "";
            }

            ContentValues artistValues = new ContentValues();

            artistValues.put(DataContract.ArtistEntry.COLUMN_SEARCH_KEY, searchTermId);
            artistValues.put(DataContract.ArtistEntry.COLUMN_ARTIST_SPOTIFY_ID, artistSpotifyId);
            artistValues.put(DataContract.ArtistEntry.COLUMN_ARTIST_NAME, artistName);
            artistValues.put(DataContract.ArtistEntry.COLUMN_ARTIST_IMAGE_URL, artistImageUrl);

            cVVector.add(artistValues);
        }

        int inserted = 0;
        // add to database
        if (cVVector.size() > 0) {
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);
            inserted = mContext.getContentResolver().bulkInsert(DataContract.ArtistEntry.CONTENT_URI, cvArray);
        } else {
            // cVVector's size is zero meaning there were no artists found for that search term
            //Insert a flag that will let the UI know this
            ContentValues noArtistsFound = new ContentValues();
            noArtistsFound.put(DataContract.ArtistEntry.COLUMN_SEARCH_KEY, searchTermId);
            noArtistsFound.put(DataContract.ArtistEntry.COLUMN_ARTIST_NAME, DataContract.ArtistEntry.FLAG_NO_ARTISTS_FOUND);
            noArtistsFound.put(DataContract.ArtistEntry.COLUMN_ARTIST_SPOTIFY_ID, DataContract.ArtistEntry.FLAG_NO_ARTISTS_FOUND);

            mContext.getContentResolver()
                    .insert(DataContract.ArtistEntry.CONTENT_URI, noArtistsFound);
        }

        Log.d(LOG_TAG, "FetchArtistsTask Complete. " + inserted + " Inserted");

    }


    @Override
    protected Void doInBackground(String... params) {

        String searchTerm = params[0];

        List<Artist> artists;

        SpotifyApi api = new SpotifyApi();
        SpotifyService spotify = api.getService();
        try {
            ArtistsPager searchResults = spotify.searchArtists(searchTerm);
            artists = searchResults.artists.items;
            addArtists(artists, searchTerm);
        } catch(RetrofitError ex){
            Toast.makeText(mContext, mContext.getResources().getString(R.string.toast_no_network_found), Toast.LENGTH_LONG).show();
        }

        return null;
    }
}