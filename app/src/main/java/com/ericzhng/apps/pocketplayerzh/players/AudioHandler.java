package com.ericzhng.apps.pocketplayerzh.players;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import com.ericzhng.apps.pocketplayerzh.audiocatalogs.AudioFormat;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.util.ArrayList;
import java.util.HashMap;

public
class AudioHandler {

    private static final float MEDIA_VOLUME_DEFAULT = 1.0f;
    private static final float MEDIA_VOLUME_DUCK = 0.2f;

    private Context mContext;

    public SimpleExoPlayer mExoPlayer;

    ArrayList<AudioFormat> mAudioList;

    private final AudioManager mAudioManager;
    private final AudioFocusHelper mAudioFocusHelper;

    private final BroadcastReceiver mAudioNoisyReceiver =
            new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                        if (isPlaying()) {
                            pause();
                        }
                    }
                }
            };

    private static final IntentFilter AUDIO_NOISY_INTENT_FILTER =
            new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);

    private boolean mAudioNoisyReceiverRegistered = false;
    private void registerAudioNoisyReceiver() {
        if (!mAudioNoisyReceiverRegistered) {
            mContext.registerReceiver(mAudioNoisyReceiver, AUDIO_NOISY_INTENT_FILTER);
            mAudioNoisyReceiverRegistered = true;
        }
    }

    private void unregisterAudioNoisyReceiver() {
        if (mAudioNoisyReceiverRegistered) {
            mContext.unregisterReceiver(mAudioNoisyReceiver);
            mAudioNoisyReceiverRegistered = false;
        }
    }

    //---------------------------------------------------------------//
    // constructor

    public AudioHandler(AudioPlayerService service, ArrayList<AudioFormat> audioList) {
        mContext = service.getApplicationContext();

        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        mAudioFocusHelper = new AudioFocusHelper();

        this.mAudioList = audioList;
        // Create an instance of the ExoPlayer.
        TrackSelector trackSelector = new DefaultTrackSelector();
        mExoPlayer = ExoPlayerFactory.newSimpleInstance(mContext, trackSelector);
        mMediaUri = "";
        mMediaId = 0;
        needStop = true;
    }


    //---------------------------------------------------------------//
    // media operations

    public
    boolean isPlaying() {
        return mExoPlayer != null && mExoPlayer.getPlayWhenReady();
    }

    public final void play() {
        if (mAudioFocusHelper.requestAudioFocus()) {
            registerAudioNoisyReceiver();
            mExoPlayer.setPlayWhenReady(true);
        }
    }

    public final void pause() {
        if (!mPlayOnAudioFocus) {
            mAudioFocusHelper.abandonAudioFocus();
        }

        unregisterAudioNoisyReceiver();
        mExoPlayer.setPlayWhenReady(false);
    }

    public
    void flipPlaying() {
        if (mExoPlayer != null) {
            if (isPlaying()) {
                pause();
            } else {
                play();
            }
        } else {
            playFile(0);
        }
    }


    public
    void stop() {
        mExoPlayer.release();
        mExoPlayer = null;
    }

    public void setVolume(float volume) {
        if (mExoPlayer != null) {
            mExoPlayer.setVolume(volume);
        }
    }

    private int mSeekWhileNotPlaying = -1;
    public void seekTo(long position) {
        if (mExoPlayer != null) {
            if (!isPlaying()) {
                mSeekWhileNotPlaying = (int) position;
            }
            mExoPlayer.seekTo(position);
        }
    }

    boolean isPrepared = true;
    public boolean songPrepared = false;
    boolean needPlay = false;
    boolean needStop = false;
    private boolean startAutoPlay = true;

    private int mMediaId;
    private String mMediaUri;

    private int startWindow = C.INDEX_UNSET;
    private long startPosition = 0;
    private boolean mCurrentMediaPlayedToCompletion = false;

    public void playFile(int id) {

        Uri mediaUri = mAudioList.get(id).getUri();

        //boolean mediaChanged = (mediaUri == null || !mediaUri.toString().equals(mMediaUri));
        boolean mediaChanged = (mMediaId == id);

        String userAgent = Util.getUserAgent(mContext, "PocketPlayer");

        final MediaSource mediaSource = new ExtractorMediaSource.Factory(new DefaultDataSourceFactory(mContext, userAgent))
                .createMediaSource(mediaUri);

        if (isPrepared) {
            isPrepared = false;
            songPrepared = true;
            needPlay = false;
            try {
                mExoPlayer.prepare(mediaSource);
                mExoPlayer.setPlayWhenReady(startAutoPlay);
            } catch (Exception e) {
                isPrepared = true;
                needPlay = true;
            }
        } else {
            needPlay = true;
        }
        play();

        boolean haveStartPosition = startWindow != C.INDEX_UNSET;
        if (haveStartPosition) {
            mExoPlayer.seekTo(startWindow, startPosition);
        }

        if (mCurrentMediaPlayedToCompletion) {
            // Last audio file was played to completion, the resourceId hasn't changed, but the
            // player was released, so force a reload of the media file for playback.
            mediaChanged = true;
            mCurrentMediaPlayedToCompletion = false;
        }

        if (!mediaChanged) {
            if (!isPlaying()) {
                play();
            }
        } else {
            stop();
        }

        mMediaUri = mediaUri.toString();
        mMediaId = id;
    }


    public
    void playNext() {
        mMediaId = mMediaId + 1;

        if (mMediaId > mAudioList.size() - 1) {
            mMediaId = mAudioList.size() - 1;
        }
        playFile(mMediaId);
    }

    public
    void playPrevious() {
        mMediaId = mMediaId + 1;

        if (mMediaId > mAudioList.size() - 1) {
            mMediaId = mAudioList.size() - 1;
        }
        playFile(mMediaId);
    }


    //---------------------------------------------------------------//
    // song list operations

    public
    void playAllSongs(final int from) {

        int size = mAudioList.size();
        MediaSource[] mediaSources = new MediaSource[size - from];

        String userAgent = Util.getUserAgent(mContext, "PocketPlayer");

        for (int k = from; k < size; k++) {
            mediaSources[k - from] = new ExtractorMediaSource.Factory(new DefaultDataSourceFactory(mContext, userAgent))
                    .createMediaSource(mAudioList.get(k).getUri());
        }

        final ConcatenatingMediaSource concatenatedSource = new ConcatenatingMediaSource(mediaSources);

        new Thread() {
            @Override
            public
            void run() {
                if (isPrepared) {
                    isPrepared = false;
                    songPrepared = true;
                    needPlay = false;
                    SimpleExoPlayer Nm = mExoPlayer;
                    mExoPlayer.release();
                    try {
                        mExoPlayer.prepare(concatenatedSource);
                        mExoPlayer.setPlayWhenReady(startAutoPlay);
                    } catch (Exception e) {
                        isPrepared = true;
                        needPlay = true;
                    }
                } else {
                    needPlay = true;
                }
            }
        }.start();

        boolean haveStartPosition = startWindow != C.INDEX_UNSET;
        if (haveStartPosition) {
            mExoPlayer.seekTo(startWindow, startPosition);
        }
    }

    public void prepareDataSource(ArrayList<Uri> uriArray){

        // player = ExoPlayerFactory.newSimpleInstance(mContext, new DefaultTrackSelector());

        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(mContext,
                Util.getUserAgent(mContext, "AudioDemo"));

        ConcatenatingMediaSource concatenatingMediaSource = new ConcatenatingMediaSource();
        for (Uri uri: uriArray) {
            MediaSource mediaSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(uri);
            concatenatingMediaSource.addMediaSource(mediaSource);
        }

        mExoPlayer.prepare(concatenatingMediaSource);
        // player.setPlayWhenReady(true);
    }


    //---------------------------------------------------------------//
    // others to deal with

    public
    ArrayList<String[]> getSongsList(int[] Id) {
        String str = "";
        for (int i = 0; i < Id.length; i++) {
            str += MediaStore.Audio.Media._ID + " = " + Id[i];
            if (Id.length - 1 != i) {
                str += " OR ";
            }
        }

        String[] projection = {MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DURATION};
        Cursor DataCursor = mContext.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection,
                str, null, MediaStore.Audio.Media.TITLE + " COLLATE NOCASE ASC ");
        ArrayList<String[]> list = new ArrayList<String[]>();

        HashMap<Integer, String[]> nData = new HashMap<Integer, String[]>();

        while (DataCursor.moveToNext()) {
            int nId = DataCursor.getInt(1);
            nData.put(nId, new String[]{DataCursor.getString(0), DataCursor.getString(1), DataCursor.getString(2), ""});
        }

        DataCursor.close();

        for (int i = 0; i < Id.length; i++) {
            if (nData.containsKey(Id[i])) {
                list.add(nData.get(Id[i]));
            }
        }
        return list;
    }


    //---------------------------------------------------------------//
    // Return current audio play position

    public
    long getCurrentAudioPosition() {
        long ret = 0;
        if (mExoPlayer != null) {
            ret = mExoPlayer.getCurrentPosition();
        }
        return ret;
    }

    // Return total audio file duration.
    public
    long getTotalAudioDuration() {
        long ret = 0;
        if (mExoPlayer != null) {
            ret = mExoPlayer.getDuration();
        }
        return ret;
    }

    // Return current audio player progress value.
    public
    long getAudioProgress() {
        long ret = 0;
        long currAudioPosition = getCurrentAudioPosition();
        long totalAudioDuration = getTotalAudioDuration();
        if (totalAudioDuration > 0) {
            ret = (currAudioPosition * 100) / totalAudioDuration;
        }
        return ret;
    }

    /**
     * Helper class for managing audio focus related tasks.
     */
    private boolean mPlayOnAudioFocus = false;

    private final class AudioFocusHelper implements AudioManager.OnAudioFocusChangeListener {

        AudioFocusRequest mFocusRequest;
        int focusRequest;

        private boolean requestAudioFocus() {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                AudioAttributes mPlaybackAttributes = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build();
                mFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                        .setAudioAttributes(mPlaybackAttributes)
                        .setAcceptsDelayedFocusGain(true)
                        .setOnAudioFocusChangeListener(this)
                        .build();

                focusRequest = mAudioManager.requestAudioFocus(mFocusRequest);
            } else {

                focusRequest = mAudioManager.requestAudioFocus(this,
                        AudioManager.STREAM_MUSIC,
                        AudioManager.AUDIOFOCUS_GAIN);

            }
            return focusRequest == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
        }

        private void abandonAudioFocus() {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mAudioManager.abandonAudioFocusRequest(mFocusRequest);
            }
            else {
                mAudioManager.abandonAudioFocus(this);
            }
        }

        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    if (mPlayOnAudioFocus && !isPlaying()) {
                        play();
                    } else if (isPlaying()) {
                        setVolume(MEDIA_VOLUME_DEFAULT);
                    }
                    mPlayOnAudioFocus = false;
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    setVolume(MEDIA_VOLUME_DUCK);
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    if (isPlaying()) {
                        mPlayOnAudioFocus = true;
                        pause();
                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    abandonAudioFocus();
                    mPlayOnAudioFocus = false;
                    stop();
                    break;
            }
        }
    }

}
