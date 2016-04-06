package com.creations.meister.jungleexplorer.domain;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.io.Serializable;

/**
 * Created by meister on 4/1/16.
 */
public class Domain implements Comparable<Domain>, Serializable {

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

    @Override
    public int compareTo(@NonNull Domain another) {
        char thisFirstLetter = TextUtils.isEmpty(
                this.getName()) ? ' ' : this.getName().charAt(0);
        char anotherFirstLetter = TextUtils.isEmpty(
                another.getName()) ? ' ' : another.getName().charAt(0);
        int firstLetterComparison = Character.toUpperCase(thisFirstLetter)
                - Character.toUpperCase(anotherFirstLetter);
        if (firstLetterComparison == 0)
            return this.getName().compareTo(another.getName());
        return firstLetterComparison;
    }
}
