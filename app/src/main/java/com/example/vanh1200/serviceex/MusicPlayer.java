package com.example.vanh1200.serviceex;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.IntDef;

import java.util.List;

public class MusicPlayer implements MediaPlayer.OnCompletionListener, IMusicController {
    private static final int DEFAULT_DURATION = 0;
    private static final boolean DEFAULT_IS_PLAYING = false;
    private static final int DEFAULT_POSITION = 0;
    public static final int NEXT = 1;
    public static final int PREV = -1;
    private static final int FIRST_SONG_POSITION = 0;
    private static final int OFFSET_LIST_SIZE = 1;
    private Context mContext;
    private List<Song> mSongs;
    private int mCurrentPosition;
    private MediaPlayer mMediaPlayer;

    public MusicPlayer(Context context) {
        mContext = context;
    }


    public List<Song> getSongs() {
        return mSongs;
    }

    public void setSongs(List<Song> songs) {
        mSongs = songs;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        changeSong(NEXT);
    }

    @IntDef({NEXT, PREV})
    @interface SongPosition {
    }

    @Override
    public void changeSong(@SongPosition int position) {
        mCurrentPosition += position;
        if (mCurrentPosition >= mSongs.size()) {
            mCurrentPosition = FIRST_SONG_POSITION;
        } else if (mCurrentPosition < FIRST_SONG_POSITION) {
            mCurrentPosition = mSongs.size() - OFFSET_LIST_SIZE;
        }
        this.create(mCurrentPosition);
        this.start();
    }

    @Override
    public void create(int index) {
        this.mCurrentPosition = index;
        if (mMediaPlayer != null)
            release();
        Uri uri = Uri.parse(mSongs.get(mCurrentPosition).getData());
        mMediaPlayer = MediaPlayer.create(mContext, uri);
        if (mMediaPlayer != null) {
            mMediaPlayer.setOnCompletionListener(this);
        }
    }

    @Override
    public void start() {
        if (mMediaPlayer != null)
            mMediaPlayer.start();
    }

    @Override
    public void pause() {
        if (mMediaPlayer != null)
            mMediaPlayer.pause();
    }

    @Override
    public void seek(int duration) {
        if (mMediaPlayer != null) {
            mMediaPlayer.seekTo(duration);
        }
    }

    @Override
    public void release() {
        if (mMediaPlayer != null)
            mMediaPlayer.release();
    }

    @Override
    public boolean isPlaying() {
        return mMediaPlayer != null ? mMediaPlayer.isPlaying() : DEFAULT_IS_PLAYING;
    }

    @Override
    public int getDuration() {
        return mMediaPlayer != null ? mMediaPlayer.getDuration() : DEFAULT_DURATION;
    }

    @Override
    public int getCurrentPosition() {
        return mMediaPlayer != null ? mMediaPlayer.getCurrentPosition() : DEFAULT_POSITION;
    }

    public int getCurrentIndex() {
        return mCurrentPosition;
    }

    @Override
    public String getName() {
        return null;
    }

}
