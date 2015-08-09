package com.example.android.spotifystreamer;

import android.content.Context;

/**
 * Created by oliviadodge on 6/30/2015.
 *
 * Singleton for storing the most recent search term for the application. This will be used to initialize the
 * MainActivityFragment.ARTISTS_LOADER when the user starts the app so he/she can see the data results for the most recent search
 */
public class SearchTermLab {

    private String mSearchTerm;

    private static SearchTermLab sSearchTermLab;
    private Context mAppContext;

    private SearchTermLab(Context appContext){
        mAppContext = appContext;
    }

    public static SearchTermLab get(Context context) {
        if (sSearchTermLab == null) {
            sSearchTermLab = new SearchTermLab(context.getApplicationContext());
        }
        return sSearchTermLab;
    }

    public String getSearchTerm() {
        return mSearchTerm;
    }

    public void setSearchTerm(String searchTerm) {
        mSearchTerm = searchTerm;
    }
}
