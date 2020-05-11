package com.escoladeltreball.cloudfile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
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

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AudioMain extends AppCompatActivity {
    private static final String TAG = "test";

    private static final int PICK_AUDIO_REQUEST = 2;

    private Button chooser;
    private Button upload;
    private Button grabar;
    private EditText audioName;
    private TextView txtInfo;

    private ProgressBar mProgressBar;

    private TextView show;

    private Uri audioUri;
    MediaRecorder recorder;
    MediaPlayer player;
    File audiofile = null;
    private static final int MY_PERMISSIONS_REQUESTS = 10;
    File sampleDir = Environment.getExternalStorageDirectory();
    File soundDir = new File(sampleDir, "Sons_Grabacio");
    List<String> llista = new ArrayList<String>();
    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_main);
        this.setTitle(R.string.audio);

        chooser = findViewById(R.id.button_choose_audio);
        upload = findViewById(R.id.upload_audio);
        audioName = findViewById(R.id.audio_file_name);
        grabar = findViewById(R.id.grabar);
        show = findViewById(R.id.show_audio_files);
        txtInfo = findViewById(R.id.info_audio);
        mProgressBar = findViewById(R.id.progress_bar);
        mStorageRef = FirebaseStorage.getInstance().getReference("uploads/audio");
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("uploads/audio");

        chooser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAudioChooser();
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
                openAudioActivity();
            }
        });

        grabar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startRecord(v);
                        break;
                    case MotionEvent.ACTION_UP:
                        stopRecord(v);
                        break;
                }
                return false;
            }
        });

        player = new MediaPlayer();

        txtInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String url = txtInfo.getText().toString();
                    if (!url.isEmpty() && !url.equals(" ")) {
                        if (player.isPlaying()) {
                            player.release();
                        }
                        player = new MediaPlayer();
                        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                        player.setDataSource(url);
                        player.prepare();
                        player.start();
                        Toast.makeText(AudioMain.this, "Reproduciendo", Toast.LENGTH_SHORT).show();
                    } else
                        Toast.makeText(AudioMain.this, "Archivo no definido ", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private void startRecord(View v) {
        String estat = Environment.getExternalStorageState();
        // comprova si hi ha SD i si puc escriure en ella
        if (estat.equals(Environment.MEDIA_MOUNTED)) {
            txtInfo.setText("");

            Log.d(TAG, "media mounted" + ", " + sampleDir);

            int permCheck1 = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
            int permCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (!(permCheck == PackageManager.PERMISSION_GRANTED) |
                    !(permCheck1 == PackageManager.PERMISSION_GRANTED)) {

                //ara cal demanar permissos...
                if ((ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) |
                        (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)))) {

                    Toast.makeText(this, "Per seguretat, està deshabilitada la SD i el microfon, habiliti'ls ", Toast.LENGTH_LONG).show();

                    txtInfo.setText("Per seguretat, està deshabilitada la SD i el microfon, habiliti'ls els dos");

                    ActivityCompat.requestPermissions
                            (this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                    Manifest.permission.RECORD_AUDIO}, MY_PERMISSIONS_REQUESTS);

                } else {
                    Toast.makeText(this, "demana permis, no rationale ", Toast.LENGTH_LONG).show();
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                    Manifest.permission.RECORD_AUDIO}, MY_PERMISSIONS_REQUESTS);
                    // The callback method gets the result of the request.
                    Log.d(TAG, "startRecording: no rationale");
                }

            } else {
                Log.d(TAG, "entra, té permissos:" + ", " + permCheck);
                try {
                    if (!soundDir.exists()) {
                        soundDir.mkdirs();
                    }
                    audiofile = File.createTempFile("sound", ".3gp", soundDir);
                    recorder = new MediaRecorder();
                    recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                    recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                    recorder.setOutputFile(audiofile.getAbsolutePath());
                    Log.d(TAG, "startRecording: " + audiofile.getAbsolutePath());
                    recorder.prepare();
                    recorder.start();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                    Log.d(TAG, "startRecording1: " + e.getMessage() + e.getCause());

                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(TAG, "sd card error: " + sampleDir + e.getMessage() + e.getCause());

                } catch (Exception e) {
                    Log.d(TAG, "startRecording3: " + e.getMessage() + e.getCause());
                    Toast.makeText(this, "Exception: missatge: " + e.getMessage() + ", causa: " + e.getCause() + ", " +
                            audiofile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                }

            }
        }

        Toast.makeText(this, "Start Recording", Toast.LENGTH_SHORT).show();
    }


    private void stopRecord(View v) {
        try {
            recorder.stop();
            recorder.release();
            txtInfo.setText(audiofile.getAbsolutePath());
            addRecordingToMediaLibrary();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            Log.d(TAG, "stopRecording: " + e.getMessage() + e.getCause());
            Toast.makeText(this, "IllegalStateException" + e.getMessage() + e.getCause(), Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(this, "Exception" + e.getMessage() + e.getCause(), Toast.LENGTH_SHORT).show();
        }

        Toast.makeText(this, "Stop Recording", Toast.LENGTH_SHORT).show();
    }

    protected void addRecordingToMediaLibrary() {
        try {
            ContentValues values = new ContentValues(4);
            long current = System.currentTimeMillis();
            values.put(MediaStore.Audio.Media.TITLE, "audio" + audiofile.getName());
            values.put(MediaStore.Audio.Media.DATE_ADDED, (int) (current / 1000));
            values.put(MediaStore.Audio.Media.MIME_TYPE, "audio/3gpp");
            values.put(MediaStore.Audio.Media.DATA, audiofile.getAbsolutePath());
            ContentResolver contentResolver = getContentResolver();

            Uri base = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            audioUri = contentResolver.insert(base, values);

            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, audioUri));
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "addRecordingToMediaLibrary: " + e.getCause() + ", " + e.getMessage() + ", ");
            Toast.makeText(this, e.getMessage() + e.getCause(), Toast.LENGTH_SHORT).show();
        }
    }


    private void openAudioChooser() {
        Intent intent = new Intent();
        intent.setType("audio/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_AUDIO_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_AUDIO_REQUEST && resultCode == RESULT_OK && data != null
                && data.getData() != null) {
            audioUri = data.getData();
            txtInfo.setText(audioUri.getPath());
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }


    private void uploadFile() {
        if (audioUri != null) {
            long yourmilliseconds = System.currentTimeMillis();
            SimpleDateFormat sdf = new SimpleDateFormat("dd-mm-yyyy - HH:mm:ss");
            Date resultdate = new Date(yourmilliseconds);

            StorageReference fileReference = mStorageRef.child(sdf.format(resultdate) + " - " + audioName.getText().toString().trim() + "." + getFileExtension(audioUri));

            fileReference.putFile(audioUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
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
                            AudioUpload upload = new AudioUpload(audioName.getText().toString().trim(),
                                    uri.toString());
                            String uploadId = mDatabaseRef.push().getKey();
                            mDatabaseRef.child(uploadId).setValue(upload);
                        }
                    });

                    Toast.makeText(AudioMain.this, "Upload Successful", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(AudioMain.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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


    private void openAudioActivity() {
        Intent intent = new Intent(this, AudioActivity.class);
        startActivity(intent);
    }

}
