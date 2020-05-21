package com.release.android.tinda;

import android.app.ProgressDialog;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class RatesActivity extends AppCompatActivity implements ShopPageAdapter.AdapterCallback{

    private String popupBusinessID, popupItemName, popupItemPrice, popupItemAmount;
    private TextView popupSave,popupCancel,popupQuestion,noListed;
    private ConstraintLayout popupLayout;
    private ShopPageAdapter adapter;
    private EditText popupAmount;
    private DatabaseReference rootRef;
    private String uid,idText;
    private FirebaseAuth firebaseAuth;
    private ArrayList<String> itemList,priceList;
    private RecyclerView recyclerView;
    private ImageView back;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rates);


        //load progress dialog//
        progressDialog = new ProgressDialog(this);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading products and services");
        progressDialog.show();
        //----------------------------------------//

        initializeVariables();
        setupViews();
        activateListeners();
        setupRecyclerView();
        setupShopItems();
    }

    private void initializeVariables() {

        idText = getIntent().getStringExtra("popupBusinessID");
        popupBusinessID = idText;

    }

    private void activateListeners() {

        popupSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                popupItemAmount = popupAmount.getText().toString();
                addItemToCart();

            }
        });

        popupCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                popupLayout.setVisibility(View.GONE);

            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();

            }
        });

    }

    private void setupViews() {

        popupLayout = (ConstraintLayout) findViewById(R.id.constraintLayoutShopPopup);
        popupSave = (TextView) findViewById(R.id.textViewShopPopupSave);
        popupCancel = (TextView) findViewById(R.id.textViewShopPopupCancel);
        popupAmount = (EditText) findViewById(R.id.editTextShopPopupAmount);
        popupQuestion = (TextView) findViewById(R.id.textViewShopPopupQuestion);

        back = (ImageView) findViewById(R.id.imageViewRatesBack);
        noListed = (TextView) findViewById(R.id.textViewRatesNoListed);

        itemList = new ArrayList<>();
        priceList = new ArrayList<>();

        recyclerView = (RecyclerView) findViewById(R.id.recyclerViewRates);

        rootRef = FirebaseDatabase.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();
        uid = firebaseAuth.getCurrentUser().getUid();

    }

    private void addItemToCart() {

        //this function is for adding the selected item to user's cart//

        DatabaseReference cartRef = rootRef.child("Cart").child(uid).child(popupBusinessID).child(popupItemName);
        cartRef.child("Item_price").setValue(popupItemPrice);
        cartRef.child("Item_amount").setValue(popupItemAmount);

        popupAmount.setText("");
        popupLayout.setVisibility(View.GONE);
        Toast.makeText(this, "Item added to cart", Toast.LENGTH_SHORT).show();

    }


    private void setupShopItems() {

        //this function is for getting the item list from the shop//

        DatabaseReference shopRef = rootRef.child("Shop").child(idText);
        shopRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    itemList.add(ds.child("Item_name").getValue().toString());
                    priceList.add(ds.child("Item_price").getValue().toString());

                    adapter.notifyDataSetChanged();
                }

                if(itemList.size() == 0){
                    noListed.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                }else{
                    recyclerView.setVisibility(View.VISIBLE);
                }
                progressDialog.dismiss();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void setupRecyclerView() {

        adapter = new ShopPageAdapter(itemList,priceList,RatesActivity.this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

    }


    @Override
    public void onMethodCallback(String itemName, String itemPrice) {

        //this function is for opening quantity prompt for items in the shop//
        popupItemName = itemName;
        popupItemPrice = itemPrice;

        popupQuestion.setText("How many " + itemName + " do you wish to avail?");
        popupLayout.setVisibility(View.VISIBLE);

    }

}
