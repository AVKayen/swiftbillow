package com.billow.swiftbillow.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

@Entity
public class Track {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String title;
    private String artist;
    private int length;
    private int trackNumber;
    private String path;

    @ManyToOne
    @JoinColumn(name = "album_id")
    @JsonBackReference
    private Album album;

    public Track() {}

    public Track(String title, String artist, int length, int trackNumber, String path, Album album) {
        this.title = title;
        this.artist = artist;
        this.length = length;
        this.trackNumber = trackNumber;
        this.path = path;
        this.album = album;
    }

    public Album getAlbum() {
        return album;
    }

    public String getTitle() {
        return title;
    }
    public String getArtist() {
        return artist;
    }
    public int getLength() {
        return length;
    }
    public int getTrackNumber() {
        return trackNumber;
    }
    public String getPath() {
        return path;
    }

    public Long getAlbumId() {
        return album.getId();
    }

    public void setTitle(String title) {
        this.title = title;
    }


    // getters and setters
}