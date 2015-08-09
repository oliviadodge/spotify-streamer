package com.example.android.spotifystreamer.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.test.AndroidTestCase;

import com.example.android.spotifystreamer.utils.PollingCheck;

import java.util.Map;
import java.util.Set;

/*
    These are functions and some test data to make it easier to test your database and
    Content Provider.  Note that you'll want your DataContract class to exactly match the one
    in our solution to use these as-given.
 */
public class TestUtilities extends AndroidTestCase {
    static final String TEST_SEARCH_TERM = "Band of Horses";

    static final String TEST_COUNTRY = "AT";
    static final String TEST_COUNTRY_NAME = "Austria";

    static final String TEST_ARTIST_ID = "0OdUWJ0sBjDrqHygGUXeCF";
    static final long TEST_ARTIST_ROW_ID = 10L;
    static final String TEST_ARTIST_NAME = "Band of Horses";

    static final String TEST_TRACK_ID = "4o0NjemqhmsYLIMwlcosvW";
    static final String TEST_TRACK_NAME = "The Funeral";
    static final String TEST_ALBUM_NAME = "Everything All The Time";

    static void validateCursor(String error, Cursor valueCursor, ContentValues expectedValues) {
        assertTrue("Empty cursor returned. " + error, valueCursor.moveToFirst());
        validateCurrentRecord(error, valueCursor, expectedValues);
        valueCursor.close();
    }

    static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found. " + error, idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals("Value '" + entry.getValue().toString() +
                    "' did not match the expected value '" +
                    expectedValue + "'. " + error, expectedValue, valueCursor.getString(idx));
        }
    }


    static ContentValues createArtistValues(long searchTermId) {
        ContentValues artistValues  = new ContentValues();
        artistValues.put(DataContract.ArtistEntry.COLUMN_SEARCH_KEY, searchTermId);
        artistValues.put(DataContract.ArtistEntry.COLUMN_ARTIST_SPOTIFY_ID, TEST_ARTIST_ID);
        artistValues.put(DataContract.ArtistEntry.COLUMN_ARTIST_NAME, TEST_ARTIST_NAME);

        return artistValues;
    }

    static ContentValues createTrackValues(long countryRowId, long artistRowId) {
        ContentValues trackValues  = new ContentValues();
        trackValues.put(DataContract.TopTrackEntry.COLUMN_COUNTRY_KEY, countryRowId);
        trackValues.put(DataContract.TopTrackEntry.COLUMN_ARTIST_KEY, artistRowId);
        trackValues.put(DataContract.TopTrackEntry.COLUMN_TRACK_SPOTIFY_ID, TEST_TRACK_ID);
        trackValues.put(DataContract.TopTrackEntry.COLUMN_TRACK_NAME, TEST_TRACK_NAME);
        trackValues.put(DataContract.TopTrackEntry.COLUMN_ALBUM_NAME, TEST_ALBUM_NAME);

        return trackValues;
    }


    static ContentValues createTestCountryValues() {
        // Create a new map of values, where column names are the keys
        ContentValues testValues = new ContentValues();
        testValues.put(DataContract.CountryEntry.COLUMN_COUNTRY_SETTING, TEST_COUNTRY);
        testValues.put(DataContract.CountryEntry.COLUMN_COUNTRY_NAME, TEST_COUNTRY_NAME);

        return testValues;
    }


    static long insertTestCountryValues(Context context) {
        // insert our test records into the database
        DataDbHelper dbHelper = new DataDbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues testValues = TestUtilities.createTestCountryValues();

        long locationRowId;
        locationRowId = db.insert(DataContract.CountryEntry.TABLE_NAME, null, testValues);

        // Verify we got a row back.
        assertTrue("Error: Failure to insert Test Country Values", locationRowId != -1);

        return locationRowId;
    }


    static ContentValues createTestSearchTermValues() {
        // Create a new map of values, where column names are the keys
        ContentValues testValues = new ContentValues();
        testValues.put(DataContract.SearchTermEntry.COLUMN_SEARCH_TERM, TEST_SEARCH_TERM);

        return testValues;
    }

    /*
        You can uncomment this function once you have finished creating the
        CountryEntry part of the DataContract as well as the DataDbHelper.
     */
    static long insertTestSearchTermValues(Context context) {
        // insert our test records into the database
        DataDbHelper dbHelper = new DataDbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues testValues = TestUtilities.createTestSearchTermValues();

        long searchTermRowId;
        searchTermRowId = db.insert(DataContract.SearchTermEntry.TABLE_NAME, null, testValues);

        // Verify we got a row back.
        assertTrue("Error: Failure to insert Test Search Term Values", searchTermRowId != -1);

        return searchTermRowId;
    }

    /*
        The functions we provide inside of TestProvider use this utility class to test
        the ContentObserver callbacks using the PollingCheck class that we grabbed from the Android
        CTS tests.

        Note that this only tests that the onChange function is called; it does not test that the
        correct Uri is returned.
     */
    static class TestContentObserver extends ContentObserver {
        final HandlerThread mHT;
        boolean mContentChanged;

        static TestContentObserver getTestContentObserver() {
            HandlerThread ht = new HandlerThread("ContentObserverThread");
            ht.start();
            return new TestContentObserver(ht);
        }

        private TestContentObserver(HandlerThread ht) {
            super(new Handler(ht.getLooper()));
            mHT = ht;
        }

        // On earlier versions of Android, this onChange method is called
        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            mContentChanged = true;
        }

        public void waitForNotificationOrFail() {
            // Note: The PollingCheck class is taken from the Android CTS (Compatibility Test Suite).
            // It's useful to look at the Android CTS source for ideas on how to test your Android
            // applications.  The reason that PollingCheck works is that, by default, the JUnit
            // testing framework is not running on the main Android application thread.
            new PollingCheck(5000) {
                @Override
                protected boolean check() {
                    return mContentChanged;
                }
            }.run();
            mHT.quit();
        }
    }

    static TestContentObserver getTestContentObserver() {
        return TestContentObserver.getTestContentObserver();
    }
}
