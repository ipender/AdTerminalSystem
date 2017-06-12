package com.bupt.adsystem.Utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import java.io.File;

/**
 * Created by hadoop on 16-8-16.
 */
public class FileListMgr {
    private static final String TAG = "FileListMgr";
    private static final boolean DEBUG = AdSystemConfig.DEBUG;

    private static final String AdSystemDB = "/ImageList.db";

    private static final String IMAGE_TABLE = "ImageTable";
    private static final String VIDEO_TABLE = "VideoTable";
    private static final String TIME_FOR_VIDEO = "TimeForVideo";
    private static final String TIME_FOR_IMAGE = "TimeForImage";

    private static final String START_TIME = "starttime";
    private static final String END_TIME = "endtime";
    private static final String FILE_NAME = "filename";
    private static final String FULL_PATH = "fullpath";
    private static final String VIDEO_VOLUME = "volume";

    private static FileListMgr sFileListMgr;
    private Context mContext;
    private FileDirMgr mFileDirMgr;
    private String mDatabasePath;
    private String mImageDbPath;
    private SQLiteDatabase mImageDB;

    private String CREATE_IMAGE_TABLE = "create table " + IMAGE_TABLE + " (" +
            "_id integer primary key autoincrement," +
            FILE_NAME + " varchar(50)," +
            FULL_PATH + " varchar(150)" +
            " )";
    private String CREATE_VIDEO_TABLE = "create table " + VIDEO_TABLE + " (" +
            "_id integer primary key autoincrement," +
            FILE_NAME + " varchar(50)," +
            FULL_PATH + " varchar(150)," +
            VIDEO_VOLUME + " smallint" +
            " )";
    private String CREATE_VIDEO_TIME = "create table " + TIME_FOR_VIDEO + " (" +
            "_id integer primary key autoincrement," +
            START_TIME + " varchar(10)," +
            END_TIME + " varchar(10)," +
            FILE_NAME + " varchar(50)," +
            FULL_PATH + " varchar(150)" +
            " )";
    private String CREATE_IMAGE_TIME = "create table " + TIME_FOR_IMAGE + " (" +
            "_id integer primary key autoincrement," +
            START_TIME + " varchar(10)," +
            END_TIME + " varchar(10)," +
            FILE_NAME + " varchar(50)," +
            FULL_PATH + " varchar(150)" +
            " )";

    public static FileListMgr instance(Context context) {
        if (sFileListMgr == null) {
            sFileListMgr = new FileListMgr(context);
        }
        return sFileListMgr;
    }

    public FileListMgr(Context context) {
        mContext = context;
        mFileDirMgr = FileDirMgr.instance();
        mDatabasePath = mFileDirMgr.getDatabasePath();
        mImageDbPath = mDatabasePath + AdSystemDB;
        File dataBasePath = new File(mImageDbPath);
        if (dataBasePath.exists()) {
            mImageDB = SQLiteDatabase.openDatabase(mImageDbPath, null,
                    SQLiteDatabase.OPEN_READWRITE);
        } else {
            mImageDB = SQLiteDatabase.openOrCreateDatabase(mImageDbPath, null);
            mImageDB.execSQL(CREATE_IMAGE_TABLE);
            mImageDB.execSQL(CREATE_VIDEO_TABLE);
            mImageDB.execSQL(CREATE_VIDEO_TIME);
            mImageDB.execSQL(CREATE_IMAGE_TIME);
        }
    }

    public void insertImageFile(@NonNull String filename, @NonNull String fullPath) {
        ContentValues values = new ContentValues();
        values.put(FILE_NAME, filename);
        values.put(FULL_PATH, fullPath);
        mImageDB.insert(IMAGE_TABLE, null, values);
    }

    public void insertVideoFile(@NonNull String filename, @NonNull String fullPath, int volume) {
        ContentValues values = new ContentValues();
        values.put(FILE_NAME, filename);
        values.put(FULL_PATH, fullPath);
        if (volume < 0) volume = -1;
        else if (volume > 100) volume = -1;
        values.put(VIDEO_VOLUME, volume);
        mImageDB.insert(VIDEO_TABLE, null, values);
    }

