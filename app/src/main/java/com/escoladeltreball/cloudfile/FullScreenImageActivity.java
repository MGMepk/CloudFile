package com.escoladeltreball.cloudfile;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class FullScreenImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);

        Uri Uri = null;
        ImageView fullScreenImageView = (ImageView) findViewById(R.id.fullScreenImageView);

        Bundle extras = getIntent().getExtras();

        if (extras != null && extras.containsKey("image")){
            Uri = Uri.parse(extras.getString("image"));

            Picasso.with(this)
                    .load(Uri)
                    .into(fullScreenImageView);
        }
    }
}
