package com.escoladeltreball.cloudfile;

import com.google.firebase.database.Exclude;

/*
 / Aquesta classe serà la que crei els objectes que pujarem a Firebase.
 */
public class Upload {
    private String mName;
    private String mUrl;
    private String mKey;

    public Upload() {
    }

    public Upload(String name, String url) {
        if (name.trim().equals("")) {
            name = "No Name";
        }
        this.mName = name;
        this.mUrl = url;
    }

    public String getmName() {
        return mName;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }

    public String getmUrl() {
        return mUrl;
    }

    public void setmUrl(String mUrl) {
        this.mUrl = mUrl;
    }

    @Exclude
    public String getKey() {
        return mKey;
    }

    @Exclude
    public void setKey(String key) {
        mKey = key;
    }
}
