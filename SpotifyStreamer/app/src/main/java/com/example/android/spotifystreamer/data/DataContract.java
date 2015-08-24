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

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Defines table and column names for the spotify database.
 */
public class DataContract {

    // The "Content authority" is a name for the entire content provider, similar to the
    // relationship between a domain name and its website.  A convenient string to use for the
    // content authority is the package name for the app, which is guaranteed to be unique on the
    // device.
    public static final String CONTENT_AUTHORITY = "com.example.android.spotifystreamer";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Possible paths (appended to base content URI for possible URI's)
    // For instance, content://com.example.android.spotifystreamer/artist/ is a valid path for
    // looking at artist data. content://com.example.android.spotifystreamer/givemeroot/ will fail,
    // as the ContentProvider hasn't been given any information on what to do with "givemeroot".
    // At least, let's hope not.  Don't be that dev, reader.  Don't be that dev.
    public static final String PATH_ARTIST = "artist";
    public static final String PATH_COUNTRY = "country";
    public static final String PATH_SEARCH_TERM = "search_term";
    public static final String PATH_TRACK = "track";

    /* Inner class that defines the table contents of the country table */
    public static final class CountryEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_COUNTRY).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_COUNTRY;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_COUNTRY;

        // Table name
        public static final String TABLE_NAME = "country";

        // The country setting string is what will be sent to the spotify api
        // as the country query.
        public static final String COLUMN_COUNTRY_SETTING = "country_setting";

        // Human readable country string, provided by the API.  Because for styling,
        // "Austria" is more recognizable than AT.
        public static final String COLUMN_COUNTRY_NAME = "country_name";

        public static Uri buildCountryUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    /* Inner class that defines the table contents of the search term table */
    public static final class SearchTermEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_SEARCH_TERM).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SEARCH_TERM;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SEARCH_TERM;

        // Table name
        public static final String TABLE_NAME = "search_term";

        // The search term string is what will be sent to the spotify api
        // as the search query.
        public static final String COLUMN_SEARCH_TERM = "search_term_string";

        public static Uri buildSearchTermUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    /* Inner class that defines the table contents of the artist table */
    public static final class ArtistEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_ARTIST).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ARTIST;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ARTIST;

        public static final String TABLE_NAME = "artist";

        // Artist id as returned by API, to query the API for top tracks
        public static final String COLUMN_ARTIST_SPOTIFY_ID = "artist_spotify_id";

        // Artist name, as provided by API.
        public static final String COLUMN_ARTIST_NAME = "artist_name";

        // Search term as a foreign key.
        public static final String COLUMN_SEARCH_KEY = "search_term_id";

        // Url to artist image.
        public static final String COLUMN_ARTIST_IMAGE_URL = "artist_image_url";


        // Flag that will be entered into the database if no artists are found for a particular
        // search term. It will be inserted into the artist fields that require non null
        // vaues (ie the artist spotify id and the artist name).
        public static final String FLAG_NO_ARTISTS_FOUND = "no_artists_found";


        public static Uri buildArtistUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildArtistWithSearchTermUri(String searchTerm) {
            return CONTENT_URI.buildUpon().appendPath(searchTerm).build();
        }

        public static Uri buildArtistWithSearchTermAndArtistId(String searchTerm, long id) {
            return CONTENT_URI.buildUpon().appendPath(searchTerm)
                    .appendPath(Long.toString(id))
                    .build();
        }

        public static String getSearchTermFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static long getArtistIdFromUri(Uri uri) {
            return Long.parseLong(uri.getPathSegments().get(2));
        }

    }

    /* Inner class that defines the table contents of the top tracks table */
    public static final class TopTrackEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TRACK).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TRACK;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TRACK;

        public static final String TABLE_NAME = "top_tracks";

        // Column with the foreign key into the country table.
        public static final String COLUMN_COUNTRY_KEY = "country_id";

        // Column with the foreign key into the artist table.
        public static final String COLUMN_ARTIST_KEY = "artist_id";

        // Track id as returned by API
        public static final String COLUMN_TRACK_SPOTIFY_ID = "track_spotify_id";

        // Track name, as provided by API.
        public static final String COLUMN_TRACK_NAME = "track_name";

        // Track album name, as provided by API.
        public static final String COLUMN_ALBUM_NAME = "album_name";


        // Url to album image.
        public static final String COLUMN_ALBUM_IMAGE_URL = "album_image_url";


        // Url to track preview
        public static final String COLUMN_TRACK_PREVIEW_URL = "track_preview_url";

        // Flag that will be entered into the database if no tracks are found for a particular
        // artist and country. It will be inserted into the track fields that require non null
        // vaues (ie the track spotify id and the track name).
        public static final String FLAG_NO_TRACKS_FOUND = "no_tracks_found";


        public static Uri buildTrackUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildTrackWithCountryAndArtistId(String countrySetting, long artistId) {
            return CONTENT_URI.buildUpon().appendPath(countrySetting)
                    .appendPath(Long.toString(artistId)).build();
        }

        public static String getCountrySettingFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }


        public static long getArtistIdFromUri(Uri uri) {
            return Long.parseLong(uri.getPathSegments().get(2));
        }

        public static long getTrackIdFromUri(Uri uri) {
            return Long.parseLong(uri.getPathSegments().get(3));
        }
    }
}
