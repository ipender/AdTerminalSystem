package com.bupt.adsystem.Camera;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.view.Surface;
import android.view.TextureView;
import android.widget.VideoView;

import com.bupt.adsystem.Utils.AdSystemConfig;
import com.serenegiant.usb.IStatusCallback;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.UVCCamera;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by hadoop on 16-8-11.
 */
public class CameraApp {

    private Context mContext;
    private static final String TAG = "CameraApp";
    private static final boolean DEBUG = AdSystemConfig.DEBUG;

    // for thread pool
    private static final int CORE_POOL_SIZE = 1;        // initial/minimum threads
    private static final int MAX_POOL_SIZE = 4;            // maximum threads
    private static final int KEEP_ALIVE_TIME = 10;        // time periods while keep the idle thread
    protected static final ThreadPoolExecutor EXECUTER
            = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE_TIME,
            TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

    // for accessing USB and USB camera
    private USBMonitor mUSBMonitor;
    private UsbManager mUsbManager;
    private UVCCamera mUVCCamera;
    private Surface mSurface;

    // for Camera preview
    private TextureView mTextureView;
    private VideoView mVideoView;

    public CameraApp(Context context, TextureView videoView) {
        mContext = context;
//        mVideoView = videoView;
//        mVideoView.setZOrderOnTop(true);
        mTextureView = videoView;
        mUsbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        mUSBMonitor = new USBMonitor(mContext, mOnDeviceConnectListener);
        PendingIntent mPermissionIntent = PendingIntent.getBroadcast(mContext, 0, new Intent(mUSBMonitor.ACTION_USB_PERMISSION), 0);
        HashMap<String, UsbDevice> usbDevcieList = mUsbManager.getDeviceList();
        if(usbDevcieList.size() == 1){
            Set<String> keySet = usbDevcieList.keySet();
            for (String key : keySet)
            mUsbManager.requestPermission(usbDevcieList.get(key), mPermissionIntent);
        }
    }

    public void startPreview() {
        if (mUVCCamera != null)
            mUVCCamera.startPreview();
    }

    public void stopPreview() {
        if (mUVCCamera != null)
            mUVCCamera.stopPreview();
    }

    public void destroy() {
        if (mUVCCamera != null)
            mUVCCamera.destroy();
        if (mUSBMonitor != null)
            mUSBMonitor.destroy();
    }

    public void registerUsbMonitor() {
        mUSBMonitor.register();
    }

    public void unregisterUsbMonitor() {
        mUSBMonitor.unregister();
    }

    private final USBMonitor.OnDeviceConnectListener mOnDeviceConnectListener = new USBMonitor.OnDeviceConnectListener() {
        @Override
        public void onAttach(UsbDevice device) {

        }

        @Override
        public void onDetach(UsbDevice device) {

        }

        @Override
        public void onConnect(UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock, boolean createNew) {
            if (mUVCCamera != null)
                mUVCCamera.destroy();
            mUVCCamera = new UVCCamera();
            EXECUTER.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        mUVCCamera.open(ctrlBlock);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        return;
                    }

                    mUVCCamera.setStatusCallback(new IStatusCallback() {
                        @Override
                        public void onStatus(final int statusClass, final int event, final int selector,
                                             final int statusAttribute, final ByteBuffer data) {

                        }
                    });
                    if (mSurface != null) {
                        mSurface.release();
                        mSurface = null;
                    }
                    try {
                        mUVCCamera.setPreviewSize(UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT, UVCCamera.FRAME_FORMAT_MJPEG);
                    } catch (final IllegalArgumentException e) {
                        // fallback to YUV mode
                        try {
                            mUVCCamera.setPreviewSize(UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT, UVCCamera.DEFAULT_PREVIEW_MODE);
                        } catch (final IllegalArgumentException e1) {
                            mUVCCamera.destroy();
                            mUVCCamera = null;
                        }
                    }
                    if (mUVCCamera != null) {
                        final SurfaceTexture st = mTextureView.getSurfaceTexture();
                        if (st != null)
                            mSurface = new Surface(st);
//                            mVideoView.setZOrderOnTop(true);
//                        mSurface = mVideoView.getHolder().getSurface();
                        mUVCCamera.setPreviewDisplay(mSurface);
//                        mUVCCamera.setFrameCallback(mIFrameCallback, UVCCamera.PIXEL_FORMAT_RGB565/*UVCCamera.PIXEL_FORMAT_NV21*/);
                        mUVCCamera.startPreview();
                    }
                }
            });
        }

        @Override
        public void onDisconnect(UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock) {
            if (mUVCCamera != null) {
                mUVCCamera.close();
                if (mSurface != null) {
                    mSurface.release();
                    mSurface = null;
                }
            }
        }

        @Override
        public void onCancel(UsbDevice device) {

        }
    };
}
