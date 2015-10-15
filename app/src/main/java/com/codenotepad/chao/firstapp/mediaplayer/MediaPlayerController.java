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
    final int PROGRESS_BAR_UPDATE_PERIOD_MSEC = 500;

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

    private boolean shuffle = false;
    private boolean repeat = false;
    private boolean repeatAll = false;

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
        currPlaylist.setRepeat();

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
                mMediaPlayer.reset();
                if (prepareNextSong()) {
                    Log.d("MediaPlayer", "move to next song in Playlist");
                    if (mPlayPause.isSelected()) {
                        play();
                    }
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
                mMediaPlayer.reset();
                if (prepareNextSong()) {
                    play();
                } else {
                    //Change pause icon to ready_to_play;
                    if (mPlayPause.isSelected()) {
                        Log.d("MediaPlayer", "set to ready to play state");
                        mPlayPause.setSelected(false);
                    }
                }
            }
        });
        mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Log.e("MediaPlayer", "onError() is called");
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


        mRepeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRepeat.isSelected()) {  //playing,
                    mRepeat.setSelected(false);

                } else {
                    mRepeat.setSelected(true);

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
                int currPosition = (int) (seekBar.getProgress() * totalDuration / 100);

                mMediaPlayer.seekTo(currPosition);
                updateProgressBar();
            }
        });
    }

    public void play() {
        Log.d("MediaPlayer", "play() is called");
        updateProgressBar();
        mMediaPlayer.start();
    }

    public void pause() {
        Log.d("MediaPlayer", "pause() is called");
        mMediaPlayer.pause();
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
            try {
                Log.d("MediaPlayer", "Media file path : " + songPath);
                mMediaPlayer.setDataSource(songPath);
                mMediaPlayer.prepare();
                return true;
            } catch (IOException e) {
                Log.e("MediaPlayer", "Cannot find audio file: " + songPath);
                return false;
            }
        } else {
            Log.d("MediaPlayer", "Reach the end of playlist");
            return false;
        }
    }

    private boolean preparePrevSong() {
        Log.d("MediaPlayer", "preparePrevSong() is called");
        if (currPlaylist.hasPrevious()) {
            String songPath = currPlaylist.previous();
            try {
                Log.d("MediaPlayer", "Media file path : " + songPath);
                mMediaPlayer.setDataSource(songPath);
                mMediaPlayer.prepare();
                return true;
            } catch (IOException e) {
                Log.e("MediaPlayer", "Cannot find audio file: " + songPath);
                return false;
            }
        } else {
            Log.d("MediaPlayer", "Reach the end of playlist");
            return false;
        }
    }


    public void stop() {
        mMediaPlayer.stop();
    }

    public void hide() {

    }

    public void show() {

    }

    public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, PROGRESS_BAR_UPDATE_PERIOD_MSEC);
    }

    private Runnable mUpdateTimeTask = new Runnable() {
        @Override
        public void run() {
            long totalDuration = mMediaPlayer.getDuration();
            long currDuration = mMediaPlayer.getCurrentPosition();
            int progress = (int)(currDuration * 100 / totalDuration);
            Log.d("MediaPlayer", "curr: " + currDuration + " total: " + totalDuration + " progress " + progress + "%");
            mCurrDuration.setText(MediaPlayerUtils.millSecondsToTime(currDuration));
            mTotalDuration.setText(MediaPlayerUtils.millSecondsToTime(totalDuration));
            mProgressBar.setProgress(progress);
            mHandler.postDelayed(this, PROGRESS_BAR_UPDATE_PERIOD_MSEC);
        }
    };

}
