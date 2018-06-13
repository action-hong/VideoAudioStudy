package com.kkopite.videoaudiostudy.mediacocdr;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.kkopite.videoaudiostudy.R;

public class MediaActivity extends AppCompatActivity implements View.OnClickListener {

    private Button mBtnAudio;
    private Button mBtnVideo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media);
        initView();
    }

    private void initView() {
        mBtnAudio = (Button) findViewById(R.id.btn_audio);
        mBtnVideo = (Button) findViewById(R.id.btn_video);

        mBtnAudio.setOnClickListener(this);
        mBtnVideo.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_audio:

                break;
            case R.id.btn_video:

                break;
        }
    }
}
