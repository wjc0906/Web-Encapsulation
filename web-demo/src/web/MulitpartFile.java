package web;

import java.io.InputStream;

public class MulitpartFile {

    private String key;
    private String fileName;
    private long size;
    private String contentType;
    private InputStream inputStream ;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public MulitpartFile(String key, String fileName, long size, String contentType, InputStream inputStream) {
        this.key = key;
        this.fileName = fileName;
        this.size = size;
        this.contentType = contentType;
        this.inputStream = inputStream;
    }

    public MulitpartFile() {}
}
