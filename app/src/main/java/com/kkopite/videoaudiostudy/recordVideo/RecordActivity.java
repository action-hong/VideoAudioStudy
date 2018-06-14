package com.kkopite.videoaudiostudy.recordVideo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.kkopite.videoaudiostudy.R;
import com.kkopite.videoaudiostudy.c2.AudioCapture;
import com.kkopite.videoaudiostudy.c2.AudioEncoder;

// 录制视频 并输出 mp4 格式的视频
public class RecordActivity extends AppCompatActivity implements AudioCapture.OnAudioFrameCapturedListener, AudioEncoder.OnAudioEncodedListener {

    // 采集音频
    private AudioCapture mAudioCapture;
    // 编码
    private AudioEncoder mAudioEncoder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        mAudioCapture.startCapture();
        mAudioEncoder.open();
        mAudioCapture.setAudioFrameCapturedListener(this);
        mAudioEncoder.setAudioEncodedListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mAudioEncoder.close();
        mAudioEncoder.close();
    }

    @Override
    public void onAudioFrameCaptured(byte[] audioData) {
        long presentationTimeUs = (System.nanoTime()) / 1000L;
        mAudioEncoder.encode(audioData, presentationTimeUs);
    }

    @Override
    public void onFrameEncoded(byte[] encoded, long presentationTimeUs) {
        // 编码得到的
    }
}
