package com.escoladeltreball.cloudfile;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import androidx.core.content.FileProvider;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
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
    private static final int PICK_IMAGE_CAPTURE_REQUEST = 4;
    private static final int REQUEST_VIDEO_CAPTURE = 6;


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
    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;
    String currentPhotoPath;
    String currentVideoPath;
    File photoFile;
    File videoFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multimedia);
        this.setTitle(R.string.multimedia);

        //choosers
        ImageButton chooserAudio = findViewById(R.id.button_choose_audio);
        ImageButton chooserVideo = findViewById(R.id.button_choose_video);
        ImageButton chooserImage = findViewById(R.id.button_choose_image);
        Button logoutButton = findViewById(R.id.logoutButton);


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
        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makePhoto();
            }
        });

        videoRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeVideo();
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });


        record.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
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

    private void logout() {

        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplicationContext(), Login.class));
        finish();
    }

    private void makeVideo() {
        String status = Environment.getExternalStorageState();
        if (status.equals(Environment.MEDIA_MOUNTED)) {
            int permCheck1 = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
            int permCheck2 = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
            int permCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (!(permCheck == PackageManager.PERMISSION_GRANTED) |
                    !(permCheck1 == PackageManager.PERMISSION_GRANTED) |
                    !(permCheck2 == PackageManager.PERMISSION_GRANTED)) {

                //Call for permission
                if ((ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) |
                        (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) |
                                (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO))))) {

                    Toast.makeText(this, R.string.request_permissions, Toast.LENGTH_LONG).show();
                    ActivityCompat.requestPermissions
                            (this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                    Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUESTS);

                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                    Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUESTS);
                }

            } else {
                Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {

                    videoFile = null;
                    try {
                        videoFile = createVideoFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (videoFile != null) {
                        fileUri = FileProvider.getUriForFile(this, "com.example.android.fileprovider", videoFile);
                        takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                        Log.d(TAG, "takeVideo: " + fileUri + "\n" + currentPhotoPath);
                        startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
                    }

                }
                mImageView.setVisibility(ImageView.INVISIBLE);
                mVideoView.setVisibility(VideoView.VISIBLE);
                txtInfo.setVisibility(TextView.INVISIBLE);
            }
        }

    }

    private File createVideoFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(new Date());
        String videoFileName = "MP4_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_MOVIES);
        File video = File.createTempFile(
                videoFileName,
                ".mp4",
                storageDir
        );


        currentVideoPath = video.getAbsolutePath();
        return video;
    }

    private void startRecord() {
        String status = Environment.getExternalStorageState();
        if (status.equals(Environment.MEDIA_MOUNTED)) {
            int permCheck1 = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
            int permCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (!(permCheck == PackageManager.PERMISSION_GRANTED) |
                    !(permCheck1 == PackageManager.PERMISSION_GRANTED)) {

                //Call for permission
                if ((ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) |
                        (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)))) {

                    Toast.makeText(this, R.string.request_permissions, Toast.LENGTH_LONG).show();
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
                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(new Date());
                    String audioFileName = "Sound_" + timeStamp + "_";
                    audioFile = File.createTempFile(audioFileName, ".ogg", getExternalFilesDir(Environment.DIRECTORY_MUSIC));
                    recorder = new MediaRecorder();
                    recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                    recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                    recorder.setOutputFile(audioFile.getAbsolutePath());
                    recorder.prepare();
                    recorder.start();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                    Log.d(TAG, "startRecording: " + e.getMessage() + e.getCause());

                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(TAG, "sd card error: " + e.getMessage() + e.getCause());

                } catch (Exception e) {
                    Log.d(TAG, "startRecording: " + e.getMessage() + e.getCause());
                    Toast.makeText(this, "Exception: message: " + e.getMessage() + ", cause: " + e.getCause() + ", " +
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
            mImageView.setVisibility(ImageView.INVISIBLE);
            mVideoView.setVisibility(VideoView.INVISIBLE);
        } catch (IllegalStateException e) {
            e.printStackTrace();
            Log.d(TAG, "stopRecording: " + e.getMessage() + e.getCause());
            Toast.makeText(this, "IllegalStateException" + e.getMessage() + e.getCause(), Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(this, "Exception" + e.getMessage() + e.getCause(), Toast.LENGTH_SHORT).show();
        }
        Toast.makeText(this, R.string.stop, Toast.LENGTH_SHORT).show();
    }

    private void openAudioChooser() {
        String status = Environment.getExternalStorageState();
        if (status.equals(Environment.MEDIA_MOUNTED)) {
            int permCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            if (!(permCheck == PackageManager.PERMISSION_GRANTED)) {
                //Call for permission
                if ((ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE))) {
                    Toast.makeText(this, R.string.request_permissions, Toast.LENGTH_LONG).show();
                    ActivityCompat.requestPermissions
                            (this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUESTS);

                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUESTS);
                }

            } else {
                Intent intent = new Intent();
                intent.setType("audio/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, PICK_AUDIO_REQUEST);
                mImageView.setVisibility(ImageView.INVISIBLE);
                mVideoView.setVisibility(VideoView.INVISIBLE);
                txtInfo.setVisibility(TextView.VISIBLE);
            }
        }
    }

    private void openImageChooser() {
        String status = Environment.getExternalStorageState();
        if (status.equals(Environment.MEDIA_MOUNTED)) {
            int permCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            if (!(permCheck == PackageManager.PERMISSION_GRANTED)) {
                //Call for permission
                if ((ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE))) {
                    Toast.makeText(this, R.string.request_permissions, Toast.LENGTH_LONG).show();
                    ActivityCompat.requestPermissions
                            (this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUESTS);

                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUESTS);
                }

            } else {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, PICK_IMAGE_REQUEST);
                mImageView.setVisibility(ImageView.VISIBLE);
                mVideoView.setVisibility(VideoView.INVISIBLE);
                txtInfo.setVisibility(TextView.INVISIBLE);
            }
        }
    }

    private void openVideoChooser() {
        String status = Environment.getExternalStorageState();
        if (status.equals(Environment.MEDIA_MOUNTED)) {
            int permCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            if (!(permCheck == PackageManager.PERMISSION_GRANTED)) {
                //Call for permission
                if ((ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE))) {
                    Toast.makeText(this, R.string.request_permissions, Toast.LENGTH_LONG).show();
                    ActivityCompat.requestPermissions
                            (this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUESTS);

                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUESTS);
                }

            } else {
                Intent intent = new Intent();
                intent.setType("video/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, PICK_VIDEO_REQUEST);
                mVideoView.setVisibility(VideoView.VISIBLE);
                mImageView.setVisibility(ImageView.INVISIBLE);
                txtInfo.setVisibility(TextView.INVISIBLE);
            }
        }
    }

    private void makePhoto() {
        String status = Environment.getExternalStorageState();
        if (status.equals(Environment.MEDIA_MOUNTED)) {
            int permCheck2 = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
            int permCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (!(permCheck == PackageManager.PERMISSION_GRANTED) |
                    !(permCheck2 == PackageManager.PERMISSION_GRANTED)) {

                //Call for permission
                if ((ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) |
                        (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)))) {

                    Toast.makeText(this, R.string.request_permissions, Toast.LENGTH_LONG).show();
                    ActivityCompat.requestPermissions
                            (this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                    Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUESTS);

                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                    Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUESTS);
                }

            } else {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (photoFile != null) {
                        fileUri = FileProvider.getUriForFile(this, "com.example.android.fileprovider", photoFile);
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                        Log.d(TAG, "takePhoto: " + fileUri + "\n" + currentPhotoPath);
                        startActivityForResult(takePictureIntent, PICK_IMAGE_CAPTURE_REQUEST);

                    }

                }

                mImageView.setVisibility(ImageView.VISIBLE);
                mVideoView.setVisibility(VideoView.INVISIBLE);
                txtInfo.setVisibility(TextView.INVISIBLE);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_AUDIO_REQUEST && resultCode == RESULT_OK && data != null
                && data.getData() != null) {
            fileUri = data.getData();

            if (fileUri.getPath().contains("primary")) {
                String path = Environment.getExternalStorageDirectory().getAbsolutePath();
                String[] audioPath = fileUri.getPath().split(":");
                path += "/" + audioPath[1];
                txtInfo.setText(path);
            } else {

                txtInfo.setText(fileUri.getPath());
            }


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

        }

        if (requestCode == PICK_IMAGE_CAPTURE_REQUEST && resultCode == RESULT_OK) {

            int targetW = 300;
            int targetH = 300;


            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;

            BitmapFactory.decodeFile(currentPhotoPath, bmOptions);

            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            bmOptions.inSampleSize = Math.min(photoW / targetW, photoH / targetH);
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inPurgeable = true;

            Bitmap bitmap2 = BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
            mImageView.setImageBitmap(bitmap2);

            galleryAddPic();

        }
        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            fileUri = data.getData();
            mVideoView.setVideoURI(fileUri);
            MediaController mediaController = new MediaController(this);
            mediaController.setAnchorView(mVideoView);
            mVideoView.setMediaController(mediaController);
            mVideoView.start();

            galleryAddPic();
        }

    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void galleryAddPic() {
        try {
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            File f = new File(currentPhotoPath);
            mediaScanIntent.setData(Uri.fromFile(f));
            sendBroadcast(mediaScanIntent);
            Log.d(TAG, "galleryAddPic: " + "\n" + Uri.fromFile(f));
        } catch (Exception e) {
            Log.d(TAG, "galleryAddPic: " + e.getMessage() + e.getCause());
        }
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

    private void uploadFile() {
        if (fileUri != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault());
            Date resultDate = new Date(System.currentTimeMillis());

            MimeTypeMap mime = MimeTypeMap.getSingleton();
            String mimeType = mime.getMimeTypeFromExtension(getFileExtension(fileUri));
            assert mimeType != null;
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


            StorageReference fileReference = mStorageRef.child(sdf.format(resultDate) + " - " + fileName.getText().toString().trim() + "." + getFileExtension(fileUri));

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

                    Toast.makeText(MultimediaMain.this, R.string.upload_success, Toast.LENGTH_SHORT).show();
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
