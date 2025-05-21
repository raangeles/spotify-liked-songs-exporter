package org.rangeles.spotifyliked.service;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.rangeles.spotifyliked.model.SpotifyTrack;
import org.rangeles.spotifyliked.model.SpotifyTrackList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
public class SpotifyService {

    private static final Logger logger = LoggerFactory.getLogger(SpotifyService.class);
    private final RestTemplate restTemplate;
    private final SpotifyAuthService spotifyAuthService;
    private final XmlMapper xmlMapper; // Declare XmlMapper as a field

    @Value("${spotify.api.url}/me/tracks")
    private String likedSongsUrl;

    // Cache file path
    private final Path cacheFilePath = Paths.get("liked_songs_cache.xml");

    public SpotifyService(
            RestTemplate restTemplate,
            SpotifyAuthService spotifyAuthService,
            @Value("${spotify.api.url}/me/tracks") String likedSongsUrl) {
        this.restTemplate = restTemplate;
        this.spotifyAuthService = spotifyAuthService;
        this.likedSongsUrl = likedSongsUrl;
        this.xmlMapper = new XmlMapper();
        this.xmlMapper.registerModule(new JavaTimeModule());
        this.xmlMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.xmlMapper.enable(SerializationFeature.INDENT_OUTPUT); // For pretty print
    }

    public List<SpotifyTrack> getLikedSongs(String accessToken, String refreshToken, String userId) {
        List<SpotifyTrack> allLikedSongs = new ArrayList<>();
        String nextUrl = likedSongsUrl + "?limit=50"; // Start with first 50 tracks

        try {
            // Try to load from cache first
            List<SpotifyTrack> cachedSongs = getLikedSongsFromCache();
            if (!cachedSongs.isEmpty()) {
                logger.info("Loaded {} liked songs from cache.", cachedSongs.size());
                return cachedSongs;
            }
        } catch (IOException e) {
            logger.warn("Could not load liked songs from cache, fetching from Spotify API: {}", e.getMessage());
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        while (nextUrl != null) {
            try {
                ResponseEntity<Map> response = restTemplate.exchange(nextUrl, HttpMethod.GET, entity, Map.class);

                if (response.getStatusCode() == HttpStatus.OK) {
                    Map<String, Object> body = response.getBody();
                    List<Map<String, Object>> items = (List<Map<String, Object>>) body.get("items");

                    for (Map<String, Object> item : items) {
                        Map<String, Object> trackData = (Map<String, Object>) item.get("track");
                        String addedAtString = (String) item.get("added_at");
                        OffsetDateTime addedAt = null;
                        if (addedAtString != null) {
                            try {
                                addedAt = OffsetDateTime.parse(addedAtString);
                            } catch (Exception e) {
                                logger.error("Failed to parse added_at timestamp: {}", addedAtString, e);
                            }
                        }

                        if (trackData != null) {
                            String title = (String) trackData.get("name");
                            Map<String, Object> albumData = (Map<String, Object>) trackData.get("album");
                            String album = (albumData != null) ? (String) albumData.get("name") : "Unknown Album";

                            List<Map<String, Object>> artists = (List<Map<String, Object>>) trackData.get("artists");
                            String artist = (artists != null && !artists.isEmpty()) ? (String) artists.get(0).get("name") : "Unknown Artist";

                            String externalUrl = "N/A";
                            if (trackData.containsKey("external_urls")) {
                                Map<String, String> externalUrls = (Map<String, String>) trackData.get("external_urls");
                                externalUrl = externalUrls.getOrDefault("spotify", "N/A");
                            }

                            allLikedSongs.add(new SpotifyTrack(title, artist, album, externalUrl, addedAt));
                        }
                    }
                    nextUrl = (String) body.get("next");
                } else if (response.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                    logger.warn("Access token expired or invalid, attempting to refresh.");
                    throw new RuntimeException("Spotify API Unauthorized: Access token expired.");
                } else {
                    logger.error("Failed to retrieve liked songs: {}", response.getStatusCode());
                    throw new RuntimeException("Failed to retrieve liked songs from Spotify API.");
                }
            } catch (Exception e) {
                logger.error("Error fetching liked songs from Spotify API: {}", e.getMessage(), e);
                throw new RuntimeException("Error fetching liked songs: " + e.getMessage(), e);
            }
        }

        // Sort songs by addedAt date in descending order
        allLikedSongs.sort(Comparator.comparing(SpotifyTrack::getAddedAt, Comparator.nullsLast(Comparator.reverseOrder())));

        // Cache the fetched songs
        try {
            cacheLikedSongs(allLikedSongs);
        } catch (IOException e) {
            logger.error("Failed to cache liked songs: {}", e.getMessage());
        }

        return allLikedSongs;
    }

    private void cacheLikedSongs(List<SpotifyTrack> songs) throws IOException {
        logger.info("Caching {} liked songs to {}", songs.size(), cacheFilePath);
        xmlMapper.writeValue(cacheFilePath.toFile(), new SpotifyTrackList(songs));
    }

    public List<SpotifyTrack> getLikedSongsFromCache() throws IOException {
        if (Files.exists(cacheFilePath) && Files.size(cacheFilePath) > 0) {
            logger.info("Loading liked songs from cache: {}", cacheFilePath);
            SpotifyTrackList trackList = xmlMapper.readValue(cacheFilePath.toFile(), SpotifyTrackList.class);
            if (trackList != null && trackList.getTracks() != null) {
                return trackList.getTracks();
            }
        }
        return new ArrayList<>(); // Return empty list if cache doesn't exist or is empty
    }

    public String exportToXml(List<SpotifyTrack> tracks) throws IOException {
        logger.info("Exporting {} tracks to XML using JacksonXmlMapper", tracks.size());
        SpotifyTrackList trackList = new SpotifyTrackList(tracks);
        try {
            // Use the class-level xmlMapper for serialization
            return xmlMapper.writeValueAsString(trackList);
        } catch (Exception e) {
            logger.error("Error converting to XML using JacksonXmlMapper", e);
            throw new IOException("Error converting to XML using JacksonXmlMapper", e);
        }
    }

    public byte[] exportToCsv(List<SpotifyTrack> tracks) throws IOException {
        StringBuilder csvContent = new StringBuilder("Title,Artist,Album,Added At\n"); // Added "Added At" to header
        for (SpotifyTrack track : tracks) {
            csvContent.append(convertToCsv(track)).append("\n");
        }
        return csvContent.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String convertToCsv(SpotifyTrack track) {
        String addedAtFormatted = track.getAddedAt() != null ? track.getAddedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : "";
        return String.format("\"%s\",\"%s\",\"%s\",\"%s\"",
                track.getTitle().replace("\"", "\"\""),
                track.getArtist().replace("\"", "\"\""),
                track.getAlbum().replace("\"", "\"\""),
                addedAtFormatted.replace("\"", "\"\""));
    }
}