/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.spotifystreamer.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

public class DataProvider extends ContentProvider {


    private static final String TAG = DataProvider.class.getSimpleName();
    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private DataDbHelper mOpenHelper;

    static final int ARTISTS = 100;
    static final int ARTIST_WITH_SEARCH_TERM = 101;
    static final int ARTIST_WITH_SEARCH_TERM_AND_ARTIST_ID = 102;
    static final int TOP_TRACKS = 200;
    static final int TOP_TRACKS_WITH_COUNTRY_AND_ARTIST_ID = 201;
    static final int TOP_TRACK_WITH_COUNTRY_ARTIST_AND_TRACK_ID = 202;
    static final int COUNTRY = 300;
    static final int SEARCH_TERM = 400;

    private static final SQLiteQueryBuilder sArtistsBySearchTermQueryBuilder;
    static{
        sArtistsBySearchTermQueryBuilder = new SQLiteQueryBuilder();
        
        //This is an inner join to join the artist and search term table
        sArtistsBySearchTermQueryBuilder.setTables(
                DataContract.ArtistEntry.TABLE_NAME + " INNER JOIN " +
                        DataContract.SearchTermEntry.TABLE_NAME +
                        " ON " + DataContract.ArtistEntry.TABLE_NAME +
                        "." + DataContract.ArtistEntry.COLUMN_SEARCH_KEY +
                        " = " + DataContract.SearchTermEntry.TABLE_NAME +
                        "." + DataContract.SearchTermEntry._ID)
        ;
    }

    private static final SQLiteQueryBuilder sTracksByCountrySettingAndArtistIdQueryBuilder;
    static{
        sTracksByCountrySettingAndArtistIdQueryBuilder = new SQLiteQueryBuilder();

        //This is an inner join to join the track, country, and artist tables so we can query for track by country and artist
        sTracksByCountrySettingAndArtistIdQueryBuilder.setTables(
                DataContract.TopTrackEntry.TABLE_NAME + " INNER JOIN " +
                        DataContract.CountryEntry.TABLE_NAME +
                        " ON " + DataContract.TopTrackEntry.TABLE_NAME +
                        "." + DataContract.TopTrackEntry.COLUMN_COUNTRY_KEY +
                        " = " + DataContract.CountryEntry.TABLE_NAME +
                        "." + DataContract.CountryEntry._ID + " INNER JOIN " +
                        DataContract.ArtistEntry.TABLE_NAME +
                        " ON " + DataContract.TopTrackEntry.TABLE_NAME +
                        "." + DataContract.TopTrackEntry.COLUMN_ARTIST_KEY +
                        " = " + DataContract.ArtistEntry.TABLE_NAME +
                        "." + DataContract.ArtistEntry._ID);
    }

    //search_term.search_term_string = ?
    private static final String sSearchTermSelection =
            DataContract.SearchTermEntry.TABLE_NAME +
                    "." + DataContract.SearchTermEntry.COLUMN_SEARCH_TERM + " = ? ";

    //search_term.search_term_string = ? AND artist_id = ?
    private static final String sSearchTermWithArtistIdSelection =
            DataContract.SearchTermEntry.TABLE_NAME +
                    "." + DataContract.SearchTermEntry.COLUMN_SEARCH_TERM + " = ? AND " +
                    DataContract.ArtistEntry.COLUMN_ARTIST_SPOTIFY_ID + " = ? ";

    //country.country_setting = ?
    private static final String sCountrySettingSelection =
            DataContract.CountryEntry.TABLE_NAME+
                    "." + DataContract.CountryEntry.COLUMN_COUNTRY_SETTING + " = ? ";

    //country.country_setting = ? AND artist_id = ?
    private static final String sCountrySettingWithArtistIdSelection =
            DataContract.CountryEntry.TABLE_NAME +
                    "." + DataContract.CountryEntry.COLUMN_COUNTRY_SETTING + " = ? AND " +
                    DataContract.TopTrackEntry.COLUMN_ARTIST_KEY + " = ? ";

