package com.ericzhng.apps.pocketplayerzh;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.ericzhng.apps.pocketplayerzh.androidutils.CommonUtils;
import com.ericzhng.apps.pocketplayerzh.audiostorage.AudioFiles;
import com.ericzhng.apps.pocketplayerzh.audiostorage.AudioOps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.ericzhng.apps.pocketplayerzh.androidutils.CommonUtils.REQUEST_ID_MULTIPLE_PERMISSIONS;
import static com.ericzhng.apps.pocketplayerzh.audiostorage.AudioOps.INTENT_ALBUM_KEY;
import static com.ericzhng.apps.pocketplayerzh.audiostorage.AudioOps.INTENT_TITLE_KEY;
import static com.ericzhng.apps.pocketplayerzh.audiostorage.AudioOps.INTENT_URL_KEY;


public class MainActivity extends AppCompatActivity implements AudioFilesAdapter.ListItemClickListener {

    AudioOps audioOps = new AudioOps(this);

    // for RecyclerView use
    private AudioFilesAdapter mAdapter;
    private RecyclerView mRecyclerView;

    @Override
    protected
    void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = findViewById(R.id.rv_music_list);

        LinearLayoutManager layoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);

        //  Use this setting to improve performance if you know that changes in content do not
        //  change the child layout size in the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        if (CommonUtils.checkAndRequestPermissions(this)) {
            audioOps.loadAudioFiles();
        }
        recyclerViewAttachAdapter();
    }


    private void recyclerViewAttachAdapter() {

        if (audioOps.audioList != null && audioOps.audioList.size() > 0) {

            mAdapter = new AudioFilesAdapter(this, audioOps.audioList, this);

        } else {

            ArrayList<AudioFiles> list = new ArrayList<>();
            mAdapter = new AudioFilesAdapter(this, list, null);
        }

        mRecyclerView.setAdapter(mAdapter);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        String TAG = "LOG_PERMISSION";
        Log.d(TAG, "Permission callback called-------");

        switch (requestCode) {
            case REQUEST_ID_MULTIPLE_PERMISSIONS: {
                Map<String, Integer> perms = new HashMap<>();

                // Initialize the map with both permissions
                perms.put(Manifest.permission.READ_PHONE_STATE, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.READ_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);

                // Fill with actual results from user
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);

                    // Check for both permissions
                    if (perms.get(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

                        Log.d(TAG, "Phone state and storage permissions granted");

                        // process the normal flow
                        //else any one or both the permissions are not granted
                        audioOps.loadAudioFiles();

                    } else {
                        Log.d(TAG, "Some permissions are not granted ask again ");
                        // permission is denied (this is the first time, when "never ask again" is not checked) so ask again explaining the usage of permission
                        // shouldShowRequestPermissionRationale will return true
                        // show the dialog or snackbar saying its necessary and try again otherwise proceed with setup.
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE) ||
                                ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE)) {

                            CommonUtils.showDialogOK(this, "Phone state and storage permissions required for this app", new DialogInterface.OnClickListener() {
                                @Override
                                public
                                void onClick(DialogInterface dialog, int which) {
                                    switch (which) {
                                        case DialogInterface.BUTTON_POSITIVE:
                                            CommonUtils.checkAndRequestPermissions(getApplicationContext());
                                            break;
                                        case DialogInterface.BUTTON_NEGATIVE:
                                            // proceed with logic by disabling the related features or quit the app.
                                            break;
                                    }
                                }
                            });
                        }
                        //permission is denied (and never ask again is  checked)
                        //shouldShowRequestPermissionRationale will return false
                        else {
                            Toast.makeText(this, "Go to settings and enable permissions", Toast.LENGTH_LONG)
                                    .show();
                            //proceed with logic by disabling the related features or quit the app.
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onListItemClick(int clickedItemIndex) {

      //  initializePlayer(audioOps.audioList.get(clickedItemIndex).getUri());

        boolean flagVideo = true;

        if (flagVideo) {

            Intent intent = new Intent(this, PlayerActivity.class);

            Uri uri = audioOps.audioList.get(clickedItemIndex).getUri();
            String title = audioOps.audioList.get(clickedItemIndex).getTitle();
            String album = audioOps.audioList.get(clickedItemIndex).getAlbum();

            intent.putExtra(INTENT_URL_KEY, uri.toString());
            intent.putExtra(INTENT_TITLE_KEY, title);
            intent.putExtra(INTENT_ALBUM_KEY, album);

            startActivity(intent);
        }
        else
        {
            Intent intent = new Intent(this, AudioService.class);

            int size = audioOps.audioList.size();

            String[] uriArrayString = new String[size - clickedItemIndex];

            for (int k = clickedItemIndex; k < size; k ++) {
                uriArrayString[k-clickedItemIndex] = audioOps.audioList.get(k).getUri().toString();
            }
            String title = audioOps.audioList.get(clickedItemIndex).getTitle();
            String album = audioOps.audioList.get(clickedItemIndex).getAlbum();

            intent.putExtra(INTENT_URL_KEY, uriArrayString);
            intent.putExtra(INTENT_TITLE_KEY, title);
            intent.putExtra(INTENT_ALBUM_KEY, album);
            startService(intent);
        }
    }
}
