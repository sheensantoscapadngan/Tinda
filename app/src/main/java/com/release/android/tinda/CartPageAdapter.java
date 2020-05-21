package com.release.android.tinda;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.release.android.tinda.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class CartPageAdapter extends RecyclerView.Adapter<CartPageAdapter.ViewHolder> {

    private ArrayList<String> nameList = new ArrayList<>();
    private ArrayList<String> genreList = new ArrayList<>();
    private ArrayList<String> itemList = new ArrayList<>();
    private ArrayList<String> priceList = new ArrayList<>();
    private ArrayList<String> amountList = new ArrayList<>();
    private ArrayList<String> idList = new ArrayList<>();
    private Context context;
    private String currentLatitude,currentLongitude;
    private AdapterCallback adapterCallback;

    public CartPageAdapter(ArrayList<String> nameList, ArrayList<String> genreList, ArrayList<String> itemList, ArrayList<String> priceList, ArrayList<String> amountList, ArrayList<String> idList, Context context, String currentLatitude, String currentLongitude) {
        this.nameList = nameList;
        this.genreList = genreList;
        this.itemList = itemList;
        this.priceList = priceList;
        this.amountList = amountList;
        this.idList = idList;
        this.context = context;
        this.currentLatitude = currentLatitude;
        this.currentLongitude = currentLongitude;

        adapterCallback = ((AdapterCallback)context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cartlistlayout,viewGroup,false);
        CartPageAdapter.ViewHolder viewHolder = new CartPageAdapter.ViewHolder(view);
        return viewHolder;

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int i) {


        String itemText = "";
        String priceText = "";
        int totalText = 0;

        //this part is for formatting the text for the Items part//
        for(int x = 0; x < itemList.size(); x++){

            String additionalText =   "-" + itemList.get(x) + " (P" + priceList.get(x) + " x " + amountList.get(x) + ")";
            if(additionalText.length() > 30){
                String temp = "";
                for(int y = 0; y < 30; y++){
                    temp += additionalText.charAt(y);
                }
                additionalText = temp + "...";
            }

            itemText += additionalText + "\n";

        }
        //---------------------------------------------------------//

        //this part is for formatting the text for the prices part//
        for(int x = 0; x < priceList.size(); x++){
            if(x < priceList.size() - 1) {
                priceText += "P" + (Integer.valueOf(priceList.get(x)) * Integer.valueOf(amountList.get(x))) + "\n";
            }
            else
                priceText += "P" + (Integer.valueOf(priceList.get(x)) * Integer.valueOf(amountList.get(x)));
        }
        //--------------------------------------------------------//

        //this part is for formatting the text for the Total//
        for(int x = 0; x < priceList.size(); x++){
            totalText += (Integer.valueOf(priceList.get(x)) * Integer.valueOf(amountList.get(x)));
        }
        //-------------------------------------------------------//

        viewHolder.name.setText(nameList.get(i));
        viewHolder.items.setText(itemText);
        viewHolder.prices.setText(priceText);
        viewHolder.total.setText("P"+ totalText);

        viewHolder.viewStore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(context,ShopActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("currentLatitude",currentLatitude);
                intent.putExtra("currentLongitude",currentLongitude);
                intent.putExtra("businessID",idList.get(i));
                intent.putExtra("businessGenre",genreList.get(i));
                intent.putExtra("request","openShop");
                context.startActivity(intent);
                ((Activity) context).finish();

            }
        });

        viewHolder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                deleteCart(idList.get(i),i);

            }
        });

        viewHolder.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                adapterCallback.onMethodCallback(idList.get(i));

            }
        });

    }

    private void deleteCart(String s, int position) {

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        String uid = firebaseAuth.getCurrentUser().getUid();

        rootRef.child("Cart").child(uid).child(s).setValue(null);

        nameList.remove(position);
        genreList.remove(position);
        itemList.remove(position);
        priceList.remove(position);
        amountList.remove(position);
        idList.remove(position);

        notifyDataSetChanged();
        Toast.makeText(context, "Entry deleted from cart", Toast.LENGTH_SHORT).show();

    }

    @Override
    public int getItemCount() {
        return nameList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView name,items,viewStore,prices,total,edit,delete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            name = (TextView) itemView.findViewById(R.id.textViewCartListName);
            items = (TextView) itemView.findViewById(R.id.textViewCartListItems);
            viewStore = (TextView) itemView.findViewById(R.id.textViewCartListViewStore);
            prices = (TextView) itemView.findViewById(R.id.textViewCartListPrices);
            total = (TextView) itemView.findViewById(R.id.textViewCartListTotal);
            edit = (TextView) itemView.findViewById(R.id.textViewCartListEdit);
            delete = (TextView) itemView.findViewById(R.id.textViewCartListDelete);

        }
    }

    public static interface AdapterCallback{
        void onMethodCallback(String idText);
        void onLastItemCallback(String idText);
        void reloadRecyclerView();
    }

}
