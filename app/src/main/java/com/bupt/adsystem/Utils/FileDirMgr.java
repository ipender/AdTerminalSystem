package com.bupt.adsystem.Utils;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by hadoop on 16-8-4.
 * 需求：从 Web 端下载的视频广告和图片广告分别存储在内部存储器的相应路径中
 * 而摄像头拍摄的视频存储到外部存储中，即SDCard
 */
public class FileDirMgr {

    public final static int VIDEO = 0;
    public final static int IMAGE = 1;
    public final static int CAMERA = 2;

    private static final String TAG = "FileDirMgr";
    private static final boolean DEBUG = AdSystemConfig.DEBUG;
    private static final String VIDEO_PATH = "/Ad_Video/";
    private static final String IMAGE_PATH = "/Ad_Image/";
    private static final String Camera_PATH = "/Camera/";
    private static final String DATABASE_PATH = "/Database/";

    private static final String PATTERN = "([^\\.]*)\\.([^\\.]*)";
    public static final String[] VIDEO_EXTENSION = {"mp4", "avi", "rmvb", "mkv", "mpeg"};
    public static final String[] IMAGE_EXTENSION = {"jpg", "png", "jpeg"};
    private static FileDirMgr mFolderMgrInstance = null;

    File file;

    private String mInternalStoragePath;
    private String mExternalStoragePath;
    private String mVideoStoragePath;

    private String mImageStoragePath;
    private String mCameraStoragePath;
    private String mDatabasePath;

    public static FileDirMgr instance() {
        if (mFolderMgrInstance == null) {
            mFolderMgrInstance = new FileDirMgr();
        }
        return mFolderMgrInstance;
    }

    public FileDirMgr() {
        mInternalStoragePath = Environment.getDataDirectory().getAbsolutePath();

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            mExternalStoragePath = Environment.getExternalStorageDirectory().getAbsolutePath();
            mCameraStoragePath = mExternalStoragePath + Camera_PATH;
            // use sdcard temporary
            mDatabasePath = mExternalStoragePath + DATABASE_PATH;
            mImageStoragePath = mExternalStoragePath + IMAGE_PATH;

            file = new File(mCameraStoragePath);
            if (!file.exists()) {
                file.mkdir();
            }
        }
        mVideoStoragePath = mExternalStoragePath + VIDEO_PATH;
//        mImageStoragePath = mInternalStoragePath + IMAGE_PATH;
//        mDatabasePath = mInternalStoragePath + DATABASE_PATH;

        file = new File(mVideoStoragePath);
        if (!file.exists()) {
            file.mkdir();
        }
        file.setWritable(true, false);

        file = new File(mImageStoragePath);
        if (!file.exists()) {
            file.mkdir();
        }
        file.setWritable(true, false);

        file = new File(mDatabasePath);
        if (!file.exists()) {
            file.mkdir();
        }
        file.setWritable(true, false);

        if (DEBUG) Log.d(TAG, "the path is :\n"
                + mVideoStoragePath + "\n"
                + mImageStoragePath + "\n"
                + mCameraStoragePath + "\n"
                + file.getAbsolutePath() + "\n");
    }

    public String getVideoStoragePath() {
        return mVideoStoragePath;
    }

    public String getCameraStoragePath() {
        return mCameraStoragePath;
    }

    public String getImageStoragePath() {
        return mImageStoragePath;
    }

    public String getDatabasePath() {
        return mDatabasePath;
    }

    public List<File> scanSpecificFolder(int mediaType) {
        String path = null;
        String[] extensionSet = null;
        switch (mediaType) {
            case VIDEO: {
                path = mCameraStoragePath;
                extensionSet = VIDEO_EXTENSION;
                break;
            }
            case IMAGE: {
                path = mImageStoragePath;
                extensionSet = IMAGE_EXTENSION;
                break;
            }
            case CAMERA: {
                break;
            }
            default: {
                path = mCameraStoragePath;
                extensionSet = VIDEO_EXTENSION;
            }
        }
        File folder = new File(path);
        if (!folder.exists() || !folder.isDirectory()) {
            if (DEBUG) Log.d(TAG, "the Path is wrong!");
            return null;
        }
        Pattern regPatten = Pattern.compile("([^\\.]*)\\.([^\\.]*)");
        List<File> results = new ArrayList<File>();
        final String[] filenames = folder.list();
        if (DEBUG) {
            String fileList = "File Lists:\n";
            for (String result : filenames) {
                fileList += result + "\n";
            }
            Log.d(TAG, fileList);
        }
        if (filenames != null) {
            for (String name : filenames) {
                File file = new File(folder, name);
                if (file.isDirectory()) {
                    file.delete();
                    continue;
                } else {
                    Matcher matcher = regPatten.matcher(name);
                    matcher.matches();
                    String fileExtension = matcher.group(2);
                    for (String extension : extensionSet) {
                        if (fileExtension.equals(extension)) {
                            results.add(file);
                            break;
                        }
                    }
                }
            }
        }
        if (DEBUG) {
            String fileList = "Filtered files:";
            for (File result : results) {
                fileList += result.getAbsolutePath() + "\n";
            }
            Log.d(TAG, fileList);
        }
        return results;
    }
}
