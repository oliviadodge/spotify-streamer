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

import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.test.AndroidTestCase;
import android.util.Log;

import com.example.android.spotifystreamer.data.DataContract.SearchTermEntry;
import com.example.android.spotifystreamer.data.DataContract.CountryEntry;
import com.example.android.spotifystreamer.data.DataContract.ArtistEntry;

/*
    Note: This is not a complete set of tests of the Sunshine ContentProvider, but it does test
    that at least the basic functionality has been implemented correctly.

     Uncomment the tests in this class as you implement the functionality in your
    ContentProvider to make sure that you've implemented things reasonably correctly.
 */
public class TestProvider extends AndroidTestCase {

    public static final String LOG_TAG = TestProvider.class.getSimpleName();

    /*
       This helper function deletes all records from both database tables using the ContentProvider.
       It also queries the ContentProvider to make sure that the database has been successfully
       deleted, so it cannot be used until the Query and Delete functions have been written
       in the ContentProvider.

        Replace the calls to deleteAllRecordsFromDB with this one after you have written
       the delete functionality in the ContentProvider.
     */
    public void deleteAllRecordsFromProvider() {
        mContext.getContentResolver().delete(
                DataContract.TopTrackEntry.CONTENT_URI,
                null,
                null
        );
        mContext.getContentResolver().delete(
                ArtistEntry.CONTENT_URI,
                null,
                null
        );
        mContext.getContentResolver().delete(
                CountryEntry.CONTENT_URI,
                null,
                null
        );
        mContext.getContentResolver().delete(
                SearchTermEntry.CONTENT_URI,
                null,
                null
        );

        Cursor cursor = mContext.getContentResolver().query(
                DataContract.TopTrackEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from top_track table during delete", 0, cursor.getCount());
        cursor.close();

        cursor = mContext.getContentResolver().query(
                ArtistEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from artist table during delete", 0, cursor.getCount());
        cursor.close();

        cursor = mContext.getContentResolver().query(
                CountryEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from country table during delete", 0, cursor.getCount());
        cursor.close();

        cursor = mContext.getContentResolver().query(
                SearchTermEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from search_term table during delete", 0, cursor.getCount());
        cursor.close();
    }

    /*
         Refactor this function to use the deleteAllRecordsFromProvider functionality once
        you have implemented delete functionality there.
     */
    public void deleteAllRecords() {
        deleteAllRecordsFromProvider();
    }

    // Since we want each test to start with a clean slate, run deleteAllRecords
    // in setUp (called by the test runner before each test).
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        deleteAllRecords();
    }

    /*
        This test checks to make sure that the content provider is registered correctly.
         Uncomment this test to make sure you've correctly registered the DataProvider.
     */
    public void testProviderRegistry() {
        PackageManager pm = mContext.getPackageManager();

        // We define the component name based on the package name from the context and the
        // DataProvider class.
        ComponentName componentName = new ComponentName(mContext.getPackageName(),
                DataProvider.class.getName());
        try {
            // Fetch the provider info using the component name from the PackageManager
            // This throws an exception if the provider isn't registered.
            ProviderInfo providerInfo = pm.getProviderInfo(componentName, 0);

            // Make sure that the registered authority matches the authority from the Contract.
            assertEquals("Error: DataProvider registered with authority: " + providerInfo.authority +
                    " instead of authority: " + DataContract.CONTENT_AUTHORITY,
                    providerInfo.authority, DataContract.CONTENT_AUTHORITY);
        } catch (PackageManager.NameNotFoundException e) {
            // I guess the provider isn't registered correctly.
            assertTrue("Error: DataProvider not registered at " + mContext.getPackageName(),
                    false);
        }
    }

    /*
            This test doesn't touch the database.  It verifies that the ContentProvider returns
            the correct type for each type of URI that it can handle.
             Uncomment this test to verify that your implementation of GetType is
            functioning correctly.
         */
    public void testGetType() {
        // content://com.example.android.sunshine.app/weather/
        String type = mContext.getContentResolver().getType(ArtistEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.example.android.sunshine.app/weather
        assertEquals("Error: the ArtistEntry CONTENT_URI should return ArtistEntry.CONTENT_TYPE",
                ArtistEntry.CONTENT_TYPE, type);

        String testSearchTerm = "Coldplay";
        type = mContext.getContentResolver().getType(
                ArtistEntry.buildArtistWithSearchTermUri(testSearchTerm));
        assertEquals("Error: the ArtistEntry CONTENT_URI with search term should return ArtistEntry.CONTENT_TYPE",
                ArtistEntry.CONTENT_TYPE, type);

        long testId = 10L;
        type = mContext.getContentResolver().getType(
                ArtistEntry.buildArtistWithSearchTermAndArtistId(testSearchTerm, testId));
        assertEquals("Error: the ArtistEntry CONTENT_URI with search term and date should return ArtistEntry.CONTENT_ITEM_TYPE",
                ArtistEntry.CONTENT_ITEM_TYPE, type);

        type = mContext.getContentResolver().getType(CountryEntry.CONTENT_URI);
        assertEquals("Error: the CountryEntry CONTENT_URI should return CountryEntry.CONTENT_TYPE",
                CountryEntry.CONTENT_TYPE, type);
    }


    /*
        This test uses the database directly to insert and then uses the ContentProvider to
        read out the data.  Uncomment this test to see if the basic weather query functionality
        given in the ContentProvider is working correctly.
     */
    public void testBasicArtistQuery() {
        // insert our test records into the database
        DataDbHelper dbHelper = new DataDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        long searchTermRowId = TestUtilities.insertTestSearchTermValues(mContext);

        ContentValues artistValues = TestUtilities.createArtistValues(searchTermRowId);

        long artistRowId = db.insert(ArtistEntry.TABLE_NAME, null, artistValues);
        assertTrue("Unable to Insert ArtistEntry into the Database", artistRowId != -1);

        db.close();

        // Test the basic content provider query
        Cursor artistCursor = mContext.getContentResolver().query(
                ArtistEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        // Make sure we get the correct cursor out of the database
        TestUtilities.validateCursor("testBasicArtistQuery", artistCursor, artistValues);
    }

    /*
        This test uses the database directly to insert and then uses the ContentProvider to
        read out the data.  Uncomment this test to see if your country queries are
        performing correctly.
     */
    public void testBasicCountryQuery() {
        // insert our test records into the database
        DataDbHelper dbHelper = new DataDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues testValues = TestUtilities.createTestCountryValues();
        long countryRowId = TestUtilities.insertTestCountryValues(mContext);

        // Test the basic content provider query
        Cursor countryCursor = mContext.getContentResolver().query(
                CountryEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        // Make sure we get the correct cursor out of the database
        TestUtilities.validateCursor("testBasicCountryQuery, country query", countryCursor, testValues);

        // Has the NotificationUri been set correctly? --- we can only test this easily against API
        // level 19 or greater because getNotificationUri was added in API level 19.
        if ( Build.VERSION.SDK_INT >= 19 ) {
            assertEquals("Error: Country Query did not properly set NotificationUri",
                    countryCursor.getNotificationUri(), CountryEntry.CONTENT_URI);
        }
    }

    /*
        This test uses the provider to insert and then update the data. Uncomment this test to
        see if your update country is functioning correctly.
     */
    public void testUpdateCountry() {
        // Create a new map of values, where column names are the keys
        ContentValues values = TestUtilities.createTestCountryValues();

        Uri countryUri = mContext.getContentResolver().
                insert(CountryEntry.CONTENT_URI, values);
        long countryRowId = ContentUris.parseId(countryUri);

        // Verify we got a row back.
        assertTrue(countryRowId != -1);
        Log.d(LOG_TAG, "New row id: " + countryRowId);

        ContentValues updatedValues = new ContentValues(values);
        updatedValues.put(CountryEntry._ID, countryRowId);
        updatedValues.put(CountryEntry.COLUMN_COUNTRY_NAME, "Australia");

        // Create a cursor with observer to make sure that the content provider is notifying
        // the observers as expected
        Cursor countryCursor = mContext.getContentResolver().query(CountryEntry.CONTENT_URI, null, null, null, null);

        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        countryCursor.registerContentObserver(tco);

        int count = mContext.getContentResolver().update(
                CountryEntry.CONTENT_URI, updatedValues, CountryEntry._ID + "= ?",
                new String[] { Long.toString(countryRowId)});
        assertEquals(count, 1);

        // Test to make sure our observer is called.  If not, we throw an assertion.
        //
        //  If your code is failing here, it means that your content provider
        // isn't calling getContext().getContentResolver().notifyChange(uri, null);
        tco.waitForNotificationOrFail();

        countryCursor.unregisterContentObserver(tco);
        countryCursor.close();

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                CountryEntry.CONTENT_URI,
                null,   // projection
                CountryEntry._ID + " = " + countryRowId,
                null,   // Values for the "where" clause
                null    // sort order
        );

        TestUtilities.validateCursor("testUpdateCountry.  Error validating country entry update.",
                cursor, updatedValues);

        cursor.close();
    }


    // Make sure we can still delete after adding/updating stuff
    //
    //  Uncomment this test after you have completed writing the insert functionality
    // in your provider.  It relies on insertions with testInsertReadProvider, so insert and
    // query functionality must also be complete before this test can be used.
    public void testInsertReadProvider() {
        ContentValues testValues = TestUtilities.createTestSearchTermValues();

        // Register a content observer for our insert.  This time, directly with the content resolver
        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(SearchTermEntry.CONTENT_URI, true, tco);
        Uri searchTermUri = mContext.getContentResolver().insert(SearchTermEntry.CONTENT_URI, testValues);

        // Did our content observer get called?    If this fails, your insert search term
        // isn't calling getContext().getContentResolver().notifyChange(uri, null);
        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);

        long searchTermRowId = ContentUris.parseId(searchTermUri);

        // Verify we got a row back.
        assertTrue(searchTermRowId != -1);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                SearchTermEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestUtilities.validateCursor("testInsertReadProvider. Error validating SearchTermEntry.",
                cursor, testValues);

        ContentValues artistValues = TestUtilities.createArtistValues(searchTermRowId);
        // The TestContentObserver is a one-shot class
        tco = TestUtilities.getTestContentObserver();

        mContext.getContentResolver().registerContentObserver(ArtistEntry.CONTENT_URI, true, tco);

        Uri artistInsertUri = mContext.getContentResolver()
                .insert(ArtistEntry.CONTENT_URI, artistValues);
        assertTrue(artistInsertUri != null);

        // Did our content observer get called?    If this fails, your insert weather
        // in your ContentProvider isn't calling
        // getContext().getContentResolver().notifyChange(uri, null);
        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);

        // A cursor is your primary interface to the query results.
        Cursor artistCursor = mContext.getContentResolver().query(
                ArtistEntry.CONTENT_URI,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null // columns to group by
        );

        TestUtilities.validateCursor("testInsertReadProvider. Error validating ArtistEntry insert.",
                artistCursor, artistValues);

        artistValues.putAll(testValues);

        artistCursor = mContext.getContentResolver().query(
                ArtistEntry.buildArtistWithSearchTermUri(TestUtilities.TEST_SEARCH_TERM),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );
        TestUtilities.validateCursor("testInsertReadProvider.  Error validating joined Artist and SearchTerm Data.",
                artistCursor, artistValues);

        artistCursor = mContext.getContentResolver().query(
                ArtistEntry.buildArtistWithSearchTermAndArtistId(
                        TestUtilities.TEST_SEARCH_TERM, TestUtilities.TEST_ARTIST_ROW_ID),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

    }

    // Make sure we can still delete after adding/updating stuff
    //
    //  Uncomment this test after you have completed writing the delete functionality
    // in your provider.  It relies on insertions with testInsertReadProvider, so insert and
    // query functionality must also be complete before this test can be used.
    public void testDeleteRecords() {
        testInsertReadProvider();

        TestUtilities.TestContentObserver searchTermObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(SearchTermEntry.CONTENT_URI, true, searchTermObserver);

        TestUtilities.TestContentObserver artistObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(ArtistEntry.CONTENT_URI, true, artistObserver);

        deleteAllRecordsFromProvider();

        //  If either of these fail, you most-likely are not calling the
        // getContext().getContentResolver().notifyChange(uri, null); in the ContentProvider
        // delete.  (only if the insertReadProvider is succeeding)
        searchTermObserver.waitForNotificationOrFail();
        artistObserver.waitForNotificationOrFail();

        mContext.getContentResolver().unregisterContentObserver(searchTermObserver);
        mContext.getContentResolver().unregisterContentObserver(artistObserver);
    }


    static private final int BULK_INSERT_RECORDS_TO_INSERT = 10;
    static ContentValues[] createBulkInsertArtistValues(long searchTermRowId) {
        StringBuilder artistId = new StringBuilder(TestUtilities.TEST_ARTIST_ID);
        ContentValues[] returnContentValues = new ContentValues[BULK_INSERT_RECORDS_TO_INSERT];

        for ( int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++) {

            artistId.setCharAt(i, 'z');
            ContentValues artistValues = new ContentValues();
            artistValues.put(ArtistEntry.COLUMN_SEARCH_KEY, searchTermRowId);
            artistValues.put(ArtistEntry.COLUMN_ARTIST_SPOTIFY_ID, artistId.toString());
            artistValues.put(ArtistEntry.COLUMN_ARTIST_NAME, TestUtilities.TEST_ARTIST_NAME);

            returnContentValues[i] = artistValues;
        }
        return returnContentValues;
    }

    //  Uncomment this test after you have completed writing the BulkInsert functionality
    // in your provider.  Note that this test will work with the built-in (default) provider
    // implementation, which just inserts records one-at-a-time, so really do implement the
    // BulkInsert ContentProvider function.
    public void testBulkInsert() {
        ContentValues testValues = TestUtilities.createTestSearchTermValues();
        Uri searchTermUri = mContext.getContentResolver().insert(SearchTermEntry.CONTENT_URI, testValues);
        long searchTermRowId = ContentUris.parseId(searchTermUri);

        // Verify we got a row back.
        assertTrue(searchTermRowId != -1);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                SearchTermEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestUtilities.validateCursor("testBulkInsert. Error validating SearchEntry.",
                cursor, testValues);

        // Now we can bulkInsert some weather.  In fact, we only implement BulkInsert for weather
        // entries.  With ContentProviders, you really only have to implement the features you
        // use, after all.
        ContentValues[] bulkInsertContentValues = createBulkInsertArtistValues(searchTermRowId);

        // Register a content observer for our bulk insert.
        TestUtilities.TestContentObserver artistObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(ArtistEntry.CONTENT_URI, true, artistObserver);

        int insertCount = mContext.getContentResolver().bulkInsert(ArtistEntry.CONTENT_URI, bulkInsertContentValues);

        //   If this fails, it means that you most-likely are not calling the
        // getContext().getContentResolver().notifyChange(uri, null); in your BulkInsert
        // ContentProvider method.
        artistObserver.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(artistObserver);

        assertEquals(insertCount, BULK_INSERT_RECORDS_TO_INSERT);

        // A cursor is your primary interface to the query results.
        cursor = mContext.getContentResolver().query(
                ArtistEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                ArtistEntry._ID + " ASC"  // sort order == by DATE ASCENDING
        );

        // we should have as many records in the database as we've inserted
        assertEquals(cursor.getCount(), BULK_INSERT_RECORDS_TO_INSERT);

        // and let's make sure they match the ones we created
        cursor.moveToFirst();
        for ( int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++, cursor.moveToNext() ) {
            TestUtilities.validateCurrentRecord("testBulkInsert.  Error validating ArtistEntry " + i,
                    cursor, bulkInsertContentValues[i]);
        }
        cursor.close();
    }
}
