package org.rangeles.spotifyliked.service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;

@Service
public class SpotifyAuthService {

    private final String SPOTIFY_TOKEN_URL;
    private final String CLIENT_ID;
    private final String CLIENT_SECRET;

    private final RestTemplate restTemplate;

    public SpotifyAuthService(
            @Value("${spotify.token.url}") String spotifyTokenUrl,
            @Value("${spotify.client.id}") String clientId,
            @Value("${spotify.client.secret}") String clientSecret) {
        this.SPOTIFY_TOKEN_URL = spotifyTokenUrl;
        this.CLIENT_ID = clientId;
        this.CLIENT_SECRET = clientSecret;
        this.restTemplate = new RestTemplate();
    }

    public String refreshAccessToken(String refreshToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(CLIENT_ID, CLIENT_SECRET);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "refresh_token");
        body.add("refresh_token", refreshToken);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                SPOTIFY_TOKEN_URL,
                HttpMethod.POST,
                request,
                Map.class
        );

        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody().get("access_token").toString();
        } else {
            throw new RuntimeException("Failed to refresh access token: " + response.getBody());
        }
    }
}
