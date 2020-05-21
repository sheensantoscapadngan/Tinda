package com.release.android.tinda;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.release.android.tinda.R;

import java.util.ArrayList;

public class ShopPageAdapter extends RecyclerView.Adapter<ShopPageAdapter.ViewHolder>{

    private AdapterCallback adapterCallback;
    private ArrayList<String> itemList = new ArrayList<>();
    private ArrayList<String> priceList = new ArrayList<>();
    private Context context;

    public ShopPageAdapter(ArrayList<String> itemList, ArrayList<String> priceList, Context context) {
        this.itemList = itemList;
        this.priceList = priceList;
        this.context = context;
        adapterCallback = ((AdapterCallback)context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.shoplistlayout,viewGroup,false);
        ShopPageAdapter.ViewHolder viewHolder = new ShopPageAdapter.ViewHolder(view);
        return viewHolder;

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int i) {

        viewHolder.name.setText(itemList.get(i));
        viewHolder.price.setText("P" + priceList.get(i));
        viewHolder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                adapterCallback.onMethodCallback(itemList.get(i),priceList.get(i));

            }
        });

    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView name,price;
        private ConstraintLayout layout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            name = (TextView) itemView.findViewById(R.id.textViewShopListName);
            price = (TextView) itemView.findViewById(R.id.textViewShopListPrice);
            layout = (ConstraintLayout) itemView.findViewById(R.id.constraintLayoutShopList);

        }
    }

    public static interface AdapterCallback{
        void onMethodCallback(String itemName, String itemPrice);
    }


}
