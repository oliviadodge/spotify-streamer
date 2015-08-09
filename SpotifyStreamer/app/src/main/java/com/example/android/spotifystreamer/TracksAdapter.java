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
public class TracksAdapter extends CursorAdapter {

    /**
     * Cache of the children views for track list item.
     */
    public static class ViewHolder {
        public final ImageView albumImageView;
        public final TextView trackNameTextView;
        public final TextView albumNameTextView;

        public ViewHolder(View view) {
            albumImageView = (ImageView) view.findViewById(R.id.list_item_image);
            albumNameTextView = (TextView) view.findViewById(R.id.list_item_album_textview);
            trackNameTextView = (TextView) view.findViewById(R.id.list_item_track_textview);
        }
    }

    public TracksAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        int layoutId = -1;

        layoutId = R.layout.list_item_track;

        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }


    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder viewHolder = (ViewHolder) view.getTag();

        String albumImageUrl = cursor.getString(TopTracksFragment.COL_ALBUM_IMAGE_URL);

        //check connectivity to see if album images can be loaded or if a placeholder must be loaded
        ConnectivityManager connMgr = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if ((albumImageUrl.length() > 0) && (networkInfo != null && networkInfo.isConnected())) {
            Picasso.with(context).load(albumImageUrl).placeholder(R.drawable.default_placeholder).error(R.drawable.default_placeholder)
                    .resize(200, 200).centerCrop().into(viewHolder.albumImageView);
        } else {
            Picasso.with(context).load(R.drawable.default_placeholder)
                    .resize(200, 200).centerCrop().into(viewHolder.albumImageView);
        }

        String albumName = cursor.getString(TopTracksFragment.COL_ALBUM_NAME);
        String trackName = cursor.getString(TopTracksFragment.COL_TRACK_NAME);

        viewHolder.albumNameTextView.setText(albumName);
        viewHolder.trackNameTextView.setText(trackName);
    }
}