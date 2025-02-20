package org.rangeles.spotifyliked.service;
//
//import org.rangeles.spotifyliked.model.Song;
//import org.springframework.http.*;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//
//@Service
//public class SpotifyService {
//
//    private static final String LIKED_SONGS_URL = "https://api.spotify.com/v1/me/tracks";
//
//    public List<Song> getLikedSongs(String accessToken) {
//        List<Song> songs = new ArrayList<>();
//        RestTemplate restTemplate = new RestTemplate();
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("Authorization", "Bearer " + accessToken);
//
//        HttpEntity<String> entity = new HttpEntity<>(headers);
//        ResponseEntity<Map> response = restTemplate.exchange(LIKED_SONGS_URL, HttpMethod.GET, entity, Map.class);
//
//        if (response.getStatusCode() == HttpStatus.OK) {
//            List<Map<String, Object>> items = (List<Map<String, Object>>) response.getBody().get("items");
//
//            for (Map<String, Object> item : items) {
//                Map<String, Object> track = (Map<String, Object>) item.get("track");
//                String title = (String) track.get("name");
//
//                List<Map<String, Object>> artistsList = (List<Map<String, Object>>) track.get("artists");
//                String artist = artistsList.get(0).get("name").toString();
//
//                Map<String, Object> albumObj = (Map<String, Object>) track.get("album");
//                String album = (String) albumObj.get("name");
//
//                String spotifyUrl = (String) track.get("external_urls");
//
//                songs.add(new Song(title, artist, album, spotifyUrl));
//            }
//        }
//
//        return songs;
//    }
//}


import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.opencsv.CSVWriter;
import org.rangeles.spotifyliked.controller.SpotifyAuthController;
import org.rangeles.spotifyliked.model.SpotifyTrack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class SpotifyService {

    private static final Logger logger = LoggerFactory.getLogger(SpotifyService.class);
    private SpotifyAuthService spotifyAuthService; // Injecting the service

    private final RestTemplate restTemplate = new RestTemplate();
    private final String apiUrl = "https://api.spotify.com/v1/me/tracks";

    public List<SpotifyTrack> getLikedSongs(String accessToken, String refreshToken) {
//        String accessToken = authentication.getPrincipal().getAttribute("access_token");
//        String refreshToken = authentication.getPrincipal().getAttribute("refresh_token");
//
//        if (accessToken == null) {
//            throw new RuntimeException("Access token is missing!");
//        }

        List<SpotifyTrack> tracks = new ArrayList<>();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        System.out.println("Making API request with Access Token: " + accessToken); // Debugging

         HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(apiUrl, HttpMethod.GET, entity, Map.class);

        if (response.getStatusCode() == HttpStatus.UNAUTHORIZED) {
            logger.info("Access token expired. Refreshing...");
            accessToken = spotifyAuthService.refreshAccessToken(refreshToken);
            headers.set("Authorization", "Bearer " + accessToken);

            entity = new HttpEntity<>(headers);
            response = restTemplate.exchange(apiUrl, HttpMethod.GET, entity, Map.class);
        }


        if (response.getStatusCode() == HttpStatus.OK) {
            List<Map<String, Object>> items = (List<Map<String, Object>>) response.getBody().get("items");

            for (Map<String, Object> item : items) {
                Map<String, Object> track = (Map<String, Object>) item.get("track");
                String name = (String) track.get("name");
                String url = (String) track.get("external_urls.spotify");

                Map<String, Object> album = (Map<String, Object>) track.get("album");
                String albumName = (String) album.get("name");

                List<Map<String, Object>> artists = (List<Map<String, Object>>) track.get("artists");
                String artistName = (String) artists.get(0).get("name");

                tracks.add(new SpotifyTrack(name, artistName, albumName, url));
            }
        }
        return tracks;
    }

    public String convertToCsv(List<SpotifyTrack> songs) {
        StringWriter writer = new StringWriter();
        CSVWriter csvWriter = new CSVWriter(writer);
        csvWriter.writeNext(new String[]{"Title", "Artist", "Album"});

        for (SpotifyTrack track : songs) {
            csvWriter.writeNext(new String[]{track.getName(), track.getArtist(), track.getAlbum()});
        }

        try {
            csvWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return writer.toString();
    }

    public String convertToXml(List<SpotifyTrack> songs) throws Exception {
        XmlMapper xmlMapper = new XmlMapper();
        return xmlMapper.writeValueAsString(songs);
    }
}


