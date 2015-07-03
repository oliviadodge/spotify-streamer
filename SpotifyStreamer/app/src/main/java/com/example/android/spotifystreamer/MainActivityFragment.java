package com.example.android.spotifystreamer;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    ArtistsAdapter mArtistsAdapter;
    String mSearchTerm;
    List<Artist> mArtists;
    ListView mListView;
    Context mContext;
    public static final String ARTIST_ID_EXTRA = "artist_id";


    public MainActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        mArtists = ArtistLab.get(getActivity()).getArtists();
        setHasOptionsMenu(true);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        final EditText editTextSearch = (EditText) rootView.findViewById(R.id.edittext_search_artist);
        editTextSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_SEARCH) {
                    mSearchTerm = textView.getText().toString();


                    ConnectivityManager connMgr = (ConnectivityManager)
                            getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                    if (networkInfo != null && networkInfo.isConnected()) {
                        FetchArtistsTask artistsTask = new FetchArtistsTask();
                        artistsTask.execute(mSearchTerm);
                    } else {
                        Toast toast = Toast.makeText(getActivity(), getString(R.string.toast_no_network_found), Toast.LENGTH_LONG);
                        toast.show();
                    }

                    InputMethodManager imm = (InputMethodManager) mContext.getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(editTextSearch.getWindowToken(), 0);
                }
                return true;
            }
        });


        mListView = (ListView) rootView.findViewById(R.id.listview_artists);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Artist artist = mArtistsAdapter.getItem(position);
                Intent i = new Intent(getActivity(), TopTracksActivity.class);
                i.putExtra(ARTIST_ID_EXTRA, artist.id);
                startActivity(i);
            }
        });
        setUpAdapter();

        return rootView;
    }

    void setUpAdapter() {
        if (getActivity() == null || mListView == null) return;

        if (mArtists != null) {
            mArtistsAdapter = new ArtistsAdapter(mArtists);
            mListView.setAdapter(mArtistsAdapter);
        } else
            mListView.setAdapter(null);
    }


    private class ArtistsAdapter extends ArrayAdapter<Artist> {

        public ArtistsAdapter(List<Artist> artists){
            super(getActivity(), 0, artists);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            //if we aren't given a view, inflate one
            if(convertView == null){
                convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_artist, null);
            }

            //configure the view for this artist
            Artist artist = getItem(position);

            ImageView imageView = (ImageView) convertView.findViewById(R.id.list_item_image);
            List<kaaes.spotify.webapi.android.models.Image> artistImages = artist.images;


            if (artistImages.size() > 0) {
              String url = artistImages.get(0).url;
                Picasso.with(getActivity()).load(url).placeholder(R.drawable.default_placeholder).error(R.drawable.default_placeholder)
                        .resize(200, 200).centerCrop().into(imageView);
            } else {
                Picasso.with(getActivity()).load(R.drawable.default_placeholder)
                        .resize(200, 200).centerCrop().into(imageView);
            }

            TextView nameTextView =
                    (TextView) convertView.findViewById(R.id.list_item_artist_textview);
            nameTextView.setText(artist.name);

            return convertView;
        }

    }

    public class FetchArtistsTask extends AsyncTask<String, Void, List<Artist>> {



        @Override
        protected List<Artist> doInBackground(String... params) {

            List<Artist> artists;

            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();

            ArtistsPager results = spotify.searchArtists(params[0]);

            artists = results.artists.items;

            return artists;
        }

        @Override
        protected void onPostExecute(List<Artist> artists) {
            mArtists = artists;

            if (mArtists.size() == 0){
                Toast toast = Toast.makeText(getActivity(), getString(R.string.toast_no_artist_found), Toast.LENGTH_LONG);
                toast.show();
            }
            ArtistLab.get(getActivity()).setArtists(mArtists);
            setUpAdapter();
        }
    }
}

