package com.example.naimish.assignment.Model;

import com.google.gson.annotations.SerializedName;

public class Track {

    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("artistName")
    private String artistName;

    @SerializedName("albumId")
    private String albumId;

    @SerializedName("albumName")
    private String albumName;

    @SerializedName("previewURL")
    private String previewURL;

    private boolean isPlaying;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getAlbumId() {
        return albumId;
    }

    public void setAlbumId(String albumId) {
        this.albumId = albumId;
    }

    public String getPreviewURL() {
        return previewURL;
    }

    public void setPreviewURL(String previewURL) {
        this.previewURL = previewURL;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        this.isPlaying = playing;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }
}
