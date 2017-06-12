package com.bupt.adsystem.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


import com.bupt.adsystem.entity.ThreadInfo;
import java.util.LinkedList;
import java.util.List;

/**
 * 数据库操作
 * Created by pd on 2016/5/14.
 */
public class ThreadDAOImpl implements ThreadDAO {
    private DBHelper dbHelper;

    public ThreadDAOImpl(Context context) {
        this.dbHelper = DBHelper.instance(context);
    }

    /**
     * 插入线程信息
     * @param info  线程信息
     */
    @Override
    public synchronized void insertThread(ThreadInfo info) {
        SQLiteDatabase db =  dbHelper.getWritableDatabase();
        db.execSQL("insert into thread_info(thread_id,url,start,end,finished) values(?,?,?,?,?)",
                new Object[]{info.getId(),info.getUrl(),info.getStart(),info.getEnd(),info.getFinished()});
        db.close();
    }

    /**
     * 删除线程信息
     * @param url          下载链接
     * @param thread_id   线程id
     */
    @Override
    public synchronized void deleteThread(String url, int thread_id) {
        SQLiteDatabase db =  dbHelper.getWritableDatabase();
        db.execSQL("delete from thread_info where url = ? and thread_id = ?",
                new Object[]{url,thread_id});
        db.close();
    }

    /**
     * 删除线程信息
     * @param url  下载链接
     */
    @Override
    public synchronized void deleteThread(String url) {
        SQLiteDatabase db =  dbHelper.getWritableDatabase();
        db.execSQL("delete from thread_info where url = ?",
                new Object[]{url});
        db.close();
    }

    /**
     * 更新线程信息
     * @param url          下载链接
     * @param thread_id   线程id
     * @param finished    下载完成进度
     */
    @Override
    public synchronized void updateThread(String url, int thread_id, int finished) {
           SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("update thread_info set finished = ? where url = ? and thread_id = ?",
                new Object[]{finished,url,thread_id});

        db.close();
    }

    /**
     * 获取线程信息
     * @param url
     * @return
     */
    @Override
    public List<ThreadInfo> getThread(String url) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<ThreadInfo> resultList = new LinkedList<ThreadInfo>();
        Cursor cursor =  db.rawQuery("select * from thread_info where url = ?",  new String[]{url});
        if(null != cursor) {
            while(cursor.moveToNext()) {
                ThreadInfo info = new ThreadInfo();
                info.setId(cursor.getInt(cursor.getColumnIndex("thread_id")));
                info.setUrl(cursor.getString(cursor.getColumnIndex("url")));
                info.setStart(cursor.getInt(cursor.getColumnIndex("start")));
                info.setEnd(cursor.getInt(cursor.getColumnIndex("end")));
                info.setFinished(cursor.getInt(cursor.getColumnIndex("finished")));
                resultList.add(info);
            }
            cursor.close();
        }
        db.close();
        return resultList;
    }

    @Override
    public boolean isExists(String url, int thread_id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor =  db.rawQuery("select * from thread_info where url = ? and thread_id = ?",
                new String[]{url, thread_id + ""});
        boolean exists = false;
        if(null != cursor) {
            exists =  cursor.moveToNext();
        }
        db.close();
        return exists;
    }
}
