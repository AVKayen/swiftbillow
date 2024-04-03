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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Stream;


@Service
public class AlbumsService {
    private enum AudioFileExtensions {
        FLAC, MP3, WAV, OGG, WMA, M4A, AAC
    }

    private enum ImageFileExtensions {
        JPG, JPEG, PNG, BMP, GIF
    }

    private final AlbumRepository albumRepository;
    private final TrackRepository trackRepository;

    private static final String ROOT_PATH = "/home/kayen/StreamripDownloads/";
    private static final byte CRAWL_DEPTH = 1;
    private static final String NO_DATA_STRING = "~";

    public AlbumsService(AlbumRepository albumRepository, TrackRepository trackRepository) {
        this.albumRepository = albumRepository;
        this.trackRepository = trackRepository;
    }

    private void saveAlbum(Album album) {
        album.getTracks().sort(Comparator.comparingInt(Track::getTrackNumber));
        albumRepository.save(album);
        trackRepository.saveAll(album.getTracks());
    }

    private Album createNewAlbum(Path path) {
        return new Album(
                NO_DATA_STRING,
                NO_DATA_STRING,
                NO_DATA_STRING,
                0,
                NO_DATA_STRING,
                NO_DATA_STRING,
                path.getFileName().toString(),
                new ArrayList<>()
        );
    }

    public void folderRefresh(Path path) {
        if (!path.equals(Paths.get(ROOT_PATH))) {
            System.out.println(path);
            Album album = createNewAlbum(path);
            processAlbumPath(path, album);
            saveAlbum(album);
        }
    }

    private static boolean isImageFile(String fileName) {
        return Arrays.stream(ImageFileExtensions.values())
                .anyMatch(extension -> fileName.toUpperCase().endsWith(extension.toString()));
    }

    private static boolean isAudioFile(String fileName) {
        return Arrays.stream(AudioFileExtensions.values())
                .anyMatch(extension -> fileName.toUpperCase().endsWith(extension.toString()));
    }

    private static void processImageFile(Path path, Album album) {
        try {
            if (isImageFile(path.getFileName().toString())) {
                album.setCover(path.getFileName().toString());
                Files.copy(path, Paths.get(ROOT_PATH + album.getPath() + "/" + path.getFileName().toString()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void processAudioFile(Path file, Album album) {
        try {
            AudioFile audioFile = AudioFileIO.read(file.toFile());
            if (album.getTracks().isEmpty()) {
                getAlbumMetadataFromFile(audioFile, album);
            }
            Tag tag = audioFile.getTag();

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

    }

    private static void getAlbumMetadataFromFile(AudioFile audioFile, Album album) {
        Tag tag = audioFile.getTag();
        album.setArtist(tag.getFirst(FieldKey.ALBUM_ARTIST));
        album.setTitle(tag.getFirst(FieldKey.ALBUM));
        album.setGenre(tag.getFirst(FieldKey.GENRE));
        album.setQuality(audioFile.getAudioHeader().getFormat() + " "
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

    private static void processAlbumPath(Path path, Album album) {
        try (Stream<Path> paths = Files.walk(path, CRAWL_DEPTH)) {
            paths.filter(Files::isRegularFile)
                    .forEach(file -> {
                        if (isImageFile(file.getFileName().toString())) {
                            processImageFile(file, album);
                        } else if (isAudioFile(file.getFileName().toString())) {
                            processAudioFile(file, album);
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void fullRefresh() {
        albumRepository.deleteAll();
        trackRepository.deleteAll();
        try (Stream<Path> paths = Files.walk(Paths.get(ROOT_PATH), CRAWL_DEPTH)) {
            paths.filter(Files::isDirectory)
                    .forEach(this::folderRefresh);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static byte[] getFileByPath(String path) {
        try {
            return Files.readAllBytes(Paths.get(ROOT_PATH + path));
        } catch (IOException e) {
            return null;
        }
    }


    public static byte[] getCoverByPath(String path, int size) {
        try {
            return Files.readAllBytes(Paths.get(ROOT_PATH + path));
        } catch (IOException e) {
            return null;
        }
    }
}