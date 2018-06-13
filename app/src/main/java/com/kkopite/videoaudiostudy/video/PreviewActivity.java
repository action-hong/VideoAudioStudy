package com.kkopite.videoaudiostudy.video;

import android.content.Context;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.kkopite.videoaudiostudy.R;

import java.io.File;
import java.io.IOException;

public class PreviewActivity extends AppCompatActivity implements View.OnClickListener, SurfaceHolder.Callback, Camera.PreviewCallback {


    private int mFaceBackCameraId;
    private int mFaceBackCameraOrientation;
    private int mFaceFrontCameraOrientation;
    private int mMFaceFrontCameraId;
    private Camera mCamera;
    private SurfaceView mSurface;
    private Button mBtnStart;
    private MediaRecorder mMediaRecorder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        initView();

        int numberOfCameras = Camera.getNumberOfCameras();

        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();

            Camera.getCameraInfo(i, cameraInfo);
            // 后置
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                mFaceBackCameraId = i;
                mFaceBackCameraOrientation = cameraInfo.orientation;
            } else if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                mMFaceFrontCameraId = i;
                mFaceFrontCameraOrientation = cameraInfo.orientation;
            }
        }

        SurfaceHolder holder = mSurface.getHolder();
        holder.addCallback(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCamera = Camera.open(mFaceBackCameraId);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCamera.release();
        mCamera = null;
    }

    private void startPreview(SurfaceHolder surfaceHolder) {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(mFaceBackCameraId, cameraInfo);
        int cameraRotationOffset = cameraInfo.orientation;

        // 相机参数
        Camera.Parameters parameters = mCamera.getParameters();
        // 设置对焦模式

        // 设置闪光模式

        int rotation = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break; // Natural orientation
            case Surface.ROTATION_90:
                degrees = 90;
                break; // Landscape left
            case Surface.ROTATION_180:
                degrees = 180;
                break;// Upside down
            case Surface.ROTATION_270:
                degrees = 270;
                break;// Landscape right
        }
        int displayRotation = 0;
        //根据前置与后置摄像头的不同，设置预览方向，否则会发生预览图像倒过来的情况。
        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            displayRotation = (cameraRotationOffset + degrees) % 360;
            displayRotation = (360 - displayRotation) % 360; // compensate
        } else {
            displayRotation = (cameraRotationOffset - degrees + 360) % 360;
        }
        mCamera.setDisplayOrientation(displayRotation);

//        parameters.setPreviewSize(500, 500);
//        parameters.setPictureSize(500, 500);

        // 默认就是 NV21
//        parameters.setPreviewFormat(ImageFormat.NV21);
        mCamera.setParameters(parameters);
        try {
            mCamera.setPreviewDisplay(surfaceHolder);
            mCamera.startPreview();
            mCamera.setPreviewCallback(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initView() {
        mSurface = (SurfaceView) findViewById(R.id.surface);
        mBtnStart = (Button) findViewById(R.id.btn_start);

        mBtnStart.setOnClickListener(this);
    }

    private boolean isRecording = false;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start:
                if (isRecording) {
                    mMediaRecorder.stop();
                    mBtnStart.setText("录制");
                } else {
                    if (prepareMediaRecorder()) {
                        mBtnStart.setText("停止");
                        mMediaRecorder.start();
                        isRecording = true;
                    }
                }
                break;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        startPreview(holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    private String getVideoFilePath(Context context) {
        final File dir = context.getExternalFilesDir(null);
        return (dir == null ? "" : (dir.getAbsolutePath() + "/"))
                + System.currentTimeMillis() + ".mp4";
    }

    protected boolean prepareMediaRecorder () {
        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);

        mMediaRecorder.setOutputFile(getVideoFilePath((this)));

        try {
            mMediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;

    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        // data 即为 nv21数据
    }
}
