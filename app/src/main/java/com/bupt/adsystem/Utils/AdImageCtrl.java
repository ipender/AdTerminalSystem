package com.bupt.adsystem.Utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ViewSwitcher;

import com.bupt.adsystem.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hadoop on 16-8-15.
 */
public class AdImageCtrl implements UpdateMedia {
    private static final String TAG = "AdVideoCtrl";
    private static final boolean DEBUG = AdSystemConfig.DEBUG;

    private static final int MSG_SWITCH_IMAGE = 0x01;
    private static final int MSG_STOP_SWITCH = 0x02;
    private static int IMAGE_SWITCH_DURATION = 5000;

    private static boolean mImageUpdate;
    private static AdImageCtrl sImageCtrl;

    private List<InOutAnimation> mAnimLibrary = new ArrayList<>();
    private int mAnimPosition = 0;

    private Context mContext;
    private FileDirMgr mImageFolderMgr;
    private List<File> mDisplayImageList;
    private File mImageFile;
    private Cursor mDisplayImageCursor;

    private int mTotalImageNum = 0;
    private int mCurrentImageId = 0;
    private ImageView mImageView;
    private ImageSwitcher mImageSwitcher;
    private FileListMgr mFileListMgr;

    private Handler mImageHandler = new Handler() {
        @Override
        public void dispatchMessage(Message msg) {
            if (msg.what == MSG_SWITCH_IMAGE && mImageSwitcher != null) {
                mImageFile = getImageByOrder();
                if (mImageFile != null) {
                    setNextAnimation();
                    Uri uri = Uri.fromFile(mImageFile);
                    if (uri != null) {
                        if (DEBUG) Log.d(TAG, uri.toString());
                        mImageSwitcher.setImageURI(uri);
                    }
                } else {
                    mImageSwitcher.reset();
                }
                mImageHandler.sendEmptyMessageDelayed(MSG_SWITCH_IMAGE, IMAGE_SWITCH_DURATION);
            } else if (msg.what == MSG_STOP_SWITCH) {

            }
        }
    };

    public static AdImageCtrl instance(Context context, ImageSwitcher imageSwitcher) {
        if (sImageCtrl == null) {
            sImageCtrl = new AdImageCtrl(context, imageSwitcher);
        }
        return sImageCtrl;
    }

    public static AdImageCtrl getInstanceIfExists() {
        return sImageCtrl;
    }

    public AdImageCtrl(Context context, ImageSwitcher imageSwitcher) {
        mContext = context;
        mImageSwitcher = imageSwitcher;
        mImageSwitcher.setInAnimation(AnimationUtils.loadAnimation(mContext,
                android.R.anim.slide_in_left));
        mImageSwitcher.setOutAnimation(AnimationUtils.loadAnimation(mContext,
                android.R.anim.slide_out_right));
        mImageSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {

                ImageView imageView = new ImageView(mContext);
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);

                FrameLayout.LayoutParams params = new ImageSwitcher.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);

