package com.bupt.adsystem.entity;

import java.io.Serializable;

/**
 * Created by luoss on 2016/5/13.
 */
public class FileInfo implements Serializable{

    private int id;
    private String url;
    private String fileName;
    private int lenght;
    private int finished;

    /**
     * 构造函数
     * @param id
     * @param url          链接
     * @param fileName    文件名
     * @param leght       文件长度
     * @param finished    文件下载进度
     */
    public FileInfo(int id, String url, String fileName, int leght, int finished) {
        this.id = id;
        this.url = url;
        this.fileName = fileName;
        this.lenght = leght;
        this.finished = finished;
    }

    public FileInfo() {
    }

    public int getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public String getFileName() {
        return fileName;
    }

    public int getLenght() {
        return lenght;
    }

    public int getFinished() {
        return finished;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setLenght(int leght) {
        this.lenght = leght;
    }

    public void setFinished(int finished) {
        this.finished = finished;
    }

    @Override
    public String toString() {
        return "FileInfo{" +
                "id=" + id +
                ", url='" + url + '\'' +
                ", fileName='" + fileName + '\'' +
                ", leght=" + lenght +
                ", finished=" + finished +
                '}';
    }


}
