package com.escoladeltreball.cloudfile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AudioActivity extends AppCompatActivity implements AudioAdapter.OnItemClickListener {
    private RecyclerView mRecyclerView;
    private AudioAdapter mAdapter;
    MediaPlayer myMediaPlayer;
    public MediaPlayer mediaPlayer;
    boolean mpReady;
    private ProgressBar mProgressCircle;

    private FirebaseStorage mStorage;
    private DatabaseReference mDatabaseRef;
    private ValueEventListener mDBListener;

    private List<AudioUpload> mUploads;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio);

        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mProgressCircle = findViewById(R.id.progress_circle);

        mUploads = new ArrayList<>();
        mAdapter = new AudioAdapter(AudioActivity.this, mUploads);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(AudioActivity.this);

        mStorage = FirebaseStorage.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("uploads/audio");

        mDBListener = mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUploads.clear();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    AudioUpload upload = postSnapshot.getValue(AudioUpload.class);
                    upload.setKey(postSnapshot.getKey());
                    mUploads.add(upload);
                }

                mAdapter.notifyDataSetChanged();
                mProgressCircle.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(AudioActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                mProgressCircle.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDatabaseRef.removeEventListener(mDBListener);
    }

    @Override
    public void onItemClick(int position) {
        AudioUpload selectedItem = mUploads.get(position);
        if (mediaPlayer.isPlaying()) {
            Toast.makeText(this, "Parando musica", Toast.LENGTH_SHORT).show();
            mediaPlayer.stop();
        }

        try {
            StorageReference audioRef = mStorage.getReferenceFromUrl(selectedItem.getAudioUrl());
            String key = audioRef.getBucket();
            mediaPlayer.setDataSource(key);
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                }
            });
            mediaPlayer.prepareAsync();

            Toast.makeText(this, "Reproduciendo musica...", Toast.LENGTH_SHORT).show();

        } catch (IllegalArgumentException | IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onDownloadClick(int position) {
       /* AudioUpload selectedItem = mUploads.get(position);
        final String selectedKey = selectedItem.getKey();

        StorageReference audioRef = mStorage.getReferenceFromUrl(selectedItem.getAudioUrl());
        audioRef.getDownloadUrl();*/

        Toast.makeText(AudioActivity.this, "No implementado", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeleteClick(int position) {
        AudioUpload selectedItem = mUploads.get(position);
        final String selectedKey = selectedItem.getKey();

        StorageReference audioRef = mStorage.getReferenceFromUrl(selectedItem.getAudioUrl());
        audioRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                mDatabaseRef.child(selectedKey).removeValue();
                Toast.makeText(AudioActivity.this, "Item borrado.", Toast.LENGTH_SHORT).show();
            }
        });

    }
}
