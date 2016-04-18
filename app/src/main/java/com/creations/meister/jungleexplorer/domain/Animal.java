package com.creations.meister.jungleexplorer.domain;

import android.location.Location;

import java.util.ArrayList;

/**
 * Created by meister on 3/28/16.
 */
public class Animal extends Domain {

    private String locationText;
    private Double latitude;
    private Double longitude;
    private String description;
    private int favorite;

    private ArrayList<Expert> animalExperts;
    private ArrayList<Group> animalGroups;

    public Animal() {
        this.setName("");
        this.setPhotoId("");
        this.setLocationText("");
        this.setDescription("");
        this.setFavorite(0);
        this.latitude = null;
        this.longitude = null;
        animalExperts = new ArrayList<>();
        animalGroups = new ArrayList<>();
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

    public ArrayList<Expert> getAnimalExperts() {
        return animalExperts;
    }

    public void setAnimalExperts(ArrayList<Expert> animalExperts) {
        this.animalExperts = animalExperts;
    }

    public ArrayList<Group> getAnimalGroups() {
        return animalGroups;
    }

    public void setAnimalGroups(ArrayList<Group> animalGroups) {
        this.animalGroups = animalGroups;
    }

    public boolean isWithinRadius(Location center, int radius) {
        boolean within = false;
        if(center != null && this.latitude != null && this.longitude != null) {
            Location animalLocation = new Location("");
            animalLocation.setLatitude(this.latitude);
            animalLocation.setLongitude(this.longitude);
            if(center.distanceTo(animalLocation) <= radius) {
                within = true;
            }
        }
        return within;
    }
}
