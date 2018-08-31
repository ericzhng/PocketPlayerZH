package com.ericzhng.apps.pocketplayerzh.userinterfaces;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.ericzhng.apps.pocketplayerzh.R;
import com.ericzhng.apps.pocketplayerzh.audiocatalogs.AudioFormat;
import com.ericzhng.apps.pocketplayerzh.audiocatalogs.AudioLibrary;
import com.ericzhng.apps.pocketplayerzh.players.DescriptionAdapter;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.NotificationUtil;
import com.google.android.exoplayer2.util.Util;

import java.util.ArrayList;

import static com.ericzhng.apps.pocketplayerzh.audiocatalogs.AudioLibrary.INTENT_ALBUM_KEY;
import static com.ericzhng.apps.pocketplayerzh.audiocatalogs.AudioLibrary.INTENT_TITLE_KEY;
import static com.ericzhng.apps.pocketplayerzh.audiocatalogs.AudioLibrary.INTENT_URL_KEY;

public
class PlayerMovieActivity extends AppCompatActivity implements PlayerNotificationManager.NotificationListener, Player.EventListener {

    private static final String TAG = PlayerMovieActivity.class.getSimpleName();

    private boolean startAutoPlay = true;
    private int startWindow = C.INDEX_UNSET;
    private long startPosition = 0;

    private static final String KEY_WINDOW = "window";
    private static final String KEY_POSITION = "position";
    private static final String KEY_AUTO_PLAY = "auto_play";

    private SimpleExoPlayer mExoPlayer;
    private PlayerView mPlayerView;
    private PlayerControlView mPlayerControl;

    private static MediaSessionCompat mMediaSession;
    private PlaybackStateCompat.Builder mStateBuilder;

    private TextView mFileName;
    private TextView mAlbum;

    PlayerNotificationManager playerNotificationManager;

    private static final String REMINDER_NOTIFICATION_CHANNEL_ID = "reminder_notification_channel";

    private static String CHANNEL_ID = "my-channel";
    private static int NOTIFICATION_ID = 0;

    DescriptionAdapter descriptionAdapter = new DescriptionAdapter(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        // Initialize the player view.
        mPlayerView = findViewById(R.id.playerView);
        mPlayerControl = findViewById(R.id.controls);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationUtil.createNotificationChannel(
                    this, REMINDER_NOTIFICATION_CHANNEL_ID, R.string.channel_name, NotificationUtil.IMPORTANCE_LOW);
        }

        playerNotificationManager = new PlayerNotificationManager(
                this, REMINDER_NOTIFICATION_CHANNEL_ID, NOTIFICATION_ID, descriptionAdapter);

        mFileName = findViewById(R.id.tv_filename);
        mAlbum = findViewById(R.id.tv_album);

        // Load the question mark as the background image until the user answers the question.
        mPlayerView.setDefaultArtwork(BitmapFactory.decodeResource
                (getResources(), R.drawable.question_mark));

        // Initialize the Media Session.
        initializeMediaSession();

        Intent intent = getIntent();
        boolean has_uri = intent.hasExtra(INTENT_URL_KEY);
        if (!has_uri) {
            return;
        }

        String strUri = intent.getStringExtra(INTENT_URL_KEY);
        Uri myUri = Uri.parse(strUri);

        String title = intent.getStringExtra(INTENT_TITLE_KEY);
        String album = intent.getStringExtra(INTENT_ALBUM_KEY);

        mFileName.setText(title);
        mAlbum.setText(album);

        if (savedInstanceState != null) {
            startAutoPlay = savedInstanceState.getBoolean(KEY_AUTO_PLAY);
            startWindow = savedInstanceState.getInt(KEY_WINDOW);
            startPosition = savedInstanceState.getLong(KEY_POSITION);
        }

