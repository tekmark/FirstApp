package com.codenotepad.chao.firstapp.mediaplayer;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FilenameFilter;
import java.net.ContentHandler;
import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * Created by chao on 10/12/15.
 */
public class SongsManager {
    public Context mContext;

    public SongsManager (Context context) {
        mContext = context;
    }


    public void scan() {
        String scanDir = null;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            scanDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath();
            Log.d("SongsManager", scanDir);
        } else {
            Log.e("SongsManager", "sdcard is not available");
        }
        MediaScannerConnection.scanFile(mContext, new String[]{scanDir}, new String[]{"audio/*"}, new MediaScannerConnection.OnScanCompletedListener() {
            @Override
            public void onScanCompleted(String path, Uri uri) {
                Log.d("SongsManger", "Scan Completed");
            }
        });
    }

    public ArrayList<String> scanAllSongsOnDevice()
    {
        Log.d("Songs Manager", "scan all songs on disk");
        ArrayList<String> songsList;
        ContentResolver musicResolver = mContext.getContentResolver();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String col[] ={android.provider.MediaStore.Audio.Media._ID};
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);

        if (musicCursor != null && musicCursor.moveToFirst())
        {
            // clear  list to prevent duplicates
            songsList = new ArrayList<>();

            //get columns
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            int isMusicColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.IS_MUSIC);
            int duration = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.DURATION);

            //add songs to list
            do
            {
                String filePath = musicCursor.getString(musicCursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                // check if the file is a music and the type is supported
                if (musicCursor.getInt(isMusicColumn) != 0 && filePath != null && musicCursor.getInt(duration) > 0)
                {
                    int thisId = musicCursor.getInt(idColumn);
                    String thisTitle = musicCursor.getString(titleColumn);
                    String thisArtist = musicCursor.getString(artistColumn);
                    /*Song song = new Song();
                    song.setId(thisId);
                    if(!thisArtist.equals("<unknown>"))
                    {
                        song.setArtist(thisArtist);
                        song.setTitle(thisTitle);
                    }
                    else
                    {
                        song.setArtist("");
                        song.setTitle("");
                    }
                    song.setSongPath(filePath);
                    File file = new File(filePath);
                    song.setFileName(file.getName().substring(0, (file.getName().length() - 4)));*/
                    Log.d("SongsManager", "ID: " + thisId + " Title: " + thisTitle + " Artist: " + thisArtist);
                    songsList.add(thisTitle);
                }
            }
            while (musicCursor.moveToNext());
        } else { // if we don't have any media in the folder that we selected set NO MEDIA
            //addNoSongs();
            songsList = new ArrayList<String>();
        }

        musicCursor.close();

        if(songsList.size() == 0)
        {
            //addNoSongs();
        }
/*
        Collections.sort(songsList, new Comparator<Song>() {
            @Override
            public int compare(Song song, Song song2) {
                int compare = song.getTitle().compareTo(song2.getTitle());
                return ((compare == 0) ? song.getArtist().compareTo(
                        song2.getArtist()) : compare);
            }
        });
*/
        return songsList;
    }
    /*
    class FileExtensionFilter implements FilenameFilter {
        public boolean accept(File dir, String name) {
            for (String s : supportedExtension) {
                if (name.endsWith(s)) {
                    return true;
                }
            }
            return false;
        }
    }
    */
}
