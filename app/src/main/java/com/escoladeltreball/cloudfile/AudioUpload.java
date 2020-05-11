package com.escoladeltreball.cloudfile;

public class AudioUpload {
    private String aName;
    private String mAudioUrl;
    private String mKey;

    public AudioUpload() {
    }

    AudioUpload(String name, String audioUrl) {
        if (name.trim().equals("")) {
            name = "No Name";
        }
        this.aName = name;
        this.mAudioUrl = audioUrl;
    }

    public String getName() {
        return aName;
    }

    public void setName(String aName) {
        this.aName = aName;
    }

    public String getAudioUrl() {
        return mAudioUrl;
    }

    public void setAudioUrl(String mAudioUrl) {
        this.mAudioUrl = mAudioUrl;
    }

    public String getKey() {
        return mKey;
    }

    public void setKey(String mKey) {
        this.mKey = mKey;
    }
}
