package com.creations.meister.jungleexplorer.domain;

/**
 * Created by meister on 3/28/16.
 */
public class Animal extends Domain {

    private String locationText;
    private Double latitude;
    private Double longitude;
    private String description;
    private int favorite;

    public Animal() {
        this.setName("");
        this.setPhotoId("");
        this.setLocationText("");
        this.setDescription("");
        this.setFavorite(0);
        this.latitude = null;
        this.longitude = null;
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

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}
