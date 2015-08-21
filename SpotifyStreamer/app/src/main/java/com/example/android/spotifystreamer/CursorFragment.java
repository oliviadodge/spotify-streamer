package com.example.android.spotifystreamer;

import android.app.Fragment;
import android.database.Cursor;
import android.os.Bundle;


/*
A simple fragment to hold the cursor object so that
it can be saved when the device is rotated
 */
public class CursorFragment extends Fragment {
    //Data object we want to retain (Cursor
    private Cursor mCursor;

    // this method is only called once for this fragment
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // retain this fragment
        setRetainInstance(true);
    }

    public void setCursor(Cursor cursor) {
        this.mCursor = cursor;
    }

    public Cursor getCursor() {
        return mCursor;
    }

}
