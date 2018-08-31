package com.ericzhng.apps.pocketplayerzh.userinterfaces;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ericzhng.apps.pocketplayerzh.R;
import com.ericzhng.apps.pocketplayerzh.audiocatalogs.AudioFormat;
import com.ericzhng.apps.pocketplayerzh.players.AudioPlayerService;

import java.util.ArrayList;

import static com.ericzhng.apps.pocketplayerzh.audiocatalogs.AudioLibrary.INTENT_POSITION_KEY;


public
class PlayerAudioActivity extends AppCompatActivity {

    private static final String TAG = PlayerAudioActivity.class.getSimpleName();

    //-----------------------------------------------------//
    // audio service

    public static ArrayList<AudioFormat> audioList;

    private AudioPlayerService service;
    private boolean isBind = false;

    private ServiceConnection serviceConn = new ServiceConnection() {
        @Override
        public
        void onServiceConnected(ComponentName name, IBinder binder) {
            isBind = true;
            AudioPlayerService.servicesBinder myBinder = (AudioPlayerService.servicesBinder) binder;
            service = myBinder.getServices();
            Log.i(TAG, "PlayerActivity - onServiceConnected");

            service.handler.playFile(pos);
        }

        @Override
        public
        void onServiceDisconnected(ComponentName name) {
            isBind = false;
            service = null;
            Log.i(TAG, "PlayerActivity - onServiceDisconnected");
        }
    };

    void doBindService() {

       // Intent intent = new Intent(this, AudioPlayerService.class);
       // intent.putExtra("from", "PlayerActivity");

        Log.i(TAG, "----------------------------------------------------------------------");
        Log.i(TAG, "PlayerActivity binds AudioPlayerService");
      //  bindService(intent, serviceConn, BIND_AUTO_CREATE);

        ONDONE();

        isBind = true;
    }

    void doUnbindService() {
        if (isBind) {
            Log.i(TAG, "----------------------------------------------------------------------");
            Log.i(TAG, "PlayerActivity unbinds AudioPlayerService");
            unbindService(serviceConn);
            isBind = false;
        }
    }

    Intent servInt;

    protected void ONDONE() {
        servInt = new Intent(getBaseContext(), AudioPlayerService.class);
        startService(servInt);
        bindService(servInt, serviceConn, BIND_ADJUST_WITH_ACTIVITY);
    }


    //-----------------------------------------------------//
    // deal with different views

    // objects
    private boolean isPlaying;
    private String mBookTitle;
    private String mBookDuration;
    private int mBookCoverID;

    // Views
    private Button mLibraryButton;
    private TextView mBookTitleTextView;
    private TextView mDurationTextView;
    private ImageView mBookCoverImageView;
    private SeekBar mSeekBar;

    private Handler audioSeekBarHandler = null;
    int pos;


    @Override
    protected
    void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_audio);
        setTitle(new String("PlayerActivity"));

        findViews();
        doBindService();

        // Getting data from the intent
        Intent intent = getIntent();

        // Determine is this intent comes from "Now Playing" button, or the user clicks on book in the list.
        pos = intent.getIntExtra(INTENT_POSITION_KEY, 0);

        if (pos >= 0) {
            // play this clicked book
            mBookTitle = audioList.get(pos).getTitle();
            mBookDuration = audioList.get(pos).getSize();
            mBookCoverID = 0;
        }

        // Setting data to the views
        mBookTitleTextView.setText(mBookTitle);
        mDurationTextView.setText(mBookDuration);
        mBookCoverImageView.setImageResource(mBookCoverID);

        // Setting click listener to "Library" button
        mLibraryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public
            void onClick(View v) {
                Intent intent = new Intent(PlayerAudioActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    private
    void findViews() {
        mLibraryButton = (Button) findViewById(R.id.library_button_view);
        mBookTitleTextView = (TextView) findViewById(R.id.book_title_text_view);
        mDurationTextView = (TextView) findViewById(R.id.duration_text_view);
        mBookCoverImageView = (ImageView) findViewById(R.id.book_cover_image_view);

        mSeekBar = (SeekBar) findViewById(R.id.seekbar_view);

        ImageView mRewindClick = (ImageView) findViewById(R.id.rewind_button);
        ImageView mPlayClick = (ImageView) findViewById(R.id.play_button);
        ImageView mForwardClick = (ImageView) findViewById(R.id.forward_button);

        // Click this button to start play audio in a background service.

        mPlayClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public
            void onClick(View view) {

                // Initialize audio progress bar updater Handler object.
               // createAudioProgressbarUpdater();

                // service.setAudioProgressUpdateHandler(audioSeekBarHandler);

                // Start audio in background service.
                service.handler.flipPlaying();

                mSeekBar.setVisibility(ProgressBar.VISIBLE);

                Toast.makeText(getApplicationContext(), "Start play web audio file.", Toast.LENGTH_LONG).show();
            }
        });
    }


    boolean needD = false;
    @Override
    protected
    void onDestroy() {
        super.onDestroy();
        doUnbindService();

        if(service.handler.isPlaying()){
            needD = false;
        }else{
            needD = true;
        }

        if(needD){
            stopService(servInt);
        }

        Log.i(TAG, "PlayerActivity - onDestroy");
    }
}
