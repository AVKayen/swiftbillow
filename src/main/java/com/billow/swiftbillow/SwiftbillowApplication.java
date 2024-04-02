package com.billow.swiftbillow;

import com.billow.swiftbillow.model.Album;
import com.billow.swiftbillow.model.AlbumShort;
import com.billow.swiftbillow.model.Track;
import com.billow.swiftbillow.repository.AlbumRepository;
import com.billow.swiftbillow.repository.TrackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@SpringBootApplication
@RestController
public class SwiftbillowApplication {
    private final AlbumRepository albumRepository;
    private final TrackRepository trackRepository;

    private static final String PATH = "/home/kayen/StreamripDownloads/";

    @Autowired
    public SwiftbillowApplication(AlbumsService albumsService, AlbumRepository albumRepository, TrackRepository trackRepository) {
        albumsService.albumsRefresh();
        this.albumRepository = albumRepository;
        this.trackRepository = trackRepository;
    }
/*
        private AlbumFull findAlbumById(String id) {
            return albums.stream()
                    .filter(album -> album.getId().equals(id))
                    .findFirst()
                    .orElse(null);
        }
*/
    public static void main(String[] args) {
        SpringApplication.run(SwiftbillowApplication.class, args);
    }

    @GetMapping("/")
    public Map<String, String> index() {
        Map<String, String> response = new HashMap<>();
        response.put("swift", "silver");
        return response;
    }

    @GetMapping("/albums")
    public List<AlbumShort> albums(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String artist,
            @RequestParam(required = false) String genre
    ) {
        return albumRepository.findShorts(search, artist, genre);
    }

    @GetMapping("/albums/{id}")
    public Album album(
            @PathVariable String id
    ) {
        return albumRepository.getFullById(Long.parseLong(id));
    }

    @GetMapping("/albums/{id}/cover")
    public ResponseEntity<byte[]> cover(
            @PathVariable String id,
            @RequestParam (required = false) String size
    ) {
        if (size == null) {
            size = "0";
        }
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(AlbumsService.cover(albumRepository.getFullById(Long.parseLong(id)).getCoverPath(), Integer.parseInt(size)));

    }
    @GetMapping("/tracks")
    public List<Track> tracks(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String artist
    ) {
        return trackRepository.findTracks(search, artist);
    }

    @GetMapping("/play/{id}/{track}")
    public ResponseEntity<byte[]> play(
            @PathVariable("id") String id,
            @PathVariable("track") int track
    ) {
        String filePath = albumRepository.getFullById(Long.parseLong(id)).getPath() + "/" +
                albumRepository.getFullById(Long.parseLong(id)).getTracks().get(track-1).getPath();
        System.out.println(filePath);
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("audio/flac"))
                .body(AlbumsService.getTrackFile(filePath));
    }

    @GetMapping("/genres")
    public List<String> genres() {
        return albumRepository.getGenres();
    }

    @GetMapping("/artists")
    public List<String> artists() {
        return albumRepository.getArtists();
    }

}
