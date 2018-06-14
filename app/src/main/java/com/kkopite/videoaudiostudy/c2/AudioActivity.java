package com.kkopite.videoaudiostudy.c2;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.kkopite.videoaudiostudy.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AudioActivity extends AppCompatActivity implements View.OnClickListener, AudioCapture.OnAudioFrameCapturedListener, AudioEncoder.OnAudioEncodedListener, AudioDecoder.OnAudioDecodedListener {
    private Button mBtnCapture;
    private Button mBtnStop;
    private Button mBtnPlay;

    // TODO 1 音频PCM 采集, 播放(PS: 录的声音大一点, 不然还以为是没有成功)
    // TODO 2 读写音频 wav 文件

    private AudioPlayer mPlayer = new AudioPlayer();
    private AudioCapture mAudioCapture = new AudioCapture();
    private AudioEncoder mAudioEncoder = new AudioEncoder();
    private AudioDecoder mAudioDecoder = new AudioDecoder();

    private File mWavDir;

    private List<byte[]> datas = new ArrayList<>();

    private byte[] audios;
    private Button mBtnStopPlay;

    private WavFileWriter mWriter = new WavFileWriter();
    private WavFileReader mReader = new WavFileReader();
    private String mFilePath;

    private static final int SAMPLES_PER_FRAME = 1024;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio);
        initView();
    }

    private void initView() {
        mBtnCapture = (Button) findViewById(R.id.btn_capture);
        mBtnStop = (Button) findViewById(R.id.btn_stop);
        mBtnPlay = (Button) findViewById(R.id.btn_play);

        mBtnCapture.setOnClickListener(this);
        mBtnStop.setOnClickListener(this);
        mBtnPlay.setOnClickListener(this);

        mAudioCapture.setAudioFrameCapturedListener(this);
        mBtnStopPlay = (Button) findViewById(R.id.btn_stop_play);
        mBtnStopPlay.setOnClickListener(this);

        File file = new File(Environment.getExternalStorageDirectory(), getPackageName());
        if (!file.exists()) {
            file.mkdir();
        }
        mWavDir = file;

        mAudioDecoder.open();
        mAudioEncoder.open();
        mAudioEncoder.setAudioEncodedListener(this);
        mAudioDecoder.setAudioDecodedListener(this);

        new Thread(mEncodeRenderRunnable).start();
        new Thread(mDecodeRenderRunnable).start();
        mAudioCapture.startCapture();


        mPlayer.startPlayer();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_capture:
                datas.clear();
                try {
                    mFilePath = generateFilePath();
                    mWriter.openFile(mFilePath, AudioCapture.DEFAULT_SAMPLE_RATE, 1, 16);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mAudioCapture.startCapture();
                break;
            case R.id.btn_stop:
                mAudioCapture.stopCapture();
//                byte[] tmp = new byte[]{};
//                for (byte[] data : datas) {
//                    tmp = concatAll(tmp, data);
//                }
//                audios = tmp;
                try {
                    mWriter.closeFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.btn_play:
                try {
                    mReader.openFile(mFilePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                mPlayer.startPlayer();
                mIsTestingExit = false;
                new Thread(AudioPlayRunnable).start();
//                for (byte[] data : datas) {
//                    mPlayer.play(data, 0, data.length);
//                }
                break;
            case R.id.btn_stop_play:
                mIsTestingExit = true;
                mPlayer.stopPlayer();
                break;
        }
    }

    @Override
    public void onAudioFrameCaptured(byte[] audioData) {
        datas.add(audioData);
        mWriter.writeData(audioData, 0, audioData.length);

        long presentationTimeUs = (System.nanoTime()) / 1000L;
        mAudioEncoder.encode(audioData, presentationTimeUs);
    }

    public static byte[] concatAll(byte[] first, byte[] rest) {

        int totalLength = first.length;


        totalLength += rest.length;


        byte[] result = Arrays.copyOf(first, totalLength);

        int offset = first.length;


        System.arraycopy(rest, 0, result, offset, rest.length);

        return result;

    }

    private String generateFilePath() {
        String name = System.currentTimeMillis() + ".wav";
        return new File(mWavDir, name).getPath();
    }

    private volatile boolean mIsTestingExit = false;

    @Override
    protected void onPause() {
        super.onPause();
        mIsTestingExit = true;
    }

    private Runnable AudioPlayRunnable = new Runnable() {
        @Override
        public void run() {
            // 音乐缓存区大小一般是 一帧的 2~N倍
            // 这里去  2倍
            // 额
            byte[] buffer = new byte[SAMPLES_PER_FRAME * 2];
            while (!mIsTestingExit && mReader.readData(buffer, 0, buffer.length) > 0){
                mPlayer.play(buffer, 0, buffer.length);
            }

            mPlayer.stopPlayer();

            try {
                mReader.closeFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public void onFrameEncoded(byte[] encoded, long presentationTimeUs) {
        mAudioDecoder.decode(encoded, presentationTimeUs);
    }

    private Runnable mEncodeRenderRunnable = new Runnable() {
        @Override
        public void run() {
            while (!mIsTestingExit) {
                mAudioEncoder.retrieve();
            }
            mAudioEncoder.close();
        }
    };

    private Runnable mDecodeRenderRunnable = new Runnable() {
        @Override
        public void run() {
            while (!mIsTestingExit) {
                mAudioDecoder.retrieve();
            }
            mAudioDecoder.close();
        }
    };

    @Override
    public void onFrameDecoded(byte[] decoded, long presentationTimeUs) {
        mPlayer.play(decoded, 0, decoded.length);
    }
}
