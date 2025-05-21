package org.rangeles.spotifyliked.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.time.OffsetDateTime; // Import for handling the timestamp

@XmlRootElement(name = "track")
@XmlAccessorType(XmlAccessType.FIELD)
public class SpotifyTrack {
    private String title;
    private String artist;
    private String album;
    private String url;
    private OffsetDateTime addedAt; // New field for the added_at timestamp

    public SpotifyTrack() {}

    // Updated constructor to include addedAt
    public SpotifyTrack(String title, String artist, String album, String url, OffsetDateTime addedAt) {
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.url = url;
        this.addedAt = addedAt;
    }

    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public String getAlbum() { return album; }
    public String getUrl() { return url; }
    public OffsetDateTime getAddedAt() { return addedAt; } // Getter for addedAt

    public void setTitle(String title) { this.title = title; } // Renamed from setName to setTitle for consistency
    public void setArtist(String artist) { this.artist = artist; }
    public void setAlbum(String album) { this.album = album; }
    public void setUrl(String url) { this.url = url; }
    public void setAddedAt(OffsetDateTime addedAt) { this.addedAt = addedAt; } // Setter for addedAt
}