package com.ericzhng.apps.pocketplayerzh.audiostorage;

/*
* Copyright (C) 2017 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*  	http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

import android.net.Uri;

import java.io.Serializable;

/**
 * Java Object representing a single sample. Also includes utility methods for obtaining samples
 * from assets.
 */
public class AudioFiles implements Serializable {

    private String data;
    private String title;
    private String album;
    private String artist;
    private String size;
    private Uri uri;

    public
    AudioFiles(String data, String title, String album, String artist, String size, Uri uri) {
        this.data = data;
        this.title = title;
        this.album = album;
        this.artist = artist;
        this.size = size;
        this.uri = uri;
    }

    public
    String getData() {
        return data;
    }

    public
    void setData(String data) {
        this.data = data;
    }

    public
    String getTitle() {
        return title;
    }

    public
    void setTitle(String title) {
        this.title = title;
    }

    public
    String getAlbum() {
        return album;
    }

    public
    void setAlbum(String album) {
        this.album = album;
    }

    public
    String getArtist() {
        return artist;
    }

    public
    void setArtist(String artist) {
        this.artist = artist;
    }

    public
    String getSize() {
        return size;
    }

    public
    void setSize(String size) {
        this.size = size;
    }

    public Uri getUri() {
        return uri;
    }
}
