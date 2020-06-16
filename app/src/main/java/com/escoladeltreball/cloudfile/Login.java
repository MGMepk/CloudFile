package com.escoladeltreball.cloudfile;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Login extends AppCompatActivity {

    EditText mEmail, mPassword;
    Button loginButton;
    FirebaseAuth fAuth;
    TextView registerHere;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        registerHere = findViewById(R.id.registerHere);
        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);


        fAuth = FirebaseAuth.getInstance();
        if (fAuth.getCurrentUser() != null) {
            Toast.makeText(this, "Already logged", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getApplicationContext(), MultimediaMain.class));
            finish();
        }


        registerHere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openRegister();
            }
        });


        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = mEmail.getText().toString().trim();
                String password = mPassword.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    mEmail.setError("Email is required");
                    return;

                }
                if (TextUtils.isEmpty(password)) {
                    mPassword.setError("Password is required");
                    return;
                }

                if (password.length() < 6) {
                    mPassword.setError("Password must be more than 6 characters");
                }

                fAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(Login.this, "Logged in successfuly", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), MultimediaMain.class));
                        } else {
                            Toast.makeText(Login.this, "Error" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    private void openRegister() {
        Intent intent = new Intent(this, Register.class);
        startActivity(intent);
    }
}