    public void insertTime4Video(@NonNull String startTime, @NonNull String endTime,
                                 @NonNull String fileName, String filePath) {
        ContentValues values = new ContentValues();
        values.put(START_TIME, startTime);
        values.put(END_TIME, endTime);
        values.put(FILE_NAME, fileName);
        values.put(FULL_PATH, filePath);
        mImageDB.insert(TIME_FOR_VIDEO, null, values);
    }

    public void insertTime4Image(@NonNull String startTime, @NonNull String endTime,
                                 @NonNull String fileName, @NonNull String filePath) {
        ContentValues values = new ContentValues();
        values.put(START_TIME, startTime);
        values.put(END_TIME, endTime);
        values.put(FILE_NAME, fileName);
        values.put(FULL_PATH, filePath);
        mImageDB.insert(TIME_FOR_IMAGE, null, values);
    }

    public Cursor getAllVideoStartTime() {
        Cursor cursor = mImageDB.query(true, TIME_FOR_VIDEO, new String[]{START_TIME}, null, null, null, null, null, null);
        return cursor;
    }

    public Cursor getAllVideoTimeInterval() {
        Cursor cursor = mImageDB.query(true, TIME_FOR_VIDEO, new String[]{START_TIME, END_TIME}, null, null, null, null, null, null);
        return cursor;
    }

    public Cursor getAllImageTimeInterval() {
        Cursor cursor = mImageDB.query(true, TIME_FOR_IMAGE, new String[]{START_TIME, END_TIME}, null, null, null, null, null, null);
        return cursor;
    }

    public Cursor getAllImageStartTime() {
        Cursor cursor = mImageDB.query(true, TIME_FOR_IMAGE, new String[]{START_TIME}, null, null, null, null, null, null);
        return cursor;
    }

    public Cursor getImageInInterval(@NonNull String startTime, @NonNull String endTime) {
        Cursor cursor = mImageDB.query(TIME_FOR_IMAGE, new String[]{FULL_PATH},
                START_TIME + " =? and " + END_TIME + " =?", new String[]{startTime, endTime}, null, null, null, null);
        return cursor;
    }

    public Cursor getVideoInInterval(@NonNull String startTime, @NonNull String endTime) {
        Cursor cursor = mImageDB.query(true, TIME_FOR_VIDEO, new String[]{FULL_PATH},
                START_TIME + "=? and " + END_TIME + "=?", new String[]{startTime, endTime}, null, null, null, null);
        return cursor;
    }

    /*
    * 当删除图像文件时，需要三步操作：
    *   1、删除 IMAGE_TABLE 里相应的 fileName 相关的记录
    *   2、删除 TimeForImage 中和 fileName 相关的记录
    *   3、删除本地的 fileName 文件
    */
    public void deleteImageFile(@NonNull String fileName) {
        Cursor cursor = mImageDB.query(IMAGE_TABLE, new String[]{FILE_NAME, FULL_PATH},
                FILE_NAME + "=?", new String[]{fileName}, null, null, null);
        int rowNum = cursor.getCount();
        cursor.moveToFirst();
        String filePath;
        File file;
        for (int i = 0; i < rowNum; i++) {
            filePath = cursor.getString(1);
            file = new File(filePath);
            if (file.exists()) {
                file.delete();
            }
            if (!cursor.moveToNext()) break;
        }
        mImageDB.delete(IMAGE_TABLE, FILE_NAME + "= ?", new String[]{fileName});
        mImageDB.delete(TIME_FOR_IMAGE, FILE_NAME + "= ?", new String[]{fileName});
    }

    /*
* 当删除图像文件时，需要三步操作：
*   1、删除 IMAGE_TABLE 里相应的 fileName 相关的记录
*   2、删除 TimeForImage 中和 fileName 相关的记录
*   3、删除本地的 fileName 文件
*/

