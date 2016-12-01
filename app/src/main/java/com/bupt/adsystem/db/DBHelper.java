package com.bupt.adsystem.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 数据库帮助类
 * Created by pd on 2016/5/14.
 */
public class DBHelper extends SQLiteOpenHelper {
    /**数据库名**/
    private static final String DB_NAME = "dowload.db";
    /**数据库版本**/
    private static final int VERSION = 1;
    /**创建数据表**/
    private static final String SQL_CREATE = "create table thread_info(_id integer primary key autoincrement," +
            "thread_id integer," +
            "url text," +
            "start integer," +
            "end integer," +
            "finished integer)";
    /**删除数据表**/
     private static final String SQL_DROP = "drop table if exists thread_info";
    private static DBHelper mDbHelper = null;

    private DBHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    public synchronized static DBHelper instance(Context context) {
        if(null == mDbHelper) {
            synchronized (DBHelper.class) {
                if(null==mDbHelper) {
                    mDbHelper = new DBHelper(context);
                }
            }
        }
        return mDbHelper;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE); //创建表

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //先删除，再创建
        db.execSQL(SQL_DROP);
        db.execSQL(SQL_CREATE);

    }
}
