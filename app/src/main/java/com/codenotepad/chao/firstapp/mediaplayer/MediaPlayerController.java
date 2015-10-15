package com.codenotepad.chao.firstapp.mediaplayer;

import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.codenotepad.chao.firstapp.R;

import java.io.IOException;


/**
 * Created by chao on 10/12/15.
 */
public class MediaPlayerController {

    final int PLAY_PREVIOUS_THRESHOLD_SEC = 10;
    final int DEFAULT_PROGRESS_BAR_REFRESH_PERIOD_MSEC = 200;
    final int DEFAULT_PROGRESS_BAR_REFRESH_TIMES = 500;


    private MediaPlayer mMediaPlayer;
    private ImageButton mPlayPause;
    private ImageButton mEnd;
    private ImageButton mSkipToStart;
    private ImageButton mShuffle;
    private ImageButton mRepeat;

    private SeekBar mProgressBar;
    private TextView mCurrDuration;
    private TextView mTotalDuration;

    private Playlist currPlaylist;

    //private int currSongIndex;
    //private int defaultSongIndex;

    private boolean isShuffle = false;
    private boolean isRepeat = false;
    private boolean isRepeatAll = false;

    private Handler mHandler = new Handler();

    private String song1 = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getPath() + "/Taylor Swift - Shake It Off.mp3";
    private String song2 = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getPath() + "/All_By_Myself-Celine_Dion.mp3";

    public MediaPlayerController (Activity activity) {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        bindLayout(activity);
        setListeners();

        currPlaylist = new Playlist("default playlist");

        loadCurrPlaylist();

        prepareNextSong();
    }

    public boolean isPlaylistEmpty() {
        return false;
    }


    private void bindLayout(Activity activity) {
        mPlayPause = (ImageButton) activity.findViewById(R.id.media_control_play_pause);
        mEnd = (ImageButton) activity.findViewById(R.id.media_control_end);
        mSkipToStart = (ImageButton) activity.findViewById(R.id.media_control_skip_to_start);
        mShuffle = (ImageButton) activity.findViewById(R.id.media_control_shuffle);
        mRepeat = (ImageButton) activity.findViewById(R.id.media_control_repeat);
        mProgressBar = (SeekBar) activity.findViewById(R.id.media_control_progress_bar);
        mCurrDuration = (TextView) activity.findViewById(R.id.label_current_time);
        mTotalDuration = (TextView) activity.findViewById(R.id.label_total_time);
    }

