package com.bupt.adsystem.entity;

/**
 * Created by luoss on 2016/5/13.
 */
public class ThreadInfo {

    private int id;
    private String url;
    private int start;
    private int end;
    private int finished;

    /**
     * 构造方法
     * @param id          线程id
     * @param url         下载链接
     * @param start       开始下载位置
     * @param end         下载结束位置
     * @param finished    总共已经完成
     */
    public ThreadInfo(int id, String url, int start, int end, int finished) {
        this.id = id;
        this.url = url;
        this.start = start;
        this.end = end;
        this.finished = finished;
    }

    public ThreadInfo() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public int getFinished() {
        return finished;
    }

    public void setFinished(int finished) {
        this.finished = finished;
    }

    @Override
    public String toString() {
        return "ThreadInfo{" +
                "id=" + id +
                ", url='" + url + '\'' +
                ", start=" + start +
                ", end=" + end +
                ", finished=" + finished +
                '}';
    }
}
