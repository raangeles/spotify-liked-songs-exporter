package org.rangeles.spotifyliked.controller;

import org.rangeles.spotifyliked.model.SpotifyTrack;
import org.rangeles.spotifyliked.util.FileService;
import org.rangeles.spotifyliked.service.SpotifyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;


@Controller
public class SpotifyController {

    private static final Logger logger = LoggerFactory.getLogger(SpotifyController.class);

    private final SpotifyService spotifyService;
    private final FileService fileService;
    private final OAuth2AuthorizedClientService authorizedClientService;

    @Autowired
    public SpotifyController(SpotifyService spotifyService, FileService fileService, OAuth2AuthorizedClientService authorizedClientService) {
        this.spotifyService = spotifyService;
        this.fileService = fileService;
        this.authorizedClientService = authorizedClientService;
    }

    @GetMapping("/fetchLikedSongs")
    @ResponseBody
    public ResponseEntity<byte[]> fetchLikedSongs(OAuth2AuthenticationToken authentication) throws IOException {
        OAuth2AuthorizedClient authorizedClient = getAuthorizedClient(authentication);
        if (authorizedClient == null) {
            return ResponseEntity.status(401).body("Unauthorized: Could not retrieve authorized client.".getBytes(StandardCharsets.UTF_8));
        }
        String accessToken = authorizedClient.getAccessToken().getTokenValue();
        String refreshToken = authorizedClient.getRefreshToken().getTokenValue();

        // Get Spotify user ID from authentication
        String userId = authentication.getName();

        List<SpotifyTrack> likedSongs = spotifyService.getLikedSongs(accessToken, refreshToken, userId);
        if (likedSongs.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        try {
            String xmlContent = spotifyService.exportToXml(likedSongs);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_XML);
            headers.setContentDispositionFormData("attachment", "liked_songs.xml");
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(xmlContent.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(("Error generating XML: " + e.getMessage()).getBytes(StandardCharsets.UTF_8));
        }
    }


    @GetMapping("/download/csv")
    public ResponseEntity<byte[]> downloadCsv(OAuth2AuthenticationToken authentication) {
        logger.info("Entering downloadCsv");
        try {
            OAuth2AuthorizedClient authorizedClient = getAuthorizedClient(authentication);
            if (authorizedClient == null) {
                logger.error("Could not retrieve authorized client.");
                return ResponseEntity.status(401).body("Unauthorized: Could not retrieve authorized client.".getBytes(StandardCharsets.UTF_8));
            }
            String accessToken = authorizedClient.getAccessToken().getTokenValue();
            String refreshToken = authorizedClient.getRefreshToken().getTokenValue();
            String userId = authentication.getName();
            logger.debug("Retrieved Access Token: {}", accessToken);

            List<SpotifyTrack> likedSongs = spotifyService.getLikedSongs(accessToken, refreshToken, userId);
            if (likedSongs.isEmpty()) {
                logger.warn("No liked songs found for download.");
                return ResponseEntity.noContent().build();
            }

            byte[] csvBytes = spotifyService.exportToCsv(likedSongs);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv"));
            headers.setContentDispositionFormData("attachment", "liked_songs.csv");

            logger.info("CSV download prepared.");
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(csvBytes);
        } catch (Exception e) {
            logger.error("Error generating CSV", e);
            return ResponseEntity.status(500).body(("Error generating CSV: " + e.getMessage()).getBytes(StandardCharsets.UTF_8));
        } finally {
            logger.info("Exiting downloadCsv");
        }
    }

    @GetMapping("/download/xml")
    public ResponseEntity<byte[]> downloadXml(OAuth2AuthenticationToken authentication) {
        logger.info("Entering downloadXml");
        try {
            OAuth2AuthorizedClient authorizedClient = getAuthorizedClient(authentication);
            if (authorizedClient == null) {
                logger.error("Could not retrieve authorized client.");
                return ResponseEntity.status(401).body("Unauthorized: Could not retrieve authorized client.".getBytes(StandardCharsets.UTF_8));
            }
            String accessToken = authorizedClient.getAccessToken().getTokenValue();
            String refreshToken = authorizedClient.getRefreshToken().getTokenValue();
            String userId = authentication.getName();

            logger.debug("Retrieved Access Token: {}", accessToken);

            List<SpotifyTrack> likedSongs = spotifyService.getLikedSongs(accessToken, refreshToken, userId);
            if (likedSongs.isEmpty()) {
                logger.warn("No liked songs found for download.");
                return ResponseEntity.noContent().build();
            }

            String xmlContent = spotifyService.exportToXml(likedSongs);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_XML);
            headers.setContentDispositionFormData("attachment", "liked_songs.xml");

            logger.info("XML download prepared.");
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(xmlContent.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            logger.error("Error generating XML", e);
            return ResponseEntity.status(500).body(("Error generating XML: " + e.getMessage()).getBytes(StandardCharsets.UTF_8));
        } finally {
            logger.info("Exiting downloadXml");
        }
    }

    private OAuth2AuthorizedClient getAuthorizedClient(OAuth2AuthenticationToken authentication) {
        return authorizedClientService.loadAuthorizedClient(
                authentication.getAuthorizedClientRegistrationId(),
                authentication.getName());
    }
}
