package com.escoladeltreball.cloudfile;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class AudioMain extends AppCompatActivity {

    private static final int PICK_AUDIO_REQUEST = 2;

    private Button chooser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_main);
        this.setTitle(R.string.audio);
        chooser = findViewById(R.id.button_choose_audio);

        chooser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAudioChooser();
            }
        });

    }

    private void openAudioChooser() {
        Intent intent = new Intent();
        intent.setType("audio/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_AUDIO_REQUEST);
    }
}
