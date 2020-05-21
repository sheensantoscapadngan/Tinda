package com.release.android.tinda;

import android.app.ProgressDialog;
import android.location.Location;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.release.android.tinda.R;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity {

    private ImageView back;
    private RecyclerView recyclerView;
    private TextView businessCount,genre;
    private String genreText,currentLatitude,currentLongitude;
    private ArrayList<String> businessNameList,businessAddressList,businessIDList,businessPictureList,businessDistanceList,businessNumberList,businessLatitude,businessLongitude,businessGenreList;
    private DatabaseReference rootRef;
    private SearchPageAdapter adapter;
    private ProgressDialog progressDialog;
    private ConstraintLayout constraintLayout;
    private int radiusDistance = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        //initialize progress Dialog//
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Finding businesses nearby...");
        progressDialog.show();
        /////////////////////////////

        //initialize constraint layout for progress hiding//
        constraintLayout = (ConstraintLayout) findViewById(R.id.constraintLayoutSearch);
        constraintLayout.setVisibility(View.INVISIBLE);
        ///////////////////////////////////////////////////

        setupViews();
        initializeVariables();
        fillUpLists();
        activateListeners();
        setupRecyclerView();

    }

    private void initializeVariables() {

        genreText = getIntent().getStringExtra("businessName");
        currentLatitude = getIntent().getStringExtra("currentLatitude");
        currentLongitude = getIntent().getStringExtra("currentLongitude");

        genre.setText(genreText);

        businessNameList = new ArrayList<>();
        businessAddressList = new ArrayList<>();
        businessIDList = new ArrayList<>();
        businessPictureList = new ArrayList<>();
        businessDistanceList = new ArrayList<>();
        businessNumberList = new ArrayList<>();
        businessLatitude = new ArrayList<>();
        businessLongitude = new ArrayList<>();
        businessGenreList = new ArrayList<>();

    }

    private void fillUpLists() {

        //this function is for adding items to the recyclerView //

        final DatabaseReference listRef = rootRef.child("Businesses").child(genreText);
        GeoFire geoFire = new GeoFire(listRef);

        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(Double.valueOf(currentLatitude),Double.valueOf(currentLongitude)),radiusDistance);
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {

                businessIDList.add(key);
                DatabaseReference businessRef = listRef.child(key);
                businessRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        //get destination location//
                        Double destinationLatitude = Double.valueOf(dataSnapshot.child("l").child("0").getValue().toString());
                        Double destinationLongitude = Double.valueOf(dataSnapshot.child("l").child("1").getValue().toString());

                        businessLatitude.add(dataSnapshot.child("l").child("0").getValue().toString());
                        businessLongitude.add(dataSnapshot.child("l").child("1").getValue().toString());


                        Location destinationLocation = new Location("");
                        destinationLocation.setLatitude(destinationLatitude);
                        destinationLocation.setLongitude(destinationLongitude);

                        //get current location//
                        Location currentLocation = new Location("");
                        currentLocation.setLatitude(Double.valueOf(currentLatitude));
                        currentLocation.setLongitude(Double.valueOf(currentLongitude));

                        Float distance = currentLocation.distanceTo(destinationLocation);
                        distance = distance / 1000;


                        businessNameList.add(dataSnapshot.child("Business_name").getValue().toString());
                        businessDistanceList.add(Double.toString(distance));
                        businessAddressList.add(dataSnapshot.child("Business_address").getValue().toString());
                        businessPictureList.add(dataSnapshot.child("Business_picture").getValue().toString());
                        businessNumberList.add(dataSnapshot.child("Business_number").getValue().toString());
                        businessCount.setText(businessNameList.size() + " businesses nearby");
                        businessGenreList.add(dataSnapshot.child("Business_genre").getValue().toString());

                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

                progressDialog.dismiss();
                constraintLayout.setVisibility(View.VISIBLE);

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });


    }

    private void setupViews() {

        back = (ImageView) findViewById(R.id.imageViewSearchBack);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerViewSearch);
        businessCount = (TextView) findViewById(R.id.textViewSearchBusinessCount);
        genre = (TextView) findViewById(R.id.textViewSearchGenre);

        //firebase
        rootRef = FirebaseDatabase.getInstance().getReference();

    }

    private void activateListeners() {

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void setupRecyclerView() {

        adapter = new SearchPageAdapter(businessNameList,businessAddressList,businessIDList,businessDistanceList,businessPictureList,SearchActivity.this,
                businessNumberList,currentLatitude,currentLongitude,businessLatitude,businessLongitude,businessGenreList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

    }

}
