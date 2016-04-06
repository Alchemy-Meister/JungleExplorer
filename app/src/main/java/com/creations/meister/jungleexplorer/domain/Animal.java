package com.creations.meister.jungleexplorer.domain;

/**
 * Created by meister on 3/28/16.
 */
public class Animal extends Domain {

    private String locationText;
    private String description;
    private int favorite;

    public Animal() {
        this.setName("");
        this.setPhotoId("");
        this.setLocationText("");
        this.setDescription("");
        this.setFavorite(0);
    }

    public String getLocationText() { return this.locationText; }

    public void setLocationText(String locationText) { this.locationText = locationText; }

    public String getDescription() { return this.description; }

    public void setDescription(String description) { this.description = description; }

    public int getFavorite() {
        return favorite;
    }

    public void setFavorite(int favorite) {
        this.favorite = favorite;
    }
}
