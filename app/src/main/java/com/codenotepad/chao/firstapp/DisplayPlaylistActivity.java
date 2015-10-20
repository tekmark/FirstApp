package com.codenotepad.chao.firstapp;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.codenotepad.chao.firstapp.mediaplayer.MediaPlayerController;
import com.codenotepad.chao.firstapp.mediaplayer.MediaPlayerUtils;
import com.codenotepad.chao.firstapp.mediaplayer.MusicFile;
import com.codenotepad.chao.firstapp.mediaplayer.PlaylistItem;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;


public class DisplayPlaylistActivity extends AppCompatActivity {

    final static private String LOG_TAG = "DisplayPlaylistActivity";
    final int DEFAULT_PROGRESS_BAR_REFRESH_PERIOD_MSEC = 200;
    final int DEFAULT_PROGRESS_BAR_REFRESH_TIMES = 200;

    private Handler mHandler = new Handler();        //handler for refresh progress bar periodically

    private int mPlaylistId = 71;

    private MusicService mMusicService;
    boolean mBound = false;

//    private ProgressBar mProgressBar;
//    private TextView mCurrDuration;
//    private TextView mTotalDuration;
//    private TextView mCurrTitle;
//    private TextView mCurrArtist;

    private MediaPlayerController mController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_playlist);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mController = new MediaPlayerController(this);
        //mController.enableProgressBar(true);
        mController.enableSeek(false);
        //bind layout
        bindLayout();
        mItemList = new ArrayList<>();
        currentOutstandingItemPos = -1;

    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent playIntent = new Intent(this, MusicService.class);
        startService(playIntent);
        bindService(playIntent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //register local receiver, in order to get informed from music service.
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("my-event"));
    }

    @Override
    protected void onPause() {
        Log.d(LOG_TAG, "onPause() is called");
        //unregister receiver if on pause.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
            Log.d(LOG_TAG, "Service disconnected");
        }
        //clear item views;
        clearItemViews();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    //define local service connection for bound service.
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            //cast the IBinder and get LocalService instance
            MusicService.MusicServiceBinder binder = (MusicService.MusicServiceBinder) service;
            mMusicService = binder.getService();
//            mMusicService.loadCurrPlaylist(playlist);
            Log.d(LOG_TAG, "Service connected");
            mMusicService.loadPlaylist(mPlaylistId);

            //bind controller to service
            mController.bindToMusicService(mMusicService);
            //mController.sync();

            //place items on screen;
            createItemsViews(mMusicService.getMusicFiles());
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    //TODO: this can be optimized by dynamically loading/adding/creating items on scroll listener.
    private void createItemsViews(List<MusicFile> list) {
        LinearLayout contentLayout = (LinearLayout)findViewById(R.id.playlist);
        ListIterator<MusicFile> it = list.listIterator();
        while (it.hasNext()) {
            int pos = it.nextIndex();
            MusicFile musicFile = it.next();
            PlaylistItem item = PlaylistItem.create(this);
            mItemList.add(item);
            //item1.setMediaPlayer(mPlayer);
            item.setArtist(musicFile.getArtist());
            item.setTitle(musicFile.getTitle());
            int duration = Integer.parseInt(musicFile.getDuration());
            item.setDuration(MediaPlayerUtils.millSecondsToTime(duration));
            item.setPosition(pos);
            item.getInfoView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //get clicked item's position in list.
                    TextView positionView = (TextView) v.findViewById(R.id.playlist_item_position);
                    int clickedPos = Integer.parseInt(positionView.getText().toString());
                    //check if clicked item is playing, if true, pause, else play it.
                    int currentPos = mMusicService.getCurrentPosition();
                    Log.d("PlayListItem", "clicked pos : " + clickedPos + " current position " + currentPos);

                    if (clickedPos == currentPos) {
                        if (mMusicService.isPlaying()) {
                            //TODO: remove refreshProgressBar callback if paused
                            //mHandler.removeCallbacks(mUpdateTimeTask);
                            mController.stopRefreshProgressBar();
                            mMusicService.pause();
                        } else {
                            mMusicService.play();
                        }
                    } else {
                        mMusicService.playPlaylistFrom(clickedPos);
                    }
                }
            });
            contentLayout.addView(item.getView());
        }
    }

    private void clearItemViews() {
        mItemList.clear();
        LinearLayout contentLayout = (LinearLayout)findViewById(R.id.playlist);
        contentLayout.removeAllViews();
    }

    /**
     * @param
     */
    private void bindLayout() {
        /*mProgressBar = (SeekBar)findViewById(R.id.music_progress_bar);
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
        mCurrArtist = (TextView) findViewById(R.id.label_artist);*/
    }
