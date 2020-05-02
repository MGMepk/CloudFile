package com.escoladeltreball.cloudfile;

public class AudioUpload {
    private String aName;
    private String mAudioUrl;
    private String mKey;

    public AudioUpload() {
    }

    public AudioUpload(String name, String audioUrl) {
        if (name.trim().equals("")) {
            name = "No Name";
        }
        this.aName = name;
        this.mAudioUrl = audioUrl;
    }

    public String getaName() {
        return aName;
    }

    public void setaName(String aName) {
        this.aName = aName;
    }

    public String getmAudioUrl() {
        return mAudioUrl;
    }

    public void setmAudioUrl(String mAudioUrl) {
        this.mAudioUrl = mAudioUrl;
    }

    public String getmKey() {
        return mKey;
    }

    public void setmKey(String mKey) {
        this.mKey = mKey;
    }
}
