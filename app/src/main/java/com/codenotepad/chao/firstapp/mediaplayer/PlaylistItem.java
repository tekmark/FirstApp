package com.codenotepad.chao.firstapp.mediaplayer;

import android.app.Activity;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.codenotepad.chao.firstapp.R;

/**
 * Created by chao on 10/15/15.
 */
public class PlaylistItem {
    private TextView mTitle;
    private TextView mArtist;
    private TextView mDuration;
    private TextView mPosition;

    private ViewGroup mMediaInfo;
    private ImageButton mMoreOption;

    //private Song song;

    private View mPlaylistItem;

    //private MediaPlayerController mMediaPlayer;

    private PlaylistItem () {

    }


    public void bindLayout(Activity activity) {
        LayoutInflater layoutInflater = activity.getLayoutInflater();
        mPlaylistItem = layoutInflater.inflate(R.layout.playlist_item, null);
        mTitle = (TextView) mPlaylistItem.findViewById(R.id.playlist_item_title);
        mArtist = (TextView) mPlaylistItem.findViewById(R.id.playlist_item_artist);
        mDuration = (TextView) mPlaylistItem.findViewById(R.id.playlist_item_duration);
        mPosition = (TextView) mPlaylistItem.findViewById(R.id.playlist_item_position);

        mMediaInfo = (ViewGroup) mPlaylistItem.findViewById(R.id.playlist_item_info);
        mMoreOption = (ImageButton) mPlaylistItem.findViewById(R.id.playlist_item_button_more);
        setListeners();
    }

    static public PlaylistItem create(Activity activity) {

        PlaylistItem item = new PlaylistItem();
        item.bindLayout(activity);
        return item;
    }

    public View getView() {
        return mPlaylistItem;
    }

    public View getInfoView() {
        return mMediaInfo;
    }
    public View getMoreOptionView() {
        return mMoreOption;
    }

    public void setDuration(long duration) {
        mDuration.setText(MediaPlayerUtils.millSecondsToTime(duration));
    }

    public void setDuration(String duration) {
        mDuration.setText(duration);
    }

    public void setTitle(String title) {
        mTitle.setText(title);
    }
    public String getTitle() {
        return mTitle.getText().toString();
    }

//    public void setTitleSize(int size) {
//        mTitle.setTextSize(size);
//    }
    public void setTitleColor(int color) {
        mTitle.setTextColor(color);
    }

    public void setArtist(String artist) {
        mArtist.setText(artist);
    }
    public void setArtistColor(int color) {
        mArtist.setTextColor(color);
    }

    public void setPosition(int position) {
        mPosition.setText(Integer.toString(position));
    }
    public void setPosition(String positon) {
        mPosition.setText(positon);
    }
    public int getPosition() {
        int pos = Integer.parseInt(mPosition.getText().toString());
        return pos;
    }

    private void setListeners() {
        /*mMediaInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("PlayListItem", "play " + mTitle.getText().toString() + " path " + songPath);
                Log.d("PlayListItem", "position : " + mPosition.getText().toString());
            }
        });*/

        mMoreOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("PlayListItem", "show extra options");
            }
        });
    }

    //for test only;
    public String songPath;
    public void setMediaPlayer(MediaPlayerController mp) {
        //mMediaPlayer = mp;
    }
//    void setmArtistSize(int size) {
//
//    }

}
