package org.rangeles.spotifyliked.util;

import org.rangeles.spotifyliked.model.Song;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Component;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

@Component
public class FileExporter {

    private static final String XML_FILE_PATH = "liked_songs.xml";
    private static final String CSV_FILE_PATH = "liked_songs.csv";

    // Export to XML
    public void exportToXML(List<Song> songs) throws IOException {
        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.writeValue(new File(XML_FILE_PATH), songs);
    }

    // Export to CSV
    public void exportToCSV(List<Song> songs) throws IOException {
        FileWriter fileWriter = new FileWriter(CSV_FILE_PATH);
        CSVPrinter csvPrinter = new CSVPrinter(fileWriter, CSVFormat.DEFAULT.withHeader("Title", "Artist", "Album", "Spotify URL"));

        for (Song song : songs) {
            csvPrinter.printRecord(song.getTitle(), song.getArtist(), song.getAlbum(), song.getSpotifyUrl());
        }

        csvPrinter.flush();
        csvPrinter.close();
    }
}
