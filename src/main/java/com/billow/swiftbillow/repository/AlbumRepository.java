package com.billow.swiftbillow.repository;

import com.billow.swiftbillow.model.AlbumShort;
import com.billow.swiftbillow.model.Album;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.stream.Collectors;

public interface AlbumRepository extends JpaRepository<Album, Long> {
    default List<AlbumShort> findShorts(String search, String artist, String genre) {
        return findAll().stream()
                .filter(album -> search == null || album.getTitle().toLowerCase().contains(search.toLowerCase()))
                .filter(album -> artist == null || album.getArtist().toLowerCase().contains(artist.toLowerCase()))
                .filter(album -> genre == null || album.getGenre().toLowerCase().contains(genre.toLowerCase()))
                .map(Album::toAlbumShort)
                .collect(Collectors.toList());
    }

    default Album getFullById(Long id) {
        return findById(id).orElse(null);
    }

    default List<String> getGenres() {
        return findAll().stream()
                .map(Album::getGenre)
                .distinct()
                .collect(Collectors.toList());
    }

    default List<String> getArtists() {
        return findAll().stream()
                .map(Album::getArtist)
                .distinct()
                .collect(Collectors.toList());
    }
}