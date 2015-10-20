package com.codenotepad.chao.firstapp.mediaplayer;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.codenotepad.chao.firstapp.MusicService;
import com.codenotepad.chao.firstapp.R;


/**
 * Created by chao on 10/12/15.
 */
public class MediaPlayerController {

    final int PLAY_PREVIOUS_THRESHOLD_SEC = 10;
    final int DEFAULT_PROGRESS_BAR_REFRESH_PERIOD_MSEC = 200;
    final int DEFAULT_PROGRESS_BAR_REFRESH_TIMES = 500;

    final static private String LOG_TAG = "MediaPlayerController";

    private ImageButton mPlayPause;
    private ImageButton mEnd;
    private ImageButton mSkipToStart;
    private ImageButton mShuffle;
    private ImageButton mRepeat;

    private SeekBar mProgressBar;
    private TextView mCurrDuration;
    private TextView mTotalDuration;

    private boolean bound;

    private TextView mTitle;
    private TextView mArtist;
    //private int currSongIndex;
    //private int defaultSongIndex;

    //private boolean isShuffle = false;
    //private boolean isRepeat = false;
    //private boolean isRepeatAll = false;

    private MusicService mService;

    private Handler mHandler = new Handler();

    public MediaPlayerController (Activity activity) {
        bound = false;
        bindLayout(activity);
    }

    //bind controller's layout to service, and set listeners
    public boolean bindToMusicService(MusicService musicService) {
        if (musicService == null) {
            bound = false;
            Log.w(LOG_TAG, "Try to bind to a invalid music service");
            return false;
        } else {
            mService = musicService;
            bound = true;
            setListeners();
            Log.d(LOG_TAG, "Music service is bound");
            //syncStatus();
            return true;
        }
    }

    public boolean isBound() {
        return bound;
    }

    public void sync() {
        Log.d(LOG_TAG, "sync() is called");
        if (!bound) {
            Log.w(LOG_TAG, "Sync failed. Cannot sync unbound controller");
            return;
        }
        if (mPlayPause != null) {
            if (mService.isPlaying()) {
                mPlayPause.setSelected(true);
            } else {
                mPlayPause.setSelected(false);
            }
        }
        if (mProgressBar != null) {
            refreshProgressBar();
        }
        if (mArtist != null) {
            String artist = mService.getCurrentArtist();
            mArtist.setText(artist);
        }
        if (mTitle != null) {
            String title = mService.getCurrentTitle();
            mTitle.setText(title);
        }
        if (mTotalDuration != null) {
            long total = mService.getDuration();
            mTotalDuration.setText(MediaPlayerUtils.millSecondsToTime(total));
        }
    }

    public void enableProgressBar(boolean enable) {
        if (mProgressBar != null) {
            mProgressBar.setEnabled(enable);
            if (enable) {
                Log.d(LOG_TAG, "Progress bar is enabled");
            } else {
                Log.d(LOG_TAG, "Progress bar is disabled");
            }
        } else {
            Log.w(LOG_TAG, "Progress bar may be missing in layout");
        }
    }

