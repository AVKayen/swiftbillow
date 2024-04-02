package com.billow.swiftbillow.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.util.List;

@Entity
public class Album {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String title;
    private String artist;
    private String genre;
    private int album_year;
    private String quality;
    private String cover;

    private String path;

    @OneToMany(mappedBy = "album", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Track> tracks;

    public Album() {}

    public Album(String title, String artist, String genre, int album_year, String quality, String cover, String path, List<Track> tracks) {
        this.title = title;
        this.artist = artist;
        this.genre = genre;
        this.album_year = album_year;
        this.quality = quality;
        this.cover = cover;
        this.path = path;
        this.tracks = tracks;
    }

    public AlbumShort toAlbumShort() {
        return new AlbumShort(
                String.valueOf(this.id),
                this.title,
                this.artist
        );
    }

    public String getTitle() {
        return title;
    }

    public Long getId() {
        return id;
    }

    public String getArtist() {
        return artist;
    }

    public String getGenre() {
        return genre;
    }

    public int getAlbumYear() {
        return album_year;
    }

    public String getQuality() {
        return quality;
    }

    public String getCover() {
        return cover;
    }

    public String getCoverPath() {
        return path + "/" + cover;
    }

    public String getPath() {
        return path;
    }

    public List<Track> getTracks() {
        return tracks;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public void setAlbum_year(int year) {
        this.album_year = year;
    }

    public void setQuality(String quality) {
        this.quality = quality;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public void setTracks(List<Track> tracks) {
        this.tracks = tracks;
    }

    public void addTrack(Track track) {
        this.tracks.add(track);
    }
}