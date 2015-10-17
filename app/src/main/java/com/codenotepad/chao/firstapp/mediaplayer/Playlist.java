package com.codenotepad.chao.firstapp.mediaplayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by chao on 10/14/15.
 */
public class Playlist {
    final static private String DEFAULT_PLAYLIST_NAME = "New Playlsit";
    public Playlist() {
        playlistName = DEFAULT_PLAYLIST_NAME;
        songs = new ArrayList<>();
        it = songs.listIterator();
        repeat = false;
    }

    public Playlist(String name) {
        playlistName = name;
        songs = new ArrayList<>();
        it = songs.listIterator();
        repeat = false;
    }

    public Playlist(Playlist playlist) {
        songs = new ArrayList<>(playlist.songs);
        playlistName = playlist.playlistName;
        it = songs.listIterator();
        repeat = false;
    }

    public String getPlaylistName() {
        return playlistName;
    }
    public void setPlaylistName (String newPlaylistName) {
        playlistName = newPlaylistName;
    }

    //public void checkFile() {}

    public void addSong(String song) {
        songs.add(song);
    }

    public String getSong(int index) {
        return songs.get(index);
    }

    public boolean hasNext() {
        if (it.hasNext()) {
            return true;
        } else if (repeat) {
            resetCur();
            return it.hasNext();
        } else {
            return false;
        }
    }

    public boolean hasPrevious() {
        if (it.hasPrevious()) {
            return true;
        } else if (repeat) {
            it = songs.listIterator(songs.size());
            return it.hasPrevious();
        } else {
            return false;
        }
    }

    //these two functions must be called after hasNext() and hasPrevious() respectively.
    public int nextIndex() {
        return it.nextIndex();
    }
    public int previousIndex() {
        return it.previousIndex();
    }

    public String next() {
        return it.next();
    }

    public String previous() {
        return it.previous();
    }

    public boolean isRepeat() {
        return repeat;
    }

    public void setRepeat() {
        repeat = true;
    }

    public void resetRepeat() {
        repeat = false;
    }

    public void shuffle() {
        Collections.shuffle(songs);
        resetCur();
    }

    public void sort() {

    }

    public void move(int i) {
        it = songs.listIterator(i);
    }

    public void resetCur() {
        it = songs.listIterator();
    }

    private List<String> songs;
    private String playlistName;
    private ListIterator<String> it;

    private boolean repeat;
}