    /*
    * delete the record of this fileName in VIDEO_TABLE and TIME_FOR_VIDEO
    **/
    public void deleteVideoFile(@NonNull String fileName) {
        Cursor cursor = mImageDB.query(VIDEO_TABLE, new String[]{FILE_NAME, FULL_PATH},
                FILE_NAME + "=?", new String[]{fileName}, null, null, null);
        int rowNum = cursor.getCount();
        cursor.moveToFirst();
        String filePath;
        File file;
        for (int i = 0; i < rowNum; i++) {
            filePath = cursor.getString(1);
            file = new File(filePath);
            if (file.exists()) {
                file.delete();
            }
            if (!cursor.moveToNext()) break;
        }
        mImageDB.delete(VIDEO_TABLE, FILE_NAME + "= ?", new String[]{fileName});
        mImageDB.delete(TIME_FOR_VIDEO, FILE_NAME + "= ?", new String[]{fileName});
    }

    public void deleteVideoTime(@NonNull String startTime, @NonNull String endTime) {
        mImageDB.delete(TIME_FOR_VIDEO, START_TIME + "= ? and " + END_TIME + "=?",
                new String[]{startTime, endTime});
    }

    public void deleteImageTime(@NonNull String startTime, @NonNull String endTime) {
        mImageDB.delete(TIME_FOR_IMAGE, START_TIME + "= ? and " + END_TIME + "=?",
                new String[]{startTime, endTime});
    }

    public void clearImageTimeInterval() {
        mImageDB.delete(TIME_FOR_IMAGE, null, null);
    }

    public void clearVideoTimeInterval() {
        mImageDB.delete(TIME_FOR_VIDEO, null, null);
    }

    public Cursor getAllImageFile() {
        Cursor cursor = mImageDB.query(IMAGE_TABLE, new String[]{FULL_PATH}, null, null, null, null, null, null);
        cursor.moveToFirst();
        return cursor;
    }

    public Cursor getAllVideoFile() {
        Cursor cursor = mImageDB.query(VIDEO_TABLE, new String[]{FULL_PATH}, null, null, null, null, null, null);
        cursor.moveToFirst();
        return cursor;
    }

    public String[] isCurrentTimeInVideoIntervel(String currentTime) {
        Cursor cursor = mImageDB.query(true, TIME_FOR_VIDEO, new String[]{START_TIME, END_TIME}, null, null, null, null, null, null);
        if (cursor == null || cursor.getCount() == 0 ) return null;

        cursor.moveToFirst();
        String startTime = null;
        String endTime = null;
        String[] interval = new String[2];

        do {
            startTime = cursor.getString(0);
            endTime = cursor.getString(1);
            if (Utils.isTimeInInterval(currentTime, startTime, endTime)) {
                interval[0] = startTime;
                interval[1] = endTime;
                return interval;
            }
        } while (cursor.moveToNext());

        return null;
    }

    public String[] isCurrentTimeInImageIntervel(String currentTime) {
        Cursor cursor = mImageDB.query(true, TIME_FOR_IMAGE, new String[]{START_TIME, END_TIME}, null, null, null, null, null, null);
        if (cursor == null || cursor.getCount() == 0 ) return null;

        cursor.moveToFirst();
        String startTime = null;
        String endTime = null;
        String[] interval = new String[2];

        do {
            startTime = cursor.getString(0);
            endTime = cursor.getString(1);
            if (Utils.isTimeInInterval(currentTime, startTime, endTime)) {
                interval[0] = startTime;
                interval[1] = endTime;
                return interval;
            }
        } while (cursor.moveToNext());

        return null;
    }

    public boolean IsDBEmpty() {
        boolean chk = false;

        Cursor cursor = mImageDB.query(IMAGE_TABLE, new String[]{"filename"}, null, null, null, null, null, null);
        if (cursor.getCount() > 0) {
            chk = true;
        }

        return chk;
    }

}
