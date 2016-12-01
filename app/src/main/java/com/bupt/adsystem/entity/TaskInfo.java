package com.bupt.adsystem.entity;



import android.util.Log;

import com.bupt.adsystem.downloadtask.OnDownload;

import java.io.Serializable;

/**
 * Created by luoss on 2016/5/16.
 */
public class TaskInfo implements Serializable {

    private String tag = "TaskInfo";
    private String url;
    private String filePath;
    private String fileName;
    private int lenght;
    private OnDownload download;

    public TaskInfo(String url, String filePath,String fileName,int lenght, OnDownload download) {
        this.url = url;
        this.filePath = filePath;
        this.fileName = fileName;
        this.lenght = lenght;
        this.download = download;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public OnDownload getDownload() {
        return download;
    }

    public void setDownload(OnDownload download) {
        this.download = download;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getLenght() {
        return lenght;
    }

    public void setLenght(int lenght) {
        this.lenght = lenght;
    }

    int objectHashCode(Object obj){

        if (obj == null)
        {
            return 0;
        }
        return obj.hashCode();
    }

    int arrayHashCode(Object[] objs)
    {
        int result = 0;
        if (objs == null)
        {
            return result;
        }
        for (Object o : objs)
        {
            result += objectHashCode(o);
        }
        return result;
    }


    @Override
    public String toString() {
        return "TaskInfo{" +
                "url='" + url + '\'' +
                ", filePath='" + filePath + '\'' +
                ", fileName='" + fileName + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        }
        if(null == o || (! (o instanceof TaskInfo))) {
            return false;
        }
        TaskInfo info = (TaskInfo)o;
/*        Log.i(tag,"this = "+this.toString());
        Log.i(tag,"info = "+info);*/
        System.out.println("this = " + this.toString());
        System.out.println("info = "+info);
        /*return this.url.equals(info.getUrl()) &&
                this.filePath.equals(info.getFilePath()) &&
                this.fileName.equals(info.getFileName()) &&
                this.lenght == info.getLenght();*/
        if(info.getUrl() == null /*||
                info.getFilePath() == null ||
                info.getFileName() == null*/){
            return false;
        }
        return info.getUrl().equals(url)/* &&
                info.getFilePath().equals(filePath) &&
                info.getFileName().equals(fileName) &&
                info.getLenght() == info.getLenght()*/;


    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37 * result + objectHashCode(url);
      /*  result = 37 * result + objectHashCode(filePath);
        result = 37 * result + objectHashCode(fileName);
        result = 37 * result + lenght;*/
//        result = 37 * result + objectHashCode(download);
        Log.i(tag,"hashcode = "+result);
        return result;
    }
}
