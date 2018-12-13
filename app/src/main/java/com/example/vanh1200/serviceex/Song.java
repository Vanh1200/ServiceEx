package com.example.vanh1200.serviceex;

public class Song {
    private String mTitle;
    private String mSinger;
    private long mDuration;
    private String mThumbnail;

    public long getAlbumId() {
        return mAlbumId;
    }

    public void setAlbumId(long albumId) {
        mAlbumId = albumId;
    }

    private long mAlbumId;

    public String getData() {
        return mData;
    }

    public void setData(String data) {
        mData = data;
    }

    private String mData;

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getSinger() {
        return mSinger;
    }

    public void setSinger(String singer) {
        mSinger = singer;
    }

    public long getDuration() {
        return mDuration;
    }

    public void setDuration(long duration) {
        mDuration = duration;
    }

    public String getThumbnail() {
        return mThumbnail;
    }

    public void setThumbnail(String thumbnail) {
        mThumbnail = thumbnail;
    }
}