/*
    public void refreshProgressBar() {
        long totalDuration = mMusicService.getDuration();
        mProgressBar.setMax((int)totalDuration);
        mTotalDuration.setText(MediaPlayerUtils.millSecondsToTime(totalDuration));
        mHandler.postDelayed(mUpdateTimeTask, DEFAULT_PROGRESS_BAR_REFRESH_PERIOD_MSEC);
    }

    private Runnable mUpdateTimeTask = new Runnable() {
        @Override
        public void run() {
            //long totalDuration = mMusicService.getDuration();
            if (mMusicService.isPlaying()) {
                long currDuration = mMusicService.getCurrentDuration();
                //set current duration
                mCurrDuration.setText(MediaPlayerUtils.millSecondsToTime(currDuration));
                //mTotalDuration.setText(MediaPlayerUtils.millSecondsToTime(totalDuration));
                //update progress in the progress bar.
                mProgressBar.setProgress((int) currDuration);
                //frequency of refreshing progress bar depends on total duration of the audio.
                //therefore, if audio is extremely long, refreshing rate will be low.
//              int updateFreq = Math.max(DEFAULT_PROGRESS_BAR_REFRESH_PERIOD_MSEC,
//                    (int) totalDuration / DEFAULT_PROGRESS_BAR_REFRESH_TIMES);
                int updateFreq = DEFAULT_PROGRESS_BAR_REFRESH_PERIOD_MSEC;
                mHandler.postDelayed(this, updateFreq);
            }
        }
    };
*/
    private List<PlaylistItem> mItemList;

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //get PLAY_ACTION,  postion,
            //1. get Title and Artist,
            //2. set playing song bold_italic, reset finished song typeface.
            //3. set bottom info bar info.
            String action = intent.getAction();
            Log.d("RECEIVER", "get action " + action);

            // Extract data included in the Intent
            int message = intent.getIntExtra("message", -1);
            setItemOutstanding(message);
            Log.d("receiver", "Got message: " + message);

            //mController.refreshProgressBar();
            mController.sync();
            //get CHECK_SOURCE
        }
    };

    private int currentOutstandingItemPos; // = -1 if no outstanding item.
    private void setItemOutstanding(int pos) {
        if (pos >= 0 && pos < mItemList.size() && pos != currentOutstandingItemPos) {
            //reset current outstanding pos;
            resetItemOutstanding(currentOutstandingItemPos);
            //make pos outstanding;
            PlaylistItem item = mItemList.get(pos);
            TextView titleView = (TextView)item.getInfoView().findViewById(R.id.playlist_item_title);
            titleView.setTypeface(null, Typeface.BOLD_ITALIC);
            TextView artistView = (TextView)item.getInfoView().findViewById(R.id.playlist_item_artist);
            String title = titleView.getText().toString();
            String artist = artistView.getText().toString();
//            mCurrTitle.setText(title);
//            mCurrArtist.setText(artist);
            //update currentOutstandingItemPos
            currentOutstandingItemPos = pos;
        }
    }
    private void resetItemOutstanding(int pos) {
        if (pos >= 0 && pos < mItemList.size()) {
            PlaylistItem item = mItemList.get(pos);
            TextView titleView = (TextView) item.getInfoView().findViewById(R.id.playlist_item_title);
            titleView.setTypeface(null, Typeface.NORMAL);
        } else {
            //Log.w();
        }
    }

    public void showCurrentPlaying(View v) {
        Intent intent = new Intent(this, DisplayNowPlaying.class);
        startActivity(intent);
        Log.d("MusicPlayer", "show current playing");
        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_no_anim);
    }
}
