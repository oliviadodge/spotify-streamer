package com.example.android.spotifystreamer;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;

import kaaes.spotify.webapi.android.models.Track;


/**
 * A placeholder fragment containing a simple view.
 */
public class TrackPlayerFragment extends Fragment {

    private static final String TAG = "TrackPlayerFragment";
    String mTrackId;
    Track mTrack;
    ImageButton mPauseButton;

    MediaPlayer mPlayer;

    public TrackPlayerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        mTrackId = getActivity().getIntent().getStringExtra(TopTracksFragment.TRACK_ID_EXTRA);

        mTrack = TopTracksLab.get(getActivity()).getTrack(mTrackId);

        mPlayer = new MediaPlayer();
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mPlayer.setDataSource(mTrack.preview_url);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mPlayer =  mediaPlayer;
                mPlayer.start();
                Log.i(TAG, "mPlayer prepared and started");
            }
        });

        mPlayer.prepareAsync();

        ConnectivityManager connMgr = (ConnectivityManager)
                getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {

        } else {
            Toast toast = Toast.makeText(getActivity(), getString(R.string.toast_no_network_found), Toast.LENGTH_LONG);
            toast.show();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_track_player, container, false);

        TextView artistTextView = (TextView) view.findViewById(R.id.track_player_artist);
        artistTextView.setText(mTrack.artists.get(0).name);

        TextView albumTextView = (TextView) view.findViewById(R.id.track_player_album);
        albumTextView.setText(mTrack.album.name);

        TextView trackTextView = (TextView) view.findViewById(R.id.track_player_song);
        trackTextView.setText(mTrack.name);

        ImageView imageView = (ImageView) view.findViewById(R.id.track_player_album_art);
        List<kaaes.spotify.webapi.android.models.Image> albumImages = mTrack.album.images;

        if (albumImages.size() > 0) {
            String url = albumImages.get(0).url;
            Picasso.with(getActivity()).load(url).placeholder(R.drawable.default_placeholder).error(R.drawable.default_placeholder)
                    .resize(600, 600).centerCrop().into(imageView);
        } else {
            Picasso.with(getActivity()).load(R.drawable.default_placeholder)
                    .resize(600, 600).centerCrop().into(imageView);
        }

        ImageButton previousButton = (ImageButton) view.findViewById(R.id.track_player_previous);
        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                mPlayer.previous();
                Log.i(TAG, "Previous button called and the value of isPlaying() is  " + mPlayer.isPlaying());

            }
        });
        mPauseButton = (ImageButton) view.findViewById(R.id.track_player_pause);
        mPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPlayer.isPlaying()) {
                    mPauseButton.setSelected(true);
                    Log.i(TAG, "Pause button clicked and the value of isPlaying() is  " + mPlayer.isPlaying() +
                            ", and the value of isSelected is " + mPauseButton.isSelected());
                    mPlayer.pause();
                    Log.i(TAG, "mPlayer.pause() called and the value of isPlaying() is  " + mPlayer.isPlaying());
                } else {
                    mPlayer.start();
                    mPauseButton.setSelected(false);
                }
            }
        });
        ImageButton nextButton = (ImageButton) view.findViewById(R.id.track_player_next);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                mPlayer.next();
            }
        });

        return view;
    }

    @Override
    public void onStop() {
        mPlayer.release();
        mPlayer = null;
        super.onStop();
    }
}
