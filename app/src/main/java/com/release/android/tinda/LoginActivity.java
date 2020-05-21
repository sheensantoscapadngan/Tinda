package com.release.android.tinda;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.release.android.tinda.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {

    private EditText number;
    private TextView next;
    private ImageView google;
    private String numberText,firstNameText,lastNameText,uid;
    private FirebaseAuth firebaseAuth;
    private GoogleSignInOptions gso;
    private final static int RC_SIGN_IN = 1;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        setupViews();
        setupGoogle();
        activateListeners();

    }


    private void setupViews() {

        number = (EditText) findViewById(R.id.editTextLoginNumber);
        next = (TextView) findViewById(R.id.textViewLoginContinue);
        google = (ImageView) findViewById(R.id.imageViewLoginGoogle);

        //firebase
        firebaseAuth = FirebaseAuth.getInstance();

    }

    private void activateListeners() {

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                numberText = "+63" + number.getText().toString();

                if(isNumberValid()){

                    moveToNextActivity();

                }else{

                    return;

                }

            }
        });

        google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                signInWithGoogle();

            }
        });


    }

    private void moveToNextActivity() {

        //this function is for passing the verification code to the next activity for user verification

        Intent intent = new Intent(LoginActivity.this,VerifyActivity.class);
        intent.putExtra("numberText", numberText);
        startActivity(intent);

    }

    private Boolean isNumberValid(){

        //this function is for checking if number is a valid number
        if(numberText.length() != 13){
            number.setError("Invalid Mobile Number");
            number.requestFocus();
            return false;
        }
        return true;

    }

    private void setupGoogle() {

        //this function is for setting up google sign in

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


    }

    private void signInWithGoogle(){

        //this function is for signing in with google

        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if(requestCode == RC_SIGN_IN){

            //this is for handling google signin

            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);

        }

    }

    private void handleSignInResult(Task<GoogleSignInAccount> task) {

        //this is for getting google account after google sign in

        try {

            GoogleSignInAccount account = task.getResult(ApiException.class);
            firstNameText = account.getGivenName();
            lastNameText = account.getFamilyName();
            firebaseAuthToGoogle(account);


        } catch (ApiException e) {
            e.printStackTrace();

        }

    }

    private void firebaseAuthToGoogle(GoogleSignInAccount account) {

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(),null);
        firebaseAuth.signInWithCredential(credential)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {

                        uid = firebaseAuth.getCurrentUser().getUid();
                        saveToDatabase();

                    }
                });


    }

    private void saveToDatabase() {

        //this is for saving information from google / facebook signin to firebase database
        //this is also for moving to main activity

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                .child("Users").child(uid);

        userRef.child("firstName").setValue(firstNameText);
        userRef.child("lastName").setValue(lastNameText);
        userRef.child("completeName").setValue(firstNameText + " " + lastNameText);

        Intent intent = new Intent(LoginActivity.this,MainActivity.class);
        startActivity(intent);
        finish();

    }




}
