package com.escoladeltreball.cloudfile;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

public class MainActivity extends AppCompatActivity {
    private ImageButton iButtonImage, iButtonAudio, iButtonVideo, iButtonDoc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        iButtonImage = findViewById(R.id.button_images);
        iButtonAudio = findViewById(R.id.button_audio);
        iButtonVideo = findViewById(R.id.button_video);
        iButtonDoc = findViewById(R.id.button_documents);


        iButtonImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // intent imageActivity
            }
        });


        iButtonAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAudioMain();

            }
        });

        iButtonVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // intent videoActivity
            }
        });

        iButtonDoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDocumentsMain();
            }
        });

    }

    private void openAudioMain() {
        Intent intent = new Intent(this, AudioMain.class);
        startActivity(intent);
    }

    private void openDocumentsMain() {
        Intent intent = new Intent(this, DocumentsMain.class);
        startActivity(intent);
    }

}
