package org.rangeles.spotifyliked.model;

public class Song {
    private String title;
    private String artist;
    private String album;
    private String spotifyUrl;

    public Song(String title, String artist, String album, String spotifyUrl) {
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.spotifyUrl = spotifyUrl;
    }

    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public String getAlbum() { return album; }
    public String getSpotifyUrl() { return spotifyUrl; }

    @Override
    public String toString() {
        return title + " - " + artist + " (" + album + ")";
    }
}
