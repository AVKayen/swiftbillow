package com.billow.swiftbillow.repository;

import com.billow.swiftbillow.model.Track;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.stream.Collectors;

public interface TrackRepository extends JpaRepository<Track, Long> {
    default List<Track> findTracks(String search, String artist) {
        return findAll().stream()
                .filter(track -> search == null || track.getTitle().toLowerCase().contains(search.toLowerCase()))
                .filter(track -> artist == null || track.getArtist().toLowerCase().contains(artist.toLowerCase()))
                .collect(Collectors.toList());
    }
}