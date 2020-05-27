package com.escoladeltreball.cloudfile;

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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MultimediaMain extends AppCompatActivity {
    private static final String TAG = "test";
    private static final String REFERENCE = "uploads/";
    private static final String AUDIO = REFERENCE + "audio";
    private static final String VIDEO = REFERENCE + "videos";
    private static final String IMAGES = REFERENCE + "images";
    private static final int PICK_AUDIO_REQUEST = 3;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PICK_VIDEO_REQUEST = 2;


    private EditText fileName;
    private TextView txtInfo;

    private VideoView mVideoView;
    private ImageView mImageView;

    private ProgressBar mProgressBar;

    private Uri fileUri;
    MediaRecorder recorder;
    MediaPlayer player;
    File audioFile = null;
    private static final int MY_PERMISSIONS_REQUESTS = 10;
    File sampleDir = Environment.getExternalStorageDirectory();
    File appDir = new File(sampleDir, "CloudFile");
    File soundDir = new File(appDir, "Records");
    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multimedia);
        this.setTitle(R.string.multimedia);

        //choosers
        ImageButton chooserAudio = findViewById(R.id.button_choose_audio);
        ImageButton chooserVideo = findViewById(R.id.button_choose_video);
        ImageButton chooserImage = findViewById(R.id.button_choose_image);

        // upload button
        Button upload = findViewById(R.id.upload_audio);
        fileName = findViewById(R.id.audio_file_name);

        //Recorders
        ImageButton record = findViewById(R.id.record);
        ImageButton videoRecord = findViewById(R.id.video_record);
        ImageButton takePhoto = findViewById(R.id.photo_shot);


        mVideoView = findViewById(R.id.video_view);
        mImageView = findViewById(R.id.image_view);
        txtInfo = findViewById(R.id.info_audio);

        mProgressBar = findViewById(R.id.progress_bar);
        mStorageRef = FirebaseStorage.getInstance().getReference(REFERENCE);
        mDatabaseRef = FirebaseDatabase.getInstance().getReference(REFERENCE);


        chooserImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageChooser();
            }
        });

        chooserAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAudioChooser();
            }
        });

        chooserVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openVideoChooser();
            }
        });

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadFile();
            }
        });


        record.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startRecord();
                        break;
                    case MotionEvent.ACTION_UP:
                        stopRecord();
                        break;
                    default:
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

                        Toast.makeText(MultimediaMain.this, R.string.playing, Toast.LENGTH_SHORT).show();
                    } else
                        Toast.makeText(MultimediaMain.this, R.string.undefined, Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private void startRecord() {
        String estat = Environment.getExternalStorageState();
        // comprova si hi ha SD i si puc escriure en ella
        if (estat.equals(Environment.MEDIA_MOUNTED)) {
            txtInfo.setText("");
            int permCheck1 = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
            int permCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (!(permCheck == PackageManager.PERMISSION_GRANTED) |
                    !(permCheck1 == PackageManager.PERMISSION_GRANTED)) {

                //ara cal demanar permissos...
                if ((ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) |
                        (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)))) {

                    Toast.makeText(this, R.string.request_permissions, Toast.LENGTH_LONG).show();

                    txtInfo.setText(R.string.request_permissions);

                    ActivityCompat.requestPermissions
                            (this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                    Manifest.permission.RECORD_AUDIO}, MY_PERMISSIONS_REQUESTS);

                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                    Manifest.permission.RECORD_AUDIO}, MY_PERMISSIONS_REQUESTS);
                }

            } else {
                try {
                    if (!soundDir.exists()) {
                        soundDir.mkdirs();
                    }
                    audioFile = File.createTempFile("sound", ".ogg", soundDir);
                    recorder = new MediaRecorder();
                    recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                    recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                    recorder.setOutputFile(audioFile.getAbsolutePath());
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
                            audioFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                }

            }
        }

        Toast.makeText(this, R.string.start, Toast.LENGTH_SHORT).show();
    }


    private void stopRecord() {
        try {
            recorder.stop();
            recorder.release();
            txtInfo.setVisibility(TextView.VISIBLE);
            txtInfo.setText(audioFile.getAbsolutePath());
            addRecordingToMediaLibrary();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            Log.d(TAG, "stopRecording: " + e.getMessage() + e.getCause());
            Toast.makeText(this, "IllegalStateException" + e.getMessage() + e.getCause(), Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(this, "Exception" + e.getMessage() + e.getCause(), Toast.LENGTH_SHORT).show();
        }
        Toast.makeText(this, R.string.stop, Toast.LENGTH_SHORT).show();
    }

    protected void addRecordingToMediaLibrary() {
        try {
            ContentValues values = new ContentValues(4);
            long current = System.currentTimeMillis();
            values.put(MediaStore.Audio.Media.TITLE, "audio" + audioFile.getName());
            values.put(MediaStore.Audio.Media.DATE_ADDED, (int) (current / 1000));
            values.put(MediaStore.Audio.Media.MIME_TYPE, "audio/ogg");
            values.put(MediaStore.Audio.Media.DATA, audioFile.getAbsolutePath());
            ContentResolver contentResolver = getContentResolver();

            Uri base = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            fileUri = contentResolver.insert(base, values);

            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, fileUri));
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
        mImageView.setVisibility(ImageView.INVISIBLE);
        mVideoView.setVisibility(VideoView.INVISIBLE);
        txtInfo.setVisibility(TextView.VISIBLE);
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
        mImageView.setVisibility(ImageView.VISIBLE);
        mVideoView.setVisibility(VideoView.INVISIBLE);
        txtInfo.setVisibility(TextView.INVISIBLE);
    }

    private void openVideoChooser() {
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_VIDEO_REQUEST);
        mVideoView.setVisibility(VideoView.VISIBLE);
        mImageView.setVisibility(ImageView.INVISIBLE);
        txtInfo.setVisibility(TextView.INVISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_AUDIO_REQUEST && resultCode == RESULT_OK && data != null
                && data.getData() != null) {
            fileUri = data.getData();
            txtInfo.setText(fileUri.getPath());
        }
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            fileUri = data.getData();

            Picasso.with(this).load(fileUri).into(mImageView);
        }
        if (requestCode == PICK_VIDEO_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            fileUri = data.getData();

            mVideoView.setVideoURI(fileUri);
            MediaController mediaController = new MediaController(this);
            mediaController.setAnchorView(mVideoView);
            mVideoView.setMediaController(mediaController);
            mVideoView.start();

            // mVideoView = (VideoView)findViewById(R.id.video_view);
            //mVideoView.setVideoURI(mImageUri);
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }


    private void uploadFile() {
        if (fileUri != null) {
            long yourmilliseconds = System.currentTimeMillis();
            SimpleDateFormat sdf = new SimpleDateFormat("dd-mm-yyyy - HH:mm:ss");
            Date resultdate = new Date(yourmilliseconds);

            String extension = getFileExtension(fileUri);

            ContentResolver cR = getContentResolver();
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            String mimeType = mime.getMimeTypeFromExtension(extension);
            String[] type = mimeType.split("/");

            if (type[0].equals("image")) {
                mStorageRef = FirebaseStorage.getInstance().getReference(IMAGES);
                mDatabaseRef = FirebaseDatabase.getInstance().getReference(IMAGES);

            }
            if (type[0].equals("video")) {
                mStorageRef = FirebaseStorage.getInstance().getReference(VIDEO);
                mDatabaseRef = FirebaseDatabase.getInstance().getReference(VIDEO);

            }

            if (type[0].equals("audio")) {
                mStorageRef = FirebaseStorage.getInstance().getReference(AUDIO);
                mDatabaseRef = FirebaseDatabase.getInstance().getReference(AUDIO);

            }


            StorageReference fileReference = mStorageRef.child(sdf.format(resultdate) + " - " + fileName.getText().toString().trim() + "." + getFileExtension(fileUri));

            fileReference.putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
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
                            Upload upload = new Upload(fileName.getText().toString().trim(),
                                    uri.toString());
                            String uploadId = mDatabaseRef.push().getKey();
                            assert uploadId != null;
                            mDatabaseRef.child(uploadId).setValue(upload);
                        }
                    });

                    Toast.makeText(MultimediaMain.this, R.string.upload_succes, Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MultimediaMain.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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


    private void openAudioActivity() {
        Intent intent = new Intent(this, AudioActivity.class);
        startActivity(intent);
    }

    private void openImagesActivity() {
        Intent intent = new Intent(this, ImagesActivity.class);
        startActivity(intent);
    }

    private void openVideosActivity() {
        Intent intent = new Intent(this, VideosActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.cloudfile_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.audio_uploads:
                openAudioActivity();
                return true;

            case R.id.Documents_uploads:
                startActivity(new Intent(this, DocumentsMain.class));
                return true;

            case R.id.image_uploads:
                openImagesActivity();
                return true;

            case R.id.video_uploads:
                openVideosActivity();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
