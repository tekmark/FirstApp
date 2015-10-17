package com.codenotepad.chao.firstapp;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.codenotepad.chao.firstapp.mediaplayer.MediaPlayerController;
import com.codenotepad.chao.firstapp.mediaplayer.MediaPlayerUtils;
import com.codenotepad.chao.firstapp.mediaplayer.PlayListUtils;
import com.codenotepad.chao.firstapp.mediaplayer.Playlist;
import com.codenotepad.chao.firstapp.mediaplayer.PlaylistItem;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class DisplayPlaylistActivity extends AppCompatActivity {

    private int mPlaylistId = 71;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_playlist);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

       //MediaPlayerController mPlayer = new MediaPlayerController(this);
        bindLayout();
        mHandler = new Handler();

        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external",mPlaylistId);

        String[] proj = {
                MediaStore.Audio.Playlists.Members.AUDIO_ID,            //0
                MediaStore.Audio.Playlists.Members.TITLE,               //1
                MediaStore.Audio.Playlists.Members.ARTIST,              //2
                MediaStore.Audio.Playlists.Members._ID,                 //3
                MediaStore.Audio.Playlists.Members.DATA,                //4
                MediaStore.Audio.Playlists.Members.DURATION,            //5
                MediaStore.Audio.Playlists.Members.ALBUM                //6
        };

        Cursor musicCursor = this.getContentResolver().query(uri, proj, null, null, null);


        int idColIndex = musicCursor.getColumnIndex(MediaStore.Audio.Playlists.Members._ID);
        int audioIdColIndex = musicCursor.getColumnIndex(MediaStore.Audio.Playlists.Members.AUDIO_ID);
        int titleColIndex = musicCursor.getColumnIndex(MediaStore.Audio.Playlists.Members.TITLE);
        int artistColIndex = musicCursor.getColumnIndex(MediaStore.Audio.Playlists.Members.ARTIST);
        int pathColIndex = musicCursor.getColumnIndex(MediaStore.Audio.Playlists.Members.DATA);
        int durationColIndex = musicCursor.getColumnIndex(MediaStore.Audio.Playlists.Members.DURATION);
        int albumColIndex = musicCursor.getColumnIndex(MediaStore.Audio.Playlists.Members.ALBUM);

        Log.d("Mediaplayer", "count: " + musicCursor.getCount());
        String names[] = musicCursor.getColumnNames();
        for (String s : names) {
            Log.d("MediaPlayer", s);
        }


        if (musicCursor.moveToFirst()) {
            int pos = 0;
            while (!musicCursor.isLast()) {
                String duration = musicCursor.getString(durationColIndex);
                String title = musicCursor.getString(titleColIndex);
                String artist = musicCursor.getString(artistColIndex);
                String album = musicCursor.getString(albumColIndex);
                String path = musicCursor.getString(pathColIndex);
                String id = musicCursor.getString(idColIndex);
                String audioId = musicCursor.getString(audioIdColIndex);
                Log.d("MediaPlayer", "Title: " + title);
                Log.d("MediaPlayer", "Artist: " + artist);
                Log.d("MediaPlayer", "Album: " + album);
                Log.d("MediaPlayer", "_ID: " + id + " AUDIO_ID: " + audioId);
                Log.d("MediaPlayer", "Path: " + path);
                LinearLayout contentLayout = (LinearLayout)findViewById(R.id.playlist);
                PlaylistItem item1 = PlaylistItem.create(this);
                //item1.setMediaPlayer(mPlayer);
                item1.setArtist(artist);
                item1.setTitle(title);
                item1.setDuration(MediaPlayerUtils.millSecondsToTime(Integer.parseInt(duration)));
                item1.setPosition(pos);
                item1.songPath = path;
                playlist.addSong(path);
                item1.getInfoView().setTag(pos);
                item1.getInfoView().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //int postion = item1.getPosition();
                        //Log.d("PlayListItem", "play " + item1.getTitle() + " path " + item1.songPath);
                        mMusicService.loadCurrPlaylist(playlist);
                        int tag = (int)v.getTag();
                        int clickedPos = tag;
                        Log.d("PlayListItem", "clicked view tag : " + tag);
                        //check if clicked item is playing, if true, pause, else play it.
                        int currentPos = mMusicService.getCurrentPosition();

                        if (clickedPos == currentPos) {
                            if (mMusicService.isPlaying()) {
                                TextView titleView = (TextView)v.findViewById(R.id.playlist_item_title);
                                titleView.setTypeface(null, Typeface.NORMAL);
                                mMusicService.pause();
                            } else {
                                TextView titleView = (TextView)v.findViewById(R.id.playlist_item_title);
                                titleView.setTypeface(null, Typeface.BOLD_ITALIC);
                                //resume;
                                mMusicService.play();
                                refreshProgressBar();
                                //mMusicService.setCurrentPosition(clickedPos);
                            }
                        } else {
                            TextView titleView = (TextView)v.findViewById(R.id.playlist_item_title);
                            titleView.setTypeface(null, Typeface.BOLD_ITALIC);
                            mMusicService.playPlaylistFrom(clickedPos);
                        }
                    }
                });

                contentLayout.addView(item1.getView());
                musicCursor.moveToNext();
                ++pos;
            }
        }

        musicCursor.close();


        //mMusicService.loadCurrPlaylist();
    }

    Playlist playlist = new Playlist("new playlist");

    @Override
    protected void onStart() {
        super.onStart();
        Intent playIntent = new Intent(this, MusicService.class);
        bindService(playIntent, mConnection, Context.BIND_AUTO_CREATE);
        //startService(playIntent);
        //mMusicService.loadCurrPlaylist(playlist);
    }


    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MusicService.MusicServiceBinder binder = (MusicService.MusicServiceBinder) service;
            mMusicService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    private MusicService mMusicService;
    boolean mBound = false;

    private ProgressBar mProgressBar;
    private TextView mCurrDuration;
    private TextView mTotalDuration;
    private TextView mCurrTitle;
    private TextView mCurrArtist;

    private void bindLayout() {
        mProgressBar = (SeekBar)findViewById(R.id.music_progress_bar);
        //disable user control
        mProgressBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        mCurrDuration = (TextView)findViewById(R.id.label_current_time);
        mTotalDuration = (TextView)findViewById(R.id.label_total_time);
        mCurrTitle = (TextView) findViewById(R.id.label_title);
        mCurrArtist = (TextView) findViewById(R.id.label_artist);
    }

    final int DEFAULT_PROGRESS_BAR_REFRESH_PERIOD_MSEC = 200;
    final int DEFAULT_PROGRESS_BAR_REFRESH_TIMES = 200;

    private Handler mHandler;
    public void refreshProgressBar() {
        long totalDuration = mMusicService.getDuration();
        mProgressBar.setMax((int)totalDuration);
        mTotalDuration.setText(MediaPlayerUtils.millSecondsToTime(totalDuration));
        mHandler.postDelayed(mUpdateTimeTask, DEFAULT_PROGRESS_BAR_REFRESH_PERIOD_MSEC);
    }

    private Runnable mUpdateTimeTask = new Runnable() {
        @Override
        public void run() {
            long totalDuration = mMusicService.getDuration();
            long currDuration = mMusicService.getCurrentDuration();

            mCurrDuration.setText(MediaPlayerUtils.millSecondsToTime(currDuration));
            //mTotalDuration.setText(MediaPlayerUtils.millSecondsToTime(totalDuration));

            mProgressBar.setProgress((int)currDuration);

            //frequency of refreshing progress bar depends on total duration of the audio.
            //therefore, if audio is extremely long, refreshing rate will be low.
            int updateFreq = Math.max(DEFAULT_PROGRESS_BAR_REFRESH_PERIOD_MSEC,
                    (int) totalDuration / DEFAULT_PROGRESS_BAR_REFRESH_TIMES);
            mHandler.postDelayed(this, updateFreq);
        }
    };


    //private List<PlaylistItem> mPlaylist;

}
