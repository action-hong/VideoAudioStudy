package com.kkopite.videoaudiostudy.extractor;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.kkopite.videoaudiostudy.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class ExtractorActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String SDCARD_PATH = Environment.getExternalStorageDirectory().getPath();

    MediaExtractor mMediaExtractor;
    MediaMuxer mMediaMuxer;
    private Button mExactor;
    private Button mMuxer;
    private Button mMuxerAudio;
    private Button mCombineVideo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extractor);
        initView();
    }

    private void exactorMedia() {
        // 分离video, audio
        FileOutputStream videoOutputStream = null;
        FileOutputStream audioOutputStream = null;

        try {
            File videoFile = new File(SDCARD_PATH, "output_video.mp4");
            if (!videoFile.exists()) {
                videoFile.createNewFile();
            }

            File audioFile = new File(SDCARD_PATH, "output_audio");
            videoOutputStream = new FileOutputStream(videoFile);
            audioOutputStream = new FileOutputStream(audioFile);
            mMediaExtractor.setDataSource(SDCARD_PATH + "/input.mp4");
            int trackCount = mMediaExtractor.getTrackCount();
            int audioTrack = -1;
            int videoTrack = -1;
            for (int i = 0; i < trackCount; i++) {
                MediaFormat trackFormat = mMediaExtractor.getTrackFormat(i);
                String mineType = trackFormat.getString(MediaFormat.KEY_MIME);

                if (mineType.startsWith("video/")) {
                    videoTrack = i;
                }

                if (mineType.startsWith("audio/")) {
                    audioTrack = i;
                }
            }

            ByteBuffer byteBuffer = ByteBuffer.allocate(500 * 1024);
            mMediaExtractor.selectTrack(videoTrack);

            while (true) {
                int readSampleCount = mMediaExtractor.readSampleData(byteBuffer, 0);
                if (readSampleCount < 0) {
                    break;
                }

                byte[] buffer = new byte[readSampleCount];
                byteBuffer.get(buffer);
                videoOutputStream.write(buffer);
                byteBuffer.clear();
                mMediaExtractor.advance();
            }

            mMediaExtractor.selectTrack(audioTrack);

            while (true) {
                int readSampleCount = mMediaExtractor.readSampleData(byteBuffer, 0);
                if (readSampleCount < 0) {
                    break;
                }

                byte[] buffer = new byte[readSampleCount];
                byteBuffer.get(buffer);
                audioOutputStream.write(buffer);
                byteBuffer.clear();
                mMediaExtractor.advance();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            mMediaExtractor.release();
            try {
                videoOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void muxerMedia() {
        mMediaExtractor = new MediaExtractor();
        int videoIndex = -1;
        try {
            mMediaExtractor.setDataSource(SDCARD_PATH + "/input.mp4");
            int trackCount = mMediaExtractor.getTrackCount();
            for (int i = 0; i < trackCount; i++) {
                MediaFormat trackFormat = mMediaExtractor.getTrackFormat(i);
                String mimeType = trackFormat.getString(MediaFormat.KEY_MIME);
                if (mimeType.startsWith("video/")) {
                    videoIndex = i;
                }
            }

            mMediaExtractor.selectTrack(videoIndex);
            MediaFormat trackFormat = mMediaExtractor.getTrackFormat(videoIndex);
            mMediaMuxer = new MediaMuxer(SDCARD_PATH + "/output_video", MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            int trackIndex = mMediaMuxer.addTrack(trackFormat);
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024 * 500);
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            mMediaMuxer.start();
            long videoSampleTime;
            {
                mMediaExtractor.readSampleData(byteBuffer, 0);
                if (mMediaExtractor.getSampleFlags() == MediaExtractor.SAMPLE_FLAG_SYNC) {
                    mMediaExtractor.advance();
                }
                mMediaExtractor.readSampleData(byteBuffer, 0);
                long firstVideoPST = mMediaExtractor.getSampleTime();
                mMediaExtractor.advance();
                mMediaExtractor.readSampleData(byteBuffer, 0);
                long secondVideoPTS = mMediaExtractor.getSampleTime();
                videoSampleTime = Math.abs(secondVideoPTS - firstVideoPST);
            }

            mMediaExtractor.unselectTrack(videoIndex);
            mMediaExtractor.selectTrack(videoIndex);

            while (true) {
                int readSampleSize = mMediaExtractor.readSampleData(byteBuffer, 0);
                if (readSampleSize < 0) {
                    break;
                }
                mMediaExtractor.advance();
                bufferInfo.size = readSampleSize;
                bufferInfo.offset = 0;
                bufferInfo.flags = mMediaExtractor.getSampleFlags();
                bufferInfo.presentationTimeUs += videoSampleTime;

                mMediaMuxer.writeSampleData(trackIndex, byteBuffer, bufferInfo);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void muxerAudio() {
        mMediaExtractor = new MediaExtractor();
        int audioIndex = -1;
        try {
            mMediaExtractor.setDataSource(SDCARD_PATH + "/input.mp4");
            int trackCount = mMediaExtractor.getTrackCount();
            for (int i = 0; i < trackCount; i++) {
                MediaFormat trackFormat = mMediaExtractor.getTrackFormat(i);
                if (trackFormat.getString(MediaFormat.KEY_MIME).startsWith("audio/")) {
                    audioIndex = i;
                }
            }
            mMediaExtractor.selectTrack(audioIndex);
            MediaFormat trackFormat = mMediaExtractor.getTrackFormat(audioIndex);
            mMediaMuxer = new MediaMuxer(SDCARD_PATH + "/output_audio", MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            int writeAudioIndex = mMediaMuxer.addTrack(trackFormat);
            mMediaMuxer.start();
            ByteBuffer byteBuffer = ByteBuffer.allocate(500 * 1024);
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

            long stampTime = 0;
            //获取帧之间的间隔时间
            {
                mMediaExtractor.readSampleData(byteBuffer, 0);
                if (mMediaExtractor.getSampleFlags() == MediaExtractor.SAMPLE_FLAG_SYNC) {
                    mMediaExtractor.advance();
                }
                mMediaExtractor.readSampleData(byteBuffer, 0);
                long secondTime = mMediaExtractor.getSampleTime();
                mMediaExtractor.advance();
                mMediaExtractor.readSampleData(byteBuffer, 0);
                long thirdTime = mMediaExtractor.getSampleTime();
                stampTime = Math.abs(thirdTime - secondTime);
                Log.e("fuck", stampTime + "");
            }

            mMediaExtractor.unselectTrack(audioIndex);
            mMediaExtractor.selectTrack(audioIndex);
            while (true) {
                int readSampleSize = mMediaExtractor.readSampleData(byteBuffer, 0);
                if (readSampleSize < 0) {
                    break;
                }
                mMediaExtractor.advance();

                bufferInfo.size = readSampleSize;
                bufferInfo.flags = mMediaExtractor.getSampleFlags();
                bufferInfo.offset = 0;
                bufferInfo.presentationTimeUs += stampTime;

                mMediaMuxer.writeSampleData(writeAudioIndex, byteBuffer, bufferInfo);
            }
            mMediaMuxer.stop();
            mMediaMuxer.release();
            mMediaExtractor.release();
            Log.e("fuck", "finish");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void combineVideo() {
        try {
            MediaExtractor videoExtractor = new MediaExtractor();
            videoExtractor.setDataSource(SDCARD_PATH + "/output_video");
            MediaFormat videoFormat = null;
            int videoTrackIndex = -1;
            int videoTrackCount = videoExtractor.getTrackCount();
            for (int i = 0; i < videoTrackCount; i++) {
                videoFormat = videoExtractor.getTrackFormat(i);
                String mimeType = videoFormat.getString(MediaFormat.KEY_MIME);
                if (mimeType.startsWith("video/")) {
                    videoTrackIndex = i;
                    break;
                }
            }

            MediaExtractor audioExtractor = new MediaExtractor();
            audioExtractor.setDataSource(SDCARD_PATH + "/output_audio");
            MediaFormat audioFormat = null;
            int audioTrackIndex = -1;
            int audioTrackCount = audioExtractor.getTrackCount();
            for (int i = 0; i < audioTrackCount; i++) {
                audioFormat = audioExtractor.getTrackFormat(i);
                String mimeType = audioFormat.getString(MediaFormat.KEY_MIME);
                if (mimeType.startsWith("audio/")) {
                    audioTrackIndex = i;
                    break;
                }
            }

            videoExtractor.selectTrack(videoTrackIndex);
            audioExtractor.selectTrack(audioTrackIndex);

            MediaCodec.BufferInfo videoBufferInfo = new MediaCodec.BufferInfo();
            MediaCodec.BufferInfo audioBufferInfo = new MediaCodec.BufferInfo();

            MediaMuxer mediaMuxer = new MediaMuxer(SDCARD_PATH + "/output", MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            int writeVideoTrackIndex = mediaMuxer.addTrack(videoFormat);
            int writeAudioTrackIndex = mediaMuxer.addTrack(audioFormat);
            mediaMuxer.start();
            ByteBuffer byteBuffer = ByteBuffer.allocate(500 * 1024);
            long sampleTime = 0;
            {
                videoExtractor.readSampleData(byteBuffer, 0);
                if (videoExtractor.getSampleFlags() == MediaExtractor.SAMPLE_FLAG_SYNC) {
                    videoExtractor.advance();
                }
                videoExtractor.readSampleData(byteBuffer, 0);
                long secondTime = videoExtractor.getSampleTime();
                videoExtractor.advance();
                long thirdTime = videoExtractor.getSampleTime();
                sampleTime = Math.abs(thirdTime - secondTime);
            }
            videoExtractor.unselectTrack(videoTrackIndex);
            videoExtractor.selectTrack(videoTrackIndex);

            while (true) {
                int readVideoSampleSize = videoExtractor.readSampleData(byteBuffer, 0);
                if (readVideoSampleSize < 0) {
                    break;
                }
                videoBufferInfo.size = readVideoSampleSize;
                videoBufferInfo.presentationTimeUs += sampleTime;
                videoBufferInfo.offset = 0;
                videoBufferInfo.flags = videoExtractor.getSampleFlags();
                mediaMuxer.writeSampleData(writeVideoTrackIndex, byteBuffer, videoBufferInfo);
                videoExtractor.advance();
            }

            while (true) {
                int readAudioSampleSize = audioExtractor.readSampleData(byteBuffer, 0);
                if (readAudioSampleSize < 0) {
                    break;
                }

                audioBufferInfo.size = readAudioSampleSize;
                audioBufferInfo.presentationTimeUs += sampleTime;
                audioBufferInfo.offset = 0;
                audioBufferInfo.flags = videoExtractor.getSampleFlags();
                mediaMuxer.writeSampleData(writeAudioTrackIndex, byteBuffer, audioBufferInfo);
                audioExtractor.advance();
            }

            mediaMuxer.stop();
            mediaMuxer.release();
            videoExtractor.release();
            audioExtractor.release();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initView() {
        mExactor = (Button) findViewById(R.id.exactor);
        mMuxer = (Button) findViewById(R.id.muxer);
        mMuxerAudio = (Button) findViewById(R.id.muxer_audio);
        mCombineVideo = (Button) findViewById(R.id.combine_video);

        mExactor.setOnClickListener(this);
        mMuxer.setOnClickListener(this);
        mMuxerAudio.setOnClickListener(this);
        mCombineVideo.setOnClickListener(this);

        mMediaExtractor = new MediaExtractor();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.exactor:
                exactorMedia();
                break;
            case R.id.muxer:
                muxerMedia();
                break;
            case R.id.muxer_audio:
                muxerAudio();
                break;
            case R.id.combine_video:
                combineVideo();
                break;
        }
    }
}
