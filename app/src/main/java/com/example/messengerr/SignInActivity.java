package com.example.messengerr;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import com.example.messengerr.Models.Users;
import com.example.messengerr.databinding.ActivitySignInBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;


public class SignInActivity extends AppCompatActivity {

    ActivitySignInBinding signInBinding;
    ProgressDialog progressDialog;
    FirebaseAuth auth;
    GoogleSignInClient mGoogleSignInClient;
    FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        signInBinding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(signInBinding.getRoot());
        getSupportActionBar().hide();
        database = FirebaseDatabase.getInstance();

        auth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(SignInActivity.this);
        progressDialog.setTitle("Log In");
        progressDialog.setMessage("login to your account");
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this , gso);

        signInBinding.btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(signInBinding.email.getText().toString().isEmpty()){
                    signInBinding.email.setError("enter your email");

                    return;
                }
                if(signInBinding.password.getText().toString().isEmpty()){
                    signInBinding.password.setError("enter your password");
                    return;
                }

                progressDialog.show();
                auth.signInWithEmailAndPassword(signInBinding.email.getText().toString() ,signInBinding.password.getText().toString())
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressDialog.dismiss();
                                if(task.isSuccessful()){

                                    Intent intent = new Intent(SignInActivity.this , MainActivity.class);
                                    startActivity(intent);
                                }else{
                                    Toast.makeText(SignInActivity.this, task.getException().getMessage() ,Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

            }
        });

        signInBinding.clicksignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignInActivity.this , SignUpActivity.class);
                startActivity(intent);
            }
        });
        signInBinding.btngoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
                if(auth.getCurrentUser() != null){
                    Intent intent = new Intent(SignInActivity.this , MainActivity.class);
                    startActivity(intent);
                }
            }
        });

        if(auth.getCurrentUser() != null){
            Intent intent = new Intent(SignInActivity.this , MainActivity.class);
            startActivity(intent);
        }
    }
 int RC_SIGN_IN = 65;
    private void signIn(){
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent ,RC_SIGN_IN );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == RC_SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try{
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d("TAG" , "firebaseAuthWithGoogle" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Log.w("TAG" , "Google Sign in failed" , e);
            }
        }
    }
    private void firebaseAuthWithGoogle(String idToken){
        AuthCredential  credential = GoogleAuthProvider.getCredential(idToken,null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Log.d("TAG" , "signin with credetntial success");
                            FirebaseUser user = auth.getCurrentUser();
                            Users users = new Users();
                            users.setUserId((user.getUid()));
                            users.setUsername(user.getDisplayName());
                            users.setProfilepic(user.getPhotoUrl().toString());
                            database.getReference().child("Users").child(user.getUid()).setValue(users);


                        }else{
                            Log.d("TAG" , "singninwithcredential:failure" , task.getException());
                        }


                    }
                });
    }

}