package com.codenotepad.chao.firstapp.mediaplayer;

import android.app.Activity;
import android.os.Handler;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.codenotepad.chao.firstapp.R;

/**
 * Created by chao on 10/17/15.
 */
public class MusicProgressBar {
    private ProgressBar mProgressBar;
    private TextView mCurrDuration;
    private TextView mTotalDuration;

    //final int DEFAULT_PROGRESS_BAR_REFRESH_PERIOD_MSEC = 200;
    //final int DEFAULT_PROGRESS_BAR_REFRESH_TIMES = 500;

    private Handler mHandler = new Handler();

    public MusicProgressBar(Activity activity) {
        bindLayout(activity);
    }

    private void bindLayout(Activity activity) {
        mProgressBar = (SeekBar) activity.findViewById(R.id.music_progress_bar);
        mCurrDuration = (TextView) activity.findViewById(R.id.label_current_time);
        mTotalDuration = (TextView) activity.findViewById(R.id.label_total_time);
    }

    public void setCurrentDuration() {

    }

    public void setTotalDuration() {

    }
//    public void refreshProgressBar() {
//        mHandler.postDelayed(mUpdateTimeTask, DEFAULT_PROGRESS_BAR_REFRESH_PERIOD_MSEC);
//    }
//
//    private Runnable mUpdateTimeTask = new Runnable() {
//        @Override
//        public void run() {
//            /*long totalDuration = mMediaPlayer.getDuration();
//            long currDuration = mMediaPlayer.getCurrentPosition();
//
//            mCurrDuration.setText(MediaPlayerUtils.millSecondsToTime(currDuration));
//            mTotalDuration.setText(MediaPlayerUtils.millSecondsToTime(totalDuration));
//
//            mProgressBar.setProgress((int)currDuration);
//
//            frequency of refreshing progress bar depends on total duration of the audio.
//            therefore, if audio is extremely long, refreshing rate will be low.
//            int updateFreq = Math.max(DEFAULT_PROGRESS_BAR_REFRESH_PERIOD_MSEC,
//                    (int) totalDuration / DEFAULT_PROGRESS_BAR_REFRESH_TIMES);
//            mHandler.postDelayed(this, updateFreq);*/
//        }
//    };
}
