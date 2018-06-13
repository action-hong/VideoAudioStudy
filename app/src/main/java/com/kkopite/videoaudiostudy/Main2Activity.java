package com.kkopite.videoaudiostudy;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.kkopite.videoaudiostudy.c1.C1Activity;
import com.kkopite.videoaudiostudy.c2.AudioActivity;
import com.kkopite.videoaudiostudy.extractor.ExtractorActivity;
import com.kkopite.videoaudiostudy.openGL.OpenGLActivity;
import com.kkopite.videoaudiostudy.video.PreviewActivity;

import java.util.Map;
import java.util.TreeMap;

public class Main2Activity extends AppCompatActivity {

    private final TreeMap<String, Class<? extends Activity>> buttons = new TreeMap<String, Class<? extends Activity>>() {{
        put("c1", C1Activity.class);
        put("c2", AudioActivity.class);
        put("video", PreviewActivity.class);
        put("音频的分离与合成", ExtractorActivity.class);
        put("openGL", OpenGLActivity.class);
    }};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        initView();

    }

    private void initView() {
        ViewGroup container = (ViewGroup) findViewById(R.id.list);
        for (final Map.Entry<String, Class<? extends Activity>> entry : buttons.entrySet()) {
            Button button = new Button(this);
            button.setText(entry.getKey());
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(Main2Activity.this, entry.getValue()));
                }
            });
            container.addView(button);
        }
    }
}
