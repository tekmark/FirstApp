package com.codenotepad.chao.firstapp.mediaplayer;

import java.util.concurrent.TimeUnit;

/**
 * Created by chao on 10/14/15.
 */
public class MediaPlayerUtils {
    static public String millSecondsToTime(long milliseconds) {
        int hour = (int) TimeUnit.MILLISECONDS.toHours(milliseconds);
        milliseconds = milliseconds - TimeUnit.HOURS.toMillis(hour);
        int min = (int) TimeUnit.MILLISECONDS.toMinutes(milliseconds);
        milliseconds = milliseconds - TimeUnit.MINUTES.toMillis(min);
        int sec = (int) TimeUnit.MILLISECONDS.toSeconds(milliseconds);

        String finalTime = "";
        String secondField = "";
        if (hour > 0) {
            finalTime = hour + ":";
        }

        if (sec < 10) {
            secondField = "0" + sec;
        } else {
            secondField = "" + sec;
        }

        finalTime = finalTime + min + ":" + secondField;
        return finalTime;
    }
    static public int getProgressPercentage (long currDuration, long totalDuration) {
        return 0;
    }
    static public long getCurrDuration(int progress, long totalDuration) {
        return 0;
    }
}
