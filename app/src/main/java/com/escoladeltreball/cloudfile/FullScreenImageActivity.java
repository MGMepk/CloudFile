package com.escoladeltreball.cloudfile;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

public class FullScreenImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);

        Uri Uri = null;
        Uri fileUri = null;

        ImageView fullScreenImageView = findViewById(R.id.fullScreenImageView);
        VideoView reproVideo = findViewById(R.id.videoRepro);

        Bundle extras = getIntent().getExtras();

        if (extras != null && extras.containsKey("image")) {

            fullScreenImageView.setVisibility(ImageView.VISIBLE);
            reproVideo.setVisibility(VideoView.INVISIBLE);
            Uri = Uri.parse(extras.getString("image"));

            Picasso.with(this)
                    .load(Uri)
                    .into(fullScreenImageView);
        }

        if (extras != null && extras.containsKey("video")) {

            reproVideo.setVisibility(VideoView.VISIBLE);
            fullScreenImageView.setVisibility(ImageView.INVISIBLE);

            Uri = Uri.parse(extras.getString("video"));
            fileUri = getIntent().getData();


            reproVideo.setVideoURI(fileUri);
            MediaController mediaController = new MediaController(this);
            mediaController.setAnchorView(reproVideo);
            reproVideo.setMediaController(mediaController);
            reproVideo.start();

        }


    }


}
