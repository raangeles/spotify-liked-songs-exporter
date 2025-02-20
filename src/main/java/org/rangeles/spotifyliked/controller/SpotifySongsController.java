//package org.rangeles.spotifyliked.controller;
//
//import org.rangeles.spotifyliked.model.Song;
//import org.rangeles.spotifyliked.service.SpotifyService;
//import org.rangeles.spotifyliked.util.FileExporter;
//import org.springframework.web.bind.annotation.*;
//
//import java.io.IOException;
//import java.util.List;
//
//@RestController
//@RequestMapping("/api")
//public class SpotifySongsController {
//
//    private final SpotifyService spotifyService;
//    private final FileExporter fileExporter;
//
//    public SpotifySongsController(SpotifyService spotifyService, FileExporter fileExporter) {
//        this.spotifyService = spotifyService;
//        this.fileExporter = fileExporter;
//    }
//
//    @GetMapping("/liked-songs")
//    public List<Song> getLikedSongs(@RequestParam String accessToken) {
//        if (accessToken == null || accessToken.isBlank()) {
//            throw new IllegalArgumentException("Access token is required.");
//        }
//        return spotifyService.getLikedSongs(accessToken);
//    }
//
//    @PostMapping("/export")
//    public String exportSongs(@RequestParam String accessToken, @RequestParam String format) throws IOException {
//        if (accessToken == null || accessToken.isBlank()) {
//            throw new IllegalArgumentException("Access token is required.");
//        }
//
//        List<Song> songs = spotifyService.getLikedSongs(accessToken);
//        if (songs.isEmpty()) {
//            throw new IllegalArgumentException("No liked songs found.");
//        }
//
//        switch (format.toLowerCase()) {
//            case "xml":
//                fileExporter.exportToXML(songs);
//                return "Liked songs saved to liked_songs.xml";
//            case "csv":
//                fileExporter.exportToCSV(songs);
//                return "Liked songs saved to liked_songs.csv";
//            default:
//                throw new IllegalArgumentException("Invalid format. Use 'xml' or 'csv'.");
//        }
//    }
//}
