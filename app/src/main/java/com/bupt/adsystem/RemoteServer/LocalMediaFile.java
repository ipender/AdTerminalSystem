package com.bupt.adsystem.RemoteServer;

import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by hadoop on 17-11-5.
 * @author lipandeng
 */
public class LocalMediaFile {

    private static final String subVideoPath = "/adVideo/";
    private static final String subImagePath = "/adImage/";

    private static final String[] VIDEO_EXTENSION = {"mp4", "avi", "rmvb", "mkv", "mpeg"};
    private static final String[] IMAGE_EXTENSION = {"jpg", "png", "jpeg"};

    public static List<String> scanFile(MediaType type, String... paths) {

        String regex;
        if (MediaType.VIDEO.equals(type)) {
            regex = "\\.(mp4|avi|rmvb|mkv|mpeg)$";
        } else if (MediaType.IMAGE.equals(type)) {
            regex = "\\.(jpg|png|jpeg)$";
        } else {
            regex = "\\.(jpg|png|jpeg)$";
        }

        Pattern regPatten = Pattern.compile(regex, Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
        List<String> results = new ArrayList<String>();

        for (String path : paths) {
            File folder = new File(path);
            if (!folder.exists()) continue;
            String[] fileList = folder.list();
            for (String name : fileList) {
                if (regPatten.matcher(name).find()) {
                    File file = new File(folder, name);
                    if (file.isDirectory()) continue;
                    results.add(file.getAbsolutePath());
                }
            }
        }
        return results;
    }

    public static List<String> getLocalVideoFile() {
        // sdcard上存储视频的路径
        String sdCardVideoPath = getSdCardRootPath() + subVideoPath;
        // 内部数据存储器上存储视频的路径
        String dataVideoPath = Environment.getDataDirectory() + subVideoPath;
        return scanFile(MediaType.VIDEO, sdCardVideoPath, subVideoPath);
    }

    public static List<String> getLocalImageFile() {
        String sdCardImagePath = getSdCardRootPath() + subImagePath;
        String dataImagePath = Environment.getDataDirectory() + subImagePath;
        return scanFile(MediaType.IMAGE, sdCardImagePath, dataImagePath);
    }

    public static String getSdCardRootPath() {
        if (isSdCardExist()) {
            return Environment.getExternalStorageDirectory().getAbsolutePath();
        }
        return null;
    }

    public static boolean isSdCardExist() {
        return Environment.getExternalStorageState()
                .equals(Environment.MEDIA_MOUNTED);
    }

    public enum MediaType {
        VIDEO,
        IMAGE
    }

}