                imageView.setLayoutParams(params);
                return imageView;
            }
        });
        initAnimLibrary();
        mImageFolderMgr = FileDirMgr.instance();
        mFileListMgr = FileListMgr.instance(mContext);
        mDisplayImageCursor = getImageListWhenStartUp();
        mImageHandler.sendEmptyMessage(MSG_SWITCH_IMAGE);
    }

    public void setImageView(ImageView imageView) {
        mImageView = imageView;
    }

    public void setImageSwitcher(ImageSwitcher imageSwitcher) {
        mImageSwitcher = imageSwitcher;
    }

    public Cursor getImageListWhenStartUp() {
        String currentTime = Utils.getCurrentTime();
        String[] timeInterval = mFileListMgr.isCurrentTimeInImageIntervel(currentTime);
        Cursor imageCursor = null;
        if (timeInterval == null) {
            imageCursor = mFileListMgr.getAllImageFile();
        } else {
            imageCursor = mFileListMgr.getImageInInterval(timeInterval[0], timeInterval[1]);
        }
        imageCursor.moveToFirst();
        return imageCursor;
    }

    public File getImageByOrder() {
        File imageFile = null;
        if (mDisplayImageCursor != null && mDisplayImageCursor.getCount() > 0) {
            if (!mDisplayImageCursor.moveToNext()) {
                mDisplayImageCursor.moveToFirst();
            }
            imageFile = new File(mDisplayImageCursor.getString(0));
            if (DEBUG) Log.d(TAG, "Got a image file:" + imageFile.getAbsolutePath());
        }

        return imageFile;
    }

    public void setNextAnimation() {
        int animNum = mAnimLibrary.size();
        if (animNum > 0) {
            mAnimPosition++;
            if (mAnimPosition >= animNum) mAnimPosition = 0;
            InOutAnimation inOut = mAnimLibrary.get(mAnimPosition);
            mImageSwitcher.setInAnimation(inOut.in);
            mImageSwitcher.setOutAnimation(inOut.out);
        }
    }

    public void initAnimLibrary() {
        mAnimLibrary.add(new InOutAnimation(AnimationUtils.loadAnimation(mContext, R.anim.slide_in_up),
                AnimationUtils.loadAnimation(mContext, R.anim.slide_out_down)));
        mAnimLibrary.add(new InOutAnimation(AnimationUtils.loadAnimation(mContext, android.R.anim.fade_in),
                AnimationUtils.loadAnimation(mContext, android.R.anim.fade_out)));
    }

    /**
     * 当终端收到服务器发出的和图片播放相关的指令时，需要调用的更新操作
     * 该操作需要考虑以下问题：
     * 1、检测当前处于什么时间
     * 2、根据时间选择播放列表
     */

    private void cursorChange(Cursor cursor) {
        cursor.moveToFirst();
//        if (cursor.getCount() > 0){
//            mDisplayImageCursor = cursor;
//        }
        mDisplayImageCursor = cursor;
    }

    @Override
    public void updateWhenAlarmUp(boolean isTimeStart) {

        Cursor newVideoCursor = null;

        if (isTimeStart) {
            String[] timeInterval = mFileListMgr.isCurrentTimeInImageIntervel(Utils.getCurrentTime());
            if (timeInterval == null) {
                newVideoCursor = mFileListMgr.getAllImageFile();
            } else {
                newVideoCursor = mFileListMgr.getImageInInterval(timeInterval[0], timeInterval[1]);
            }
        } else {
            newVideoCursor = mFileListMgr.getAllImageFile();
        }

        cursorChange(newVideoCursor);
    }

    @Override
    public void updateWhenIntervalAddOrEdit(@NonNull String startTime, @NonNull String endTime) {
        Cursor playListCursor;
        String currentTime = Utils.getCurrentTime();
        if (Utils.isTimeInInterval(currentTime, startTime, endTime)) {
            playListCursor = mFileListMgr.getImageInInterval(startTime, endTime);
            cursorChange(playListCursor);
        }
    }

    @Override
    public void updateWhenIntervalDelete(@NonNull String startTime, @NonNull String endTime) {
        Cursor playListCursor;
        String currentTime = Utils.getCurrentTime();
        if (Utils.isTimeInInterval(currentTime, startTime, endTime)) {
            playListCursor = mFileListMgr.getAllImageFile();
            cursorChange(playListCursor);
        }
    }

    @Override
    public void updateWhenStrategyChanged() {
        Cursor playListCursor;
        Cursor intervalCursor = mFileListMgr.getAllImageTimeInterval();
        final int rowNum = intervalCursor.getCount();
        if (rowNum > 0) {
            intervalCursor.moveToFirst();
            String currentTime = Utils.getCurrentTime();
            String startTime = null;
            String endTime = null;
            int i = 0;
            for (; i < rowNum; i++) {
                startTime = intervalCursor.getString(0);
                endTime = intervalCursor.getString(1);
                if (Utils.isTimeInInterval(currentTime, startTime, endTime)) {
                    break;
                }
                intervalCursor.moveToNext();
            }
            if (i < rowNum) return;
        }

        playListCursor = mFileListMgr.getAllImageFile();
        cursorChange(playListCursor);
    }

    @Override
    public void updateWhenFileDelete() {

        String[] timeInterval = null;
        timeInterval = mFileListMgr.isCurrentTimeInImageIntervel(Utils.getCurrentTime());
        Cursor newCursor;

        if (timeInterval == null) {
            newCursor = mFileListMgr.getAllImageFile();
        } else {
            newCursor = mFileListMgr.getImageInInterval(timeInterval[0], timeInterval[1]);
        }
        cursorChange(newCursor);
    }

    @Override
    public void updateWhenDownloadFinished() {

    }

    class InOutAnimation {

        Animation in;
        Animation out;

        public InOutAnimation(Animation in, Animation out) {
            this.in = in;
            this.out = out;
        }
    }
}
