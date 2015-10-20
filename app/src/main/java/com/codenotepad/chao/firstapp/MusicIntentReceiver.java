package com.codenotepad.chao.firstapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MusicIntentReceiver extends BroadcastReceiver {
    public MusicIntentReceiver() {
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d("Global Receiver", "get action : " + action);
        if (intent.getAction().equals(
                android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
            //TODO:
            // signal your service to stop playback
            // (via an Intent, for instance)
            Log.d("MusicIntentReceiver", "becoming noisy, pause playback");
        }
    }
}
