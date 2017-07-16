package com.fonsecakarsten.audiobooky;

import java.io.Serializable;

/**
 * Created by Karsten on 6/16/2017.
 * Audiobook template
 */

class AudioBook implements Serializable {
    private String title;
    private String author;
    private String coverImagePath;

    private String subtitle;
    private String publisher;
    private String absolutePath;
    private String publishDate;
    private String description;
    private int rating;
    private String ISBN;

    String getCoverImagePath() {
        return coverImagePath;
    }

    void setCoverImagePath(String coverImagePath) {
        this.coverImagePath = coverImagePath;
    }

    String getTitle() {
        if (subtitle != null) {
            return title + ": " + subtitle;
        }
        return title;
    }

    void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    String getAuthor() {
        return author;
    }

    void setAuthor(String author) {
        this.author = author;
    }

    public String getISBN() {
        return ISBN;
    }

    public void setISBN(String ISBN) {
        this.ISBN = ISBN;
    }

    public String getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(String publishDate) {
        this.publishDate = publishDate;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAbsolutePath() {
        return absolutePath;
    }

    public void setAbsolutePath(String absolutePath) {
        this.absolutePath = absolutePath;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }
}