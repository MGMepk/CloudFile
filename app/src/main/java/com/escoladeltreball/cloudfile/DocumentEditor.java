package com.escoladeltreball.cloudfile;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Logger;

public class DocumentEditor extends AppCompatActivity {
    String TAG = "reader";
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
                assert extras != null;
                dName.setText(String.valueOf(extras.get("name")));

                StringBuilder stringBuilder = new StringBuilder();
                String line;
                FileReader reader = new FileReader(new File(String.valueOf(extras.get("uri"))));
                Toast.makeText(this, "hola: " +String.valueOf(extras.get("uri")), Toast.LENGTH_SHORT).show();
                BufferedReader in = null;
                try {

                    in = new BufferedReader(reader);
                    while ((line = in.readLine()) != null) stringBuilder.append(line);
                    in.close();
                    reader.close();
                } catch (FileNotFoundException e) {
                    Log.d(TAG, e.getMessage() + " " + e.getCause());
                } catch (IOException e) {
                    Log.d(TAG, e.getMessage() + " " + e.getCause());
                }

                dContent.setText(stringBuilder.toString());

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveDocument();
                String saving = getString(R.string.saving);
                Toast.makeText(DocumentEditor.this, saving + ": " + dName.getText().toString(), Toast.LENGTH_SHORT).show();
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
