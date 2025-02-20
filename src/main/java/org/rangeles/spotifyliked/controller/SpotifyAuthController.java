package org.rangeles.spotifyliked.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Controller
public class SpotifyAuthController {

    @GetMapping("/")
    public String home(@AuthenticationPrincipal OAuth2User user, Model model) {
        if (user != null) {
            model.addAttribute("name", user.getAttribute("display_name"));
            model.addAttribute("email", user.getAttribute("email"));
        }
        return "index"; // Redirects to index.html
    }

    @GetMapping("/login")
    public String login() {
        return "redirect:/oauth2/authorization/spotify"; // Redirect to Spotify login
    }

    @GetMapping("/user")
    public ResponseEntity<String> getUserDetails(OAuth2AuthenticationToken authentication) {
        return ResponseEntity.ok(authentication.getPrincipal().getAttributes().toString());
    }

    @GetMapping("/user-info")
    @ResponseBody
    public Map<String, Object> getUserInfo(OAuth2AuthenticationToken authentication) {
        return authentication.getPrincipal().getAttributes();
    }

    private static final String SPOTIFY_TOKEN_URL = "https://accounts.spotify.com/api/token";

    @Value("${spotify.client.id}")
    private String CLIENT_ID;

    @Value("${spotify.client.secret}")
    private String CLIENT_SECRET;

    public String refreshAccessToken(String refreshToken) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(CLIENT_ID, CLIENT_SECRET);

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

        return response.getBody().get("access_token").toString();
    }
}
