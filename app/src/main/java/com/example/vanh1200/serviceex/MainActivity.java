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
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SongAdapter.OnItemClicked,
        SeekBar.OnSeekBarChangeListener, View.OnClickListener, MusicService.OnSyncActivityListener {
    private static final String TAG = "MainActivity";
    private static final int REQUEST_PERMISSION = 1;
    private static final long DELAY_MESSAGE_TIME = 1000;
    private static final long TIME_LOOP = 5000;
    private static final float DEGREE_MIN = 0;
    private static final float DEGREE_MAX = 360;
    private static final float PIVOTE_X = 0.5f;
    private static final float PIVOTE_Y = 0.5f;
    private static final float FROM_X_DELTA = 0;
    private static final float FROM_Y_DELTA = 500;
    private static final float TO_X_DELTA = 0;
    private static final float TO_Y_DELTA = 0;
    private String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private List<Song> mSongs;
    private ContentResolver mResolver;
    private MusicPlayer mMusicPlayer;
    private Handler mHandler;
    private RecyclerView mRecyclerSongs;
    private SongAdapter mSongAdapter;
    private MusicService mService;
    private ImageView mImagePrev;
    private ImageView mImageNext;
    private ImageView mImagePlayPause;
    private TextView mTextTitle;
    private ImageView mImageSong;
    private SeekBar mSeekBarSong;
    private TextView mTextCurrentDuration;
    private TextView mTextDuration;
    private View mViewMiniPlay;
    private final String CHARACTER_EQUAL = "=";
    private final String CHARACTER_QUESTION = "?";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (checkPermission(permissions)) {
            initViews();
        }
    }

    private boolean checkPermission(String[] permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String permission : permissions) {
                if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(permissions, REQUEST_PERMISSION);
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions
            , @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (checkPermission(permissions)) {
            initViews();
        } else {
            finish();
        }
    }

    private void initRecycler() {
        mSongAdapter = new SongAdapter(mSongs);
        mSongAdapter.setListener(this);
        mRecyclerSongs.setAdapter(mSongAdapter);
    }

    private void initViews() {
        mResolver = getContentResolver();
        mSongs = getSongs();
        mRecyclerSongs = findViewById(R.id.recycler_songs);
        mImageNext = findViewById(R.id.image_next);
        mImagePrev = findViewById(R.id.image_prev);
        mImagePlayPause = findViewById(R.id.image_play_pause);
        mImageSong = findViewById(R.id.image_song_mini);
        mTextTitle = findViewById(R.id.text_title);
        mSeekBarSong = findViewById(R.id.seek_bar_mini);
        mTextCurrentDuration = findViewById(R.id.text_current_duration);
        mTextDuration = findViewById(R.id.text_duration);
        mViewMiniPlay = findViewById(R.id.mini_play);
        registerEvents();
        initHandler();
        initRecycler();
        initService();
    }

    private void initHandler() {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                mTextCurrentDuration.setText(Utils.convertToTime(mService.getMusicPlayer().getCurrentPosition()));
                mSeekBarSong.setMax(mService.getMusicPlayer().getDuration());
                mSeekBarSong.setProgress(mService.getMusicPlayer().getCurrentPosition());
                sendMessageDelayed(new Message(), DELAY_MESSAGE_TIME);
            }
        };
    }

    private void registerEvents() {
        mSeekBarSong.setOnSeekBarChangeListener(this);
        mImagePlayPause.setOnClickListener(this);
        mImagePrev.setOnClickListener(this);
        mImageNext.setOnClickListener(this);
    }

    private void initService() {
        Intent intent = new Intent(this, MusicService.class);
        if (mService == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent);
            } else {
                startService(intent);
            }
        }
        bindService(intent, mConnection, BIND_AUTO_CREATE);
    }

    public List<Song> getSongs() {
        List<Song> songs = new ArrayList<>();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = mResolver.query(uri, null, null,
                null, null);
        if (cursor == null)
            return songs;

        cursor.moveToFirst();
        int indexTitle = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
        int indexSinger = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
        int indexDuration = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
        int indexData = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
        int indexAlbumId = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);

        while (!cursor.isAfterLast()) {
            Song song = new Song();
            song.setTitle(cursor.getString(indexTitle));
            song.setSinger(cursor.getString(indexSinger));
            song.setDuration(cursor.getLong(indexDuration));
            song.setData(cursor.getString(indexData));
            song.setAlbumId(cursor.getLong(indexAlbumId));
            songs.add(song);
            cursor.moveToNext();
        }

        setThumbnail(songs);
        cursor.close();
        return songs;
    }

    private void setThumbnail(List<Song> songs) {
        for (int i = 0; i < songs.size(); i++) {
            Cursor cursor = mResolver.query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                    null,
                    mergeString(MediaStore.Audio.Albums._ID, CHARACTER_EQUAL, CHARACTER_QUESTION),
                    new String[]{Long.toString(songs.get(i).getAlbumId())},
                    null);
            if (cursor.moveToFirst()) {
                songs.get(i).setThumbnail(cursor.getString(cursor.getColumnIndex(
                        MediaStore.Audio.Albums.ALBUM_ART)));
            }
            cursor.close();
        }
    }

    private String mergeString(String... input) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String s : input) {
            stringBuilder.append(s);
        }
        return stringBuilder.toString();
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder musicBinder = (MusicService.MusicBinder) service;
            mService = musicBinder.getService();
            mService.getMusicPlayer().setSongs(mSongs);
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
        if (mViewMiniPlay.getVisibility() == View.GONE) {
            mViewMiniPlay.startAnimation(getTranlateAnimation());
        }

        mImageSong.startAnimation(getInfiniteRotation());
        Glide.with(this)
                .load(mSongs.get(position).getThumbnail())
                .apply(RequestOptions.circleCropTransform())
                .into(mImageSong);
        mTextTitle.setText(mSongs.get(position).getTitle());
        mTextDuration.setText(Utils.convertToTime(mSongs.get(position).getDuration()));
        if (mService.getMusicPlayer().isPlaying()) {
            mImagePlayPause.setImageResource(R.drawable.ic_pause_black_24dp);
        } else {
            mImagePlayPause.setImageResource(R.drawable.ic_play_arrow_black_24dp);
        }
    }

    private Animation getTranlateAnimation() {
        Animation animation = new TranslateAnimation(FROM_X_DELTA, TO_X_DELTA, FROM_Y_DELTA, TO_Y_DELTA);
        animation.setDuration(1500);
        animation.setFillAfter(true);
        return animation;
    }

    private Animation getInfiniteRotation() {
        Animation animation = new RotateAnimation(DEGREE_MIN,
                DEGREE_MAX, Animation.RELATIVE_TO_SELF, PIVOTE_X,
                Animation.RELATIVE_TO_SELF, PIVOTE_Y);
        animation.setDuration(TIME_LOOP);
        animation.setInterpolator(new LinearInterpolator());
        animation.setRepeatMode(Animation.RESTART);
        animation.setRepeatCount(Animation.INFINITE);
        return animation;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
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
        switch (v.getId()) {
            case R.id.image_prev:
                clickPrev();
                break;
            case R.id.image_play_pause:
                clickPlayPause();
                break;
            case R.id.image_next:
                clickNext();
            default:
                break;
        }
    }

    private void clickNext() {
        mService.getMusicPlayer().changeSong(MusicPlayer.NEXT);
        updateMiniPlayer(mService.getMusicPlayer().getCurrentIndex());
        mService.updateNotification();
    }

    private void clickPlayPause() {
        if (mService.getMusicPlayer().isPlaying()) {
            mService.getMusicPlayer().pause();
            mImagePlayPause.setImageResource(R.drawable.ic_play_arrow_black_24dp);
        } else {
            mService.getMusicPlayer().start();
            mImagePlayPause.setImageResource(R.drawable.ic_pause_black_24dp);
        }
        mService.updateNotification();
    }

    private void clickPrev() {
        mService.getMusicPlayer().changeSong(MusicPlayer.PREV);
        updateMiniPlayer(mService.getMusicPlayer().getCurrentIndex());
        mService.updateNotification();
    }

    @Override
    public void onSyncActivity(int position, boolean isPlaying) {
        updateMiniPlayer(position);
    }
}
