package com.creations.meister.jungleexplorer.domain;

import android.net.Uri;

/**
 * Created by meister on 3/30/16.
 */
public class Expert extends Domain {
    private Uri contactUri;

    public Uri getContactUri() {
        return contactUri;
    }

    public void setContactUri(Uri contactUri) {
        this.contactUri = contactUri;
    }
}
