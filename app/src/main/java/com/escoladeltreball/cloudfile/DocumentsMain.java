package com.escoladeltreball.cloudfile;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class DocumentsMain extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_documents_main);
        this.setTitle(R.string.docs);
    }
}
