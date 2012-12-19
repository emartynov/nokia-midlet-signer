package com.ebuddy.nokia.s40;

/**
 * User: Eugen
 */
public class UploadFile {
    private String name;
    private String filename;
    private String mimeType;

    public UploadFile(String name, String filename, String mimeType) {
        this.name = name;
        this.filename = filename;
        this.mimeType = mimeType;
    }

    public String getMimeType() {
        return mimeType;
    }

    public String getFilename() {
        return filename;
    }

    public String getName() {
        return name;
    }
}
