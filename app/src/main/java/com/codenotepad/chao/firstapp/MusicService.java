package com.codenotepad.chao.firstapp;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import com.codenotepad.chao.firstapp.mediaplayer.Playlist;

import java.io.IOException;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener,
        AudioManager.OnAudioFocusChangeListener {

    //private static final String ACTION_PLAY = "com.example.action.PLAY";
    private final MusicServiceBinder mBinder = new MusicServiceBinder();

    private Playlist currentPlaylist;
    private int currentPosition;

    private boolean isShuffle = false;
    private boolean isRepeat = false;
    private boolean isRepeatAll = false;

    public void initMediaPlayer() {
        mMediaPlayer = new MediaPlayer();                            // initialize MediaPlayer
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);  // set stream type to music
        //set wake lock
        mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        //set listeners
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setOnCompletionListener(this);

        //set currentPositison to -1;
        currentPosition = -1;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initMediaPlayer();
        currentPlaylist = new Playlist("default playlist");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        /*if (intent.getAction().equals(ACTION_PLAY)) {
            mMediaPlayer.prepareAsync(); // prepare async to not block main thread
        }*/

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (mMediaPlayer != null) mMediaPlayer.release();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return true;
    }


    private MediaPlayer mMediaPlayer = null;

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.d("MusicService", "OnCompleteListener is called");
        reset();
        if (prepareNextSong()) {
            //play();
        } else {
            resetPlaylist();
        }
    }
    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.d("MusicService", "OnPrepared() is called");
        play();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.e("MediaPlayer", "onError() is called, ERROR TYPE is " + what +
                " ERROR CODE is " + extra);
        return false;
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                // resume playback
                if (mMediaPlayer == null) initMediaPlayer();
                else if (!mMediaPlayer.isPlaying()) mMediaPlayer.start();
                mMediaPlayer.setVolume(1.0f, 1.0f);
                break;

            case AudioManager.AUDIOFOCUS_LOSS:
                // Lost focus for an unbounded amount of time: stop playback and release media player
                if (mMediaPlayer.isPlaying()) mMediaPlayer.stop();
                mMediaPlayer.release();
                mMediaPlayer = null;
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                if (mMediaPlayer.isPlaying()) mMediaPlayer.pause();
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                if (mMediaPlayer.isPlaying()) mMediaPlayer.setVolume(0.1f, 0.1f);
                break;
        }
    }

    public class MusicServiceBinder extends Binder {
        MusicService getService () {
            return MusicService.this;
        }
    }

    public void play() {
        Log.d("MediaPlayer", "play() is called");
        //refreshProgressBar();
        mMediaPlayer.start();
    }

    public boolean isPlaying() {
        return mMediaPlayer.isPlaying();
    }

    public void playPlaylistFrom(int pos) {
        mMediaPlayer.reset();
        currentPlaylist.move(pos);
        currentPosition = pos;
        prepareNextSong();
    }

    public void addToPlaylist(String path) {
        currentPlaylist.addSong(path);
    }

    public void pause() {
        Log.d("MediaPlayer", "pause() is called");
        //mHandler.removeCallbacks(mUpdateTimeTask);
        mMediaPlayer.pause();
    }

    public void reset() {
        mMediaPlayer.reset();
        //mHandler.removeCallbacks(mUpdateTimeTask);
    }
    public void skipToStart() {
        Log.d("MediaPlayer", "skipToStart() is called");
        mMediaPlayer.seekTo(0);
    }

    public int getProgress() {
        return 0;
    }

    public void loadCurrPlaylist(Playlist list) {
        currentPlaylist = new Playlist(list);
        currentPlaylist.resetCur();
        //prepareNextSong();
    }

    private boolean prepareNextSong() {
        Log.d("MediaPlayer", "prepareNextSong() is called");
        if (currentPlaylist.hasNext()) {
            String songPath = currentPlaylist.next();
            return prepare(songPath);
        } else {
            Log.d("MediaPlayer", "Reach the end of playlist");
            return false;
        }
    }

    private boolean preparePrevSong() {
        Log.d("MediaPlayer", "preparePrevSong() is called");
        if (currentPlaylist.hasPrevious()) {
            String songPath = currentPlaylist.previous();
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
            mMediaPlayer.prepareAsync();
            //mProgressBar.setProgress(0);
            //mProgressBar.setMax(mMediaPlayer.getDuration());
            //long totalDuration = mMediaPlayer.getDuration();
            //long currDuration = mMediaPlayer.getCurrentPosition();
            //mCurrDuration.setText(MediaPlayerUtils.millSecondsToTime(currDuration));
            //mTotalDuration.setText(MediaPlayerUtils.millSecondsToTime(totalDuration));
            return true;
        } catch (IOException e) {
            Log.e("MediaPlayer", "Cannot find audio file: " + songPath);
            return false;
        }
    }


    private void resetPlaylist() {
        Log.d("MediaPlayer", "reset cursor in playlist");
        currentPlaylist.resetCur();
        if (prepareNextSong()) {
            Log.d("MediaPlayer", "move to next song in Playlist");
        } else {
            Log.e("MediaPlayer", "cannot find next song after reset");
        }
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    public long getDuration() {
        return mMediaPlayer.getDuration();
    }
    public long getCurrentDuration() {
        return mMediaPlayer.getCurrentPosition();
    }
}
