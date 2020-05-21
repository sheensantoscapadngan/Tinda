package com.release.android.tinda;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.health.TimerStat;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.ServerValue;
import com.release.android.tinda.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class ShopActivity extends AppCompatActivity{

    private ImageView back,message,call,direction,image,popupBack;
    private TextView name,address,distance,noPosts;
    private RecyclerView recyclerView;
    private String nameText,addressText,distanceText,numberText,imageText,idText,currentLatitude,currentLongitude,destinationLatitude,destinationLongitude;
    private static final int REQUEST_CALL = 1;
    private DatabaseReference rootRef;
    private String uid,userID,userName;
    private FirebaseAuth firebaseAuth;
    private String genreText;
    private TextView viewRates,writeReview,popupSubmit,follow;
    private ShopFeedAdapter adapter;
    private ArrayList<String> postTimeList,postHeaderList,postImageList,postDescriptionList,postLikesList,postCommentList,commentNameList,commentContentList,postIdList;
    private ProgressDialog progressDialog;
    private RatingBar ratingBar,popupRatingBar;
    private EditText popupContent;
    private ConstraintLayout popupLayout;
    private int reviewState = 0,currentRatingCount = 0,followState = 0;
    private String reviewContentText;
    private Float currentRating = 0f;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);

        //load progress dialog//
        progressDialog = new ProgressDialog(this);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading shop...");
        progressDialog.show();
        //----------------------------------------//

        checkCallingActivity();
    }

    private void checkCallingActivity() {

        //this function is for checking what activity called it//
        //request == null means its from search activity, else its from Cart Activity//

        String request = getIntent().getStringExtra("request");
        if(request == null){

            setupViews();
            initializeVariables();
            activateListeners();


        }else{

            setupViews();
            initializeCartVariables();

        }

        checkFollowState();

    }

    private void checkFollowState() {

        DatabaseReference followRef = rootRef.child("Following").child(uid).child(idText);
        followRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    followState = 1;
                    follow.setText("Unfollow");
                }else{
                    followState = 0;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
    private void checkReviewState() {

        DatabaseReference reviewRef = rootRef.child("Review_States").child(uid).child(idText);
        reviewRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()){
                    reviewState = 1;
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void setupRecyclerView() {

        adapter = new ShopFeedAdapter(postTimeList,postHeaderList,postImageList,postDescriptionList,postLikesList,postCommentList,ShopActivity.this,userID,userName,postIdList,idText);
        recyclerView.setAdapter(adapter);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        recyclerView.setLayoutManager(linearLayoutManager);
    }

    private void initializeCartVariables() {

        //this function is for initializing the variables if activity is started from cart activity//

        currentLatitude = getIntent().getStringExtra("currentLatitude");
        currentLongitude = getIntent().getStringExtra("currentLongitude");

        Log.d("LOC_CHECK","SHOP CART LATITUDE IS " + currentLatitude);
        Log.d("LOC_CHECK","SHOP CART LONGITUDE IS " + currentLongitude);

        idText = getIntent().getStringExtra("businessID");
        genreText = getIntent().getStringExtra("businessGenre");

        DatabaseReference businessRef = rootRef.child("Businesses").child(genreText).child(idText);
        businessRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                nameText = dataSnapshot.child("Business_name").getValue().toString();
                addressText = dataSnapshot.child("Business_address").getValue().toString();
                numberText = dataSnapshot.child("Business_number").getValue().toString();
                imageText = dataSnapshot.child("Business_picture").getValue().toString();
                destinationLatitude = dataSnapshot.child("l").child("0").getValue().toString();
                destinationLongitude = dataSnapshot.child("l").child("1").getValue().toString();

                Location currentLocation = new Location("");
                currentLocation.setLatitude(Double.valueOf(currentLatitude));
                currentLocation.setLongitude(Double.valueOf(currentLongitude));

                Location destinationLocation = new Location("");
                destinationLocation.setLatitude(Double.valueOf(destinationLatitude));
                destinationLocation.setLongitude(Double.valueOf(destinationLongitude));

                String distanceVal = String.valueOf(currentLocation.distanceTo(destinationLocation));

                //round off for distance//
                Double distances= Double.valueOf(distanceVal);
                distances /= 1000;
                DecimalFormat df = new DecimalFormat("#.000");
                final String distanceText = df.format(distances);

                name.setText(nameText);
                address.setText(addressText);
                distance.setText(distanceText + " km away");



                Glide.with(ShopActivity.this).load(imageText).into(image);

                //proceed to Activity setup//
                setupRatingBar();
                activateListeners();
                checkReviewState();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    private void initializeVariables() {

        //Values needed are current longitude, current latitude, businessID, genre//

        //this function is for getting the variables from search//
        nameText = getIntent().getStringExtra("businessName");
        addressText = getIntent().getStringExtra("businessAddress");
        distanceText = getIntent().getStringExtra("businessDistance");
        numberText = getIntent().getStringExtra("businessNumber");
        imageText = getIntent().getStringExtra("businessPicture");
        idText = getIntent().getStringExtra("businessID");
        currentLatitude = getIntent().getStringExtra("currentLatitude");
        currentLongitude = getIntent().getStringExtra("currentLongitude");
        destinationLatitude = getIntent().getStringExtra("destinationLatitude");
        destinationLongitude = getIntent().getStringExtra("destinationLongitude");
        genreText = getIntent().getStringExtra("businessGenre");


        Log.d("LOC_CHECK","SHOP LATITUDE IS " + currentLatitude);
        Log.d("LOC_CHECK","SHOP LONGITUDE IS " + currentLongitude);

        name.setText(nameText);
        address.setText(addressText);
        distance.setText(distanceText + " km away");

        Glide.with(ShopActivity.this).load(imageText).into(image);

        setupRatingBar();
        checkReviewState();

    }

    private void setupViews() {

        back = (ImageView) findViewById(R.id.imageViewShopBack);
        message = (ImageView) findViewById(R.id.imageViewShopMessage);
        call = (ImageView) findViewById(R.id.imageViewShopCall);
        direction = (ImageView) findViewById(R.id.imageViewShopDirections);
        name = (TextView) findViewById(R.id.textViewShopName);
        address = (TextView) findViewById(R.id.textViewShopAddress);
        distance = (TextView) findViewById(R.id.textViewShopDistance);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerViewShop);
        image = (ImageView) findViewById(R.id.imageViewShopImage);
        viewRates = (TextView) findViewById(R.id.textViewShopViewRates);
        noPosts = (TextView) findViewById(R.id.textViewShopNoPosts);
        ratingBar = (RatingBar) findViewById(R.id.ratingBarShop);
        writeReview = (TextView) findViewById(R.id.textViewShopWriteAReview);
        follow = (TextView) findViewById(R.id.textViewShopFollow);

        popupBack = (ImageView) findViewById(R.id.imageViewShopPopupBack);
        popupSubmit = (TextView) findViewById(R.id.textViewShopPopupSubmit);
        popupRatingBar = (RatingBar) findViewById(R.id.ratingBarShopPopup);
        popupContent = (EditText) findViewById(R.id.editTextShopPopupContent);
        popupLayout = (ConstraintLayout) findViewById(R.id.constraintLayoutShopPopup);

        postTimeList = new ArrayList<>();
        postHeaderList = new ArrayList<>();
        postImageList = new ArrayList<>();
        postDescriptionList = new ArrayList<>();
        postLikesList = new ArrayList<>();
        postCommentList = new ArrayList<>();
        postIdList = new ArrayList<>();

        commentNameList = new ArrayList<>();
        commentContentList = new ArrayList<>();

        //firebase
        rootRef = FirebaseDatabase.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();
        uid = firebaseAuth.getCurrentUser().getUid();

        getUserInformation();
    }

    private void setupRatingBar() {

        //this function is for setting up rating bar in shop//

        final DatabaseReference ratingRef = rootRef.child("Business_Ratings").child(idText);
        ratingRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()) {

                    currentRating = Float.parseFloat(dataSnapshot.child("Total_rating").getValue().toString());
                    currentRatingCount = Integer.valueOf(dataSnapshot.child("Total_count").getValue().toString());

                    ratingBar.setRating(currentRating / currentRatingCount);

                }else{

                    currentRatingCount = 0;
                    currentRating = 0f;

                    ratingBar.setRating(currentRating / currentRatingCount);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void getUserInformation() {


        userID = uid;
        rootRef.child("Users").child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                userName = dataSnapshot.child("completeName").getValue().toString();

                setupRecyclerView();
                loadFeed();

            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void activateListeners() {

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                finish();

            }
        });

        message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                makePhoneMessage();

            }
        });

        call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                makePhonecall();

            }
        });

        direction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                openGoogleDirections();

            }
        });

        viewRates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(ShopActivity.this,RatesActivity.class);
                intent.putExtra("popupBusinessID",idText);
                startActivity(intent);

            }
        });

        popupBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                popupLayout.setVisibility(View.GONE);

            }
        });

        popupSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                reviewContentText = popupContent.getText().toString();
                Boolean isValid = validateReviewInput();

                if (isValid) {

                    postReviewToDB();

                }

            }
        });

        writeReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(reviewState == 0) {
                    popupLayout.setVisibility(View.VISIBLE);
                }else{
                    Toast.makeText(ShopActivity.this, "You have already submitted a review", Toast.LENGTH_SHORT).show();
                }

            }
        });

        follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(followState == 0) {
                    followBusiness();
                    addUserFromFollowers();
                    followState = 1;
                }else{
                    unfollowBusiness();
                    removeUserFromFollowers();
                    followState = 0;
                }

            }
        });

    }

    private void addUserFromFollowers() {

        //this function is for adding the user to the followers list for business size//
        DatabaseReference followerRef = rootRef.child("Followers").child(idText).child(uid);
        followerRef.setValue(ServerValue.TIMESTAMP);

    }

    private void removeUserFromFollowers(){

        //this function is for removing the user from the followers list for business size//
        DatabaseReference followerRef = rootRef.child("Followers").child(idText).child(uid);
        followerRef.setValue(null);

    }

    private void unfollowBusiness() {

        //this function is for unfollowing business in the user side//

        DatabaseReference followRef = rootRef.child("Following").child(uid).child(idText);
        followRef.setValue(null);

        follow.setText("Follow");
    }

    private void followBusiness() {

        //this function is for following business in the user side//

        DatabaseReference followRef = rootRef.child("Following").child(uid).child(idText);
        followRef.child("Business_name").setValue(nameText);
        followRef.child("Business_address").setValue(addressText);
        followRef.child("Business_image").setValue(imageText);
        followRef.child("Business_genre").setValue(genreText);

        follow.setText("Unfollow");
    }

    private void postReviewToDB() {
        //this function is for saving the review to db//

        progressDialog.setMessage("Uploading review");
        progressDialog.show();

        Float reviewScore = popupRatingBar.getRating();
        Long now = System.currentTimeMillis();

        //add to overall rating db//
        DatabaseReference ratingRef = rootRef.child("Business_Ratings").child(idText);
        ratingRef.child("Total_rating").setValue(reviewScore + currentRating);
        ratingRef.child("Total_count").setValue(currentRatingCount + 1);

        //add to review for business db//
        DatabaseReference reviewRef = rootRef.child("Business_Reviews").child(idText).push();
        reviewRef.child("Reviewer_name").setValue(userName);
        reviewRef.child("Reviewer_rating").setValue(reviewScore);
        reviewRef.child("Reviewer_timestamp").setValue(now);
        reviewRef.child("Reviewer_content").setValue(reviewContentText);

        //add to personal state checker//
        DatabaseReference stateRef = rootRef.child("Review_States").child(uid);
        stateRef.child(idText).setValue(ServerValue.TIMESTAMP);

        ratingBar.setRating((reviewScore + currentRating) / (currentRatingCount + 1));

        notifyReviewSuccess(reviewScore);
    }

    private void notifyReviewSuccess(float reviewScore) {

        //this function is for notifying review upload success//
        ratingBar.setRating((reviewScore + currentRating) / (currentRatingCount + 1));
        progressDialog.dismiss();

        popupLayout.setVisibility(View.GONE);
        reviewState = 1;

        Toast.makeText(this, "Review Submitted", Toast.LENGTH_SHORT).show();

    }

    private Boolean validateReviewInput() {

        //this function is for checking if review content input is valid//

        if(reviewContentText.length() == 0){
            popupContent.setError("This cannot be left blank");
            popupContent.requestFocus();
            return false;
        }

        return true;

    }


    private void loadFeed() {

        //this function is for loading the feed content from the db//

        final DatabaseReference feedRef = rootRef.child("Posts").child(idText);

        feedRef.orderByChild("Post_timestamp").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(DataSnapshot ds : dataSnapshot.getChildren()){

                    postIdList.add(ds.getKey());
                    postDescriptionList.add(ds.child("Post_description").getValue().toString());
                    postHeaderList.add(ds.child("Post_header").getValue().toString());
                    postImageList.add(ds.child("Post_image").getValue().toString());
                    postTimeList.add(ds.child("Post_timestamp").getValue().toString());

                    //get like count//
                    feedRef.child(ds.getKey()).child("Post_likes").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {


                            int likeCount = 0;

                            for(DataSnapshot likeSnapshot : dataSnapshot.getChildren()){
                                likeCount++;
                            }

                            postLikesList.add(Integer.toString(likeCount));

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    //get comment count//
                    feedRef.child(ds.getKey()).child("Post_comments").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            for(DataSnapshot commentSnapshot : dataSnapshot.getChildren()){
                                commentNameList.add(commentSnapshot.getKey());
                                commentContentList.add(commentSnapshot.getValue().toString());
                            }

                            postCommentList.add(Integer.toString(commentNameList.size()));

                            if(postTimeList.size() != 0){
                                noPosts.setVisibility(View.GONE);
                            }

                            adapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }

                if(postTimeList.size() == 0){
                    noPosts.setVisibility(View.VISIBLE);
                }

                progressDialog.dismiss();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    private void openGoogleDirections() {

        //this function is for opening Google directions//
        String uri = "https://www.google.com/maps/dir/?api=1&origin=" + currentLatitude + "," + currentLongitude + "&destination=" + destinationLatitude + "," + destinationLongitude;
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri));
        startActivity(Intent.createChooser(intent, "Select an application"));

    }

    private void makePhoneMessage() {

        //this function is for opening sms to chat a number
        Uri smsUri = Uri.parse("smsto:" + numberText);
        Intent intent = new Intent(Intent.ACTION_SENDTO,smsUri);
        startActivity(intent);

    }


    private void makePhonecall() {

        //this function is for calling the business contact number//
        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED){

            ActivityCompat.requestPermissions(ShopActivity.this,new String[]{Manifest.permission.CALL_PHONE},REQUEST_CALL);

        }else{

            String dial = "tel:" + numberText;
            startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(dial)));
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == REQUEST_CALL){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                makePhonecall();
            }
        }

    }


}
