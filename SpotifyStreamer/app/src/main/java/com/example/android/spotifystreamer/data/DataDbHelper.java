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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


/**
 * Manages a local database for music data.
 */
public class DataDbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;

    public static final String DATABASE_NAME = "data.db"; //made public for testing purposes only. // TODO delete 'public' qualifier

    public DataDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // Create a table to hold countries.  A country consists of the string supplied in the
        // country setting and the country name
        final String SQL_CREATE_COUNTRY_TABLE = "CREATE TABLE " + DataContract.CountryEntry.TABLE_NAME + " (" +
                DataContract.CountryEntry._ID + " INTEGER PRIMARY KEY," +
                DataContract.CountryEntry.COLUMN_COUNTRY_SETTING + " TEXT UNIQUE NOT NULL, " +
                DataContract.CountryEntry.COLUMN_COUNTRY_NAME + " TEXT " +
                " );";


        // Create a table to hold search terms.
        final String SQL_CREATE_SEARCH_TERM_TABLE = "CREATE TABLE " + DataContract.SearchTermEntry.TABLE_NAME + " (" +
                DataContract.SearchTermEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DataContract.SearchTermEntry.COLUMN_SEARCH_TERM + " TEXT UNIQUE NOT NULL" +
                " );";

        final String SQL_CREATE_ARTIST_TABLE = "CREATE TABLE " + DataContract.ArtistEntry.TABLE_NAME + " (" +
                // autoincrement so that the order of the artists returned from the API can be incapsulated by the artist
                // _id in the db. This is important because users will likely want to see the more relevant artists first
                DataContract.ArtistEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                DataContract.ArtistEntry.COLUMN_ARTIST_SPOTIFY_ID + " TEXT UNIQUE NOT NULL, " +
                DataContract.ArtistEntry.COLUMN_ARTIST_IMAGE_URL + " TEXT, " +
                DataContract.ArtistEntry.COLUMN_SEARCH_KEY + " INTEGER NOT NULL, " +
                DataContract.ArtistEntry.COLUMN_ARTIST_NAME + " TEXT NOT NULL," +

                // Set up the search term column as a foreign key to artist table.
                " FOREIGN KEY (" + DataContract.ArtistEntry.COLUMN_SEARCH_KEY + ") REFERENCES " +
                DataContract.SearchTermEntry.TABLE_NAME + " (" + DataContract.SearchTermEntry._ID + ") " +
                " );";

        final String SQL_CREATE_TRACK_TABLE = "CREATE TABLE " + DataContract.TopTrackEntry.TABLE_NAME + " (" +
                    // Why AutoIncrement here, and not above?
                    // Unique keys will be auto-generated in either case.  But for weather
                    // forecasting, it's reasonable to assume the user will want information
                    // for a certain date and all dates *following*, so the forecast data
                    // should be sorted accordingly.
                    DataContract.ArtistEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                    DataContract.TopTrackEntry.COLUMN_COUNTRY_KEY + " INTEGER NOT NULL, " +
                    DataContract.TopTrackEntry.COLUMN_ARTIST_KEY + " INTEGER NOT NULL, " +
                    DataContract.TopTrackEntry.COLUMN_TRACK_SPOTIFY_ID + " TEXT UNIQUE NOT NULL, " +
                    DataContract.TopTrackEntry.COLUMN_TRACK_NAME + " TEXT NOT NULL, " +
                    DataContract.TopTrackEntry.COLUMN_ALBUM_NAME + " TEXT, " +
                    DataContract.TopTrackEntry.COLUMN_ALBUM_IMAGE_URL + " TEXT," +


                // Set up the artist column as a foreign key to artist table.
                    " FOREIGN KEY (" + DataContract.TopTrackEntry.COLUMN_ARTIST_KEY + ") REFERENCES " +
                    DataContract.ArtistEntry.TABLE_NAME + " (" + DataContract.ArtistEntry._ID + "), " +

                    // Set up the country column as a foreign key to country table.
                    " FOREIGN KEY (" + DataContract.TopTrackEntry.COLUMN_COUNTRY_KEY + ") REFERENCES " +
                    DataContract.CountryEntry.TABLE_NAME + " (" + DataContract.CountryEntry._ID + ")" +
                " );";

            sqLiteDatabase.execSQL(SQL_CREATE_COUNTRY_TABLE);
            sqLiteDatabase.execSQL(SQL_CREATE_SEARCH_TERM_TABLE);
            sqLiteDatabase.execSQL(SQL_CREATE_ARTIST_TABLE);
            sqLiteDatabase.execSQL(SQL_CREATE_TRACK_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        // If you want to update the schema without wiping data, commenting out the next 2 lines
        // should be your top priority before modifying this method.
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DataContract.CountryEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DataContract.SearchTermEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DataContract.ArtistEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DataContract.TopTrackEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
