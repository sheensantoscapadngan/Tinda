package com.release.android.tinda;

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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class CartActivity extends AppCompatActivity implements CartPageAdapter.AdapterCallback{

    private RecyclerView recyclerView,popupRecyclerView;
    private ImageView back;
    private String currentLatitude, currentLongitude,uid;
    private ArrayList<String> nameList,genreList,itemList,priceList,amountList,idList,itemNameList,itemAmountList,itemPriceList;
    private DatabaseReference rootRef;
    private FirebaseAuth firebaseAuth;
    private CartPageAdapter adapter;
    private ConstraintLayout popupLayout;
    private CartPopupPageAdapter popupAdapter;
    private TextView close;
    private int resumeState = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        setupViews();
        initializeVariables();
        setupRecyclerView();
        loadInformationFromDatabase();
        activateListeners();

        resumeState = 1;
    }

    private void initializeVariables() {

        currentLatitude = getIntent().getStringExtra("currentLatitude");
        currentLongitude = getIntent().getStringExtra("currentLongitude");

        nameList = new ArrayList<>();
        genreList = new ArrayList<>();
        itemList = new ArrayList<>();
        priceList = new ArrayList<>();
        amountList = new ArrayList<>();
        idList = new ArrayList<>();

        itemNameList = new ArrayList<>();
        itemPriceList = new ArrayList<>();
        itemAmountList = new ArrayList<>();
    }

    private void activateListeners() {

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                popupLayout.setVisibility(View.GONE);

            }
        });

    }

    private void setupViews() {

        recyclerView = (RecyclerView) findViewById(R.id.recyclerViewCart);
        back = (ImageView) findViewById(R.id.imageViewCartBack);
        close = (TextView) findViewById(R.id.textViewCartPopupClose);

        popupLayout = (ConstraintLayout) findViewById(R.id.constraintLayoutCartPopup);
        popupRecyclerView = (RecyclerView) findViewById(R.id.recyclerViewCartPopup);

        //firebase
        rootRef = FirebaseDatabase.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();
        uid = firebaseAuth.getCurrentUser().getUid();

    }

    private void setupRecyclerView() {

        adapter = new CartPageAdapter(nameList,genreList,itemList,priceList,amountList,idList,CartActivity.this,currentLatitude,currentLongitude);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


    }


    private void loadInformationFromDatabase() {

        //this function is for loading the information from the Cart db//

        DatabaseReference cartRef = rootRef.child("Cart").child(uid);
        cartRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(final DataSnapshot idSnapshot : dataSnapshot.getChildren()){

                    idList.add(idSnapshot.getKey());

                    Log.d("DATA_CHECK",idSnapshot.getKey());

                    DatabaseReference businessRef = rootRef.child("Genre_List").child(idSnapshot.getKey());
                    businessRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            genreList.add(dataSnapshot.child("Business_genre").getValue().toString());
                            nameList.add(dataSnapshot.child("Business_name").getValue().toString());

                            for(DataSnapshot itemSnapshot : idSnapshot.getChildren()){

                                itemList.add(itemSnapshot.getKey());
                                priceList.add(itemSnapshot.child("Item_price").getValue().toString());
                                amountList.add(itemSnapshot.child("Item_amount").getValue().toString());

                                adapter.notifyDataSetChanged();

                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    @Override
    public void onMethodCallback(String idText) {

        //this function is for opening cart popup//

        itemNameList.clear();
        itemPriceList.clear();
        itemAmountList.clear();

        popupLayout.setVisibility(View.VISIBLE);
        loadPopupInformation(idText);

    }

    @Override
    public void onLastItemCallback(String idText) {

        //this function is called when an item is deleted when only a single item is left in an id//

        int position = 0;
        for(int x = 0; x < idList.size(); x++){
            if(idText.equals(idList.get(x))){
                position = x;
            }
        }

        idList.remove(position);
        nameList.remove(position);
        genreList.remove(position);
        itemList.remove(position);
        priceList.remove(position);
        amountList.remove(position);

        adapter.notifyDataSetChanged();

    }

    @Override
    public void reloadRecyclerView() {

        //this reloads the information in the cart recycler view//

        idList.clear();
        nameList.clear();
        genreList.clear();
        itemList.clear();
        priceList.clear();
        amountList.clear();

        loadInformationFromDatabase();

    }

    private void loadPopupInformation(String idText) {

        //this function is for loading item information to popup recyclerView//

        popupAdapter = new CartPopupPageAdapter(itemNameList,itemPriceList,itemAmountList,idText,CartActivity.this);
        popupRecyclerView.setAdapter(popupAdapter);
        popupRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        DatabaseReference itemRef = rootRef.child("Cart").child(uid).child(idText);
        itemRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(DataSnapshot ds : dataSnapshot.getChildren()){

                    itemNameList.add(ds.getKey());
                    itemPriceList.add(ds.child("Item_price").getValue().toString());
                    itemAmountList.add(ds.child("Item_amount").getValue().toString());

                    popupAdapter.notifyDataSetChanged();

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}
