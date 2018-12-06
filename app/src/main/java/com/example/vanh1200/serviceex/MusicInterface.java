package com.example.vanh1200.serviceex;

public interface MusicInterface {
    void changeSong(int position);

    void create(int index);

    void start();

    void pause();

    void seek(int duration);

    void release();

    boolean isPlaying();

    int getDuration();

    int getCurrentPosition();

    String getName();
}
