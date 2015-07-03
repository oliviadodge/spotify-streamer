package com.example.android.spotifystreamer;

import android.app.Fragment;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;


/**
 * A placeholder fragment containing a simple view.
 */
public class TopTracksFragment extends Fragment {

    TracksAdapter mTracksAdapter;
    String mArtistId;
    List<Track> mTopTracks;
    ListView mListView;


    public TopTracksFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mArtistId = getActivity().getIntent().getStringExtra(MainActivityFragment.ARTIST_ID_EXTRA);

        ConnectivityManager connMgr = (ConnectivityManager)
                getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            FetchTopTracksTask fetchTopTracksTask = new FetchTopTracksTask();
            fetchTopTracksTask.execute(mArtistId);
        } else {
            Toast toast = Toast.makeText(getActivity(), getString(R.string.toast_no_network_found), Toast.LENGTH_LONG);
            toast.show();
        }

        TopTracksLab topTracksLab = TopTracksLab.get(getActivity());
        if (mArtistId.equals(topTracksLab.getArtistId())) {
            mTopTracks = TopTracksLab.get(getActivity()).getTopTracks();
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_top_tracks, container, false);

        mListView = (ListView) rootView.findViewById(R.id.listview_top_tracks);
        setUpAdapter();

        return rootView;
    }

    void setUpAdapter() {
        if (getActivity() == null || mListView == null) return;
        if (mTopTracks != null) {
            mTracksAdapter = new TracksAdapter(mTopTracks);
            mListView.setAdapter(mTracksAdapter);
        } else
            mListView.setAdapter(null);
    }


    private class TracksAdapter extends ArrayAdapter<Track> {

        public TracksAdapter(List<Track> tracks){
            super(getActivity(), 0, tracks);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            //if we aren't given a view, inflate one
            if(convertView == null){
                convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_track, null);
            }

            //configure the view for this track
            Track track = getItem(position);

            ImageView imageView = (ImageView) convertView.findViewById(R.id.list_item_image);
            List<kaaes.spotify.webapi.android.models.Image> albumImages = track.album.images;


            if (albumImages.size() > 0) {
                String url = albumImages.get(0).url;
                Picasso.with(getActivity()).load(url).placeholder(R.drawable.default_placeholder).error(R.drawable.default_placeholder)
                        .resize(200, 200).centerCrop().into(imageView);
            } else {
                Picasso.with(getActivity()).load(R.drawable.default_placeholder)
                        .resize(200, 200).centerCrop().into(imageView);
            }

            TextView nameTextView =
                    (TextView) convertView.findViewById(R.id.list_item_track_textview);
            nameTextView.setText(track.name);

            TextView albumNameTextView =
                    (TextView) convertView.findViewById(R.id.list_item_album_textview);
            albumNameTextView.setText(track.album.name);

            return convertView;
        }

    }

    public class FetchTopTracksTask extends AsyncTask<String, Void, List<Track>> {


        @Override
        protected List<Track> doInBackground(String... params) {

            List<Track> tracks;

            Map<String, Object> queryParams = new HashMap<>();
            queryParams.put("country", "US");

            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();

            Tracks results = spotify.getArtistTopTrack(params[0], queryParams);

            tracks = results.tracks;

            return tracks;
        }

        @Override
        protected void onPostExecute(List<Track> tracks) {
            mTopTracks = tracks;
            if (mTopTracks.size() == 0) {
                Toast toast = Toast.makeText(getActivity(), getString(R.string.toast_no_track_found), Toast.LENGTH_LONG);
                toast.show();
            }

            TopTracksLab topTracksLab = TopTracksLab.get(getActivity());
            topTracksLab.setArtistId(mArtistId);
            topTracksLab.setTopTracks(mTopTracks);
            setUpAdapter();
        }
    }
}

