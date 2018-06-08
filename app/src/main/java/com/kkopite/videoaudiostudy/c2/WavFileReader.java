package com.kkopite.videoaudiostudy.c2;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by kkopite on 2018/6/8.
 */
public class WavFileReader {

    private static final String TAG = "WavFileReader";

    private DataInputStream mDataInputStream;
    private WavFileHeader mWavFileHeader;

    public boolean openFile(String filePath) throws IOException {
        if (mDataInputStream != null) {
            closeFile();
        }
        mDataInputStream = new DataInputStream(new FileInputStream(filePath));
        return readHeader();
    }

    public WavFileHeader getmWavFileHeader() {
        return mWavFileHeader;
    }

    public int readData(byte[] buffer, int offset, int count) {
        if (mDataInputStream == null || mWavFileHeader == null) {
            return -1;
        }

        try {
            int nbytes = mDataInputStream.read(buffer, offset, count);
            if (nbytes == -1) {
                return 0;
            }
            return nbytes;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return -1;
    }

    private boolean readHeader() {
        if (mDataInputStream == null) {
            return false;
        }

        WavFileHeader header = new WavFileHeader();

        byte[] intValue = new byte[4];
        byte[] shortValue = new byte[2];

        try {
            // chunkID
            header.mChunkID = "" + (char) mDataInputStream.readByte() + (char) mDataInputStream.readByte() + (char) mDataInputStream.readByte() + (char) mDataInputStream.readByte();

            // chunkSize
            mDataInputStream.read(intValue);
            header.mChunkSize = byteArrayToInt(intValue);

            // format
            header.mFormat = "" + "" + (char) mDataInputStream.readByte() + (char) mDataInputStream.readByte() + (char) mDataInputStream.readByte() + (char) mDataInputStream.readByte();

            // subchunk1ID
            header.mSubChunk1ID = "" + "" + (char) mDataInputStream.readByte() + (char) mDataInputStream.readByte() + (char) mDataInputStream.readByte() + (char) mDataInputStream.readByte();

            // subChunk1Size
            mDataInputStream.read(intValue);
            header.mSubChunk1Size = byteArrayToInt(intValue);

            // audioFormat
            mDataInputStream.read(shortValue);
            header.mAudioFormat = byteArrayToShort(shortValue);

            // channel
            mDataInputStream.read(shortValue);
            header.mNumChannel = byteArrayToShort(shortValue);

            // sampleRate
            mDataInputStream.read(intValue);
            header.mSampleRate = byteArrayToInt(intValue);

            // byteRate
            mDataInputStream.read(intValue);
            header.mByteRate = byteArrayToInt(intValue);

            // blockAlign
            mDataInputStream.read(shortValue);
            header.mBlockAlign = byteArrayToShort(shortValue);

            // bitsPerSample
            mDataInputStream.read(shortValue);
            header.mBitsPerSample = byteArrayToShort(shortValue);

            // subchunk2ID
            header.mSubChunk2ID = "" + (char) mDataInputStream.readByte() + (char) mDataInputStream.readByte() + (char) mDataInputStream.readByte() + (char) mDataInputStream.readByte();

            // subchunk2Size
            mDataInputStream.read(intValue);
            header.mSubChunk2Size = byteArrayToInt(intValue);

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        mWavFileHeader = header;
        return true;


    }

    public void closeFile() throws IOException {
        if (mDataInputStream != null) {
            mDataInputStream.close();
            mDataInputStream = null;
        }
    }

    private static short byteArrayToShort(byte[] b) {
        return ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getShort();
    }

    private static int byteArrayToInt(byte[] b) {
        return ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

}
