package com.example.android.spotifystreamer;

import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * Created by oliviadodge on 8/4/2015.
 */
public class ArtistsAdapter extends CursorAdapter {

    /**
     * Cache of the children views for an artist list item.
     */
    public static class ViewHolder {
        public final ImageView artistImageView;
        public final TextView artistNameTextView;

        public ViewHolder(View view) {
            artistImageView = (ImageView) view.findViewById(R.id.list_item_image);
            artistNameTextView = (TextView) view.findViewById(R.id.list_item_artist_textview);
        }
    }

    public ArtistsAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        int layoutId = -1;

        layoutId = R.layout.list_item_artist;

        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }


    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder viewHolder = (ViewHolder) view.getTag();

        String artistImageUrl = cursor.getString(MainActivityFragment.COL_ARTIST_IMAGE_URL);

        //check connectivity to see if artist images can be loaded or if a placeholder must be loaded
        ConnectivityManager connMgr = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if ((artistImageUrl.length() > 0) && (networkInfo != null && networkInfo.isConnected())) {
            Picasso.with(context).load(artistImageUrl).placeholder(R.drawable.default_placeholder).error(R.drawable.default_placeholder)
                    .resize(200, 200).centerCrop().into(viewHolder.artistImageView);
        } else {
            Picasso.with(context).load(R.drawable.default_placeholder)
                    .resize(200, 200).centerCrop().into(viewHolder.artistImageView);
        }

        String artistName = cursor.getString(MainActivityFragment.COL_ARTIST_NAME);

        viewHolder.artistNameTextView.setText(artistName);
    }
}