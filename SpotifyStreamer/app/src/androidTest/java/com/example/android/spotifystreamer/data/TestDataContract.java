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

import android.net.Uri;
import android.test.AndroidTestCase;

/*
    Students: This is NOT a complete test for the WeatherContract --- just for the functions
    that we expect you to write.
 */
public class TestDataContract extends AndroidTestCase {

    // intentionally includes a slash to make sure Uri is getting quoted correctly
    private static final String TEST_SEARCH_TERM = "/Band of Horses";


    public void testBuildArtistWithSearchTerm() {
        Uri uri = DataContract.ArtistEntry.buildArtistWithSearchTermUri(TEST_SEARCH_TERM);
        assertNotNull("Error: Null Uri returned from buildArtistWithSearchTermUri",
                uri);
        assertEquals("Error: Search term not properly appended to the end of the Uri",
                TEST_SEARCH_TERM, uri.getLastPathSegment());
        assertEquals("Error: Artist with Search Term Uri doesn't match our expected result",
                uri.toString(),
                "content://com.example.android.spotifystreamer/artist/%2FBand%20of%20Horses");
    }
}
