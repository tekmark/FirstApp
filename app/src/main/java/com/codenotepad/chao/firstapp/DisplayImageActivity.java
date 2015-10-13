package com.codenotepad.chao.firstapp;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class DisplayImageActivity extends AppCompatActivity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = false;

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

    private static final int PLAY_MODE = 0;
    private static final int REVIEW_MODE = 0;

    private int mode;

    //private View mContentView;
    private ImageView mImageView;
    private View mControlsView;
    private boolean mVisible;
    private Button mPlayButton;

    private GestureDetectorCompat mDetector;

    private List<File> images;

    private SensorManager mSensorManager;
    private Sensor mLight;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_display_image);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mImageView = (ImageView) findViewById(R.id.fullscreen_content);

        // Set up the user interaction to manually show or hide the system UI.
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        //Set up the user interaction to manually swipe left or right to change to next or previous
        //image;
        mDetector = new GestureDetectorCompat(this, new MyGestureListener());
        mImageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mDetector.onTouchEvent(event)) {
                    return false;
                }
                return true;
            }
        });

        //TODO: onResume and onPause, release sensors.
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        if(mLight != null){
            Log.d("Sensor", "TYPE_LIGHT Available");
            mSensorManager.registerListener(
                    mLightSensorListner,
                    mLight,
                    SensorManager.SENSOR_DELAY_NORMAL);

        }else{
            Log.d("Sensor", "TYPE_LIGHT NOT Available");
        }

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        mPlayButton = (Button) findViewById(R.id.button_play);
        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button b = (Button) v;
                if (mode == PLAY_MODE && b.getText().equals("Play")) {
                    b.setText("Exit");
                    mode = REVIEW_MODE;
                    mPlayHander.postDelayed(mPlayRunnable, 0);
                } else if (mode == REVIEW_MODE && b.getText().equals("Exit")) {
                    b.setText("Play");
                    mode = PLAY_MODE;
                    mPlayHander.removeCallbacks(mPlayRunnable);
                } else {
                    Log.e("ModeError", "Unrecognized Mode");
                }
            }
        });

        mode = REVIEW_MODE;
        images = readAlbumFiles();
        //goShuffleMode();
        dispalyImages();


        WindowManager.LayoutParams layout = getWindow().getAttributes();
        layout.screenBrightness = 0;
        getWindow().setAttributes(layout);
    }

    private SensorEventListener mLightSensorListner = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            Log.d("Sensor", "");
            if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
                Log.d("Sensor", "Light value: " + event.values[0]);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    private Bitmap mPrevImageBitmap;
    private Bitmap mCurrImageBitmap;
    private Bitmap mNextImageBitmap;

    private int currImageIndex = -1;
    private int prevImageIndex = -1;
    private int nextImageIndex = -1;

    //return filenames in Album;
    private List<File> readAlbumFiles() {
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM);

        if (storageDir.exists() && storageDir.isDirectory()) {
            FilenameFilter imageFilter = new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    String lowercaseName = filename.toLowerCase();
                    //TODO: more types should be supported.
                    if (lowercaseName.endsWith(".jpg")) {
                        return true;
                    } else {
                        return false;
                    }
                }
            };
            images = Arrays.asList(storageDir.listFiles(imageFilter));
            for (File file : images) {
                Log.d("Filename : ", file.getAbsolutePath());
            }
        }
        return images;
    }

    //shuffle filenames;
    private void goShuffleMode () {
        //TODO: update album before shuffle.
        Collections.shuffle(images);
        for (File f : images) {
            Log.d("After Shuffle", f.getAbsolutePath());
        }
    }

    public void dispalyImages() {

        if (currImageIndex < 0) {
            Log.d("Display", "NO current image, load first image in folder");
            for (int i = 0; i < images.size(); ++i) {
                File image = images.get(i);
                if (image.exists() && image.isFile() && !image.isHidden()) {
                    currImageIndex = i;
                    break;
                }
            }
        }

        //get next and prev image index;
        if (currImageIndex >= 0 && currImageIndex < images.size()) {
            Log.d("DisplayImage", "Current Image is :" + images.get(currImageIndex).getAbsolutePath());
            for (int i = currImageIndex + 1; i < images.size(); ++i) {
                File image = images.get(i);
                if (image.exists() && image.isFile() && !image.isHidden()) {
                    Log.d("DispalyImage", "Next Image is :" + image.getAbsolutePath());
                    nextImageIndex = i;
                    break;
                }
            }
            for (int j = currImageIndex - 1; j >= 0; --j) {
                File image = images.get(j);
                if (image.exists() && image.isFile() && !image.isHidden()) {
                    Log.d("DispalyImage", "Prev Image is :" + image.getAbsolutePath());
                    prevImageIndex = j;
                    break;
                }
            }
        }

        mCurrImageBitmap = BitmapFactory.decodeFile(images.get(currImageIndex).getAbsolutePath());
        mImageView.setImageBitmap(mCurrImageBitmap);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        this.mDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final String DEBUG_TAG = "Gestures";

        @Override
        public boolean onDown(MotionEvent event) {
            Log.d(DEBUG_TAG,"onDown: " + event.toString());
            return true;
        }

        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2,
                               float velocityX, float velocityY) {
            Log.d(DEBUG_TAG, "onFling: " + e1.toString() + e2.toString());
            boolean result = false;
            try {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    Log.i("Gesture", "horizon gesture");
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            Log.i("Gesture", "swipe right, display prev image");
                            prevImage();
                        } else {
                            Log.i("Gesture", "swipe left, display next image");
                            nextImage();
                        }
                    }
                    return true;
                }
            } catch (Exception exception) {
                Log.e("Gesture", "Excetpiton");
                exception.printStackTrace();
            }
            return result;
        }
    }


    public void preloadNextImage () {

    }

    public void nextImage () {
        prevImageIndex = currImageIndex;
        currImageIndex = nextImageIndex;
        Log.d("DispalyImage", prevImageIndex + " " + currImageIndex + " " + nextImageIndex);
        dispalyImages();
    }
    public void prevImage () {
        nextImageIndex = currImageIndex;
        currImageIndex = prevImageIndex;
        Log.d("DispalyImage", prevImageIndex + " " + currImageIndex + " " + nextImageIndex);
        dispalyImages();
    }

    private Handler mPlayHander = new Handler();
    private Runnable mPlayRunnable = new Runnable() {
        @Override
        public void run() {
            nextImage();
            mPlayHander.postDelayed(this, 5000);
        }
    };


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

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            Log.d("Play", "Play");
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
            mImageView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
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
        mImageView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
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
}
