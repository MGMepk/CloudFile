package com.escoladeltreball.cloudfile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Register extends AppCompatActivity {
    EditText mFullName, mEmail, mPassword, mPhone;
    Button mRegisterButton;
    FirebaseAuth fAuth;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);


        mFullName = findViewById(R.id.fullName);
        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        mPhone = findViewById(R.id.phone);
        mRegisterButton = findViewById(R.id.registerButton);

        fAuth = FirebaseAuth.getInstance();

        if (fAuth.getCurrentUser() != null ) {
            Toast.makeText(this, R.string.already_logged, Toast.LENGTH_SHORT).show();
            startActivity(new Intent( getApplicationContext(), MultimediaMain.class));
            finish();
        }


        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEmail.getText().toString().trim();
                String password = mPassword.getText().toString().trim();

                if (TextUtils.isEmpty(email)){
                    String message = getString(R.string.email_required);
                    mEmail.setError(message);
                    return;

                }
                if (TextUtils.isEmpty(password)){
                    String message = getString(R.string.password_required);
                    mPassword.setError(message);
                    return;
                }

                if(password.length() < 6){
                    String message = getString(R.string.password_length);
                    mPassword.setError(message);
                }

                fAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(Register.this, R.string.user_created, Toast.LENGTH_SHORT).show();
                            startActivity(new Intent( getApplicationContext(), MultimediaMain.class));

                        }else{
                            Toast.makeText(Register.this, "Error" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    }
                });
            }
        });
    }
}
