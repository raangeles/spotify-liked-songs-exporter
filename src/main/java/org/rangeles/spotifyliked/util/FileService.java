package org.rangeles.spotifyliked.util;

import org.rangeles.spotifyliked.model.SpotifyTrack;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.opencsv.CSVWriter;
import org.springframework.stereotype.Service;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

@Service
public class FileService {

    public void saveAsCSV(List<SpotifyTrack> tracks, String filename) throws IOException {
        FileWriter fileWriter = new FileWriter(filename);
        CSVWriter csvWriter = new CSVWriter(fileWriter);

        String[] header = {"Name", "Artist", "Album", "URL"};
        csvWriter.writeNext(header);

        for (SpotifyTrack track : tracks) {
            csvWriter.writeNext(new String[]{track.getName(), track.getArtist(), track.getAlbum(), track.getUrl()});
        }

        csvWriter.close();
    }

    public void saveAsXML(List<SpotifyTrack> tracks, String filename) throws IOException {
        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.writeValue(new File(filename), tracks);
    }
}
