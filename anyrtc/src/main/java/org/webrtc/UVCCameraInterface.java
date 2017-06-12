package org.webrtc;

import android.graphics.SurfaceTexture;

import java.util.List;

/**
 * Created by hadoop on 17-1-12.
 */
public interface UVCCameraInterface {

    interface UVCFrameCallback{
        void onUVCFrame(byte[] frameData);
    }

    void setUVCCameraFrameCallback(UVCFrameCallback frameCallback);

    void setUVCPreviewTexture(SurfaceTexture texture);

    void setUVCPreviewSizeRate(int width, int height, int minFps, int maxFps);

    void startUVCPreview();

    void stopUVCPreview();

    List<CameraEnumerationAndroid.CaptureFormat> getSupportedFormats();
}
