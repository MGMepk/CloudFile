package com.escoladeltreball.cloudfile;

import com.google.firebase.database.Exclude;

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

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String mUrl) {
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
