package com.escoladeltreball.cloudfile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class DocumentEditor extends AppCompatActivity {

    EditText dName;
    EditText dContent;
    Button save;
    File sampleDir = Environment.getExternalStorageDirectory();
    File appDir = new File(sampleDir, "CloudFile");
    File docsDir = new File(appDir, "Docs");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document_editor);
        dName = findViewById(R.id.document_name);
        dContent = findViewById(R.id.document_Edit);
        save = findViewById(R.id.button_save);
        try {
            Intent intent = getIntent();
            if (intent != null) {
                Bundle extras = intent.getExtras();
                dName.setText(String.valueOf(extras.get("name")));
                FileReader reader = new FileReader(String.valueOf(extras.get("uri")));
                dContent.setText(reader.read());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveDocument();
                Toast.makeText(DocumentEditor.this, "Guardando: " + dName.getText().toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveDocument() {
        String name = "unnamed";
        try {
            if (!docsDir.exists()) {
                docsDir.mkdir();
            }
            if (!dName.getText().toString().isEmpty()) {
                name = dName.getText().toString();
            }

            File file = new File(docsDir, name + ".txt");
            file.createNewFile();

            FileWriter myWriter = new FileWriter(file);
            myWriter.write(dContent.getText().toString());
            myWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
