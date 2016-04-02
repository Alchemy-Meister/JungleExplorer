package com.creations.meister.jungleexplorer.domain;

/**
 * Created by meister on 4/1/16.
 */
public class Domain {

    private long id;
    private String photoId;
    private String name;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPhotoId() {
        return photoId;
    }

    public void setPhotoId(String photoId) {
        this.photoId = photoId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
