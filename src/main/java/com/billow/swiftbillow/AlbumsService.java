package com.billow.swiftbillow;

import com.billow.swiftbillow.model.Album;
import com.billow.swiftbillow.model.Track;
import com.billow.swiftbillow.repository.AlbumRepository;
import com.billow.swiftbillow.repository.TrackRepository;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;


@Service
public class AlbumsService {
    private final AlbumRepository albumRepository;
    private final TrackRepository trackRepository;

    private static final String PATH = "/home/kayen/StreamripDownloads/";

    public AlbumsService(AlbumRepository albumRepository, TrackRepository trackRepository) {
        this.albumRepository = albumRepository;
        this.trackRepository = trackRepository;
    }

    public void albumsRefresh() {
        albumRepository.deleteAll();
        trackRepository.deleteAll();
        try {
            Files.walk(Paths.get(PATH), 1)
                    .filter(Files::isDirectory)
                    .forEach(path -> {
                        if (!path.equals(Paths.get(PATH))) {
                            System.out.println(path);
                            Album album = new Album(
                                    "-",
                                    "-",
                                    "-",
                                    0,
                                    "-",
                                    "-",
                                    path.getFileName().toString(),
                                    new ArrayList<>()
                            );

                            try {
                                Files.walk(path)
                                        .filter(Files::isRegularFile)
                                        .forEach(file -> {
                                            if (file.toString().endsWith(".flac")) {
                                                try {
                                                    AudioFile audioFile = AudioFileIO.read(file.toFile());
                                                    Tag tag = audioFile.getTag();

                                                    if (album.getArtist().equals("-")) {
                                                        album.setArtist(tag.getFirst(FieldKey.ALBUM_ARTIST));
                                                        album.setTitle(tag.getFirst(FieldKey.ALBUM));
                                                        album.setGenre(tag.getFirst(FieldKey.GENRE));
                                                        album.setQuality("FLAC "
                                                                + audioFile.getAudioHeader().getBitsPerSample()
                                                                + "bit/" + String.format("%.1f", Float.parseFloat(audioFile.getAudioHeader().getSampleRate()) / 1000) + "kHz");

                                                        String dateStr = tag.getFirst(FieldKey.YEAR);
                                                        if (dateStr != null && !dateStr.isEmpty()) {
                                                            try {
                                                                album.setAlbum_year(Integer.parseInt(dateStr));
                                                            } catch (NumberFormatException e) {
                                                                try {
                                                                    LocalDate date = LocalDate.parse(dateStr);
                                                                    album.setAlbum_year(date.getYear());
                                                                } catch (DateTimeParseException ex) {
                                                                    ex.printStackTrace();
                                                                }
                                                            }
                                                        }
                                                    }

                                                    Track track = new Track(
                                                            tag.getFirst(FieldKey.TITLE),
                                                            tag.getFirst(FieldKey.ARTIST),
                                                            audioFile.getAudioHeader().getTrackLength(),
                                                            Integer.parseInt(tag.getFirst(FieldKey.TRACK)),
                                                            file.getFileName().toString(),
                                                            album
                                                    );


                                                    album.addTrack(track);

                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            } else if (file.toString().endsWith(".jpg")
                                                    || file.toString().endsWith(".png")) {
                                                album.setCover(file.getFileName().toString());
                                            }

                                        });
                                album.getTracks().sort(Comparator.comparingInt(Track::getTrackNumber));

                                albumRepository.save(album);
                            trackRepository.saveAll(album.getTracks());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static byte[] cover(String coverPath, int size) {
        try {
            return Files.readAllBytes(Paths.get(PATH + coverPath));
        } catch (IOException e) {
            return null;
        }
    }

    public static byte[] getTrackFile(String filePath) {
        String path = PATH + filePath;
        try {
            return Files.readAllBytes(Paths.get(path));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}