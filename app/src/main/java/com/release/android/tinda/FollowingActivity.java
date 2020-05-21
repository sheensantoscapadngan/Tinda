package com.release.android.tinda;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FollowingActivity extends AppCompatActivity {

    private ImageView back;
    private RecyclerView recyclerView;
    private ArrayList<String> followingNameList,followingAddressList,followingGenreList,followingIdList,followingImageList;
    private String currentLatitude,currentLongitude,uid;
    private DatabaseReference rootRef;
    private FirebaseAuth firebaseAuth;
    private FollowingPageAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_following);

        initializeVariables();
        setupViews();
        setupRecyclerView();
        activateListeners();

    }

    private void initializeVariables() {

        //this function is for getting values from main activity//

        currentLatitude = getIntent().getStringExtra("currentLatitude");
        currentLongitude = getIntent().getStringExtra("currentLongitude");

        Log.d("LOC_CHECK","FOLLOWING LATITUDE IS " + currentLatitude);
        Log.d("LOC_CHECK","FOLLOWING LONGITUDE IS " + currentLongitude);


    }

    private void activateListeners() {

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private void setupViews() {

        back = (ImageView) findViewById(R.id.imageViewFollowingBack);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerViewFollowing);

        followingNameList = new ArrayList<>();
        followingAddressList = new ArrayList<>();
        followingIdList = new ArrayList<>();
        followingImageList = new ArrayList<>();
        followingGenreList = new ArrayList<>();

        rootRef = FirebaseDatabase.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();
        uid = firebaseAuth.getCurrentUser().getUid();

    }

    private void setupRecyclerView() {

        adapter = new FollowingPageAdapter(followingNameList,followingAddressList,followingImageList,followingGenreList,followingIdList,this,currentLatitude,currentLongitude);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

    }


    private void loadFollowingList() {

        followingNameList.clear();
        followingAddressList.clear();
        followingGenreList.clear();
        followingImageList.clear();
        followingIdList.clear();

        // this function is for loading following list //
        DatabaseReference followingRef = rootRef.child("Following").child(uid);
        followingRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()) {

                    for (DataSnapshot ds : dataSnapshot.getChildren()) {

                        followingNameList.add(ds.child("Business_name").getValue().toString());
                        followingAddressList.add(ds.child("Business_address").getValue().toString());
                        followingGenreList.add(ds.child("Business_genre").getValue().toString());
                        followingIdList.add(ds.getKey());
                        followingImageList.add(ds.child("Business_image").getValue().toString());

                        adapter.notifyDataSetChanged();
                    }

                }else{
                    adapter.notifyDataSetChanged();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();

        loadFollowingList();
    }
}