    //country.country_setting = ? AND artist_id = ? AND track_id = ?
    private static final String sCountrySettingWithArtistIdAndTrackIdSelection =
            DataContract.CountryEntry.TABLE_NAME +
                    "." + DataContract.CountryEntry.COLUMN_COUNTRY_SETTING + " = ? AND " +
                    DataContract.TopTrackEntry.COLUMN_ARTIST_KEY + " = ? " + " = ? AND " +
                    DataContract.TopTrackEntry.COLUMN_TRACK_SPOTIFY_ID + " = ? ";


    private Cursor getArtistsBySearchTerm(Uri uri, String[] projection, String sortOrder) {
        String searchTerm = DataContract.ArtistEntry.getSearchTermFromUri(uri);

        return sArtistsBySearchTermQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                sSearchTermSelection,
                new String[]{searchTerm},
                null,
                null,
                sortOrder
        );
    }

    private Cursor getArtistBySearchTermAndArtistId(Uri uri, String[] projection, String sortOrder) {
        String searchTerm = DataContract.ArtistEntry.getSearchTermFromUri(uri);
        long artistId = DataContract.ArtistEntry.getArtistIdFromUri(uri);

        return sArtistsBySearchTermQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                sSearchTermWithArtistIdSelection,
                new String[]{searchTerm, Long.toString(artistId)},
                null,
                null,
                sortOrder
        );
    }

    private Cursor getTracksByCountryAndArtist(Uri uri, String[] projection, String sortOrder) {
        String countrySetting = DataContract.TopTrackEntry.getCountrySettingFromUri(uri);
        long artistId = DataContract.TopTrackEntry.getArtistIdFromUri(uri);

        String[] selectionArgs;
        String selection;

        selectionArgs = new String[]{countrySetting, Long.toString(artistId)};
        selection = sCountrySettingWithArtistIdSelection;


        return sTracksByCountrySettingAndArtistIdQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getTrackByCountryArtistAndTrackId(Uri uri, String[] projection, String sortOrder) {
        String countrySetting = DataContract.TopTrackEntry.getCountrySettingFromUri(uri);
        long artistId = DataContract.TopTrackEntry.getArtistIdFromUri(uri);
        long trackId = DataContract.TopTrackEntry.getTrackIdFromUri(uri);

        return sTracksByCountrySettingAndArtistIdQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                sCountrySettingWithArtistIdAndTrackIdSelection,
                new String[]{countrySetting, Long.toString(artistId), Long.toString(trackId)},
                null,
                null,
                sortOrder
        );
    }

    /*
        Here is where you need to create the UriMatcher. This UriMatcher will
        match each URI to the ARTIST_WITH_ARTIST_ID, TOP_TRACKS_WITH_COUNTRY_AND_ARTIST_ID, TOP_TRACK_WITH_COUNTRY_ARTIST_AND_TRACK_ID,
        and COUNTRY integer constants defined above.  You can test this by uncommenting the
        testUriMatcher test within TestUriMatcher.
     */
    static UriMatcher buildUriMatcher() {

        // All paths added to the UriMatcher have a corresponding code to return when a match is
        // found.  The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = DataContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, DataContract.PATH_ARTIST, ARTISTS);
        matcher.addURI(authority, DataContract.PATH_ARTIST + "/*", ARTIST_WITH_SEARCH_TERM);
        matcher.addURI(authority, DataContract.PATH_ARTIST + "/*/#", ARTIST_WITH_SEARCH_TERM_AND_ARTIST_ID);
        matcher.addURI(authority, DataContract.PATH_TRACK, TOP_TRACKS);
        matcher.addURI(authority, DataContract.PATH_TRACK + "/*/#", TOP_TRACKS_WITH_COUNTRY_AND_ARTIST_ID);
        matcher.addURI(authority, DataContract.PATH_TRACK + "/*/#/#", TOP_TRACK_WITH_COUNTRY_ARTIST_AND_TRACK_ID);

        matcher.addURI(authority, DataContract.PATH_COUNTRY, COUNTRY);
        matcher.addURI(authority, DataContract.PATH_SEARCH_TERM, SEARCH_TERM);
        return matcher;
    }

    /*
        We've coded this for you.  We just create a new DataDbHelper for later use
        here.
     */
    @Override
    public boolean onCreate() {
        mOpenHelper = new DataDbHelper(getContext());
        return true;
    }

    /*
        Here's where you'll code the getType function that uses the UriMatcher.  You can
        test this by uncommenting testGetType in TestProvider.

     */
    @Override
    public String getType(Uri uri) {

        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case TOP_TRACK_WITH_COUNTRY_ARTIST_AND_TRACK_ID:
                return DataContract.TopTrackEntry.CONTENT_ITEM_TYPE;
            case TOP_TRACKS_WITH_COUNTRY_AND_ARTIST_ID:
                return DataContract.TopTrackEntry.CONTENT_TYPE;
            case TOP_TRACKS:
                return DataContract.TopTrackEntry.CONTENT_TYPE;
            case ARTIST_WITH_SEARCH_TERM_AND_ARTIST_ID:
                return DataContract.ArtistEntry.CONTENT_ITEM_TYPE;
            case ARTIST_WITH_SEARCH_TERM:
                return DataContract.ArtistEntry.CONTENT_TYPE;
            case ARTISTS:
                return DataContract.ArtistEntry.CONTENT_TYPE;
            case COUNTRY:
                return DataContract.CountryEntry.CONTENT_TYPE;
            case SEARCH_TERM:
                return DataContract.SearchTermEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.

        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "track/*/*"
            case TOP_TRACK_WITH_COUNTRY_ARTIST_AND_TRACK_ID:
            {
                retCursor = getTrackByCountryArtistAndTrackId(uri, projection, sortOrder);
                break;
            }
            // "track/*/#"
            case TOP_TRACKS_WITH_COUNTRY_AND_ARTIST_ID: {
                retCursor = getTracksByCountryAndArtist(uri, projection, sortOrder);
                break;
            }
            // "track"
            case TOP_TRACKS: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        DataContract.TopTrackEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "artist/*/*"
            case ARTIST_WITH_SEARCH_TERM_AND_ARTIST_ID: {
                retCursor = getArtistBySearchTermAndArtistId(uri, projection, sortOrder);
                break;
            }
            // "artist/*"
            case ARTIST_WITH_SEARCH_TERM: {
                retCursor = getArtistsBySearchTerm(uri, projection, sortOrder);
                break;
            }
            // "artist"
            case ARTISTS: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        DataContract.ArtistEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "country"
            case COUNTRY: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        DataContract.CountryEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "search_term"
            case SEARCH_TERM: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        DataContract.SearchTermEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

         
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case ARTISTS: {
                long _id = db.insert(DataContract.ArtistEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = DataContract.ArtistEntry.buildArtistUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case TOP_TRACKS: {
                long _id = db.insert(DataContract.TopTrackEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = DataContract.TopTrackEntry.buildTrackUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case SEARCH_TERM: {
                long _id = db.insert(DataContract.SearchTermEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = DataContract.SearchTermEntry.buildSearchTermUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case COUNTRY: {
                long _id = db.insert(DataContract.CountryEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = DataContract.CountryEntry.buildCountryUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if ( null == selection ) selection = "1";
        switch (match) {
            case ARTISTS:
                rowsDeleted = db.delete(
                        DataContract.ArtistEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case TOP_TRACKS:
                rowsDeleted = db.delete(
                        DataContract.TopTrackEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case SEARCH_TERM:
                rowsDeleted = db.delete(
                        DataContract.SearchTermEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case COUNTRY:
                rowsDeleted = db.delete(
                        DataContract.CountryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(
            Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case ARTISTS:
                rowsUpdated = db.update(DataContract.ArtistEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case TOP_TRACKS:
                rowsUpdated = db.update(DataContract.TopTrackEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case SEARCH_TERM:
                rowsUpdated = db.update(DataContract.SearchTermEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case COUNTRY:
                rowsUpdated = db.update(DataContract.CountryEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int returnCount = 0;
        switch (match) {
            case ARTISTS: {
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(DataContract.ArtistEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            }
            case TOP_TRACKS: {
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(DataContract.TopTrackEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            }
            default:
                Log.i(TAG, "Default bulkInsert(uri, values) method called for some reason");
                return super.bulkInsert(uri, values);
        }
    }
}

    // You do not need to call this method. This is a method specifically to assist the testing
    // framework in running smoothly. You can read more at:
    // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()

