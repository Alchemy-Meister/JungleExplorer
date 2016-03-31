package com.creations.meister.jungleexplorer.domain;

import android.net.Uri;

/**
 * Created by meister on 3/28/16.
 */
public class Animal {

    private long id;
    private String photoId;
    private String name;
    private String locationText;
    private String description;

    public long getId() { return this.id; }

    public void setId(long id) { this.id = id; }

    public String getLocationText() { return this.locationText; }

    public void setLocationText(String locationText) { this.locationText = locationText; }

    public String getName() { return this.name; }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhotoId() {
        return this.photoId;
    }

    public void setPhotoId(String photoId) {
        this.photoId = photoId;
    }

    public String getDescription() { return this.description; }

    public void setDescription(String description) { this.description = description; }
}
