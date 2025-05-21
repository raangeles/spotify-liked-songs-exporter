package org.rangeles.spotifyliked.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.List;

@XmlRootElement(name = "tracks")
@XmlAccessorType(XmlAccessType.FIELD)
public class SpotifyTrackList {
    @XmlElement(name = "track")
    private List<SpotifyTrack> tracks;

    public SpotifyTrackList() {}

    public SpotifyTrackList(List<SpotifyTrack> tracks) {
        this.tracks = tracks;
    }

    public List<SpotifyTrack> getTracks() {
        return tracks;
    }

    public void setTracks(List<SpotifyTrack> tracks) {
        this.tracks = tracks;
    }
}
