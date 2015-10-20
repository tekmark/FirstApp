package com.codenotepad.chao.firstapp.mediaplayer;

/**
 * Created by chao on 10/18/15.
 */
public class MusicFile {
    final static public String UNKNOWN_MUSIC="Unknown Music";
    final static public String UNKNOWN_ARTIST="Unknown Artist";
    final static public String UNKNOWN_ALBUM="Unknown Album";
    final static public String UNKNOWN_DURATION = "0:00";
    final static public String INVALID_PATH = "";
    final static public int INVALID_ID = -1;

    final static public int AUDIO_ID = 0;                      //0
    final static public int TITLE = 1;                         //1
    final static public int ARTIST = 2;                        //2
    final static public int ID = 3;                            //3
    final static public int PATH = 4;                          //4
    final static public int DURATION = 5;                      //5
    final static public int ALBUM = 6;                         //6

    private int id;
    private int audioId;
    private String title;
    private String artist;
    private String album;
    private String duration;
    private String path;

    public MusicFile() {
        title = UNKNOWN_MUSIC;
        artist = UNKNOWN_ARTIST;
        album = UNKNOWN_ALBUM;
        path = INVALID_PATH;
        duration = UNKNOWN_DURATION;
        id = INVALID_ID;
        audioId = INVALID_ID;
    }

    public MusicFile(MusicFile musicFile) {
        title = musicFile.title;
        artist = musicFile.artist;
        album = musicFile.album;
        path = musicFile.path;
        duration = musicFile.path;
        id = musicFile.id;
        audioId = musicFile.audioId;
    }

    public void set(int pos, String s) {
        switch (pos) {
            case AUDIO_ID:
                setAudioId(Integer.parseInt(s));
            case ID:
                setId(Integer.parseInt(s));
            case TITLE:
                setTitle(s);
            case ARTIST:
                setArtist(s);
            case ALBUM:
                setAlbum(s);
            case PATH:
                setPath(s);
            case DURATION:
                setDuration(s);
        }
    }

    public String get(int pos) {
        switch (pos) {
            case AUDIO_ID:
                return Integer.toString(getAudioId());
            case ID:
                return Integer.toString(getId());
            case TITLE:
                return getTitle();
            case ARTIST:
                return getArtist();
            case ALBUM:
                return getAlbum();
            case PATH:
                return getPath();
            case DURATION:
                return getDuration();
            default:
                return "";
        }
    }

    public int getAudioId() {
        return audioId;
    }

    public int getId() {
        return id;
    }

    public String getAlbum() {
        return album;
    }

    public String getArtist() {
        return artist;
    }

    public String getDuration() {
        return duration;
    }

    public String getPath() {
        return path;
    }

    public String getTitle() {
        return title;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setAudioId(int audioId) {
        this.audioId = audioId;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
