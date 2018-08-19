package com.ericzhng.apps.pocketplayerzh;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import static com.ericzhng.apps.pocketplayerzh.audiostorage.AudioOps.INTENT_ALBUM_KEY;
import static com.ericzhng.apps.pocketplayerzh.audiostorage.AudioOps.INTENT_TITLE_KEY;
import static com.ericzhng.apps.pocketplayerzh.audiostorage.AudioOps.INTENT_URL_KEY;

public
class AudioService extends Service {

    private boolean startAutoPlay = true;
    private int startWindow = C.INDEX_UNSET;
    private long startPosition = 0;

    private SimpleExoPlayer player;
    public static final String PLAYBACK_CHANNEL_ID = "my-channel";
    private PlayerNotificationManager playerNotificationManager;
    private static final int PLAYBACK_NOTIFICATION_ID = 32;

    private final Context mContext = this;

    @Override
    public
    void onCreate() {
        super.onCreate();

        player = ExoPlayerFactory.newSimpleInstance(mContext, new DefaultTrackSelector());

        playerNotificationManager = PlayerNotificationManager.createWithNotificationChannel(
                this,
                PLAYBACK_CHANNEL_ID,
                R.string.playback_channel_name,
                PLAYBACK_NOTIFICATION_ID,
                new PlayerNotificationManager.MediaDescriptionAdapter() {
                    @Override
                    public
                    String getCurrentContentTitle(Player player) {
                        return null;
                    }

                    @Nullable
                    @Override
                    public
                    PendingIntent createCurrentContentIntent(Player player) {
                        Intent intent = new Intent(mContext, MainActivity.class);
                        return PendingIntent.getActivity(mContext, 0,  intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    }

                    @Nullable
                    @Override
                    public
                    String getCurrentContentText(Player player) {
                        return null;
                    }

                    @Nullable
                    @Override
                    public
                    Bitmap getCurrentLargeIcon(Player player, PlayerNotificationManager.BitmapCallback callback) {
                        return null;
                    }
                });

        playerNotificationManager.setNotificationListener(
                new PlayerNotificationManager.NotificationListener() {
                    @Override
                    public
                    void onNotificationStarted(int notificationId, Notification notification) {
                        startForeground(notificationId, notification);
                    }

                    @Override
                    public
                    void onNotificationCancelled(int notificationId) {
                        stopSelf();
                    }
                }
        );
        playerNotificationManager.setPlayer(player);
    }


    void initPlayer(Uri[] uriArray){

        // player = ExoPlayerFactory.newSimpleInstance(mContext, new DefaultTrackSelector());

        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(this,
                Util.getUserAgent(mContext, "AudioDemo"));

        ConcatenatingMediaSource concatenatingMediaSource = new ConcatenatingMediaSource();
        for (Uri uri: uriArray) {
            MediaSource mediaSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(uri);
            concatenatingMediaSource.addMediaSource(mediaSource);
        }

        player.prepare(concatenatingMediaSource);
        player.setPlayWhenReady(true);
    }


    @Nullable
    @Override
    public
    IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public
    int onStartCommand(Intent intent, int flags, int startId) {
        //return super.onStartCommand(intent, flags, startId);

        String[] Uris = intent.getStringArrayExtra(INTENT_URL_KEY);
        String title = intent.getStringExtra(INTENT_TITLE_KEY);
        String album = intent.getStringExtra(INTENT_ALBUM_KEY);

        Uri[] uriArray = new Uri[Uris.length];

        for (int k = 0; k < Uris.length; k ++) {
            uriArray[k] = Uri.parse(Uris[k]);
        }

        initPlayer(uriArray);

        return START_STICKY;
    }

    @Override
    public
    void onDestroy() {
        player.release();
        player = null;
        playerNotificationManager.setPlayer(null);
        super.onDestroy();
    }
}
