package com.escoladeltreball.cloudfile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DocumentsMain extends AppCompatActivity {
    private static final int PICK_DOC_REQUEST = 3;
    private static final String REFERENCE = "uploads/documents";

    private Button chooser;
    private Button upload;
    private EditText docName;
    private TextView documentView;
    private TextView show;
    private ProgressBar mProgressBar;
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
        documentView = findViewById(R.id.document_view);
        mProgressBar = findViewById(R.id.progress_bar);
        mStorageRef = FirebaseStorage.getInstance().getReference(REFERENCE);
        mDatabaseRef = FirebaseDatabase.getInstance().getReference(REFERENCE);

        chooser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDocChooser();
            }
        });

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadFile();
            }
        });

        show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDocumentsActivity();
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
            documentView.setText(docUri.getPath());

        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));

    }

    private void uploadFile() {
        if (docUri != null) {
            long yourmilliseconds = System.currentTimeMillis();
            SimpleDateFormat sdf = new SimpleDateFormat("dd-mm-yyyy - HH:mm:ss");
            Date resultdate = new Date(yourmilliseconds);

            StorageReference fileReference = mStorageRef.child(sdf.format(resultdate) + " - " + docName.getText().toString().trim() + "." + getFileExtension(docUri));

            fileReference.putFile(docUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mProgressBar.setProgress(0);
                        }
                    }, 500);

                    taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Upload upload = new Upload(docName.getText().toString().trim(),
                                    uri.toString());
                            String uploadId = mDatabaseRef.push().getKey();
                            mDatabaseRef.child(uploadId).setValue(upload);
                        }
                    });

                    Toast.makeText(DocumentsMain.this, "Upload Successful", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(DocumentsMain.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                    mProgressBar.setProgress((int) progress);
                }
            });
        } else {
            Toast.makeText(this, "no file selected", Toast.LENGTH_SHORT).show();
        }
    }

    public void openDocumentsActivity() {
        Intent intent = new Intent(this, DocumentsActivity.class);
        startActivity(intent);
    }

}
