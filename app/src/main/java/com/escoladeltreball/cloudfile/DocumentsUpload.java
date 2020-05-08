package com.escoladeltreball.cloudfile;

public class DocumentsUpload {
    private String aName;
    private String mDocUrl;
    private String mKey;


    public DocumentsUpload() {
    }

    public DocumentsUpload(String name, String mDocUrl) {
        if (name.trim().equals("")) {
            name = "No Name";
        }
        this.aName = name;
        this.mDocUrl = mDocUrl;
    }

    public String getaName() {
        return aName;
    }

    public void setaName(String aName) {
        this.aName = aName;
    }

    public String getmDocUrl() {
        return mDocUrl;
    }

    public void setmDocUrl(String mDocUrl) {
        this.mDocUrl = mDocUrl;
    }

    public String getmKey() {
        return mKey;
    }

    public void setmKey(String mKey) {
        this.mKey = mKey;
    }
}
