package com.ericzhng.apps.pocketplayerzh.androidutils;

import android.content.Context;
import android.content.SharedPreferences;

import com.ericzhng.apps.pocketplayerzh.audiostorage.AudioFiles;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Created by Valdio Veliu on 16-07-30.
 */
public class StorageUtil {

    private final String STORAGE_KEY = ".StorageUtil";
    private final String AUDIO_LIST_KEY = "audioArrayList";
    private final String AUDIO_INDEX_KEY = "audioIndex";

    private SharedPreferences preferences;
    private Context context;

    public StorageUtil(Context context) {
        this.context = context;
    }

    public void storeAudio(ArrayList<AudioFiles> arrayList) {
        preferences = context.getSharedPreferences(STORAGE_KEY, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences.edit();

        Gson gson = new Gson();
        String json = gson.toJson(arrayList);

        editor.putString(AUDIO_LIST_KEY, json);
        editor.apply();
    }

    public ArrayList<AudioFiles> loadAudio() {
        preferences = context.getSharedPreferences(STORAGE_KEY, Context.MODE_PRIVATE);

        Gson gson = new Gson();
        String json = preferences.getString(AUDIO_LIST_KEY, null);

        Type type = new TypeToken<ArrayList<AudioFiles>>() {}.getType();
        return gson.fromJson(json, type);
    }

    public void setAudioIndex(int index) {
        preferences = context.getSharedPreferences(STORAGE_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(AUDIO_INDEX_KEY, index);
        editor.apply();
    }

    public int getAudioIndex() {
        preferences = context.getSharedPreferences(STORAGE_KEY, Context.MODE_PRIVATE);
        return preferences.getInt(AUDIO_INDEX_KEY, -1);//return -1 if no data found
    }

    public void clearCachedAudioPlaylist() {
        preferences = context.getSharedPreferences(STORAGE_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
    }
}
