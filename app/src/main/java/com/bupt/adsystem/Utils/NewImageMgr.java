package com.bupt.adsystem.Utils;

import android.content.Context;
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
import com.bupt.adsystem.RemoteServer.MediaStrategyMgr;
import com.bupt.adsystem.view.MainActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hadoop on 16-10-24.
 */
public class NewImageMgr implements UpdateMedia{
    private static final String TAG = "AdVideoCtrl";
    private static final boolean DEBUG = AdSystemConfig.DEBUG;

    private static final int MSG_SWITCH_IMAGE = 0x01;
    private static final int MSG_STOP_SWITCH = 0x02;
    private static int IMAGE_SWITCH_DURATION = 5000;

    private List<InOutAnimation> mAnimLibrary = new ArrayList<>();
    private int mAnimPosition = 0;

    private Context mContext;
    private List<String> mImageList;
    private File mImageFile;
    private String mFilePath;
    private int mCurrentImageId = 0;
    private ImageSwitcher mImageSwitcher;
    private MediaStrategyMgr mStrategyMgr;
    private static NewImageMgr sImageMgr;


    public static NewImageMgr instance(Context context, ImageSwitcher imageSwitcher) {
        if (sImageMgr == null) {
            sImageMgr = new NewImageMgr(context, imageSwitcher);
        }
        return sImageMgr;
    }

    private Handler mImageHandler = new Handler() {
        @Override
        public void dispatchMessage(Message msg) {
            if (msg.what == MSG_SWITCH_IMAGE && mImageSwitcher != null) {
                mFilePath = getImageByOrder();
                if (mFilePath != null) {
                    mImageFile = new File(mFilePath);
                    if (!mImageFile.exists()) {
                        if (DEBUG) Log.e(TAG, "This Image Should Exists! : " + mFilePath);
                        mImageHandler.sendEmptyMessageDelayed(MSG_SWITCH_IMAGE, IMAGE_SWITCH_DURATION);
                        return;
                    }
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

    public NewImageMgr(Context context, ImageSwitcher imageSwitcher) {
        mContext = context;
        mImageSwitcher = imageSwitcher;
        mStrategyMgr = MediaStrategyMgr.instance(context);
        mStrategyMgr.setImageUpdateMedia(this);
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
        mImageList = getImageListWhenStartUp();
        if (mImageList.size() > 0) {
            MainActivity.instance().switchImageView(0);
        }
        mImageHandler.sendEmptyMessage(MSG_SWITCH_IMAGE);
    }

    /**
     * 当终端收到服务器发出的和图片播放相关的指令时，需要调用的更新操作
     * 该操作需要考虑以下问题：
     * 1、检测当前处于什么时间
     * 2、根据时间选择播放列表
     */
    private void changeImageList(){

    }

    public List<String> getImageListWhenStartUp() {
        return mStrategyMgr.getImageList();
    }

    public String getImageByOrder() {
        int size;
        if ( (mImageList == null) || ((size = mImageList.size())) <= 0) return null;
        mCurrentImageId++;
        if (mCurrentImageId >= size) mCurrentImageId = 0;
        return mImageList.get(mCurrentImageId);
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

    @Override
    public void updateWhenAlarmUp(boolean isTimeStart) {

    }

    @Override
    public void updateWhenIntervalAddOrEdit(@NonNull String startTime, @NonNull String endTime) {

    }

    @Override
    public void updateWhenIntervalDelete(@NonNull String startTime, @NonNull String endTime) {

    }

    @Override
    public void updateWhenStrategyChanged() {
        mImageList = mStrategyMgr.getImageList();
        if (!mImageHandler.hasMessages(MSG_SWITCH_IMAGE)) {
            mImageHandler.sendEmptyMessage(MSG_SWITCH_IMAGE);
        }
    }

    @Override
    public void updateWhenFileDelete() {

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
