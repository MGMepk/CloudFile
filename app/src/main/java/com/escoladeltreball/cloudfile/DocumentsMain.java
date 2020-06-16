package com.escoladeltreball.cloudfile;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DocumentsMain extends AppCompatActivity {
    private FirebaseUser user;
    FirebaseAuth fAuth;
    private static final int PICK_DOC_REQUEST = 3;
    private String reference = "";
    private static final int MY_PERMISSIONS_REQUESTS = 10;
    private EditText docName;
    private TextView documentView;
    private ProgressBar mProgressBar;
    private Uri docUri;
    String TAG = "reader";

    Button upload;
    ImageButton editor;
    ImageButton chooser;
    ImageButton newDoc;
    TextView tagName;

    EditText dContent;
    Button save;
    Button cancel;


    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_documents_main);
        this.setTitle(R.string.docs);

        fAuth = FirebaseAuth.getInstance();
        user = fAuth.getCurrentUser();
        assert user != null;
        reference = user.getUid() + "/";

        chooser = findViewById(R.id.button_choose_doc);
        upload = findViewById(R.id.upload_doc);
        editor = findViewById(R.id.edit_doc);
        tagName = findViewById(R.id.doc_name);
        save = findViewById(R.id.button_save);
        cancel = findViewById(R.id.button_cancel);
        newDoc = findViewById(R.id.new_doc);
        docName = findViewById(R.id.document_name);
        dContent = findViewById(R.id.document_Edit);
        documentView = findViewById(R.id.document_view);
        mProgressBar = findViewById(R.id.progress_bar);
        mStorageRef = FirebaseStorage.getInstance().getReference(reference + "documents");
        mDatabaseRef = FirebaseDatabase.getInstance().getReference(reference + "documents");

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

        editor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openEditorMode();
            }
        });

        newDoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editMode();
                Toast.makeText(DocumentsMain.this, R.string.new_doc, Toast.LENGTH_SHORT).show();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveDocument();
                String saving = getString(R.string.saving);
                Toast.makeText(DocumentsMain.this, saving + ": " + docName.getText().toString(), Toast.LENGTH_SHORT).show();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadMode();
                if (!dContent.getText().toString().isEmpty()) {
                    dContent.setText("");
                }
                Toast.makeText(DocumentsMain.this, R.string.cancelled, Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PICK_DOC_REQUEST:{
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, do your work....
                    openDocChooser();
                } else {
                    // permission denied
                    // Disable the functionality that depends on this permission.
                    Toast.makeText(this, R.string.permission, Toast.LENGTH_SHORT).show();
                }
                return;
            }
            case MY_PERMISSIONS_REQUESTS:{
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, do your work....
                    openEditorMode();
                } else {
                    // permission denied
                    // Disable the functionality that depends on this permission.
                    Toast.makeText(this, R.string.permission, Toast.LENGTH_SHORT).show();
                }
            }

        }
        }

    private void openDocChooser() {
        String status = Environment.getExternalStorageState();
        if (status.equals(Environment.MEDIA_MOUNTED)) {
            int permCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            if (!(permCheck == PackageManager.PERMISSION_GRANTED)) {
                //Call for permission
                if ((ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE))) {
                    ActivityCompat.requestPermissions
                            (this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PICK_DOC_REQUEST);
                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PICK_DOC_REQUEST);
                }

            } else {

                Intent intent = new Intent();
                intent.setType("*/*");
                String[] mimeTypes = {"text/*", "application/pdf"};
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, PICK_DOC_REQUEST);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_DOC_REQUEST && resultCode == RESULT_OK && data != null
                && data.getData() != null) {
            docUri = data.getData();
            if (docUri != null) {
                if (docUri.getPath().contains("primary")) {
                    String path = Environment.getExternalStorageDirectory().getAbsolutePath();
                    String[] audioPath = docUri.getPath().split(":");
                    path += "/" + audioPath[1];
                    documentView.setText(path);
                } else {
                    documentView.setText(docUri.getPath());
                }
            }
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));

    }

    private void uploadFile() {
        if (docUri != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-mm-yyyy - HH:mm:ss", java.util.Locale.getDefault());
            Date resultDate = new Date(System.currentTimeMillis());

            StorageReference fileReference = mStorageRef.child(sdf.format(resultDate) + " - " + docName.getText().toString().trim() + "." + getFileExtension(docUri));

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
                            assert uploadId != null;
                            mDatabaseRef.child(uploadId).setValue(upload);
                        }
                    });
                    Toast.makeText(DocumentsMain.this, R.string.upload_success, Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, R.string.no_file, Toast.LENGTH_SHORT).show();
        }
    }

    public void openDocumentsActivity() {
        Intent intent = new Intent(this, DocumentsActivity.class);
        startActivity(intent);
    }

    public void openEditorMode() {
        String status = Environment.getExternalStorageState();
        if (status.equals(Environment.MEDIA_MOUNTED)) {
            int permCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (!(permCheck == PackageManager.PERMISSION_GRANTED)) {
                //Call for permission
                if ((ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE))) {
                    Toast.makeText(this, R.string.request_permissions, Toast.LENGTH_LONG).show();
                    ActivityCompat.requestPermissions
                            (this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUESTS);

                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUESTS);
                }

            } else {
                if (docUri != null) {
                    if (getFileExtension(docUri).equals("txt")) {
                        editMode();
                        StringBuilder stringBuilder = new StringBuilder();
                        String line;
                        FileReader reader = null;
                        try {
                            reader = new FileReader(new File(documentView.getText().toString()));
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        BufferedReader in = null;
                        try {
                            assert reader != null;
                            in = new BufferedReader(reader);
                            while ((line = in.readLine()) != null) stringBuilder.append(line);
                            in.close();
                            reader.close();
                        } catch (IOException e) {
                            Log.d(TAG, e.getMessage() + " " + e.getCause());
                        }

                        dContent.setText(stringBuilder.toString());
                    } else {
                        Toast.makeText(this, R.string.non_editable, Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(this, R.string.no_file, Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.docs_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {

            case R.id.documents_uploads:
                openDocumentsActivity();
                return true;
            case R.id.multimedia:
                startActivity(new Intent(this, MultimediaMain.class));
                return true;
            case R.id.log_out:
                logout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplicationContext(), Login.class));
        finish();
    }

    private void editMode() {
        upload.setVisibility(Button.INVISIBLE);
        documentView.setVisibility(TextView.INVISIBLE);
        editor.setVisibility(View.INVISIBLE);
        chooser.setVisibility(View.INVISIBLE);
        mProgressBar.setVisibility(View.INVISIBLE);
        newDoc.setVisibility(View.INVISIBLE);

        save.setVisibility(Button.VISIBLE);
        dContent.setVisibility(EditText.VISIBLE);
        tagName.setVisibility(View.VISIBLE);
        cancel.setVisibility(View.VISIBLE);
    }

    private void uploadMode() {
        upload.setVisibility(Button.VISIBLE);
        documentView.setVisibility(TextView.VISIBLE);
        editor.setVisibility(View.VISIBLE);
        chooser.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.VISIBLE);
        newDoc.setVisibility(View.VISIBLE);

        save.setVisibility(Button.INVISIBLE);
        dContent.setVisibility(EditText.INVISIBLE);
        tagName.setVisibility(View.INVISIBLE);
        cancel.setVisibility(View.INVISIBLE);
    }


    private void saveDocument() {
        String name = "unnamed";
        try {
            if (!docName.getText().toString().isEmpty()) {
                name = docName.getText().toString();
            }
            File file = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), name + ".txt");
            file.createNewFile();
            FileWriter myWriter = new FileWriter(file);
            myWriter.write(dContent.getText().toString());
            myWriter.close();
            uploadMode();
            docUri = FileProvider.getUriForFile(this, "com.example.android.fileprovider", file);
            documentView.setText(docUri.getPath());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
