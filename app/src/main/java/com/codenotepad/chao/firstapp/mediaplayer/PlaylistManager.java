package com.codenotepad.chao.firstapp.mediaplayer;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.codenotepad.chao.firstapp.R;

/**
 * Created by chao on 10/16/15.
 */
public class PlaylistManager {

    final static private String TAG = "PlaylistManager";

    //Note: must follow the order of MusicFile field.
    final static private String[] proj = {
            MediaStore.Audio.Playlists.Members.AUDIO_ID,            //0
            MediaStore.Audio.Playlists.Members.TITLE,               //1
            MediaStore.Audio.Playlists.Members.ARTIST,              //2
            MediaStore.Audio.Playlists.Members._ID,                 //3
            MediaStore.Audio.Playlists.Members.DATA,                //4
            MediaStore.Audio.Playlists.Members.DURATION,            //5
            MediaStore.Audio.Playlists.Members.ALBUM                //6
    };

//    public PlaylistManager() {
//        playlist = new Playlist();
//        currPlaylistID = -1;
//    }

    public static Playlist getPlaylistFromMediaStore(Context context, int playlistId) {

        Playlist playlist = new Playlist();
        //currPlaylistID = playlistId;

        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId);
        Cursor musicCursor = context.getContentResolver().query(uri, proj, null, null, null);

        Log.d(TAG, "PlaylistID: " + playlistId + ", # of rows: "  + musicCursor.getCount());

        String columnNames[] = musicCursor.getColumnNames();
        int indexes[] = new int[columnNames.length];
        Log.d(TAG, "Column Names: " + columnNames.toString());
        for (int i = 0; i < proj.length; ++i) {
            indexes[i] = musicCursor.getColumnIndex(columnNames[i]);
        }

        if (musicCursor.moveToFirst()) {
            while (!musicCursor.isLast()) {
                MusicFile musicFile = new MusicFile();
                String duration = musicCursor.getString(indexes[MusicFile.DURATION]);
                String title = musicCursor.getString(indexes[MusicFile.TITLE]);
                String artist = musicCursor.getString(indexes[MusicFile.ARTIST]);
                String album = musicCursor.getString(indexes[MusicFile.ALBUM]);
                String path = musicCursor.getString(indexes[MusicFile.PATH]);
                String id = musicCursor.getString(indexes[MusicFile.ID]);
                String audioId = musicCursor.getString(indexes[MusicFile.AUDIO_ID]);


                musicFile.setAudioId(Integer.parseInt(audioId));
                musicFile.setId(Integer.parseInt(id));
                musicFile.setDuration(duration);
                musicFile.setAlbum(album);
                musicFile.setArtist(artist);
                musicFile.setTitle(title);
                musicFile.setPath(path);

                //order: audioId, title, artist, album, duration
                Log.d(TAG, audioId + "|" + title + "|" + artist + "|" + album + "|" + duration);
                Log.d(TAG, "Path: " + path);

                playlist.addMusicFile(musicFile);
                musicCursor.moveToNext();
            }
//            musicCursor.close();
//            return true;
        }
        musicCursor.close();
        return playlist;

    }

//    public int getCurrPlaylistSize() {
//        return playlist.size();
//    }
//
//    public int getCurrPlaylistID() {
//        return currPlaylistID;
//    }


//                LinearLayout contentLayout = (LinearLayout)findViewById(R.id.playlist);

//                PlaylistItem item1 = PlaylistItem.create(this);
//                mItemList.add(item1);
//                item1.setMediaPlayer(mPlayer);
//                item1.setArtist(artist);
//                item1.setTitle(title);
//                item1.setDuration(MediaPlayerUtils.millSecondsToTime(Integer.parseInt(duration)));
//                item1.setPosition(pos);
//                item1.songPath = path;
//                playlist.addSong(path);
//                item1.getInfoView().setTag(pos);
//                item1.getInfoView().setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        int postion = item1.getPosition();
//                        Log.d("PlayListItem", "play " + item1.getTitle() + " path " + item1.songPath);
//                        int tag = (int)v.getTag();
//                        int clickedPos = tag;
//                        check if clicked item is playing, if true, pause, else play it.
//                        TextView positionView = (TextView) v.findViewById(R.id.playlist_item_position);
//                        int clickedPos = Integer.parseInt(positionView.getText().toString());
//                        int currentPos = mMusicService.getCurrentPosition();
//                        Log.d("PlayListItem", "clicked pos : " + clickedPos + " current position " + currentPos);
//
//                        if (clickedPos == currentPos) {
//                            if (mMusicService.isPlaying()) {
//                                TextView titleView = (TextView)v.findViewById(R.id.playlist_item_title);
//                                titleView.setTypeface(null, Typeface.NORMAL);
//                                TODO: remove refreshProgressBar callback if paused
//                                mHandler.removeCallbacks(mUpdateTimeTask);
//                                mMusicService.pause();
//                            } else {
//                                TextView titleView = (TextView)v.findViewById(R.id.playlist_item_title);
//                                titleView.setTypeface(null, Typeface.BOLD_ITALIC);
//                                resume
//                                refreshProgressBar();
//                                mMusicService.play();
//                                mMusicService.setCurrentPosition(clickedPos);
//                            }
//                        } else {
//                            TextView titleView = (TextView)v.findViewById(R.id.playlist_item_title);
//                            titleView.setTypeface(null, Typeface.BOLD_ITALIC);
//                            mMusicService.playPlaylistFrom(clickedPos);
//                        }
//                    }
//                });
//
//                contentLayout.addView(item1.getView());
//                musicCursor.moveToNext();
//                ++pos;
//            }
//        }
//
//        musicCursor.close();
//
//        return false;
//    }

//    private int currPlaylistID;

//    private Playlist playlist;
}
