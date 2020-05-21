package com.release.android.tinda;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.release.android.tinda.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DetailsActivity extends AppCompatActivity {

    private TextView next;
    private EditText firstName,lastName;
    private String firstNameText,lastNameText;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference rootRef;
    private String uid;
    private ImageView back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        setupViews();
        activateListeners();

    }



    private void setupViews() {

        next = (TextView) findViewById(R.id.textViewDetailsContinue);
        firstName = (EditText) findViewById(R.id.editTextDetailsFirstName);
        lastName = (EditText) findViewById(R.id.editTextDetailsLastName);
        back = (ImageView) findViewById(R.id.imageViewDetailsBack);

        //firebase
        firebaseAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
        uid = firebaseAuth.getCurrentUser().getUid();

    }

    private void activateListeners() {

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                firstNameText = firstName.getText().toString();
                lastNameText = lastName.getText().toString();

                if(areNamesValid()){

                    editDatabase();
                    moveToNextActivity();

                }else{
                    return;
                }

            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                firebaseAuth.signOut();

                Intent intent = new Intent(DetailsActivity.this,VerifyActivity.class);
                startActivity(intent);
                finish();

            }
        });


    }

    private void moveToNextActivity() {

        Intent intent = new Intent(DetailsActivity.this,MainActivity.class);
        startActivity(intent);
        finish();

    }

    private void editDatabase() {

        DatabaseReference userRef = rootRef.child("Users").child(uid);
        userRef.child("firstName").setValue(firstNameText);
        userRef.child("lastName").setValue(lastNameText);
        userRef.child("completeName").setValue(firstNameText + " " + lastNameText);


    }

    private boolean areNamesValid() {

        //this function is for checking if input fields are empty

        if(firstNameText.length() == 0) {
            firstName.setError("This field cannot be left blank");
            firstName.requestFocus();
            return false;
        }

        if(lastNameText.length() == 0){
            lastName.setError("This field cannot be left blank");
            lastName.requestFocus();
            return false;
        }

        return true;


    }


}
