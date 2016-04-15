package com.creations.meister.jungleexplorer.domain;

import android.net.Uri;

import java.io.Serializable;

/**
 * Created by meister on 3/30/16.
 */
public class Expert extends Domain implements Serializable {
    private String contactUri;

    public Uri getContactUri() {
        return Uri.parse(contactUri) ;
    }

    public void setContactUri(Uri contactUri) {
        this.contactUri = contactUri.toString();
    }
}
