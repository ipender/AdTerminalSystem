package org.webrtc;

import android.content.Context;
import android.nfc.Tag;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

import org.webrtc.CameraEnumerationAndroid.CaptureFormat;

import java.util.HashSet;
import java.util.IllegalFormatCodePointException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created by hadoop on 17-1-11.
 */
public class UVCCameraVideoCapturer implements VideoCapturer,
        SurfaceTextureHelper.OnTextureFrameAvailableListener,
        UVCCameraInterface.UVCFrameCallback {

    private final static boolean DEBUG = true;
    private final static String TAG = "UVCCameraVideoCapturer";
    private static final int CAMERA_STOP_TIMEOUT_MS = 7000;
    private static UVCCameraVideoCapturer sUVCCameraVideoCapturer;


    /* |mCameraThreadHandler| must be synchronized on |mCameraThreadLock| when not on the camera thread,
     * or when modifying the reference. Use maybePostOnCameraThread() instead of posting directly to
     * the handler - this way all callbacks with a specifed token can be removed at once.*/
    private final Object mCameraThreadLock = new Object();
    private SurfaceTextureHelper mSurfaceHelper;
    private Handler mCameraThreadHandler;   // this is got from SurfaceTextureHelper
    private int openCameraAttempts;

    private final Set<byte[]> queuedBuffers = new HashSet<byte[]>();

    private int requestedWidth;
    private int requestedHeight;
    private int requestedFramerate;

    private CaptureFormat captureFormat;
    private CapturerObserver frameObserver = null;
    private Context applicationContext;

    private final boolean isCapturingToTexture;

    private int oriention = 0;

    private UVCCameraInterface mUVCCamera;


    public UVCCameraVideoCapturer(UVCCameraInterface uvcCameraInterface, boolean isCapturingToTexture) {
        this.mUVCCamera = uvcCameraInterface;
        this.isCapturingToTexture = isCapturingToTexture;
    }

    public void setUVCCameraProxy(UVCCameraInterface proxy) {
        this.mUVCCamera = proxy;
    }

    // Returns true if this VideoCapturer is setup to capture video frames to a SurfaceTexture.
    public boolean isCapturingToTexture() {
        return isCapturingToTexture;
    }

    @Override
    public List<CameraEnumerationAndroid.CaptureFormat> getSupportedFormats() {
        if (DEBUG) Log.d(TAG, "getSupportedFormats() is done!");
        return mUVCCamera.getSupportedFormats();
    }

    // Note that this actually opens the camera, and Camera callbacks run on the
    // thread that calls open(), so this is done on the CameraThread.
    @Override
    public void startCapture(final int width, final int height, final int framerate,
                             SurfaceTextureHelper surfaceTextureHelper, final Context applicationContext,
                             final CapturerObserver frameObserver) {

        if (surfaceTextureHelper == null) {
            frameObserver.onCapturerStarted(false);     // start capture failed
            return;
        }

        if (applicationContext == null) {
            throw new IllegalArgumentException("AnyRTC application context is not set!");
        }

        synchronized (mCameraThreadLock) {
            if (this.mCameraThreadHandler != null) {
                throw new RuntimeException("Camera has already been started!");
            }
            this.mCameraThreadHandler = surfaceTextureHelper.getHandler();
            this.mSurfaceHelper = surfaceTextureHelper;
            final boolean didPost = maybePostOnCameraThread(new Runnable() {
                @Override
                public void run() {
                    openCameraAttempts = 0;
                    startCaptureOnCameraThread(width, height, framerate, frameObserver,
                            applicationContext);
                }
            });

            if (!didPost) {
                frameObserver.onCapturerStarted(false);
            }
        }

        if (DEBUG) Log.d(TAG, "startCapture( " + width + ", " + height + ", "
                + framerate + ", " + surfaceTextureHelper + ", " + applicationContext
                + ", " + frameObserver + ") is done!");
    }

    // Blocks until camera is known to be stopped.
    @Override
    public void stopCapture() throws InterruptedException {
        final boolean didPost = maybePostOnCameraThread(new Runnable() {
            @Override
            public void run() {
                stopCaptureOnCameraThread(true);
            }
        });

        if (!didPost) {
            if (DEBUG) Log.e(TAG, "Calling stopCapture() for already stopped camera.");
        }
        if (DEBUG) Log.d(TAG, "stopCapture() is done");
    }

    // Requests a new output format from the video capturer. Captured frames
    // by the camera will be scaled/or dropped by the video capturer.
    // It does not matter if width and height are flipped. I.E, |width| = 640, |height| = 480 produce
    // the same result as |width| = 480, |height| = 640.
    @Override
    public void onOutputFormatRequest(final int width, final int height, final int framerate) {
        maybePostOnCameraThread(new Runnable() {
            @Override
            public void run() {
                onOutputFormatRequestOnCameraThread(width, height, framerate);
            }
        });
        if (DEBUG) Log.d(TAG, "onOutputFormatRequest(" + width + ", "
                + height + ", " + framerate + ") is done!");
    }

    // Reconfigure the camera to capture in a new format. This should only be called while the camera
    // is running.
    @Override
    public void changeCaptureFormat(final int width, final int height, final int framerate) {

        maybePostOnCameraThread(new Runnable() {
            @Override
            public void run() {
                startPreviewOnCameraThread(width, height, framerate);
            }
        });

        if (DEBUG) Log.d(TAG, "changeCaptureFormat(" + width + ", "
                + height + ", " + framerate + ") is done");
    }

    @Override
    public void dispose() {
        if (DEBUG) Log.d(TAG, "dispose()");
    }

    // (Re)start preview with the closest supported format to |width| x |height| @ |framerate|.
    private void startPreviewOnCameraThread(int width, int height, int framerate) {
        synchronized (mCameraThreadLock) {
            if (mCameraThreadHandler == null || mUVCCamera == null) {
                return;
            } else {
                checkIsOnCameraThread();
            }
        }

        requestedWidth = width;
        requestedHeight = height;
        requestedFramerate = framerate;

        // Find closest supported format for |width| x |height| @ |framerate|.
        //The framerate varies because of lightning conditions.
        // The values are multiplied by 1000, so 1000 represents one frame per second.
        final CaptureFormat.FramerateRange fpsRange =
                new CaptureFormat.FramerateRange(5000, 30000);  // fix the value of framerate for test
        /*
        *  surpported UVC camera size is: width x height
        *  640 x 480
        *
        * */

        final Size previewSize = new Size(640, 480); // fix the value of size for test 640 480

        final CaptureFormat captureFormat =
                new CaptureFormat(previewSize.width, previewSize.height, fpsRange);

        // Check if we are already using this capture format, then we don't need to do anything.
        if (captureFormat.equals(this.captureFormat)) {
            return;
        }

        if (!isCapturingToTexture) {

        }
        // update camera parameters
        mUVCCamera.setUVCPreviewSizeRate(captureFormat.width, captureFormat.height,
                captureFormat.framerate.min, captureFormat.framerate.max);

        if (this.captureFormat != null) {
            mUVCCamera.stopUVCPreview();
            mUVCCamera.setUVCCameraFrameCallback(null);
        }

        this.captureFormat = captureFormat;
        mUVCCamera.setUVCCameraFrameCallback(this);
        mUVCCamera.startUVCPreview();
    }

    private void startCaptureOnCameraThread(final int width, final int height, final int framerate,
                                            final CapturerObserver frameObserver, final Context applicationContext) {
        synchronized (mCameraThreadLock) {
            if (mCameraThreadHandler == null) {
                return;
            } else {
                checkIsOnCameraThread();
            }
        }

        this.applicationContext = applicationContext;
        this.frameObserver = frameObserver;

        // test if it is suitable to move UVCCamera open function to here

        if (mSurfaceHelper == null) Log.d(TAG, "SurfaceTextureHelper is null!");
        mUVCCamera.setUVCPreviewTexture(mSurfaceHelper.getSurfaceTexture());
        startPreviewOnCameraThread(width, height, framerate);
        frameObserver.onCapturerStarted(true);
        if (isCapturingToTexture) {
            mSurfaceHelper.startListening(this);
        }
    }

    /*
    * The thing need to be done in this function:
    *
    *   release camera resource
    * */
    private void stopCaptureOnCameraThread(boolean isStopHandler) {
        synchronized (mCameraThreadLock) {
            if (mCameraThreadHandler == null) {
                if (DEBUG) Log.d(TAG, "stopCaptureOnCameraThread: Camera is stopped");
            } else {
                checkIsOnCameraThread();
            }
        }

        if (DEBUG) Log.d(TAG, "stopCaptureOnCameraThread()");

        // Note that the camera might still not be started here if startCaptureOnCameraThread failed
        // and we posted a retry.

        // Make sure onTextureFrameAvailable() is not called anymore.
        if (mSurfaceHelper != null) {
            mSurfaceHelper.stopListening();
        }

        if (isStopHandler) {
            synchronized (mCameraThreadLock) {
                /* Clear the mCameraThreadHandler first, in case stopPreview or
                   other driver code deadlocks. Deadlock in
                   android.hardware.Camera._stopPreview(Native Method) has
                   been observed on Nexus 5 (hammerhead), OS version LMY48I.
                   The camera might post another one or two preview frames
                   before stopped, so we have to check for a null
                   cameraThreadHandler in our handler. Remove all pending
                   Runnables posted from |this|. */
                if (mCameraThreadHandler != null) {
                    mCameraThreadHandler.removeCallbacksAndMessages(this);
                    mCameraThreadHandler = null;
                }
                mSurfaceHelper = null;
            }
        }

        if (mUVCCamera != null) {
            mUVCCamera.stopUVCPreview();
        }

        if (DEBUG) Log.d(TAG, "stopCaptureOnCameraThread is done");

    }



    private void onOutputFormatRequestOnCameraThread(int width, int height, int framerate) {
        synchronized (mCameraThreadLock) {
            if (mCameraThreadHandler == null) {
                return;
            } else {
                checkIsOnCameraThread();
            }
        }

        frameObserver.onOutputFormatRequest(width, height, framerate);
    }

    // this function should be called by hardware camera when a video frame is ready
    @Override
    public void onUVCFrame(final byte[] frameData) {
        maybePostOnCameraThread(new Runnable() {
            @Override
            public void run() {
                onUVCFrameRunOnCameraThread(frameData);
            }
        });
    }

    public void onUVCFrameRunOnCameraThread(byte[] frameData) {
        synchronized (mCameraThreadLock) {
            if (mCameraThreadHandler == null) {
                return;
            } else {
                checkIsOnCameraThread();
            }
        }

        final long captureTimeNs =
                TimeUnit.MILLISECONDS.toNanos(SystemClock.elapsedRealtime());
        frameObserver.onByteBufferFrameCaptured(frameData, captureFormat.width, captureFormat.height,
                oriention, captureTimeNs);
    }

    @Override
    public void onTextureFrameAvailable(final int oesTextureId,
                                        final float[] transformMatrix,
                                        final long timestampNs) {
        maybePostOnCameraThread(new Runnable() {
            @Override
            public void run() {
                if (DEBUG) Log.d(TAG, "onTextureFrameAvailable(" + oesTextureId
                        + ", " + transformMatrix
                        + ", " + timestampNs);
            }
        });
    }

    private boolean maybePostOnCameraThread(Runnable runnable) {
        return maybePostDelayedOnCameraThread(0 /* delayMs */, runnable);
    }

    private boolean maybePostDelayedOnCameraThread(int delayMs, Runnable runnable) {
        synchronized (mCameraThreadLock) {
            return mCameraThreadHandler != null
                    && mCameraThreadHandler.postAtTime (
                    runnable, this /* token */, SystemClock.uptimeMillis() + delayMs);
        }
    }

    private void checkIsOnCameraThread() {
        synchronized (mCameraThreadLock) {
            if (mCameraThreadHandler == null) {
                Logging.e(TAG, "Camera is stopped - can't check thread.");
            } else if (Thread.currentThread() != mCameraThreadHandler.getLooper().getThread()) {
                throw new IllegalStateException("Wrong thread");
            }
        }
    }

}
