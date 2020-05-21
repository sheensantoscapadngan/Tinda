package com.release.android.tinda;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.release.android.tinda.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class CartPopupPageAdapter extends RecyclerView.Adapter<CartPopupPageAdapter.ViewHolder> {

    private ArrayList<String> itemNameList = new ArrayList<>();
    private ArrayList<String> itemPriceList = new ArrayList<>();
    private ArrayList<String> itemAmountList = new ArrayList<>();
    private String idText;
    private Context context;
    private CartPageAdapter.AdapterCallback adapterCallback;

    public CartPopupPageAdapter(ArrayList<String> itemNameList, ArrayList<String> itemPriceList, ArrayList<String> itemAmountList, String idText,Context context) {
        this.itemNameList = itemNameList;
        this.itemPriceList = itemPriceList;
        this.itemAmountList = itemAmountList;
        this.idText = idText;
        this.context = context;
        adapterCallback = ((CartPageAdapter.AdapterCallback)context);
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cartpopuplistlayout,viewGroup,false);
        CartPopupPageAdapter.ViewHolder viewHolder = new CartPopupPageAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int i) {

        viewHolder.itemName.setText(itemNameList.get(i));
        viewHolder.itemPrice.setText("P"+ itemPriceList.get(i));
        viewHolder.itemAmount.setText(itemAmountList.get(i));
        viewHolder.remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
                rootRef.child("Cart").child(firebaseAuth.getCurrentUser().getUid()).child(idText).child(itemNameList.get(i)).setValue(null);

                if(itemNameList.size() == 1){
                    adapterCallback.onLastItemCallback(idText);
                }else{
                    adapterCallback.reloadRecyclerView();
                }

                itemNameList.remove(i);
                itemPriceList.remove(i);
                itemAmountList.remove(i);

                notifyDataSetChanged();

            }
        });

    }

    @Override
    public int getItemCount() {
        return itemNameList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView itemName, itemPrice, itemAmount;
        private ImageView remove;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            itemName = (TextView) itemView.findViewById(R.id.textViewCartPopupListItemName);
            itemPrice = (TextView) itemView.findViewById(R.id.textViewCartPopupListPrice);
            itemAmount = (TextView) itemView.findViewById(R.id.textViewCartPopupListAmount);
            remove = (ImageView) itemView.findViewById(R.id.imageViewCartPopupListRemove);

        }
    }

}
