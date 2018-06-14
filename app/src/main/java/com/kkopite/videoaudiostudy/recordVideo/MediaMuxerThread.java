package com.kkopite.videoaudiostudy.recordVideo;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.util.Log;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.Vector;

/**
 * 音视频混合线程
 * Created by kkopite on 2018/6/14.
 */
public class MediaMuxerThread extends Thread{

    private static final String TAG = "MediaMuxerThread";

    public static final int TRACK_VIDEO = 0;
    public static final int TRACK_AUDIO = 1;

    private final Object lock = new Object();

    private static MediaMuxerThread muxerThread ;

    private AudioEncoderThread mAudioEncoderThread;
    private VideoEncoderThread mVideoEncoderThread;

    private MediaMuxer mMediaMuxer;
    private Vector<MuxerData> mMuxerData;

    private int videoTrackIndex = -1;
    private int audioTrackIndex = -1;

    private FileUtils fileSwapHelper;

    private volatile boolean isVideoTrackAdd;
    private volatile boolean isAudioTrackAdd;

    private volatile boolean isExit = false;


    public static void startMuxer() {
        if (muxerThread == null) {
            synchronized (MediaMuxerThread.class) {
                if (muxerThread == null) {
                    muxerThread = new MediaMuxerThread();
                    muxerThread.start();
                }
            }
        }
    }

    public static void stopMuxer() {
        if (muxerThread == null) {
            muxerThread.exit();
            try {
                muxerThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            muxerThread = null;
        }
    }

    private void readyStart() throws IOException {
        fileSwapHelper.requestSwapFile(true);
        readyStart(fileSwapHelper.getNextFileName());
    }

    private void readyStart(String filePath) throws IOException {
        isExit = false;
        isAudioTrackAdd = false;
        isVideoTrackAdd = false;
        mMuxerData.clear();

        mMediaMuxer = new MediaMuxer(filePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        if (mAudioEncoderThread != null) {
            mAudioEncoderThread.setMuxerReady(true);
        }
        if (mVideoEncoderThread != null) {
            mVideoEncoderThread.setMuxerReady(true);
        }
    }

    // 添加视频帧数据
    public static void addVideoFrameData(byte[] data) {
        if (muxerThread != null) {
            muxerThread.addVideoData(data);
        }
    }

    public void addMuxerData(MuxerData data) {
        if (!isMuxerStart()) {
            return;
        }

        mMuxerData.add(data);
        synchronized (lock) {
            lock.notify();
        }
    }

    // 添加视频/音频轨
    public synchronized void addTrackIndex(int index, MediaFormat mediaFormat) {
        if (isMuxerStart()) {
            return;
        }

        if ((index == TRACK_AUDIO && isAudioTrackAdd()) || (index == TRACK_VIDEO && isVideoTrackAdd())) {
            return;
        }

        if (mMediaMuxer != null) {
            int track = 0;
            track = mMediaMuxer.addTrack(mediaFormat);

            if (index == TRACK_VIDEO) {
                videoTrackIndex = track;
                isVideoTrackAdd = true;
            } else {
                audioTrackIndex = track;
                isAudioTrackAdd = true;
            }

            requestStart();
        }
    }

    // 请求混合器开始启动
    private void requestStart() {
        synchronized (lock) {
            if (isMuxerStart()) {
                mMediaMuxer.start();
                Log.e(TAG, "requestStart启动混合器..开始等待数据输入...");
                lock.notify();
            }
        }
    }

    private boolean isVideoTrackAdd() {
        return isVideoTrackAdd;
    }

    private boolean isAudioTrackAdd() {
        return isAudioTrackAdd;
    }


    private boolean isMuxerStart() {
        return isAudioTrackAdd && isVideoTrackAdd;
    }

    private void addVideoData(byte[] data) {
        if (mVideoEncoderThread != null) {
            mVideoEncoderThread.add(data);
        }
    }

    private void initMuxer() {
        mMuxerData = new Vector<>();
        fileSwapHelper = new FileUtils();
        mAudioEncoderThread = new AudioEncoderThread(new WeakReference<>(this));
        mVideoEncoderThread = new VideoEncoderThread(1920, 1080, new WeakReference<>(this));
        mAudioEncoderThread.start();
        mVideoEncoderThread.start();
        try {
            readyStart();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        super.run();
        // 初始化混合器
        initMuxer();
        while (!isExit) {
            if (isMuxerStart()) {
                if (mMuxerData.isEmpty()) {
                    synchronized (lock) {
                        try {
                            Log.e(TAG, "等待混合数据");
                            lock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    if (fileSwapHelper.requestSwapFile()) {
                        // 需要切换文件
                        String nextFileName = fileSwapHelper.getNextFileName();
                        Log.e(TAG, "正在重启混合器..." + nextFileName );
                        restart(nextFileName);
                    } else {
                        MuxerData data = mMuxerData.remove(0);
                        int track;
                        if (data.trackIndex == TRACK_AUDIO) {
                            track = audioTrackIndex;
                        } else {
                            track = videoTrackIndex;
                        }
                        Log.e(TAG, "写入混合数据" + data.mBufferInfo.size );
                        mMediaMuxer.writeSampleData(track, data.mByteBuffer, data.mBufferInfo);
                    }
                }
            } else {
                synchronized (lock) {
                    try {
                        Log.e(TAG, "等待音视轨添加...");
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            readyStop();
            Log.e(TAG, "混合器退出");
        }
    }

    private void restart()  {
        fileSwapHelper.requestSwapFile(true);
        String nextFileName = fileSwapHelper.getNextFileName();
        restart(nextFileName);
    }

    private void restart(String filePath) {
        restartAudioVideo();
        readyStop();

        try {
            readyStart(filePath);
        } catch (IOException e) {
            Log.e(TAG, "readyStart(filePath, true) " + "重启混合器失败 尝试再次重启!" + e.toString());
            restart();
            return;
        }
        Log.e(TAG, "重启混合器完成");
    }

    private void readyStop() {
        if (mMediaMuxer != null) {
            try {
                mMediaMuxer.stop();
            } catch (Exception e) {
                Log.e(TAG, "mMediaMuxer.stop() 异常:" + e.toString());
            }
            try {
                mMediaMuxer.release();
            } catch (Exception e) {
                Log.e(TAG, "mMediaMuxer.release() 异常:" + e.toString());
            }
            mMediaMuxer = null;
        }
    }

    private void restartAudioVideo() {
        if (mAudioEncoderThread != null) {
            audioTrackIndex = -1;
            isAudioTrackAdd = false;
            mAudioEncoderThread.restart();
        }
        if (mVideoEncoderThread != null) {
            videoTrackIndex = -1;
            isVideoTrackAdd = false;
            mVideoEncoderThread.restart();
        }
    }

    private void exit() {
        if (mVideoEncoderThread != null) {
            mVideoEncoderThread.exit();
            try {
                mVideoEncoderThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (mAudioEncoderThread != null) {
            mAudioEncoderThread.exit();
            try {
                mAudioEncoderThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        isExit = true;
        synchronized (lock) {
            lock.notify();
        }
    }


    private static class MuxerData {
        int trackIndex;
        ByteBuffer mByteBuffer;
        MediaCodec.BufferInfo mBufferInfo;

        public MuxerData(int trackIndex, ByteBuffer byteBuffer, MediaCodec.BufferInfo bufferInfo) {
            this.trackIndex = trackIndex;
            mByteBuffer = byteBuffer;
            mBufferInfo = bufferInfo;
        }
    }
}
