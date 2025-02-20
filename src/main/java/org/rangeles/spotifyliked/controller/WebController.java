//package org.rangeles.spotifyliked.controller;
//
//import org.rangeles.spotifyliked.model.Song;
//import org.rangeles.spotifyliked.service.SpotifyService;
//import org.rangeles.spotifyliked.util.FileExporter;
//import org.springframework.core.io.FileSystemResource;
//import org.springframework.core.io.Resource;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.List;
//
//@Controller
//public class WebController {
//
//    private final SpotifyService spotifyService;
//    private final FileExporter fileExporter;
//
//    public WebController(SpotifyService spotifyService, FileExporter fileExporter) {
//        this.spotifyService = spotifyService;
//        this.fileExporter = fileExporter;
//    }
//
//    @GetMapping("/")
//    public String index() {
//        return "index";
//    }
//
//    @GetMapping("/songs")
//    public String fetchSongs(@RequestParam String accessToken, Model model) {
//        List<Song> songs = spotifyService.getLikedSongs(accessToken);
//        model.addAttribute("songs", songs);
//        model.addAttribute("accessToken", accessToken);
//        return "index";
//    }
//
//    @PostMapping("/export")
//    public String exportSongs(@RequestParam String accessToken, @RequestParam String format, Model model) {
//        List<Song> songs = spotifyService.getLikedSongs(accessToken);
//        String filePath = null;
//
//        try {
//            if ("xml".equalsIgnoreCase(format)) {
//                fileExporter.exportToXML(songs);
//                filePath = "liked_songs.xml";
//            } else if ("csv".equalsIgnoreCase(format)) {
//                fileExporter.exportToCSV(songs);
//                filePath = "liked_songs.csv";
//            } else {
//                model.addAttribute("message", "Invalid format. Use 'xml' or 'csv'.");
//                return "index";
//            }
//
//            model.addAttribute("message", "File exported successfully! Click the download button.");
//            model.addAttribute("filePath", filePath);
//        } catch (IOException e) {
//            model.addAttribute("message", "Error saving file: " + e.getMessage());
//        }
//
//        model.addAttribute("songs", songs);
//        model.addAttribute("accessToken", accessToken);
//        return "index";
//    }
//
//    @GetMapping("/download")
//    public ResponseEntity<Resource> downloadFile(@RequestParam String filePath) {
//        File file = new File(filePath);
//        if (!file.exists()) {
//            return ResponseEntity.notFound().build();
//        }
//
//        Resource resource = new FileSystemResource(file);
//        return ResponseEntity.ok()
//                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getName())
//                .body(resource);
//    }
//}
