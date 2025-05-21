package org.rangeles.spotifyliked.util;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.text.StringEscapeUtils;
import org.rangeles.spotifyliked.model.SpotifyTrack;
import org.rangeles.spotifyliked.model.SpotifyTrackList;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class FileExporter {

    private static final String XML_FILE_PATH = "liked_songs.xml";
    private static final String CSV_FILE_PATH = "liked_songs.csv";

    public void exportToXML(List<SpotifyTrackList> trackLists, OutputStream outputStream) throws Exception {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));

        writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        writer.write("<tracklist>\n");

        for (SpotifyTrackList track : trackLists) {
            String title = StringEscapeUtils.escapeXml10(track.getTitle());
            String artist = StringEscapeUtils.escapeXml10(track.getArtist());
            String album = StringEscapeUtils.escapeXml10(track.getAlbum());

            writer.write("  <item>\n");
            writer.write("    <name>" + title + "</name>\n");
            writer.write("    <artist>" + artist + "</artist>\n");
            writer.write("    <album>" + album + "</album>\n");
            writer.write("  </item>\n");
        }

        writer.write("</tracklist>\n");
        writer.flush();
        writer.close();
    }

    // Export to CSV
    public void exportToCSV(List<SpotifyTrack> trackList) throws IOException {
        FileWriter fileWriter = new FileWriter(CSV_FILE_PATH);
        CSVPrinter csvPrinter = new CSVPrinter(fileWriter, CSVFormat.DEFAULT.withHeader("Title", "Artist", "Album", "Spotify URL"));

        for (SpotifyTrack track : trackList) {
            csvPrinter.printRecord(track.getTitle(), track.getArtist(), track.getAlbum(), track.getUrl());
        }

        csvPrinter.flush();
        csvPrinter.close();
    }
}
