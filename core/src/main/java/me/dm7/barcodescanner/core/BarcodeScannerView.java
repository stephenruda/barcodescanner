package me.dm7.barcodescanner.core;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

public abstract class BarcodeScannerView extends FrameLayout implements Camera.PreviewCallback  {

    private CameraWrapper mCameraWrapper;
    protected CameraPreview mPreview;
    private Boolean mFlashState;
    private boolean mAutofocusState = true;
    private boolean mShouldScaleToFill = false;
    private float mAspectTolerance = 0.1f;

    public BarcodeScannerView(Context context) {
        super(context);
    }

    public BarcodeScannerView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public final void setupLayout(CameraWrapper cameraWrapper) {
        removeAllViews();
        mPreview = new CameraPreview(getContext(), cameraWrapper, this);
        mPreview.setAspectTolerance(mAspectTolerance);
        mPreview.setShouldScaleToFill(mShouldScaleToFill);
        mPreview.setBackgroundColor(Color.TRANSPARENT);
        if (!mShouldScaleToFill) {
            RelativeLayout relativeLayout = new RelativeLayout(getContext());
            relativeLayout.setGravity(Gravity.CENTER);
            relativeLayout.setBackgroundColor(Color.TRANSPARENT);
            relativeLayout.addView(mPreview);
            addView(relativeLayout);
        } else {
            addView(mPreview);
        }
    }

    public void startCamera(int cameraId) {
        Camera camera = CameraUtils.getCameraInstance(cameraId);
        setupCameraPreview(CameraWrapper.getWrapper(camera, cameraId));
    }

    public void setupCameraPreview(CameraWrapper cameraWrapper) {
        mCameraWrapper = cameraWrapper;
        if(mCameraWrapper != null) {
            setupLayout(mCameraWrapper);
            if(mFlashState != null) {
                setFlash(mFlashState);
            }
            setAutoFocus(mAutofocusState);
        }
    }

    public void startCamera() {
        startCamera(CameraUtils.getDefaultCameraId());
    }

    public void stopCamera() {
        if(mCameraWrapper != null) {
            mPreview.stopCameraPreview();
            mPreview.setCamera(null, null);
            mCameraWrapper.mCamera.release();
            mCameraWrapper = null;
        }
    }

    public void stopCameraPreview() {
        if(mPreview != null) {
            mPreview.stopCameraPreview();
        }
    }

    protected void resumeCameraPreview() {
        if(mPreview != null) {
            mPreview.showCameraPreview();
        }
    }

    public void setFlash(boolean flag) {
        mFlashState = flag;
        if(mCameraWrapper != null && CameraUtils.isFlashSupported(mCameraWrapper.mCamera)) {

            Camera.Parameters parameters = mCameraWrapper.mCamera.getParameters();
            if(flag) {
                if(parameters.getFlashMode().equals(Camera.Parameters.FLASH_MODE_TORCH)) {
                    return;
                }
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            } else {
                if(parameters.getFlashMode().equals(Camera.Parameters.FLASH_MODE_OFF)) {
                    return;
                }
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            }
            mCameraWrapper.mCamera.setParameters(parameters);
        }
    }

    public boolean getFlash() {
        if(mCameraWrapper != null && CameraUtils.isFlashSupported(mCameraWrapper.mCamera)) {
            Camera.Parameters parameters = mCameraWrapper.mCamera.getParameters();
            if(parameters.getFlashMode().equals(Camera.Parameters.FLASH_MODE_TORCH)) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    public void toggleFlash() {
        if(mCameraWrapper != null && CameraUtils.isFlashSupported(mCameraWrapper.mCamera)) {
            Camera.Parameters parameters = mCameraWrapper.mCamera.getParameters();
            if(parameters.getFlashMode().equals(Camera.Parameters.FLASH_MODE_TORCH)) {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            } else {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            }
            mCameraWrapper.mCamera.setParameters(parameters);
        }
    }

    public void setAutoFocus(boolean state) {
        mAutofocusState = state;
        if(mPreview != null) {
            mPreview.setAutoFocus(state);
        }
    }

    public void setShouldScaleToFill(boolean shouldScaleToFill) {
        mShouldScaleToFill = shouldScaleToFill;
    }

    public void setAspectTolerance(float aspectTolerance) {
        mAspectTolerance = aspectTolerance;
    }

    public byte[] getRotatedData(byte[] data, Camera camera) {
        Camera.Parameters parameters = camera.getParameters();
        Camera.Size size = parameters.getPreviewSize();
        int width = size.width;
        int height = size.height;

        int rotationCount = getRotationCount();

        if(rotationCount == 1 || rotationCount == 3) {
            for (int i = 0; i < rotationCount; i++) {
                byte[] rotatedData = new byte[data.length];
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++)
                        rotatedData[x * height + height - y - 1] = data[x + y * width];
                }
                data = rotatedData;
                int tmp = width;
                width = height;
                height = tmp;
            }
        }

        return data;
    }

    public int getRotationCount() {
        int displayOrientation = mPreview.getDisplayOrientation();
        return displayOrientation / 90;
    }
}

