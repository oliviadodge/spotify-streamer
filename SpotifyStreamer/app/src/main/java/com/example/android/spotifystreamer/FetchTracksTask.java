package com.example.android.spotifystreamer;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.example.android.spotifystreamer.data.DataContract;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;

/**
 * Created by oliviadodge on 8/4/2015.
 */

public class FetchTracksTask extends AsyncTask<String, Void, Void> {

    private final String LOG_TAG = FetchTracksTask.class.getSimpleName();

    private final Context mContext;

    public FetchTracksTask(Context context) {
        mContext = context;
    }

    long addCountry(String countrySetting) {
        long countryId;

        Cursor countryCursor = mContext.getContentResolver().query(
                DataContract.CountryEntry.CONTENT_URI,
                new String[]{DataContract.CountryEntry._ID},
                DataContract.CountryEntry.COLUMN_COUNTRY_SETTING + " = ?",
                new String[]{countrySetting}, null);

        if (countryCursor.moveToFirst()) {
            int countryIdIndex = countryCursor.getColumnIndex(DataContract.SearchTermEntry._ID);
            countryId = countryCursor.getLong(countryIdIndex);
        } else {
            ContentValues values = new ContentValues();

            values.put(DataContract.CountryEntry.COLUMN_COUNTRY_SETTING, countrySetting);

            Uri insertedUri = mContext.getContentResolver().insert(DataContract.CountryEntry.CONTENT_URI,
                    values);

            countryId = ContentUris.parseId(insertedUri);
        }
        return countryId;
    }


    private void addTracks(List<Track> tracks, String countrySetting, String artistId) {

        long countryId = addCountry(countrySetting);

        // Insert the new tracks information into the database
        Vector<ContentValues> cVVector = new Vector<>(tracks.size());

        for (int i = 0; i < tracks.size(); i++) {
            // These are the values that will be collected.
            String trackSpotifyId;
            String trackName;
            String albumName;
            String albumImageUrl;
            String trackPreviewUrl;

            trackSpotifyId = tracks.get(i).id;
            trackName = tracks.get(i).name;
            albumName = tracks.get(i).album.name;

            int numOfImages;
            numOfImages = tracks.get(i).album.images.size();
            if (numOfImages > 0) {
                albumImageUrl = tracks.get(i).album.images.get((numOfImages - 1) / 2).url;
            } else {
                albumImageUrl = "";
            }

            trackPreviewUrl = tracks.get(i).preview_url;

            ContentValues trackValues = new ContentValues();

            trackValues.put(DataContract.TopTrackEntry.COLUMN_COUNTRY_KEY, countryId);
            trackValues.put(DataContract.TopTrackEntry.COLUMN_ARTIST_KEY, Long.parseLong(artistId));
            trackValues.put(DataContract.TopTrackEntry.COLUMN_TRACK_SPOTIFY_ID, trackSpotifyId);
            trackValues.put(DataContract.TopTrackEntry.COLUMN_TRACK_NAME, trackName);
            trackValues.put(DataContract.TopTrackEntry.COLUMN_ALBUM_NAME, albumName);
            trackValues.put(DataContract.TopTrackEntry.COLUMN_ALBUM_IMAGE_URL, albumImageUrl);
            trackValues.put(DataContract.TopTrackEntry.COLUMN_TRACK_PREVIEW_URL, trackPreviewUrl);

            cVVector.add(trackValues);
        }

        int inserted = 0;
        // add to database
        if (cVVector.size() > 0) {
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);
            inserted = mContext.getContentResolver().bulkInsert(DataContract.TopTrackEntry.CONTENT_URI, cvArray);
        }

        Log.d(LOG_TAG, "FetchTracksTask Complete. " + inserted + " Inserted");

    }

    @Override
    protected Void doInBackground(String... params) {

        String countrySetting = params[0];
        String artistSpotifyId = params[1];
        String artistId = params[2];


        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("country", countrySetting);

        List<Track> tracks;

        SpotifyApi api = new SpotifyApi();
        SpotifyService spotify = api.getService();

        Tracks results = spotify.getArtistTopTrack(artistSpotifyId, queryParams);

        tracks = results.tracks;

        addTracks(tracks, countrySetting, artistId);

        return null;
    }
}
