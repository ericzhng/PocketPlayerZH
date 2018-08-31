package com.ericzhng.apps.pocketplayerzh.players;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.ericzhng.apps.pocketplayerzh.audiocatalogs.AudioFormat;
import com.ericzhng.apps.pocketplayerzh.userinterfaces.MainActivity;
import com.ericzhng.apps.pocketplayerzh.R;
import com.ericzhng.apps.pocketplayerzh.userinterfaces.PlayerAudioActivity;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;
import java.util.ArrayList;

import static com.ericzhng.apps.pocketplayerzh.audiocatalogs.AudioLibrary.INTENT_POSITION_KEY;

public
class AudioPlayerService extends Service {

    private static final String TAG = AudioPlayerService.class.getSimpleName();

    public ArrayList<AudioFormat> audioList;

    public static final String PLAYBACK_CHANNEL_ID = "my-channel";
    private static final int PLAYBACK_NOTIFICATION_ID = 32;
    private PlayerNotificationManager playerNotificationManager;


    private final Context mContext = this;
    public AudioHandler handler;


    private int mPosition;

    @Override
    public
    void onCreate() {
        super.onCreate();

        handler = new AudioHandler(this, audioList);

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

        playerNotificationManager.setPlayer(handler.mExoPlayer);
    }

    @Nullable
    @Override
    public
    IBinder onBind(Intent intent) {
        if (intent != null)
            Log.i(TAG, "AudioPlayerService - onUnbind");

        return mBinder;
    }

    //-----------------------------------------------------//
    // obtain service binder
    private servicesBinder mBinder = new servicesBinder(PlayerAudioActivity.audioList);

    public
    class servicesBinder extends Binder {
        public servicesBinder(ArrayList<AudioFormat> m_audioList) {
            audioList = m_audioList;
        }
        public AudioPlayerService getServices() {
            return AudioPlayerService.this;
        }
    }

    @Override
    public
    int onStartCommand(Intent intent, int flags, int startId) {
        //return super.onStartCommand(intent, flags, startId);

        handler = new AudioHandler(this, audioList);

        mPosition = intent.getIntExtra(INTENT_POSITION_KEY, 0);

        return START_STICKY;
    }

    @Override
    public
    void onDestroy() {
        handler.stop();
        playerNotificationManager.setPlayer(null);
        super.onDestroy();
    }

    @Override
    public
    boolean onUnbind(Intent intent) {

        if (intent != null)
            Log.i(TAG, "AudioPlayerService - onUnbind");

        return false;
    }

    //---------------------------------------------------------------//
    // phony interruption

    boolean byCall;

    private void TelephoneEventRegister() {

        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(getBaseContext().TELEPHONY_SERVICE);
        PhoneStateListener callStateListener = new PhoneStateListener() {
            public
            void onCallStateChanged(int state, String incomingNumber) {
                if (state == TelephonyManager.CALL_STATE_RINGING) {
                    //Toast.makeText(getApplicationContext(),"Phone Is Riging", Toast.LENGTH_LONG).show();
                    if (handler.isPlaying()) {
                        byCall = true;
                        handler.pause();
                    }
                }

                if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
                    //Toast.makeText(getApplicationContext(),"Phone is Currently in A call", Toast.LENGTH_LONG).show();
                    if (handler.isPlaying()) {
                        byCall = true;
                        handler.pause();
                    }
                }

                if (state == TelephonyManager.CALL_STATE_IDLE) {
                    //Toast.makeText(getApplicationContext(),"phone is neither ringing nor in a call", Toast.LENGTH_LONG).show();
                    if (byCall) {
                        byCall = false;
                        handler.play();
                    }
                }
            }
        };
        telephonyManager.listen(callStateListener, PhoneStateListener.LISTEN_CALL_STATE);
    }
}
