

package com.example.android.spotifystreamer.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import java.util.HashSet;

public class TestDb extends AndroidTestCase {

    public static final String LOG_TAG = TestDb.class.getSimpleName();

    // Since we want each test to start with a clean slate
    void deleteTheDatabase() {
        mContext.deleteDatabase(DataDbHelper.DATABASE_NAME);
    }

    /*
        This function gets called before each test is executed to delete the database.  This makes
        sure that we always have a clean test.
     */
    public void setUp() {
        deleteTheDatabase();
    }


    public void testCreateDb() throws Throwable {
        // build a HashSet of all of the table names we wish to look for
        // Note that there will be another table in the DB that stores the
        // Android metadata (db version information)
        final HashSet<String> tableNameHashSet = new HashSet<String>();
        tableNameHashSet.add(DataContract.CountryEntry.TABLE_NAME);
        tableNameHashSet.add(DataContract.ArtistEntry.TABLE_NAME);
        tableNameHashSet.add(DataContract.SearchTermEntry.TABLE_NAME);
        tableNameHashSet.add(DataContract.TopTrackEntry.TABLE_NAME);

        mContext.deleteDatabase(DataDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new DataDbHelper(
                this.mContext).getWritableDatabase();

        assertEquals(true, db.isOpen());

        // have we created the tables we want?
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        assertTrue("Error: This means that the database has not been created correctly",
                c.moveToFirst());

        // verify that the tables have been created
        do {
            tableNameHashSet.remove(c.getString(0));
        } while( c.moveToNext() );

        // if this fails, it means that your database doesn't contain the tables
        assertTrue("Error: Your database was created without the necessary tables",
                tableNameHashSet.isEmpty());

        // now, do our tables contain the correct columns?
        c = db.rawQuery("PRAGMA table_info(" + DataContract.CountryEntry.TABLE_NAME + ")",
                null);

        assertTrue("Error: This means that we were unable to query the database for table information.",
                c.moveToFirst());

        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> locationColumnHashSet = new HashSet<String>();
        locationColumnHashSet.add(DataContract.CountryEntry._ID);
        locationColumnHashSet.add(DataContract.CountryEntry.COLUMN_COUNTRY_NAME);
        locationColumnHashSet.add(DataContract.CountryEntry.COLUMN_COUNTRY_SETTING);
        int columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            locationColumnHashSet.remove(columnName);
        } while(c.moveToNext());

        // if this fails, it means that your database doesn't contain all of the required location
        // entry columns
        assertTrue("Error: The database doesn't contain all of the required country entry columns",
                locationColumnHashSet.isEmpty());
        db.close();
    }

    /*
        Students:  Here is where you will build code to test that we can insert and query the
        location database.  We've done a lot of work for you.  You'll want to look in TestUtilities
        where you can uncomment out the "createTestCountryValues" function.  You can
        also make use of the ValidateCurrentRecord function from within TestUtilities.
    */
    public void testCountryTable() {
        insertCountry();
    }

    public void testSearchTermTable() {
        insertSearchTerm();
    }

    public void testArtistTable() {
        insertArtist();
    }

    public void testTopTrackTable() {

        long countryRowId = insertCountry();
        long artistRowId = insertArtist();

        // Make sure we have a valid row ID.
        assertFalse("Error: Country Not Inserted Correctly", countryRowId == -1L);
        assertFalse("Error: Artist Not Inserted Correctly", artistRowId == -1L);

         // First step: Get reference to writable database
        // If there's an error in those massive SQL table creation Strings,
        // errors will be thrown here when you try to get a writable database.
        DataDbHelper dbHelper = new DataDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Second Step: Create track values
        ContentValues trackValues = TestUtilities.createTrackValues(countryRowId, artistRowId);

        // Third Step: Insert ContentValues into database and get a row ID back
        long trackRowId = db.insert(DataContract.TopTrackEntry.TABLE_NAME, null, trackValues);
        assertTrue("Error: Track Not Inserted Correctly", trackRowId != -1);

        // Fourth Step: Query the database and receive a Cursor back
        // A cursor is your primary interface to the query results.
        Cursor trackCursor = db.query(
                DataContract.TopTrackEntry.TABLE_NAME,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null  // sort order
        );

        // Move the cursor to the first valid database row and check to see if we have any rows
        assertTrue("Error: No Records returned from track query", trackCursor.moveToFirst());

        // Fifth Step: Validate the track Query
        TestUtilities.validateCurrentRecord("testInsertReadDb trackEntry failed to validate",
                trackCursor, trackValues);

        // Move the cursor to demonstrate that there is only one record in the database
        assertFalse("Error: More than one record returned from track query",
                trackCursor.moveToNext());

        // Sixth Step: Close cursor and database
        trackCursor.close();
        dbHelper.close();
    }


