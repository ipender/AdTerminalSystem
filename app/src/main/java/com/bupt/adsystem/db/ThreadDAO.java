package com.bupt.adsystem.db;



import com.bupt.adsystem.entity.ThreadInfo;
import java.util.List;

/**
 * Created by pd on 2016/5/14.
 */
public interface ThreadDAO {

    public void insertThread(ThreadInfo info);
    public void deleteThread(String url, int thread_id);
    public void deleteThread(String url);
    public void updateThread(String url, int thread_id, int finished);
    public List<ThreadInfo> getThread(String url);
    public boolean isExists(String url, int thread_id);
}
