package com.kkopite.videoaudiostudy.c2;

/**
 * Created by kkopite on 2018/6/8.
 */
public class WavFileHeader {

    public static final int WAV_FILE_HEADER_SIZE = 44;
    // chunksize 是不包含前八个字符的
    public static final int WAV_CHUNKSIZE_EXCLUDE_DATA = 36;

    // chunksize 在 4 - 8 之间
    public static final int WAV_CHUNKSIZE_OFFSET = 4;

    // Subchunk1Size(不包含 Subchunk1ID, Subchunk1Size, 所以是常数 16) 在 第 16 - 20
    public static final int WAV_SUB_CHUNKSIZE1_OFFSET = 16;
    // Subchunk2Size(不包含 Subchunk2ID, Subchunk2Size, 所以是PCM音频的字符数) 在 第 40 - 44
    public static final int WAV_SUB_CHUNKSIZE2_OFFSET = 40;

    public String mChunkID = "RIFF";
    public int mChunkSize = 0;
    public String mFormat = "WAVE";

    // 4个字符 有个空格
    public String mSubChunk1ID = "fmt ";
    public int mSubChunk1Size = 16;
    public short mAudioFormat = 1;
    public short mNumChannel = 1;
    public int mSampleRate = 8000;
    public int mByteRate = 0;
    public short mBlockAlign = 0;
    public short mBitsPerSample = 8;

    public String mSubChunk2ID = "data";
    public int mSubChunk2Size = 0;

    public WavFileHeader(int sampleRateInHz, int bitsPerSample, int channels) {
        mSampleRate = sampleRateInHz;
        mBitsPerSample = (short) bitsPerSample;
        mNumChannel = (short) channels;
        mByteRate = mSampleRate * mBitsPerSample * mNumChannel / 8;
        mBlockAlign = (short) (mNumChannel * mBitsPerSample / 8);
    }

    public WavFileHeader(){

    }






}