    public void enableSeek(boolean enable) {
        if (mProgressBar != null) {
            enableProgressBar(true);
            if (enable) {
                mProgressBar.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return false;
                    }
                });
                Log.d(LOG_TAG, "Progress bar is seekable");
            } else {
                mProgressBar.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return true;
                    }
                });
                Log.d(LOG_TAG, "Progress bar is not seekable");
            }
        } else {
            Log.w(LOG_TAG, "Progress bar is not bound or missing in layout");
        }
    }
    private void bindLayout(Activity activity) {
        mPlayPause = (ImageButton) activity.findViewById(R.id.button_play_pause);
        if (mPlayPause != null) {
            Log.d(LOG_TAG, "Button Play/Pause is found");
        } else {
            Log.w(LOG_TAG, "Button Play/Pause isn't found");
        }
        mEnd = (ImageButton) activity.findViewById(R.id.button_end);
        if (mEnd != null) {
            Log.d(LOG_TAG, "Button End is found");
        } else {
            Log.w(LOG_TAG, "Button End isn't found");
        }
        mSkipToStart = (ImageButton) activity.findViewById(R.id.button_skip_to_start);
        if (mSkipToStart != null) {
            Log.d(LOG_TAG, "Button Skip_to_Start is found");
        } else {
            Log.w(LOG_TAG, "Button Skip_to_Start isn't found");
        }
        //mShuffle = (ImageButton) activity.findViewById(R.id.button_shuffle);
        //mRepeat = (ImageButton) activity.findViewById(R.id.media_control_repeat);
        mProgressBar = (SeekBar) activity.findViewById(R.id.seek_bar);
        if (mProgressBar != null) {
            Log.d(LOG_TAG, "Progress Bar is found");
        } else {
            Log.w(LOG_TAG, "Progress Bar isn't found");
        }
        mCurrDuration = (TextView) activity.findViewById(R.id.label_current_time);
        if (mCurrDuration != null) {
            Log.d(LOG_TAG, "Label Current_Duration is found");
        } else {
            Log.w(LOG_TAG, "Label Current_Duration End isn't found");
        }
        mTotalDuration = (TextView) activity.findViewById(R.id.label_total_time);
        if (mCurrDuration != null) {
            Log.d(LOG_TAG, "Label Total_Duration is found");
        } else {
            Log.w(LOG_TAG, "Label Total_Duration isn't found");
        }
        mTitle = (TextView) activity.findViewById(R.id.label_title);
        if (mTitle != null) {
            Log.d(LOG_TAG, "Label Title is found");
        } else {
            Log.w(LOG_TAG, "Label Title isn't found");
        }
        mArtist = (TextView) activity.findViewById(R.id.label_artist);
        if (mArtist != null) {
            Log.d(LOG_TAG, "Label Artist is found");
        } else {
            Log.w(LOG_TAG, "Label Artist isn't found");
        }
    }

    private void setListeners() {
        if (mPlayPause != null) setPlayPauseButtonListner();
        if (mEnd != null) setEndButtonListener();
        if (mSkipToStart != null) setSkipToStartButtonListener();
        if (mShuffle != null) setShuffleButtonListener();
        if (mRepeat != null) setRepeatButtonListener();
        if (mProgressBar != null) setSeekBarListener();
    };
    private void setPlayPauseButtonListner() {
        mPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG, "PlayPause Button is clicked");
                if (mPlayPause.isSelected() && mService.isPlaying()) { //if image is pause (playing)
                    mPlayPause.setSelected(false);      //change image from pause to play
                    mService.pause();                   //pause
                    Log.d(LOG_TAG, "Button image: Pause(Ready_To_Pause) -> Play(Ready_to_Play)");
                } else if (!mPlayPause.isSelected() && !mService.isPlaying()) { //if paused
                    //NOTE: isPlaying() == false doesn't mean that MusicService is paused. MediaPlayer
                    //maybe not prepared. One situation is that next() is called, since currently is
                    //not playing, but asynPrepare() is not called.
                    mPlayPause.setSelected(true);
                    mService.play();
                    Log.d(LOG_TAG, "Button image: Play(Ready_to_Play) -> Pause(Ready_to_Pause)");
                } else {    //handle error here
                    mPlayPause.setSelected(mService.isPlaying());
                    Log.e(LOG_TAG, "PlayPause ButtonImageERROR, image has been hanged");
                }
            }
        });
    };
    private void setEndButtonListener() {
        mEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG, "End Button is clicked");
                mService.next();
            }
        });
    }
    private void setSkipToStartButtonListener() {
        mSkipToStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG, "SkipToStart Button is clicked");
                int played_sec = mService.getCurrentPosition() / 1000;
                if (played_sec < PLAY_PREVIOUS_THRESHOLD_SEC) {
//                    mMediaPlayer.reset();
//                    if (preparePrevSong()) {
//                        Log.d("MediaPlayer", "move to previous song in Playlist");
//                        if (mPlayPause.isSelected()) {
//                            play();
//                        }
//                    } else {
//                        Log.d("MediaPlayer", "Fail to find previous song in Playlist");
//                    }
                    mService.previous();
                } else {
                    Log.d(LOG_TAG, "Played Sec : " + played_sec + " Skip to Start of Current");
                    mService.skipToStart();
                }
            }
        });
    }
    private void setShuffleButtonListener() {
        mShuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG, "Shuffle button is clicked");
                if (mShuffle.isSelected()) {  //playing,
                    mShuffle.setSelected(false);
                } else {
                    mShuffle.setSelected(true);

                }
            }
        });
    }

    private void setRepeatButtonListener() {
        //TODO: add repeat current one;
        mRepeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG, "Repeat button is clicked");
