package com.kkopite.videoaudiostudy.codec;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Handler;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by kkopite on 2018/6/14.
 */
public class MediaCodecWrapper {

    private Handler mHandler;

    public interface OutputFormatChangedListener {
        void outputFormatChanged(MediaCodecWrapper sender, MediaFormat newFormat);
    }

    private OutputFormatChangedListener mOutputFormatChangedListener;

    public interface OutputSampleListener {
        void outputSample(MediaCodecWrapper sender, MediaCodec.BufferInfo info, ByteBuffer buffer);
    }

    private MediaCodec mDecoder;

    public MediaCodecWrapper(MediaCodec decoder) {
        mDecoder = decoder;
        decoder.start();
    }

    public void stopAndRelease() {
        mDecoder.stop();
        mDecoder.release();
        mDecoder = null;
        mHandler = null;
    }

    private static MediaCodec.CryptoInfo sCryptoInfo = new MediaCodec.CryptoInfo();

    public static MediaCodecWrapper fromVideoFormat(MediaFormat trackFormat, Surface surface) throws IOException {
        MediaCodecWrapper result = null;
        MediaCodec videoCodec = null;

        String mimeType = trackFormat.getString(MediaFormat.KEY_MIME);

        if (mimeType.contains("video/")) {
            videoCodec = MediaCodec.createDecoderByType(mimeType);
            videoCodec.configure(trackFormat, surface, null, 0);
        }

        if (videoCodec != null) {
            result = new MediaCodecWrapper(videoCodec);
        }

        return result;
    }

    public boolean writeSample(MediaExtractor extractor,
                               boolean isSecure,
                               long presentationTimeUs,
                               int flag) {
        boolean result = false;

        int inputBufferIndex = mDecoder.dequeueInputBuffer(1000);
        if (inputBufferIndex >= 0) {
            ByteBuffer inputBuffer = mDecoder.getInputBuffer(inputBufferIndex);
            inputBuffer.clear();
            int size = extractor.readSampleData(inputBuffer, 0);
            if (size <= 0) {
                flag |= MediaCodec.BUFFER_FLAG_END_OF_STREAM;
            }

            if (!isSecure) {
                mDecoder.queueInputBuffer(inputBufferIndex, 0, size, presentationTimeUs, flag);
            } else {
                extractor.getSampleCryptoInfo(sCryptoInfo);
                mDecoder.queueSecureInputBuffer(inputBufferIndex, 0, sCryptoInfo, presentationTimeUs, flag);
            }

            result = true;
        }

        return result;

    }


}
