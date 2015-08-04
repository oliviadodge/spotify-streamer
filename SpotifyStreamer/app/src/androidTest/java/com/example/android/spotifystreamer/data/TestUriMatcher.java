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

import android.content.UriMatcher;
import android.net.Uri;
import android.test.AndroidTestCase;

/*
    Uncomment this class when you are ready to test your UriMatcher.  Note that this class utilizes
    constants that are declared with package protection inside of the UriMatcher, which is why
    the test must be in the same data package as the Android app code.  Doing the test this way is
    a nice compromise between data hiding and testability.
 */
public class TestUriMatcher extends AndroidTestCase {
    private static final String SEARCH_TERM = "Coldplay";
    private static final long TEST_ID = 10L;

    // content://com.example.android.sunshine.app/weather"
    private static final Uri TEST_ARTIST_DIR = DataContract.ArtistEntry.CONTENT_URI;
    private static final Uri TEST_ARTIST_WITH_SEARCH_TERM_DIR = DataContract.ArtistEntry.buildArtistWithSearchTermUri(SEARCH_TERM);
    private static final Uri TEST_ARTIST_WITH_SEARCH_TERM_AND_ARTIST_ID =
            DataContract.ArtistEntry.buildArtistWithSearchTermAndArtistId(SEARCH_TERM, TEST_ID);
    // content://com.example.android.sunshine.app/location"
    private static final Uri TEST_SEARCH_TERM_DIR = DataContract.SearchTermEntry.CONTENT_URI;

    /*
        Students: This function tests that your UriMatcher returns the correct integer value
        for each of the Uri types that our ContentProvider can handle.  Uncomment this when you are
        ready to test your UriMatcher.
     */
    public void testUriMatcher() {
        UriMatcher testMatcher = DataProvider.buildUriMatcher();

        assertEquals("Error: The WEATHER URI was matched incorrectly.",
                testMatcher.match(TEST_ARTIST_DIR), DataProvider.ARTISTS);
        assertEquals("Error: The WEATHER WITH LOCATION URI was matched incorrectly.",
                testMatcher.match(TEST_ARTIST_WITH_SEARCH_TERM_DIR), DataProvider.ARTIST_WITH_SEARCH_TERM);
        assertEquals("Error: The WEATHER WITH LOCATION AND DATE URI was matched incorrectly.",
                testMatcher.match(TEST_ARTIST_WITH_SEARCH_TERM_AND_ARTIST_ID), DataProvider.ARTIST_WITH_SEARCH_TERM_AND_ARTIST_ID);
        assertEquals("Error: The LOCATION URI was matched incorrectly.",
                testMatcher.match(TEST_SEARCH_TERM_DIR), DataProvider.SEARCH_TERM);
    }
}
