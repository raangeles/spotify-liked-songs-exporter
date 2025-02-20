package org.rangeles.spotifyliked.controller;


import org.rangeles.spotifyliked.model.SpotifyTrack;
import org.rangeles.spotifyliked.util.FileService;
import org.rangeles.spotifyliked.service.SpotifyService;
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
import java.util.List;
import java.util.Map;

@Controller
public class SpotifyController {

    private final SpotifyService spotifyService;
    private final FileService fileService;

    public SpotifyController(SpotifyService spotifyService, FileService fileService) {
        this.spotifyService = spotifyService;
        this.fileService = fileService;
    }

    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;

    @GetMapping("/fetchLikedSongs")
    @ResponseBody
    public String fetchLikedSongs(OAuth2AuthenticationToken authentication,
                                  @RequestParam(required = false, defaultValue = "csv") String format) {
        Map<String, Object> attributes = authentication.getPrincipal().getAttributes();


        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                authentication.getAuthorizedClientRegistrationId(),
                authentication.getName()
        );

        if (client == null || client.getAccessToken() == null) {
            return "Error: Access token not found. Please re-authenticate.";
        }

        String accessToken = client.getAccessToken().getTokenValue();
        String refreshToken = client.getRefreshToken().getTokenValue();
        //authentication.getPrincipal().getAttribute("refresh_token");

        System.out.println("Retrieved Access Token: " + accessToken); // Debugging

        //return "Token retrieved successfully";
        List<SpotifyTrack> tracks = spotifyService.getLikedSongs(accessToken, refreshToken);

        try {
            if ("xml".equalsIgnoreCase(format)) {
                fileService.saveAsXML(tracks, "liked_songs.xml");
                return "Liked songs saved as liked_songs.xml";
            } else {
                fileService.saveAsCSV(tracks, "liked_songs.csv");
                return "Liked songs saved as liked_songs.csv";
            }
        } catch (IOException e) {
            return "Error saving file: " + e.getMessage();
        }
    }

    @RestController
    @RequestMapping("/download")
    public class SpotifyDownloadController {

        @Autowired
        private SpotifyService spotifyService;
        @Autowired
        private OAuth2AuthorizedClientService authorizedClientService;

        @GetMapping("/csv")
        public ResponseEntity<byte[]> downloadCsv(OAuth2AuthenticationToken authentication) {

            OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                    authentication.getAuthorizedClientRegistrationId(),
                    authentication.getName()
            );

            if (client == null) {
                throw new RuntimeException("OAuth2AuthorizedClient not found for user!");
            }

            String accessToken = client.getAccessToken().getTokenValue();
            String refreshToken = client.getRefreshToken().getTokenValue();

            if (accessToken == null) {
                throw new RuntimeException("Access token is missing!");
            }

            List<SpotifyTrack> likedSongs = spotifyService.getLikedSongs(accessToken, refreshToken);
            String csvContent = spotifyService.convertToCsv(likedSongs);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);
            headers.setContentDispositionFormData("attachment", "liked_songs.csv");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(csvContent.getBytes());
        }

        @GetMapping("/xml")
        public ResponseEntity<byte[]> downloadXml(OAuth2AuthenticationToken authentication) {

            OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                    authentication.getAuthorizedClientRegistrationId(),
                    authentication.getName()
            );

            if (client == null || client.getAccessToken() == null) {
                throw new RuntimeException("Access token is missing or expired.");
            }

            String accessToken = client.getAccessToken().getTokenValue();
            String refreshToken = client.getRefreshToken().getTokenValue();

            if (accessToken == null) {
                throw new RuntimeException("Access token is missing!");
            }
            //return ResponseEntity.ok(spotifyService.getLikedSongs(accessToken, refreshToken));

            List<SpotifyTrack> likedSongs = spotifyService.getLikedSongs(accessToken, refreshToken);
            String xmlContent = null;
            try {
                xmlContent = spotifyService.convertToXml(likedSongs);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_XML);
            headers.setContentDispositionFormData("attachment", "liked_songs.xml");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(xmlContent.getBytes());
        }
    }
}
