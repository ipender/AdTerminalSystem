package com.bupt.adsystem.downloadtask;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.bupt.adsystem.db.ThreadDAOImpl;
import com.bupt.adsystem.entity.TaskInfo;
import com.bupt.adsystem.entity.ThreadInfo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Created by pd on 2016/5/14.
 */public class DownloadTask  {


    private String tag = "DownloadTask";
    /**默认启用线程的数量**/
    private static final int DEFAULT_THREAD_COUNT = 3;
    /**下载进度消息**/
    private static final int MSG_DOWNLOADING = 0x01;
    private Context mContext;
    /**下载任务信息**/
    private TaskInfo mTaskInfo;
    /**线程数据库操作**/
    private ThreadDAOImpl mThreadDao;
    /**停止下载标志位**/
    private boolean isPause = false;
    /**启用下载线程数量**/
    private int mThreadCount = DEFAULT_THREAD_COUNT;
    /***保存各个线程的下载进度**/
    private int[] mTotalFinished ;
    private  Handler mHandler;
    /**线程池**/
    private ExecutorService mThreadPool;
    /**下载链接**/
    private String mStrDownloadUrl;

    public String getUrl() {
        return this.mStrDownloadUrl;
    }

/*    public TaskInfo getTaskInfo() {
        return this.mTaskInfo;
    }*/

    public boolean isPaused() {
        return isPause;
    }

    /**
     * 构造函数
     * @param mContext      上下文
     * @param taskInfo      任务信息
     * @param threadPool   线程池
     * @param threadCount  开启的下载线程数
     */
    public DownloadTask(Context mContext, final TaskInfo taskInfo,ExecutorService threadPool,int threadCount) {
        this.mContext = mContext;
        this.mTaskInfo = taskInfo;
        this.mThreadPool = threadPool;
        this.mStrDownloadUrl = taskInfo.getUrl();
        init(threadCount);
    }

    /**
     * 初始化
     * 初始化成员变量
     * @param threadCount  开启的下载线程数
     */
    private void init(int threadCount ) {
//        mDownloadList = new LinkedList<DownloadThread>();
        mThreadDao = new ThreadDAOImpl(mContext);
        if(threadCount > 0) {
            mThreadCount = threadCount;
        }
        mTotalFinished = new int[mThreadCount];
        if(null == mHandler) {
            mHandler = new Handler() {
                @Override
                public void dispatchMessage(Message msg) {

                    if(msg.what == MSG_DOWNLOADING) {
                        int progress = (int) msg.obj;    //下载进度
                        OnDownload download = mTaskInfo.getDownload();
                        if(null == download) {
                            return;
                        }
                        download.onDownloading(mTaskInfo.getUrl(), progress); //执行回调，更新下载进度
//                        Log.i(tag, "name = " + mTaskInfo.getFileName()+",progress = "+progress);
                        //start 下载完成
                        if(progress >= 100) {
                            File file = new File(mTaskInfo.getFilePath(),mTaskInfo.getFileName());
                            download.onDownloadFinished(file);             //执行回调，
                            sendBroadcastDownloadSuc();                    //广播下载完成
                            mThreadDao.deleteThread(mTaskInfo.getUrl()); //删除数据库中存储的对应的线程信息
                        }
                        //end 下载完成
                    }
                    super.dispatchMessage(msg);
                }
            };
        }
    }

    /**
     * 启动下载
     */
    public void downlaod() {

        List<ThreadInfo> threadInfoList = new LinkedList<ThreadInfo>(); //建立线程信息列表
        threadInfoList = mThreadDao.getThread(mTaskInfo.getUrl());  //从数据库中取出对应的线程信息
        Log.i(tag,"threadInfoList getFromDb = "+threadInfoList);

        //start 数据库没有对应的线程信息，则创建相应的线程信息
        if(threadInfoList.size() <=0) {
            int block = mTaskInfo.getLenght()/mThreadCount; //将下载文件分段
            if(block > 0) {
                //start 根据线程数量分别建立线程信息
                for(int i = 0;i < mThreadCount;i++) {
                    ThreadInfo info = new ThreadInfo(i,mTaskInfo.getUrl(),i*block,(i+1)*block-1,0);
                    if(i == mThreadCount -1) {
                        info.setEnd(mTaskInfo.getLenght()); //分段最后一个，结束位置到文件总长度末尾
                    }
                    threadInfoList.add(info);         //加入列表
                    mThreadDao.insertThread(info);   //向数据库插入线程信息
                }
                //end 根据线程数量分别建立线程信息
            }else {
                ThreadInfo info = new ThreadInfo(0,mTaskInfo.getUrl(),0,mTaskInfo.getLenght(),0);
                threadInfoList.add(info);
                mThreadDao.insertThread(info);
            }
        }
        //end 数据库中没有对应的线程信息，则创建相应的线程信息

        //start 启动下载线程
        for(ThreadInfo info : threadInfoList) {

            DownloadThread thread = new DownloadThread(info);
            if(!mThreadPool.isShutdown()) {
                mThreadPool.execute(thread);
            }

        }
        //end 启动下载线程
    }

   /* private synchronized void checkIsAllThreadFinished() {
        boolean allFinished = true;
        for(DownloadThread  thread:mDownloadList) {
            if(!thread.isFinished) {
                allFinished = false;
                break;
            }
        }
        if(allFinished){
            mThreadDao.deleteThread(mTaskInfo.getUrl());
            Log.i(tag, "all finished " + mTaskInfo.getFileName());
           Intent intent = new Intent();
            intent.setAction(DownloadService.ACTION_FINISHED);
//            intent.putExtra("fileInfo", mTaskInfo) ;
            intent.putExtra(DownloadService.EXTRA_DOWNLOAD_URL,mTaskInfo.getUrl());
            intent.putExtra(DownloadService.EXTRA_FILE_NAME,mTaskInfo.getFileName());
            intent.putExtra(DownloadService.EXTRA_FILE_PATH,mTaskInfo.getFilePath());
            mContext.sendBroadcast(intent);

        }
    }*/

    /**
     * 广播通知，下载完成
     */
    private void sendBroadcastDownloadSuc() {
        Intent intent = new Intent();
        intent.setAction(DownloadService.ACTION_FINISHED);
        Log.i(tag, "sendBroadcastDownloadSuc");
        intent.putExtra(DownloadService.EXTRA_DOWNLOAD_URL,mTaskInfo.getUrl());  //下载链接
      /*  intent.putExtra(DownloadService.EXTRA_FILE_NAME,mTaskInfo.getFileName());
        intent.putExtra(DownloadService.EXTRA_FILE_PATH,mTaskInfo.getFilePath());*/
        mContext.sendBroadcast(intent);
    }

    /**
     * 停止下载
     */
    public void pause() {
        isPause = true;
    }

    /**
     * 重新开始下载
     */
    public void restart() {
        Log.i(tag,"restart");
        isPause = false;
        downlaod();
    }

    /**
     * 下载线程
     */
    class DownloadThread extends  Thread {
        private ThreadInfo threadInfo;  //线程信息
//        public boolean isFinished = false;
        public DownloadThread(ThreadInfo threadInfo) {
            this.threadInfo = threadInfo;
            Log.i(tag,"thread info = "+threadInfo);
        }

        @Override
        public void run() {
                URL url = null;
                HttpURLConnection con = null;      //http链接
                RandomAccessFile accessFile = null; //下载文件
                InputStream inputStream = null;      //输入流
                try {

                    int start = threadInfo.getStart()+threadInfo.getFinished(); //读取文件的位置
                    //start 初始化下载链接
                    url = new URL(threadInfo.getUrl());
                    con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");
                    con.setConnectTimeout(5000);
                    con.setRequestProperty("Range", "bytes=" + start + "-" + threadInfo.getEnd()); //设置读取文件的位置，和结束位置
                    //end 初始化下载链接
                    //start 初始化下载到本地的文件
                    accessFile  = new RandomAccessFile(new File(mTaskInfo.getFilePath(), mTaskInfo.getFileName()),"rwd");
                    accessFile.seek(start);    //设置开始写入的位置
                    //end 初始化下载到本地的文件

                    int responseCode = con.getResponseCode();
                    if((con.getResponseCode() == HttpURLConnection.HTTP_PARTIAL) ||
                            (con.getResponseCode() == HttpURLConnection.HTTP_OK) ) {
                        inputStream = con.getInputStream();
                        int finished = threadInfo.getFinished();               //已经下载的长度
//                        int len = threadInfo.getEnd()-threadInfo.getStart();  //本线程要下载的长度
                        int readLen = -1;                                       //读取的长度
                        byte[] buffer = new byte[1024*4];
                        long time = System.currentTimeMillis();

                        //start 读取输入流写入文件
                        while((readLen = inputStream.read(buffer))!=-1) {
                            accessFile.write(buffer, 0, readLen);
//                            Log.i(tag, "readLen = " + readLen);
                            finished += readLen;
                            threadInfo.setFinished(finished);    //设置已经下载进度
                            if(System.currentTimeMillis() - time >2000) {
//                                Log.i(tag, "readLen = " + readLen);
                                notifyProgress(threadInfo.getId(), finished); //每隔2秒通知下载进度
                                time = System.currentTimeMillis();
                            }
                            //start 停止下载，保存进度
                            if(isPause) {
                                Log.i(tag,"pause name = "+mTaskInfo.getFileName());
                                notifyProgress(threadInfo.getId(), finished);        //通知下载进度
                                mThreadDao.updateThread(threadInfo.getUrl(),threadInfo.getId(),finished);  //更新数据库对应的线程信息
                                return;
                            }
                            //end 停止下载，保存进度
                        }
                        //end 读取输入流写入文件

//                        mThreadDao.updateThread(threadInfo.getUrl(),threadInfo.getId(),finished);
//                        isFinished = true;
//                        checkIsAllThreadFinished();
//                        broadcastFinished(threadInfo.getId(),finished);
                        notifyProgress(threadInfo.getId(),finished);
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (ProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {

                        try {
                            if(inputStream!=null){
                                inputStream.close();
                            }
                            if(accessFile!=null) {
                                accessFile.close();
                            }
                            if(null!=con) {
                                con.disconnect();
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                }
            super.run();
        }
    }

    /**
     * 通知下载进度
     * @param threadId  线程id
     * @param finished  已经完成的进度
     */
    private synchronized void notifyProgress(int threadId,int finished) {

            if(threadId>=0 && threadId < mTotalFinished.length) {
                mTotalFinished[threadId] = finished;
                int nowFinished = 0;
                for(int i = 0; i<mTotalFinished.length;i++) {
                    nowFinished += mTotalFinished[i];
                }
 /*               Log.i(tag,"lastFinished = "+lastFinished+",nowFinished = "+nowFinished);
                if(lastFinished>=nowFinished){
                    return;
                }*/
                int progress = (int) (((float) nowFinished / (float) mTaskInfo.getLenght()) * 100);
                mHandler.sendMessage(mHandler.obtainMessage(MSG_DOWNLOADING, progress));

             /*   intent.setAction(DownloadService.ACTION_UPDATE);
//                                intent.putExtra("finished", (int) (((float) finished / (float) len) * 100));
                intent.putExtra("finished", (int) (((float) tatolFinished / (float) mTaskInfo.getLenght()) * 100));
                mContext.sendBroadcast(intent);*/
            }
    }

    /**
     * 获取存放下载文件的存储路径
     * @param context
     * @return
     */
   /* private  String getFilePath(Context context) {
        String path = null;

        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
//            path = getExternalCacheDir().getPath();
            path = context.getExternalFilesDir(null).getAbsolutePath();
            Log.i(tag, "path = " + path);

        } else {
            path = context.getFilesDir().getAbsolutePath();

        }
        File downloadFile = new File(path,DOWNLOAD_FILE);
        if(!downloadFile.exists()) {
            downloadFile.mkdirs();
        }
        path  = downloadFile.getAbsolutePath();
        return path;
    }*/





}