    private void setListeners() {

        mPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPlayPause.isSelected()) {  //playing,
                    mPlayPause.setSelected(false);
                    pause();
                    Log.d("mediaPlayer", "pressed: " + mPlayPause.isPressed() + " enabled: " + mPlayPause.isEnabled()
                            + " selected : " + mPlayPause.isSelected());
                } else {
                    mPlayPause.setSelected(true);
                    play();
                    Log.d("mediaPlayer", "pressed: " + mPlayPause.isPressed() + " enabled: " + mPlayPause.isEnabled()
                            + " selected : " + mPlayPause.isSelected());
                }
            }
        });

        mEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reset();
                if (prepareNextSong()) {
                    Log.d("MediaPlayer", "move to next song in Playlist");
                    if (mPlayPause.isSelected()) {      //if playing;
                        play();
                    } else {        //if paused;

                    }
                } else {
                    resetPlaylist();
                }
            }
        });

        mSkipToStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int played_sec = mMediaPlayer.getCurrentPosition() / 1000;
                Log.d("MediaPlayer", "Played Sec : " + played_sec);
                if (played_sec < PLAY_PREVIOUS_THRESHOLD_SEC) {
                    mMediaPlayer.reset();
                    if (preparePrevSong()) {
                        Log.d("MediaPlayer", "move to previous song in Playlist");
                        if (mPlayPause.isSelected()) {
                            play();
                        }
                    } else {
                        Log.d("MediaPlayer", "Fail to find previous song in Playlist");
                    }
                } else {
                    Log.d("MediaPlayer", "Skip to Start");
                    mMediaPlayer.seekTo(0);
                }
            }
        });

        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Log.d("MediaPlayer", "OnCompleteListener is called");
                reset();
                if (prepareNextSong()) {
                    play();
                } else {
                    resetPlaylist();
                }
            }
        });
        mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Log.e("MediaPlayer", "onError() is called, ERROR TYPE is " + what +
                        " ERROR CODE is " + extra);
                return false;
            }
        });

        mShuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mShuffle.isSelected()) {  //playing,
                    mShuffle.setSelected(false);

                } else {
                    mShuffle.setSelected(true);

                }
            }
        });

        //TODO: add repeat current one;
        mRepeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRepeat.isSelected() && isRepeatAll && currPlaylist.isRepeat()) {
                    mRepeat.setSelected(false);
                    isRepeatAll = false;
                    currPlaylist.resetRepeat();
                    Log.d("MediaPlayer", "disable repeat playlist when finished");
                } else if (!mRepeat.isSelected() && !isRepeatAll && !currPlaylist.isRepeat()){
                    mRepeat.setSelected(true);
                    isRepeatAll = true;
                    currPlaylist.setRepeat();
                    Log.d("MediaPlayer", "enable repeat playlist when finished");
                } else {
                    Log.e("MediaPlayer", "Repeat statues conflicts, reset to false");
                    mRepeat.setSelected(false);
                    isRepeatAll = false;
                    currPlaylist.resetRepeat();
                }
            }
        });

        mProgressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mHandler.removeCallbacks(mUpdateTimeTask);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mHandler.removeCallbacks(mUpdateTimeTask);
                long totalDuration = mMediaPlayer.getDuration();
                //int currPosition = (int) (seekBar.getProgress() * totalDuration / 100);

                mMediaPlayer.seekTo(seekBar.getProgress());
                play();
                refreshProgressBar();
            }
        });
    }

    public void play() {
        Log.d("MediaPlayer", "play() is called");
        refreshProgressBar();
        mMediaPlayer.start();
    }

    public void pause() {
        Log.d("MediaPlayer", "pause() is called");
        mHandler.removeCallbacks(mUpdateTimeTask);
        mMediaPlayer.pause();
    }

    public void reset() {
        mMediaPlayer.reset();
        mHandler.removeCallbacks(mUpdateTimeTask);
    }
    public void skipToStart() {
        Log.d("MediaPlayer", "skipToStart() is called");
        mMediaPlayer.seekTo(0);
    }

    public boolean isPlaying() {

        return mMediaPlayer.isPlaying();
    }


    public void playSingleSong(String path) {
        /*if (mMediaPlayer.isPlaying()) {

            Log.d("MediaPlayer", "playing");
        }
        try {
            //mMediaPlayer.setDataSource(context, mUri);
            mMediaPlayer.setDataSource(path);
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        } catch (IOException e) {
            Log.e("MediaPlayer", "no such song");
        }*/
    }


    private void loadCurrPlaylist() {
        currPlaylist.addSong(song1);
        currPlaylist.addSong(song2);
        currPlaylist.resetCur();
    }

    private boolean prepareNextSong() {
        Log.d("MediaPlayer", "prepareNextSong() is called");
        if (currPlaylist.hasNext()) {
            String songPath = currPlaylist.next();
            return prepare(songPath);
        } else {
            Log.d("MediaPlayer", "Reach the end of playlist");
            return false;
        }
    }

    private boolean preparePrevSong() {
        Log.d("MediaPlayer", "preparePrevSong() is called");
        if (currPlaylist.hasPrevious()) {
            String songPath = currPlaylist.previous();
            return prepare(songPath);
        } else {
            Log.d("MediaPlayer", "Reach the end of playlist");
            return false;
        }
    }
    private boolean prepare(String songPath) {
        try {
            Log.d("MediaPlayer", "Media file path : " + songPath);
            mMediaPlayer.setDataSource(songPath);
            mMediaPlayer.prepare();
            mProgressBar.setProgress(0);
            mProgressBar.setMax(mMediaPlayer.getDuration());
            long totalDuration = mMediaPlayer.getDuration();
            long currDuration = mMediaPlayer.getCurrentPosition();
            mCurrDuration.setText(MediaPlayerUtils.millSecondsToTime(currDuration));
            mTotalDuration.setText(MediaPlayerUtils.millSecondsToTime(totalDuration));
            return true;
        } catch (IOException e) {
            Log.e("MediaPlayer", "Cannot find audio file: " + songPath);
            return false;
        }
    }


    private void resetPlaylist() {
        Log.d("MediaPlayer", "reset cursor in playlist");
        currPlaylist.resetCur();
        if (mPlayPause.isSelected()) {          //if playing state
            mPlayPause.setSelected(false);
        }
        if (prepareNextSong()) {
            Log.d("MediaPlayer", "move to next song in Playlist");
        } else {
            Log.e("MediaPlayer", "cannot find next song after reset");
        }
    }

    public void updatePlaylist() {

    }

    public void stop() {
        mMediaPlayer.stop();
    }

    public void hide() {

    }

    public void show() {

    }

    public void refreshProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, DEFAULT_PROGRESS_BAR_REFRESH_PERIOD_MSEC);
    }

    private Runnable mUpdateTimeTask = new Runnable() {
        @Override
        public void run() {
            long totalDuration = mMediaPlayer.getDuration();
            long currDuration = mMediaPlayer.getCurrentPosition();

            mCurrDuration.setText(MediaPlayerUtils.millSecondsToTime(currDuration));
            mTotalDuration.setText(MediaPlayerUtils.millSecondsToTime(totalDuration));

            mProgressBar.setProgress((int)currDuration);

            //frequency of refreshing progress bar depends on total duration of the audio.
            //therefore, if audio is extremely long, refreshing rate will be low.
            int updateFreq = Math.max(DEFAULT_PROGRESS_BAR_REFRESH_PERIOD_MSEC,
                    (int) totalDuration / DEFAULT_PROGRESS_BAR_REFRESH_TIMES);
            mHandler.postDelayed(this, updateFreq);
        }
    };

}