        // Initialize the player.
        initializePlayer(myUri);
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
        }
    }

    private
    void updateStartPosition() {
        if (mExoPlayer != null) {
            startAutoPlay = mExoPlayer.getPlayWhenReady();
            startWindow = mExoPlayer.getCurrentWindowIndex();
            startPosition = Math.max(0, mExoPlayer.getCurrentPosition());
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        updateStartPosition();
        outState.putBoolean(KEY_AUTO_PLAY, startAutoPlay);
        outState.putInt(KEY_WINDOW, startWindow);
        outState.putLong(KEY_POSITION, startPosition);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /*
         * Normally, calling setDisplayHomeAsUpEnabled(true) (we do so in onCreate here) as well as
         * declaring the parent activity in the AndroidManifest is all that is required to get the
         * up button working properly. However, in this case, we want to navigate to the previous
         * screen the user came from when the up button was clicked, rather than a single
         * designated Activity in the Manifest.
         *
         * We use the up button's ID (android.R.id.home) to listen for when the up button is
         * clicked and then call onBackPressed to navigate to the previous Activity when this
         * happens.
         */
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initializeMediaSession() {

        // Create a MediaSessionCompat.
        mMediaSession = new MediaSessionCompat(this, TAG);

        // Enable callbacks from MediaButtons and TransportControls.
        mMediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        // Do not let MediaButtons restart the player when the app is not visible.
        mMediaSession.setMediaButtonReceiver(null);

        // Set an initial PlaybackState with ACTION_PLAY, so media buttons can start the player.
        mStateBuilder = new PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY |
                                PlaybackStateCompat.ACTION_PAUSE |
                                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                                PlaybackStateCompat.ACTION_PLAY_PAUSE);

        mMediaSession.setPlaybackState(mStateBuilder.build());


        // MySessionCallback has methods that handle callbacks from a media controller.
        mMediaSession.setCallback(new MySessionCallback());

        // Start the Media Session since the activity is active.
        mMediaSession.setActive(true);
    }

    private void initializePlayer(Uri mediaUri) {

        if (mExoPlayer == null) {
            // Create an instance of the ExoPlayer.
            TrackSelector trackSelector = new DefaultTrackSelector();

            mExoPlayer = ExoPlayerFactory.newSimpleInstance(this, trackSelector);
            mPlayerView.setPlayer(mExoPlayer);
            mPlayerControl.setPlayer(mExoPlayer);

            playerNotificationManager.setPlayer(mExoPlayer);
            // Set the ExoPlayer.EventListener to this activity.
            mExoPlayer.addListener(this);
        }

        String userAgent = Util.getUserAgent(this, "VideoPlayer");
        MediaSource mediaSource = new ExtractorMediaSource.Factory(new DefaultDataSourceFactory(this, userAgent))
                .createMediaSource(mediaUri);

        mExoPlayer.prepare(mediaSource);
        mExoPlayer.setPlayWhenReady(startAutoPlay);

        boolean haveStartPosition = startWindow != C.INDEX_UNSET;
        if (haveStartPosition) {
            mExoPlayer.seekTo(startWindow, startPosition);
        }
    }


    /**
     * Release ExoPlayer.
     */
    private void releasePlayer() {
        mExoPlayer.stop();
        mExoPlayer.release();
        mExoPlayer = null;
    }

    /**
     * Release the player when the activity is destroyed.
     */
    @Override
    protected void onDestroy() {
        playerNotificationManager.setPlayer(null);
        super.onDestroy();
        releasePlayer();
        mMediaSession.setActive(false);
    }


    @Override
    public
    void onNotificationStarted(int notificationId, Notification notification) {

    }

    @Override
    public
    void onNotificationCancelled(int notificationId) {

    }

    @Override
    public
    void onTimelineChanged(Timeline timeline, @Nullable Object manifest, int reason) {

    }

    @Override
    public
    void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public
    void onLoadingChanged(boolean isLoading) {

    }

    @Override
    public
    void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

    }

    @Override
    public
    void onRepeatModeChanged(int repeatMode) {

    }

    @Override
    public
    void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

    }

    @Override
    public
    void onPlayerError(ExoPlaybackException error) {

    }

    @Override
    public
    void onPositionDiscontinuity(int reason) {

    }

    @Override
    public
    void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }

    @Override
    public
    void onSeekProcessed() {

    }


    /**
     * Media Session Callbacks, where all external clients control the player.
     */
    private class MySessionCallback extends MediaSessionCompat.Callback {
        @Override
        public void onPlay() {
            mExoPlayer.setPlayWhenReady(true);
        }

        @Override
        public void onPause() {
            mExoPlayer.setPlayWhenReady(false);
        }

        @Override
        public void onSkipToPrevious() {
            mExoPlayer.seekTo(0);
        }
    }

    public static class MediaReceiver extends BroadcastReceiver {

        public MediaReceiver () {

        }

        @Override
        public
        void onReceive(Context context, Intent intent) {
            MediaButtonReceiver.handleIntent(mMediaSession, intent);
        }
    }
}
