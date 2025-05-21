package org.rangeles.spotifyliked.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.rangeles.spotifyliked.service.SpotifyAuthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;


@Controller
public class SpotifyAuthController {

    private final SpotifyAuthService spotifyAuthService;

    @Autowired
    public SpotifyAuthController(SpotifyAuthService spotifyAuthService) {
        this.spotifyAuthService = spotifyAuthService;
    }

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

}