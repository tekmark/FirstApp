package com.codenotepad.chao.firstapp;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextClock;

import java.util.Calendar;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class DisplayClockActivity extends AppCompatActivity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;

    private TextClock mClockView;
    private TextClock mDateView;
    private View mControlsView;
    private boolean mVisible;

    //for alarm
    private AlarmManager mAlarmManager;
    private static DisplayClockActivity inst;
    private PendingIntent pendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_display_clock);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //get screen dpi
        Log.d("Screen", "dpi" + getResources().getDisplayMetrics().densityDpi);
        Log.d("Screen", "width px:" + getResources().getDisplayMetrics().widthPixels);
        Log.d("Screen", "width px:" + getResources().getDisplayMetrics().heightPixels);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mClockView = (TextClock)findViewById(R.id.fullscreen_clock);
        mDateView = (TextClock)findViewById(R.id.date);

        // Set up the user interaction to manually show or hide the system UI.
        mClockView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        Typeface tf = Typeface.createFromAsset(this.getAssets(), "fonts/LiquidCrystal/LiquidCrystal-Bold.otf");
        //Typeface tf = Typeface.createFromAsset(this.getAssets(), "fonts/BlackBall/Black Ball.ttf");
        mClockView.setTypeface(tf);
        mDateView.setTypeface(tf);
        resizeClockText();
        mClockView.measure(View.MeasureSpec.EXACTLY, View.MeasureSpec.EXACTLY);
        Log.d("Measure", "width :" + mClockView.getWidth());


        hideSecond();
        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);

        mAlarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        //setAlarm();
        //blink();
    }

    private void setAlarm() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE) + 1;
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        Log.d("Alarm", "hour : " + hour + " minute " + minute);
        Intent mIntent = new Intent(DisplayClockActivity.this, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(DisplayClockActivity.this, 0, mIntent, 0);
        mAlarmManager.set(AlarmManager.RTC, calendar.getTimeInMillis(), pendingIntent);

        //Intent mIntent = new Intent();
        //pendingIntent = PendingIntent.getBroadcast(this, 0, mIntent, 0);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button.
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void hideSecond() {
        //mClockView.setFormat12Hour("hh: mm a");
    }

    //TODO: a new resizing algorithm.
    private void resizeClockText() {
        int screen_width_px = getResources().getDisplayMetrics().widthPixels;
        float width_max = screen_width_px * 9 / 10;
        float curr_width = mClockView.getPaint().measureText("00:00");
        float curr_text_size = mClockView.getTextSize();
        Log.d("Size", "screen px: " + screen_width_px + " width " + width_max + " curr : " + curr_width + "curr_text : " + curr_text_size);
        float size = width_max * curr_text_size / curr_width;
        Log.d("Size", "new size: " + size);
        mClockView.setTextSize(size/2);
    }

    public void blink() {
        Animation anim = new AlphaAnimation(1.0f, 0.0f);
        anim.setDuration(500);
        //anim.setStartOffset(1000);
        //anim.setRepeatCount(Animation.INFINITE);
        anim.setRepeatCount(10);
        mClockView.startAnimation(anim);
        //mClockView.clearAnimation();
    }

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mClockView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mClockView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };

    private final Handler mHideHandler = new Handler();
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    //for alarm receiver
    public static DisplayClockActivity instance() {
        return inst;
    }

    @Override
    public void onStart() {
        super.onStart();
        inst = this;
    }


    public void play(View view) {
        Log.d("MediaPlayer", "play");
    }
}
