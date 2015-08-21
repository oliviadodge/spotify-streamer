package com.example.android.spotifystreamer;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by FashyunGod on 8/13/2015.
 */
public class TrackPlayerDialogFragment extends DialogFragment {

    private static final String TAG = TrackPlayerDialogListener.class.getSimpleName();

    /* The activity that creates an instance of this dialog fragment must
 * implement this interface in order to receive event callbacks.
 * Each method passes the DialogFragment in case the host needs to query it. */
    public interface TrackPlayerDialogListener {
        public void onPreviousClick(DialogFragment dialog);

        public void onPlayPauseClick(DialogFragment dialog);

        public void onNextClick(DialogFragment dialog);

        public void onDialogViewCreated(View view);

        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser);
    }

    // Use this instance of the interface to deliver action events
    TrackPlayerDialogListener mListener;
    TrackPlayerDialogFragment mDialog;
    ImageButton mPauseButton;

    ArrayList<String> mTrackInfo;

    // Override the Fragment.onAttach() method to instantiate the listener
    @Override

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the listener so we can send events to the host
            mListener = (TrackPlayerDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement TrackPlayerDialogListener");
        }
    }

    /**
     * The system calls this to get the DialogFragment's layout, regardless
     * of whether it's being displayed as a dialog or an embedded fragment.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //get a reference to this dialog so that it can be passed to listener callback methods
        mDialog = this;

        //get fragment arguments to set up the view with
        Bundle arguments = getArguments();
        if (arguments != null) {
            mTrackInfo = arguments.getStringArrayList(TopTracksActivity.KEY_TRACK_INFO_ARRAY_LIST);
            Log.i(TAG, "arguments are not null and mTrackInfo is " + mTrackInfo);
        } else {
            Log.i(TAG, "onCreateView called and arguments are null!");
        }
        // Inflate the layout to use as dialog or embedded fragment
        View view = inflater.inflate(R.layout.fragment_track_player, container, false);

        TextView artistTextView = (TextView) view.findViewById(R.id.track_player_artist);
        artistTextView.setText(mTrackInfo.get(TopTracksFragment.COL_ARTIST_NAME));

        TextView albumTextView = (TextView) view.findViewById(R.id.track_player_album);
        albumTextView.setText(mTrackInfo.get(TopTracksFragment.COL_ALBUM_NAME));

        TextView trackTextView = (TextView) view.findViewById(R.id.track_player_song);
        trackTextView.setText(mTrackInfo.get(TopTracksFragment.COL_TRACK_NAME));

        ImageView imageView = (ImageView) view.findViewById(R.id.track_player_album_art);


        String albumImageUrl = mTrackInfo.get(TopTracksFragment.COL_ALBUM_IMAGE_URL);
        if (albumImageUrl.length() > 0) {
            Picasso.with(getActivity()).load(albumImageUrl).placeholder(R.drawable.default_placeholder).error(R.drawable.default_placeholder)
                    .resize(600, 600).centerCrop().into(imageView);
        } else {
            Picasso.with(getActivity()).load(R.drawable.default_placeholder)
                    .resize(600, 600).centerCrop().into(imageView);
        }

        SeekBar seekBar = (SeekBar) view.findViewById(R.id.track_player_seek_bar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mListener.onProgressChanged(seekBar, progress, fromUser);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        ImageButton previousButton = (ImageButton) view.findViewById(R.id.track_player_previous);
        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onPreviousClick(mDialog);
            }
        });
        mPauseButton = (ImageButton) view.findViewById(R.id.track_player_pause);
        if (getArguments().getBoolean(TopTracksActivity.KEY_IS_TRACK_PAUSED)) {
            mPauseButton.setSelected(true);
        }
        mPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onPlayPauseClick(mDialog);
            }
        });
        ImageButton nextButton = (ImageButton) view.findViewById(R.id.track_player_next);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onNextClick(mDialog);
            }
        });
        Log.i(TAG, "track player view successfully updated");



        return view;

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mListener != null) {
            mListener.onDialogViewCreated(view);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        boolean isPaused = getArguments().getBoolean(TopTracksActivity.KEY_IS_TRACK_PAUSED);
        mPauseButton.setSelected(isPaused);
    }

    /**
     * The system calls this only when creating the layout in a dialog.
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // The only reason you might override this method when using onCreateView() is
        // to modify any dialog characteristics. For example, the dialog includes a
        // title by default, but your custom layout might not need it. So here you can
        // remove the dialog title, but you must call the superclass to get the Dialog.
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }
}