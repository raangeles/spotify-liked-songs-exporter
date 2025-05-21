package org.rangeles.spotifyliked.util;

import org.rangeles.spotifyliked.model.SpotifyTrack;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.opencsv.CSVWriter;
import org.rangeles.spotifyliked.model.SpotifyTrackList;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

@Service
public class FileService {

    public void saveAsCSV(List<SpotifyTrack> tracks, String filename) throws IOException {
        try (FileWriter fileWriter = new FileWriter(filename);
             CSVWriter csvWriter = new CSVWriter(fileWriter)) {

            String[] header = {"Title", "Artist", "Album", "URL"}; // Corrected header
            csvWriter.writeNext(header);

            for (SpotifyTrack track : tracks) {
                csvWriter.writeNext(new String[]{track.getTitle(), track.getArtist(), track.getAlbum(), track.getUrl()});
            }
        }
    }

    public void saveAsXML(List<SpotifyTrack> tracks, String filename) throws IOException {
        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.writeValue(new File(filename), new SpotifyTrackList(tracks)); // Use SpotifyTrackList
    }
}
