package com.ericzhng.apps.pocketplayerzh.audiocatalogs;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;

public class AudioLibrary {

    public static final String INTENT_URL_KEY = "url-key";
    public static final String INTENT_POSITION_KEY = "position-key";
    public static final String INTENT_TITLE_KEY = "title-key";
    public static final String INTENT_ALBUM_KEY = "album-key";

    public ArrayList<AudioFormat> audioList = new ArrayList<>();

    private Context mContext;

    public
    AudioLibrary(Context mContext) {
        this.mContext = mContext;
    }

    public void loadAudioFiles() {

        ContentResolver contentResolver = mContext.getContentResolver();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";

        Cursor cursor = contentResolver.query(uri, null, selection, null, sortOrder);

        if (cursor != null && cursor.getCount() > 0) {

            audioList.clear();
            while (cursor.moveToNext()) {

                String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                String size = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE));

                Uri uriFile= ContentUris
                        .withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID)));

                audioList.add(new AudioFormat(data, title, album, artist, size, uriFile));
            }
        }
        if (cursor != null)
            cursor.close();
    }
}
