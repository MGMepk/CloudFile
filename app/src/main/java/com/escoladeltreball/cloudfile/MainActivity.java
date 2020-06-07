package com.escoladeltreball.cloudfile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Button multimedia = findViewById(R.id.button_multimedia);
        Button doc = findViewById(R.id.button_documents);


        multimedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMultimediaMain();
            }
        });


        doc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDocumentsMain();
            }
        });

    }

    private void openMultimediaMain() {
        Intent intent = new Intent(this, MultimediaMain.class);
        startActivity(intent);
    }

    private void openDocumentsMain() {
        Intent intent = new Intent(this, Login.class);
        startActivity(intent);
    }

}
