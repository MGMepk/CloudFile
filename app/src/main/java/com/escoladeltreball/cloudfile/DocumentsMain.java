package com.escoladeltreball.cloudfile;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class DocumentsMain extends AppCompatActivity {
    private static final int PICK_DOC_REQUEST = 3;

    private Button chooser;
    private Button upload;
    private EditText docName;
    private TextView show;

    private Uri docUri;

    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_documents_main);
        this.setTitle(R.string.docs);

        chooser = findViewById(R.id.button_choose_doc);
        upload = findViewById(R.id.upload_doc);
        docName = findViewById(R.id.document_name);
        show = findViewById(R.id.show_doc_files);

        mStorageRef = FirebaseStorage.getInstance().getReference("uploads/documents");
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("uploads/documents");

        chooser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDocChooser();
            }
        });

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }


    private void openDocChooser() {
        Intent intent = new Intent();
        intent.setType("text/* application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_DOC_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_DOC_REQUEST && resultCode == RESULT_OK && data != null
                && data.getData() != null) {
            docUri = data.getData();
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));

    }

}