    public long insertCountry() {
        // First step: Get reference to writable database
        // If there's an error in those massive SQL table creation Strings,
        // errors will be thrown here when you try to get a writable database.
        DataDbHelper dbHelper = new DataDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Second Step: Create ContentValues of what you want to insert
        // (you can use the createTestCountryValues if you wish)
        ContentValues testValues = TestUtilities.createTestCountryValues();

        // Third Step: Insert ContentValues into database and get a row ID back
        long countryRowId;
        countryRowId = db.insert(DataContract.CountryEntry.TABLE_NAME, null, testValues);

        // Verify we got a row back.
        assertTrue(countryRowId != -1);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // Fourth Step: Query the database and receive a Cursor back
        // A cursor is your primary interface to the query results.
        Cursor cursor = db.query(
                DataContract.CountryEntry.TABLE_NAME,  // Table to Query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        // Move the cursor to a valid database row and check to see if we got any records back
        // from the query
        assertTrue( "Error: No Records returned from country query", cursor.moveToFirst() );

        // Fifth Step: Validate data in resulting Cursor with the original ContentValues
        // (you can use the validateCurrentRecord function in TestUtilities to validate the
        // query if you like)
        TestUtilities.validateCurrentRecord("Error: Location Query Validation Failed",
                cursor, testValues);

        // Move the cursor to demonstrate that there is only one record in the database
        assertFalse( "Error: More than one record returned from country query",
                cursor.moveToNext() );

        // Sixth Step: Close Cursor and Database
        cursor.close();
        db.close();
        return countryRowId;
    }

    /*
        This is a helper method for testArtistTable.
     */
    public long insertSearchTerm() {
        // First step: Get reference to writable database
        // If there's an error in those massive SQL table creation Strings,
        // errors will be thrown here when you try to get a writable database.
        DataDbHelper dbHelper = new DataDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Second Step: Create ContentValues of what you want to insert
        // (you can use the createTestCountryValues if you wish)
        ContentValues testValues = TestUtilities.createTestSearchTermValues();

        // Third Step: Insert ContentValues into database and get a row ID back
        long searchTermRowId;
        searchTermRowId = db.insert(DataContract.SearchTermEntry.TABLE_NAME, null, testValues);

        // Verify we got a row back.
        assertTrue(searchTermRowId != -1);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // Fourth Step: Query the database and receive a Cursor back
        // A cursor is your primary interface to the query results.
        Cursor cursor = db.query(
                DataContract.SearchTermEntry.TABLE_NAME,  // Table to Query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        // Move the cursor to a valid database row and check to see if we got any records back
        // from the query
        assertTrue( "Error: No Records returned from search term query", cursor.moveToFirst() );

        // Fifth Step: Validate data in resulting Cursor with the original ContentValues
        // (you can use the validateCurrentRecord function in TestUtilities to validate the
        // query if you like)
        TestUtilities.validateCurrentRecord("Error: Search Term Query Validation Failed",
                cursor, testValues);

        // Move the cursor to demonstrate that there is only one record in the database
        assertFalse( "Error: More than one record returned from search term query",
                cursor.moveToNext() );

        // Sixth Step: Close Cursor and Database
        cursor.close();
        db.close();
        return searchTermRowId;
    }

    public long insertArtist() {
        long searchTermRowId = insertSearchTerm();

        // Make sure we have a valid row ID.
        assertFalse("Error: Searh Term Not Inserted Correctly", searchTermRowId == -1L);

        // First step: Get reference to writable database
        // If there's an error in those massive SQL table creation Strings,
        // errors will be thrown here when you try to get a writable database.
        DataDbHelper dbHelper = new DataDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Second Step (Weather): Create weather values
        ContentValues artistValues = TestUtilities.createArtistValues(searchTermRowId);

        // Third Step (Weather): Insert ContentValues into database and get a row ID back
        long artistRowId = db.insert(DataContract.ArtistEntry.TABLE_NAME, null, artistValues);
        assertTrue(artistRowId != -1);

        // Fourth Step: Query the database and receive a Cursor back
        // A cursor is your primary interface to the query results.
        Cursor artistCursor = db.query(
                DataContract.ArtistEntry.TABLE_NAME,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null  // sort order
        );

        // Move the cursor to the first valid database row and check to see if we have any rows
        assertTrue("Error: No Records returned from artist query", artistCursor.moveToFirst());

        // Fifth Step: Validate the artist Query
        TestUtilities.validateCurrentRecord("testInsertReadDb artistEntry failed to validate",
                artistCursor, artistValues);

        // Move the cursor to demonstrate that there is only one record in the database
        assertFalse("Error: More than one record returned from weather query",
                artistCursor.moveToNext());

        // Sixth Step: Close cursor and database
        artistCursor.close();
        dbHelper.close();

        return artistRowId;
    }
}
