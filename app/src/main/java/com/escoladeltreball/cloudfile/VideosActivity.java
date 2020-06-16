package com.escoladeltreball.cloudfile;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class VideosActivity extends AppCompatActivity implements VideoAdapter.OnItemClickListener {
    private RecyclerView mRecyclerView;
    private VideoAdapter mAdapter;
    private ProgressBar mProgressCircle;

    private FirebaseStorage mStorage;
    private DatabaseReference mDatabaseRef;
    private ValueEventListener mDBListener;
    private List<Upload> mUploads;
    private static final int MY_PERMISSIONS_REQUESTS = 10;

    private FirebaseUser user;
    FirebaseAuth fAuth;
    private String reference = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recycler_layout);

        fAuth = FirebaseAuth.getInstance();
        user = fAuth.getCurrentUser();
        reference = user.getUid() + "/";

        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mProgressCircle = findViewById(R.id.progress_circle);

        mUploads = new ArrayList<>();

        mAdapter = new VideoAdapter(VideosActivity.this, mUploads);
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(VideosActivity.this);

        mStorage = FirebaseStorage.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference(reference + "videos");

        mDBListener = mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                mUploads.clear();

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Upload upload = postSnapshot.getValue(Upload.class);
                    assert upload != null;
                    upload.setKey(postSnapshot.getKey());
                    mUploads.add(upload);
                }

                mAdapter.notifyDataSetChanged();
                mProgressCircle.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(VideosActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                mProgressCircle.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    public void onItemClick(int position) {


    }

    @Override
    public void onOpenClick(int position) {
        Upload uploadCurrent = mUploads.get(position);
        String uploadUri = uploadCurrent.getUrl();
        Intent intent = new Intent(Intent.ACTION_SEND, Uri.parse(uploadUri));
        intent.setDataAndType(Uri.parse(uploadCurrent.getUrl()), "video/mp4");
        intent.setClass(this, FullScreenImageActivity.class);
        intent.putExtra("video", uploadUri);
        startActivity(intent);

        String op = getString(R.string.open);
        Toast.makeText(this, op + ": " + uploadCurrent.getName(), Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onDownloadClick(int position) {
        String status = Environment.getExternalStorageState();
        if (status.equals(Environment.MEDIA_MOUNTED)) {
            int permCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (!(permCheck == PackageManager.PERMISSION_GRANTED)) {
                //Call for permission
                if ((ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE))) {
                    ActivityCompat.requestPermissions
                            (this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUESTS);

                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUESTS);
                }

            } else {
                try {
                    final Upload selectedItem = mUploads.get(position);
                    String url = selectedItem.getUrl();
                    final StorageReference ref = mStorage.getReferenceFromUrl(url);

                    File rootPath = new File(Environment.getExternalStorageDirectory(), "Cloudfile");
                    File videoPath = new File(rootPath, "Videos");
                    if (!videoPath.exists()) {
                        videoPath.mkdirs();
                    }
                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(new Date());
                    String videoFileName = "MP4_" + timeStamp + "_";
                    final File localFile = new File(videoPath, videoFileName + selectedItem.getName() + "." + getFileExtension(url));
                    ref.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {

                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                            mediaScanIntent.setData(Uri.fromFile(localFile));
                            sendBroadcast(mediaScanIntent);
                            Toast.makeText(VideosActivity.this, R.string.file_success, Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(VideosActivity.this, R.string.file_fail, Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d("Download", e.getMessage() + " " + e.getCause());
                }
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUESTS) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                int a = 1+1;
            } else {
                // permission denied
                // Disable the functionality that depends on this permission.
                Toast.makeText(this, R.string.permission, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDeleteClick(int position) {
        final Upload selectedItem = mUploads.get(position);
        final String selectedKey = selectedItem.getKey();

        new AlertDialog.Builder(this)
                .setTitle(R.string.app_name)
                .setMessage(R.string.delete_message)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        StorageReference audioRef = mStorage.getReferenceFromUrl(selectedItem.getUrl());
                        audioRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                mDatabaseRef.child(selectedKey).removeValue();
                                Toast.makeText(VideosActivity.this, R.string.item_deleted, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private String getFileExtension(String url) {
        String extension = url.substring(url.lastIndexOf(".") + 1);
        extension = extension.substring(0, extension.indexOf("?"));
        return extension;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDatabaseRef.removeEventListener(mDBListener);
    }
}
