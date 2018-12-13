package com.example.vanh1200.serviceex;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

public class MusicService extends Service {
    private static final int PENDING_NEXT = 1;
    private static final int PENDING_PREV = 2;
    private static final int PENDING_PLAY_PAUSE = 3;
    private static final String KEY_PENDING = "KEY_PENDING";
    private static final int REQUEST_MUSIC_SERVICE = 1;
    private static final String MUSIC_CHANEL_ID = "1";
    private static final CharSequence MUSIC_CHANEL_NAME = "MUSIC_CHANEL";
    private static final int MUSIC_NOTIFICATION = 1;
    private static final int DEFAULT_ACTION = -1;
    private MusicPlayer mMusicPlayer;
    private final IBinder mIBinder = new MusicBinder();
    private RemoteViews mRemoteViews;
    private NotificationCompat.Builder mBuilder;
    private OnSyncActivityListener mListener;
    private static final String TAG = "MusicService";

    public void setListener(OnSyncActivityListener listener) {
        mListener = listener;
    }

    public MusicPlayer getMusicPlayer() {
        return mMusicPlayer;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mIBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mMusicPlayer = new MusicPlayer(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null)
            handlePendingIntent(intent);
        createNotification();
        return START_STICKY;
    }

    private void handlePendingIntent(Intent intent) {
        int action = DEFAULT_ACTION;
        if (intent.hasExtra(KEY_PENDING)) {
            action = intent.getIntExtra(KEY_PENDING, action);
            switch (action) {
                case PENDING_NEXT:
                    mMusicPlayer.changeSong(MusicPlayer.NEXT);
                    updateNotification();
                    break;
                case PENDING_PLAY_PAUSE:
                    if (mMusicPlayer.isPlaying()) {
                        mMusicPlayer.pause();
                    } else {
                        mMusicPlayer.start();
                    }
                    updateNotification();
                    break;
                case PENDING_PREV:
                    mMusicPlayer.changeSong(MusicPlayer.PREV);
                    updateNotification();
                    break;
                default:
                    break;
            }
        }
    }


    private void createNotification() {
        mRemoteViews = new RemoteViews(getPackageName(), R.layout.notify_mp3);
        mRemoteViews.setOnClickPendingIntent(R.id.image_play_pause, getPendingIntent(PENDING_PLAY_PAUSE));
        mRemoteViews.setOnClickPendingIntent(R.id.image_prev, getPendingIntent(PENDING_PREV));
        mRemoteViews.setOnClickPendingIntent(R.id.image_next, getPendingIntent(PENDING_NEXT));
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(MUSIC_CHANEL_ID, MUSIC_CHANEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(channel);
        }
        mBuilder = new NotificationCompat.Builder(this, MUSIC_CHANEL_ID);
        mBuilder.setCustomContentView(mRemoteViews);
        mBuilder.setSmallIcon(R.drawable.ic_play_arrow_black_24dp);
        startForeground(MUSIC_NOTIFICATION, mBuilder.build());
    }

    public void updateNotification() {
        if (mListener != null) {
            mListener.onSyncActivity(mMusicPlayer.getCurrentIndex(), mMusicPlayer.isPlaying());
        }
        mRemoteViews.setTextViewText(R.id.text_title,
                mMusicPlayer.getSongs().get(mMusicPlayer.getCurrentIndex()).getTitle());
        if (mMusicPlayer.isPlaying()) {
            mRemoteViews.setImageViewResource(R.id.image_play_pause, R.drawable.ic_pause_black_24dp);
        } else {
            mRemoteViews.setImageViewResource(R.id.image_play_pause, R.drawable.ic_play_arrow_black_24dp);
        }
        mBuilder.setContent(mRemoteViews);
        startForeground(MUSIC_NOTIFICATION, mBuilder.build());
    }

    private PendingIntent getPendingIntent(int pending) {
        switch (pending) {
            case PENDING_PREV: {
                return getPrevPending();
            }

            case PENDING_PLAY_PAUSE: {
                return getPlayPausePending();
            }

            case PENDING_NEXT: {
                return getNextPendingIntent();
            }
            default:
                return null;
        }
    }

    private PendingIntent getNextPendingIntent() {
        Intent intent = new Intent(this, MusicService.class);
        intent.putExtra(KEY_PENDING, PENDING_NEXT);
        PendingIntent pendingIntent = PendingIntent.getService(this,
                PENDING_NEXT, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }

    private PendingIntent getPlayPausePending() {
        Intent intent = new Intent(this, MusicService.class);
        intent.putExtra(KEY_PENDING, PENDING_PLAY_PAUSE);
        PendingIntent pendingIntent = PendingIntent.getService(this,
                PENDING_PLAY_PAUSE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }

    private PendingIntent getPrevPending() {
        Intent intent = new Intent(this, MusicService.class);
        intent.putExtra(KEY_PENDING, PENDING_PREV);
        PendingIntent pendingIntent = PendingIntent.getService(this,
                PENDING_PREV, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    public interface OnSyncActivityListener {
        void onSyncActivity(int position, boolean isPlaying);
    }
}
