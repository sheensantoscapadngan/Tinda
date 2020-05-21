package com.release.android.tinda;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.release.android.tinda.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.TimeUnit;

public class VerifyActivity extends AppCompatActivity {

    private ImageView back,resend;
    private EditText number1,number2,number3,number4,number5,number6;
    private TextView verify,number;
    private String numberText,verificationCodeText;
    private FirebaseAuth firebaseAuth;
    private PhoneAuthProvider.ForceResendingToken token;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);

        initializeVariables();
        sendVerification();
        setupViews();
        activateListeners();

    }

    private void initializeVariables() {

        numberText = getIntent().getStringExtra("numberText");


    }

    private void setupViews() {

        back = (ImageView) findViewById(R.id.imageViewVerifyBack);
        resend = (ImageView) findViewById(R.id.imageViewVerifyResend);
        number1 = (EditText) findViewById(R.id.editTextVerify1);
        number2 = (EditText) findViewById(R.id.editTextVerify2);
        number3 = (EditText) findViewById(R.id.editTextVerify3);
        number4 = (EditText) findViewById(R.id.editTextVerify4);
        number5 = (EditText) findViewById(R.id.editTextVerify5);
        number6 = (EditText) findViewById(R.id.editTextVerify6);
        verify = (TextView) findViewById(R.id.textViewVerifyVerify);
        number =  (TextView) findViewById(R.id.textViewVerifyNumber);


        //update UI
        number.setText(numberText);

        //setup firebase for login
        firebaseAuth = FirebaseAuth.getInstance();

    }

    private void activateListeners() {

        activateListenerForEditText();

        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String inputCode = number1.getText().toString() + number2.getText().toString() +
                        number3.getText().toString() + number4.getText().toString() +
                        number5.getText().toString() + number6.getText().toString();

                verifySigninCode(inputCode);

            }
        });

        resend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                resendVerificationCode(numberText,token);

            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                finish();

            }
        });


    }

    private void sendVerification() {

        //this function is for sending the verification code to the number inputted
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    numberText,
                    30,
                    TimeUnit.SECONDS,
                    this,
                    mCallBacks
            );


    }


    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallBacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

            signInWithPhoneAuthCredential(phoneAuthCredential);

        }

        @Override
        public void onVerificationFailed(FirebaseException e) {

        }

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);

            verificationCodeText = s;
            token = forceResendingToken;
            Toast.makeText(VerifyActivity.this, "Verification Code sent!", Toast.LENGTH_SHORT).show();

        }
    };


    private void resendVerificationCode(String phoneNumber,
                                        PhoneAuthProvider.ForceResendingToken token) {

        //this function is for resending verification code to number

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                30,                // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallBacks,         // OnVerificationStateChangedCallbacks
                token);             // ForceResendingToken from callbacks
        Toast.makeText(this, "Verification Code Resent!", Toast.LENGTH_SHORT).show();

    }



    private void verifySigninCode(String inputCode) {

        //this function is for setting up verification code

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationCodeText,inputCode);
        signInWithPhoneAuthCredential(credential);

    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential){

        //this function is for signing in with verification code

        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if(task.isSuccessful()){

                            checkIfDetailsExist();

                        }else{

                            if(task.getException() instanceof FirebaseAuthInvalidCredentialsException)
                                Toast.makeText(VerifyActivity.this, "Invalid Code", Toast.LENGTH_SHORT).show();

                        }

                    }
                });

    }

    private void checkIfDetailsExist() {

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").
                child(firebaseAuth.getCurrentUser().getUid());

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()){

                    Intent intent = new Intent(VerifyActivity.this,MainActivity.class);
                    startActivity(intent);
                    finish();

                }else{

                    Intent intent = new Intent(VerifyActivity.this,DetailsActivity.class);
                    startActivity(intent);
                    finish();

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }


    private void activateListenerForEditText() {

        //this function is for activating auto-next feature for each editText

        number1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                number2.requestFocus();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        number2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                number3.requestFocus();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        number3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                number4.requestFocus();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        number4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                number5.requestFocus();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        number5.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                number6.requestFocus();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


    }

}
