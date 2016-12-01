package com.bupt.adsystem.downloadtask;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.bupt.adsystem.Utils.AdSystemConfig;
import com.bupt.adsystem.entity.TaskInfo;

import java.util.LinkedList;
import java.util.concurrent.Semaphore;


/**
 * Created by pd on 2016/5/16.
 */
    public class DownloadManager {
    private String TAG = "DownloadManager";
    private static final boolean DEBUG = AdSystemConfig.DEBUG;

    private static final int MSG_BIND_SUC = 0x01;

    private static  DownloadManager mManager;
    private Context mContext;
    /***下载服务**/
    private DownloadService mService;
    /***绑定服务的链接**/
    private ServiceConnection mServiceConn;

    private Handler mHandler;
    /**下载任务列表**/
    private LinkedList<TaskInfo> mListTask;
    /**信号量确保对任务列表操作的原子性**/
    private Semaphore mSemaphList ;

    /**
     *
     * @param context
     */
    private DownloadManager(Context context){
        this.mContext = context;
        init();
    }

    /**
     * 获取实例
     * @param context
     * @return
     */
    public synchronized static DownloadManager instance(Context context) {
        if(null == mManager) {
            synchronized (DownloadManager.class){
                if(null == mManager) {
                    mManager = new DownloadManager(context);
                }
            }
        }
        return mManager;
    }

    /**
     * 初始化
     * 成员变量的初始化等
     */
    private void init() {
        mListTask = new LinkedList<TaskInfo>();
        mSemaphList = new Semaphore(1);

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {

                if(msg.what == MSG_BIND_SUC) {  //服务绑定成功
                    try {
                        mSemaphList.acquire();

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    while(!mListTask.isEmpty()) {
                        mService.download(mListTask.removeLast()); //执行列表的下载任务，并删除任务
                    }
                    mSemaphList.release();
                }
                super.handleMessage(msg);

            }
        };

    }

    /**
     * 开始下载
     * @param url              文件链接地址
     * @param filePathParam  文件存放的路径
     * @param fileNameParam  文件名
     * @param download        回调接口
     */
    public void startDownload(String url,String filePathParam,String fileNameParam,OnDownload download) {
        if(TextUtils.isEmpty(url)) {
            return ;
        }
        TaskInfo info = new TaskInfo(url,filePathParam,fileNameParam,0,download); //建立下载任务对象信息
        if (DEBUG) Log.d(TAG,"info = "+info);
        if(mService == null) {  //服务未绑定
            bindService();      //绑定服务
            try {
                mSemaphList.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(mListTask.contains(info)) {  //是否已经存在此任务
                return;
            }
            mListTask.add(info);      //将下载任务添加到下载列表
            mSemaphList.release();
        } else {
            mService.download(info);   //服务已经绑定，直接下载

        }
    }

    /**
     * 暂停任务
     * @param url 文件下载链接
     */
    public void pauseDownload(String url) {
        if(null == url) {
            return;
        }
//        TaskInfo info = new TaskInfo(url,null,null,0,null);
        if(null != mService) {
            mService.pause(url);
        }
    }

    /**
     * 初始化服务绑定
     */
    private void initServiceConn() {
        mServiceConn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                if (DEBUG) Log.d(TAG, "ServiceConnected!");
                mService = ((DownloadService.DownloadBinder)service).getService();
                if(null != mService) {
                    mHandler.sendEmptyMessage(MSG_BIND_SUC);  //绑定成功
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
    }

    /**
     * 绑定服务
     * 先启动服务，再绑定服务
     */
    public void bindService() {
        if (DEBUG) Log.d(TAG, "bindService");
        initServiceConn();
        Intent intent = new Intent(mContext,DownloadService.class);
        mContext.startService(intent);
        mContext.bindService(intent, mServiceConn, Context.BIND_AUTO_CREATE);
    }

    /**
     * 销毁
     */
    public void destroy() {
        if(null != mService) {
            Intent intent = new Intent(mContext,DownloadService.class);
            mContext.stopService(intent);
            mListTask.clear();
            mManager = null;
        }
    }

}
