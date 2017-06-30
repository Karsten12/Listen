package com.fonsecakarsten.audiobooky;

import android.graphics.Bitmap;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Karsten on 6/16/2017.
 */

class AudioBook implements Serializable {
    private Bitmap coverImage;
    private String title;
    private String author;
    private String description;
    private String publisher;
    private String publishDate;
    private String ISBN;
    private ArrayList<String> pageText = new ArrayList<>();

    public void setPageText(String newPage) {
        pageText.add(newPage);
    }

    public Bitmap getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(Bitmap coverImage) {
        this.coverImage = coverImage;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPageText(int page) {
        return pageText.get(page);
    }

//    public int getChapters() {
//        return null
//    }

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
}