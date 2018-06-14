package com.kkopite.videoaudiostudy.codec;

import android.media.MediaExtractor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;

import com.kkopite.videoaudiostudy.R;

import java.io.IOException;

public class CodecActivity extends AppCompatActivity implements View.OnClickListener {

    private TextureView mPlaybackView;
    private Button mBtnPlay;

    private MediaExtractor mExtractor = new MediaExtractor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_codec);
        initView();
    }

    private void initView() {
        mPlaybackView = (TextureView) findViewById(R.id.playback_view);
        mBtnPlay = (Button) findViewById(R.id.btn_play);

        mBtnPlay.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_play:

                break;
        }
    }

    public void startPlay () {

        Uri videoUri = Uri.parse("android.resource://"
            + getPackageName() + "/"
            + R.raw.vid_bigbuckbunny);

        // 拿出视频流

        try {
            mExtractor.setDataSource(this, videoUri, null);
            int trackCount = mExtractor.getTrackCount();

            for (int i = 0; i < trackCount; i++) {
                mExtractor.unselectTrack(i);
            }

            for (int i = 0; i < trackCount; i++) {
                // 选择视频流

            }


        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
