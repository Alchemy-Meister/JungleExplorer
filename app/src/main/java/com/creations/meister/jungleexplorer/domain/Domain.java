package com.creations.meister.jungleexplorer.domain;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by meister on 4/1/16.
 */
public class Domain implements Comparable<Domain>, Serializable {

    private int id;
    private String photoId;
    private String name;

    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    public static ArrayList<Domain> getRandomDomains()
    {
        ArrayList<Domain> result=new ArrayList<>();
        Random r=new Random();
        StringBuilder sb=new StringBuilder();
        for(int i=0;i<1000;++i)
        {
            Domain animal = new Domain();
            sb.delete(0,sb.length());
            int strLength=r.nextInt(10)+1;
            for(int j=0;j<strLength;++j)
                switch(r.nextInt(3))
                {
                    case 0:
                        sb.append((char)('a'+r.nextInt('z'-'a')));
                        break;
                    case 1:
                        sb.append((char)('A'+r.nextInt('Z'-'A')));
                        break;
                    case 2:
                        sb.append((char)('0'+r.nextInt('9'-'0')));
                        break;
                }

            animal.setName(sb.toString());
            result.add(animal);
        }
        return result;
    }
}
