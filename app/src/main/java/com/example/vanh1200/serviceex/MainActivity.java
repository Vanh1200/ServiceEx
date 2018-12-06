package com.example.vanh1200.serviceex;

import android.Manifest;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements SongAdapter.OnItemClicked, SeekBar.OnSeekBarChangeListener, View.OnClickListener, MusicService.OnSyncActivityListener {
    private static final String TAG = "MainActivity";
    private static final int REQUEST_PERMISSION = 1;
    private static final long DELAY_MESSAGE_TIME = 1000;
    private String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private ArrayList<Song> mSongs;
    private ContentResolver mResolver;
    private MusicPlayer mMusicPlayer;
    private Handler mHandler;
    private RecyclerView mRecyclerSong;
    private SongAdapter mSongAdapter;
    private MusicService mService;
    private ImageView mImagePrev;
    private ImageView mImageNext;
    private ImageView mImagePlayPause;
    private TextView mTextTitle;
    private CircleImageView mCircleImageSong;
    private SeekBar mSeekBarSong;
    private TextView mTextCurrentDuration;
    private TextView mTextDuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(checkPermission(permissions)){
            initViews();
        }
    }
    
    private boolean checkPermission(String[] permissions){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            for(String permission : permissions){
                if(checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED){
                    requestPermissions(permissions, REQUEST_PERMISSION);
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(checkPermission(permissions)){
            initViews();
        }
        else{
            finish();
        }
    }

    private void initRecycler() {
        mSongAdapter = new SongAdapter(mSongs);
        mSongAdapter.setListener(this);
        mRecyclerSong.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerSong.setAdapter(mSongAdapter);
    }

    private void initViews() {
        mResolver = getContentResolver();
        mSongs = getSongs();
        mRecyclerSong = findViewById(R.id.recycler_song);
        mImageNext = findViewById(R.id.image_next);
        mImagePrev = findViewById(R.id.image_prev);
        mImagePlayPause = findViewById(R.id.image_play_pause);
        mCircleImageSong = findViewById(R.id.circle_image_song_mini);
        mTextTitle = findViewById(R.id.text_title);
        mSeekBarSong = findViewById(R.id.seek_bar_mini);
        mTextCurrentDuration = findViewById(R.id.text_current_duration);
        mTextDuration = findViewById(R.id.text_duration);

        mSeekBarSong.setOnSeekBarChangeListener(this);
        mImagePlayPause.setOnClickListener(this);
        mImagePrev.setOnClickListener(this);
        mImageNext.setOnClickListener(this);

        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                mTextCurrentDuration.setText(Utils.convertToTime(mService.getMusicPlayer().getCurrentPosition()));
                mSeekBarSong.setMax(mService.getMusicPlayer().getDuration());
                mSeekBarSong.setProgress(mService.getMusicPlayer().getCurrentPosition());
                sendMessageDelayed(new Message(), DELAY_MESSAGE_TIME);
            }
        };

        initRecycler();
        initService();
    }

    private void initService() {
        Intent intent = new Intent(this, MusicService.class);
        if(mService == null){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                startForegroundService(intent);
            }else{
                startService(intent);
            }
        }
        bindService(intent, mConnection, BIND_AUTO_CREATE);
    }

    public ArrayList<Song> getSongs(){
        ArrayList<Song> songs = new ArrayList<>();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = mResolver.query(uri, null, null,
                null, null);
        if(cursor == null)
            return songs;

        cursor.moveToFirst();
        int indexTitle = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
        int indexSinger = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
        int indexDuration = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
        int indexData = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
        int indexAlbumId = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);

        while (!cursor.isAfterLast()){
            Song song = new Song();
            song.setTitle(cursor.getString(indexTitle));
            song.setSinger(cursor.getString(indexSinger));
            song.setDuration(cursor.getLong(indexDuration));
            song.setData(cursor.getString(indexData));
            song.setAlbumId(cursor.getLong(indexAlbumId));
            songs.add(song);
            cursor.moveToNext();
        }

        for (int i = 0; i < songs.size(); i++) {
            cursor = mResolver.query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                    null,
                    MediaStore.Audio.Albums._ID + " = ?",
                    new String[]{Long.toString(songs.get(i).getAlbumId())},
                    null);
            if (cursor.moveToFirst()) {
                songs.get(i).setThumbnail(cursor.getString(cursor.getColumnIndex(
                        MediaStore.Audio.Albums.ALBUM_ART)));
            }
        }
        cursor.close();
        return songs;
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder musicBinder = (MusicService.MusicBinder) service;
            mService = musicBinder.getService();
            mService.getMusicPlayer().setSongs(mSongs);
            Log.d(TAG, "onServiceConnected: ");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            unbindService(this);
        }
    };

    @Override
    public void onItemClickListener(int position) {
        mService.setListener(this);
        mService.getMusicPlayer().create(position);
        mService.getMusicPlayer().start();
        mService.updateNotification();
        updateMiniPlayer(position);
        mHandler.sendMessageDelayed(new Message(), DELAY_MESSAGE_TIME);

    }

    private void updateMiniPlayer(int position) {
        Glide.with(this)
                .load(mSongs.get(position).getThumbnail())
                .into(mCircleImageSong);
        mTextTitle.setText(mSongs.get(position).getTitle());
        mTextDuration.setText(Utils.convertToTime(mSongs.get(position).getDuration()));
        if(mService.getMusicPlayer().isPlaying()){
            mImagePlayPause.setImageResource(R.drawable.ic_pause_black_24dp);
        }else{
            mImagePlayPause.setImageResource(R.drawable.ic_play_arrow_black_24dp);
        }
//        mTextDuration.setText(Utils.convertToTime(mService.getMusicPlayer().get));
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if(fromUser){
            mService.getMusicPlayer().seek(progress);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.image_prev:
                mService.getMusicPlayer().changeSong(MusicPlayer.PREV);
                updateMiniPlayer(mService.getMusicPlayer().getCurrentIndex());
                mService.updateNotification();
                break;
            case R.id.image_play_pause:
                if(mService.getMusicPlayer().isPlaying()){
                    mService.getMusicPlayer().pause();
                    mImagePlayPause.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                }else{
                    mService.getMusicPlayer().start();
                    mImagePlayPause.setImageResource(R.drawable.ic_pause_black_24dp);
                }
                mService.updateNotification();
                break;

            case R.id.image_next:
                mService.getMusicPlayer().changeSong(MusicPlayer.NEXT);
                updateMiniPlayer(mService.getMusicPlayer().getCurrentIndex());
                mService.updateNotification();
            default:
                break;
        }
    }

    @Override
    public void onSyncActivity(int position, boolean isPlaying) {
        updateMiniPlayer(position);
    }
}