//                if (mRepeat.isSelected() && isRepeatAll && currPlaylist.isRepeat()) {
//                    mRepeat.setSelected(false);
//                    isRepeatAll = false;
//                    currPlaylist.resetRepeat();
//                    Log.d("MediaPlayer", "disable repeat playlist when finished");
//                } else if (!mRepeat.isSelected() && !isRepeatAll && !currPlaylist.isRepeat()){
//                    mRepeat.setSelected(true);
//                    isRepeatAll = true;
//                    currPlaylist.setRepeat();
//                    Log.d("MediaPlayer", "enable repeat playlist when finished");
//                } else {
//                    Log.e("MediaPlayer", "Repeat statues conflicts, reset to false");
//                    mRepeat.setSelected(false);
//                    isRepeatAll = false;
//                    currPlaylist.resetRepeat();
//                }
            }
        });
    }

    private void setSeekBarListener() {
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
                long totalDuration = mService.getDuration();
                //int currPosition = (int) (seekBar.getProgress() * totalDuration / 100);
                mService.seekTo(seekBar.getProgress());
                //play();
                refreshProgressBar();
            }
        });
    }

    public void refreshProgressBar() {
        //TODO: update getProgress() in MusicService
        long total = mService.getDuration();
        mProgressBar.setMax((int)total);

        //mTotalDuration.setText(MediaPlayerUtils.millSecondsToTime(total));
        if (mTotalDuration != null) {
            mTotalDuration.setText(MediaPlayerUtils.millSecondsToTime(total));
        }
        mHandler.postDelayed(mUpdateTimeTask, DEFAULT_PROGRESS_BAR_REFRESH_PERIOD_MSEC);
    }

    public void stopRefreshProgressBar() {
        mHandler.removeCallbacks(mUpdateTimeTask);
    }

    private Runnable mUpdateTimeTask = new Runnable() {
        @Override
        public void run() {
            if (mService.isPlaying()) {
                long totalDuration = mService.getDuration();
                long currDuration = mService.getCurrentDuration();

                mCurrDuration.setText(MediaPlayerUtils.millSecondsToTime(currDuration));
                mTotalDuration.setText(MediaPlayerUtils.millSecondsToTime(totalDuration));

                mProgressBar.setProgress((int) currDuration);

                //frequency of refreshing progress bar depends on total duration of the audio.
                //therefore, if audio is extremely long, refreshing rate will be low.
                int updateFreq = Math.max(DEFAULT_PROGRESS_BAR_REFRESH_PERIOD_MSEC,
                        (int) totalDuration / DEFAULT_PROGRESS_BAR_REFRESH_TIMES);
                mHandler.postDelayed(this, updateFreq);
            } else {
                if (mPlayPause != null && !mPlayPause.isSelected()) {
                    mPlayPause.setSelected(false);
                }
                mHandler.postDelayed(this, DEFAULT_PROGRESS_BAR_REFRESH_PERIOD_MSEC);
            }
        }
    };
    /*
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
    }*/

    /*
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
    }*/

    /*
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
    */
}
