package com.example.findmyfam.AccountActivity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;


import com.example.findmyfam.MainActivity;
import com.example.findmyfam.R;
import com.firebase.client.Firebase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private EditText inputEmail, inputPassword;
    private FirebaseAuth auth;
    private FirebaseUser auth2;
    private ProgressBar progressBar;
    private Button btnSignup, btnLogin, btnReset;
    static final Integer LOCATION = 0x1;
    static final Integer Contacts = 0x2;
    static final Integer Telephony = 0x3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //Get Firebase auth instance

        //Request Permissions
        askForPermission(Manifest.permission.ACCESS_FINE_LOCATION, LOCATION);
        askForPermission(Manifest.permission.READ_CONTACTS, Contacts);
        askForPermission(Manifest.permission.READ_PHONE_STATE, Telephony);
        askForPermission(Manifest.permission.ACCESS_COARSE_LOCATION, LOCATION);


        FirebaseMessaging.getInstance().subscribeToTopic("updates");
        auth = FirebaseAuth.getInstance();
        auth2 = auth.getCurrentUser();

        {
            if (auth.getCurrentUser() != null) {

                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                Intent i = new Intent(LoginActivity.this, MainActivity.class);
                String name = auth2.getDisplayName();

                int index = user.getEmail().indexOf('@');
                int index2 = user.getEmail().indexOf('.');
                String subString = "";
                if (index != -1) {
                    subString = user.getEmail().substring(0, index); //this will give abc
                }
                if (index2 != -1) {
                    subString = user.getEmail().substring(0, index2); //this will give abc
                }

                i.putExtra("message", subString.trim());
                startActivity(i);
                finish();
            }
        }

        setContentView(R.layout.activity_login);
        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        btnSignup = (Button) findViewById(R.id.btn_signup);
        btnLogin = (Button) findViewById(R.id.btn_login);


        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, SignupActivity.class));
            }
        });


        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = inputEmail.getText().toString();
                final String password = inputPassword.getText().toString();

                if (email.trim().equals("admin")) {
                    if (password.trim().equals("admin@123")) {
                        Intent i = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(i);
                    } else {
                        Toast.makeText(getApplicationContext(), "Enter Correct Password!!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (TextUtils.isEmpty(email)) {
                        Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (TextUtils.isEmpty(password)) {
                        Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    progressBar.setVisibility(View.VISIBLE);

                    //authenticate user

                    auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    // If sign in fails, display a message to the user. If sign in succeeds
                                    // the auth state listener will be notified and logic to handle the
                                    // signed in user can be handled in the listener.
                                    progressBar.setVisibility(View.GONE);
                                    if (!task.isSuccessful()) {
                                        // there was an error
                                        if (password.length() < 6) {
                                            inputPassword.setError(getString(R.string.minimum_password));
                                        } else {
                                            Toast.makeText(LoginActivity.this, getString(R.string.auth_failed), Toast.LENGTH_LONG).show();
                                        }
                                    } else {


                                        int index = email.indexOf('@');
                                        int index2 = email.indexOf('.');
                                        String subString = "";
                                        if (index != -1) {
                                            subString = email.substring(0, index); //this will give abc
                                        }
                                        if (index2 != -1) {
                                            subString = email.substring(0, index2); //this will give abc
                                        }
                                        SharedPreferences pref = getBaseContext().getSharedPreferences("MyPref", MODE_PRIVATE);
                                        SharedPreferences.Editor editor = pref.edit();
                                        editor.putString("USERID", subString);
                                        editor.apply();
                                        editor.commit();
                                        Intent i = new Intent(LoginActivity.this, MainActivity.class);
                                        if (subString != "") {
                                            i.putExtra("message", subString + "");
                                            startActivity(i);
                                        }

                                    }
                                }
                            });
                }
            }
        });
    }




    @Override
    protected void onStop() {
        super.onStop();

    }

    private void askForPermission(String permission, Integer requestCode) {
        if (ContextCompat.checkSelfPermission(LoginActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(LoginActivity.this, permission)) {

                //This is called if user has denied the permission before
                //In this case I am just asking the permission again
                ActivityCompat.requestPermissions(LoginActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.READ_CONTACTS}, requestCode);

            } else {

                ActivityCompat.requestPermissions(LoginActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.READ_CONTACTS}, requestCode);
            }
        } else {

        }
    }


}

