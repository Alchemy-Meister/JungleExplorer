package com.creations.meister.jungleexplorer.domain;

/**
 * Created by meister on 3/30/16.
 */
public class Group {

    private long id;
    private String photoId;
    private String name;

    public long getId() { return this.id; }

    public void setId(long id) { this.id = id; }

    public String getName() { return this.name; }

    public void setName(String name) { this.name = name; }

    public String getPhotoId() { return this.photoId; }

    public void setPhotoId(String photoId) { this.photoId = photoId; }
}
