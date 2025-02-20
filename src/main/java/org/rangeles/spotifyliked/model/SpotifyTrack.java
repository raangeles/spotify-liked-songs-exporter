package org.rangeles.spotifyliked.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "track")
public class SpotifyTrack {
    private String name;
    private String artist;
    private String album;
    private String url;

    public SpotifyTrack() {}

    public SpotifyTrack(String name, String artist, String album, String url) {
        this.name = name;
        this.artist = artist;
        this.album = album;
        this.url = url;
    }

    public String getName() { return name; }
    public String getArtist() { return artist; }
    public String getAlbum() { return album; }
    public String getUrl() { return url; }

    public void setName(String name) { this.name = name; }
    public void setArtist(String artist) { this.artist = artist; }
    public void setAlbum(String album) { this.album = album; }
    public void setUrl(String url) { this.url = url; }
}
