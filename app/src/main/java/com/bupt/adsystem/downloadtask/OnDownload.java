package com.bupt.adsystem.downloadtask;

/**
 * Created by pd on 2016/5/16.
 * 下载回调接口
 */

import java.io.File;

public  interface OnDownload {
    /**
     * 下载进度
     * @param url       下载链接
     * @param finished 下载进度
     */
    public  void  onDownloading(String url, int finished);

    /**
     * 下载完成
     * @param downloadFile  下载完成后的文件
     */
    public  void onDownloadFinished(File downloadFile);
}
