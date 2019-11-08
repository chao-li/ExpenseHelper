package com.example.expensehelper;

import com.wonderkiln.camerakit.CameraKitError;
import com.wonderkiln.camerakit.CameraKitEvent;
import com.wonderkiln.camerakit.CameraKitEventListener;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;

public class CameraKitUtility implements CameraKitEventListener {


    private CameraFunctionHandler mCameraFunctionHandler;


    public interface CameraFunctionHandler {
        void onIsCameraReady(Boolean isReady);

        void onImageCaptured(CameraKitImage cameraKitImage);


    }

    // constructor
    public CameraKitUtility(CameraFunctionHandler cameraFunctionHandler) {
        mCameraFunctionHandler = cameraFunctionHandler;
    }


    // Camera listener
    @Override
    public void onEvent(CameraKitEvent cameraKitEvent) {
        switch (cameraKitEvent.getType()) {
            case CameraKitEvent.TYPE_CAMERA_OPEN:
                mCameraFunctionHandler.onIsCameraReady(true);
                break;

            case CameraKitEvent.TYPE_CAMERA_CLOSE:
                mCameraFunctionHandler.onIsCameraReady(false);
                break;
        }

    }

    @Override
    public void onError(CameraKitError cameraKitError) {
    }

    @Override
    public void onImage(CameraKitImage cameraKitImage) {
        mCameraFunctionHandler.onImageCaptured(cameraKitImage);
    }

    @Override
    public void onVideo(CameraKitVideo cameraKitVideo) {

    }

}