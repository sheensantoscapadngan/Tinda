package com.release.android.tinda;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

public class FollowingPageAdapter extends RecyclerView.Adapter<FollowingPageAdapter.ViewHolder> {

    private ArrayList<String> followingNameList = new ArrayList<>();
    private ArrayList<String> followingAddressList = new ArrayList<>();
    private ArrayList<String> followingImageList = new ArrayList<>();
    private ArrayList<String> followingGenreList = new ArrayList<>();
    private ArrayList<String> followingIdList = new ArrayList<>();
    private Context context;
    private String currentLatitude,currentLongitude;

    public FollowingPageAdapter(ArrayList<String> followingNameList, ArrayList<String> followingAddressList, ArrayList<String> followingImageList, ArrayList<String> followingGenreList, ArrayList<String> followingIdList, Context context, String currentLatitude, String currentLongitude) {
        this.followingNameList = followingNameList;
        this.followingAddressList = followingAddressList;
        this.followingImageList = followingImageList;
        this.followingGenreList = followingGenreList;
        this.followingIdList = followingIdList;
        this.context = context;
        this.currentLatitude = currentLatitude;
        this.currentLongitude = currentLongitude;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.followinglistlayout,parent,false);
        FollowingPageAdapter.ViewHolder viewHolder = new FollowingPageAdapter.ViewHolder(view);
        return viewHolder;

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {

        String nameText = "";
        String addressText = "";

        //shorten name if long//
        if(followingNameList.get(position).length() >= 32){
            for(int x = 0; x < 32; x++){
                nameText += followingNameList.get(position).charAt(x);
            }
            nameText += "...";
        }else{
            nameText = followingNameList.get(position);
        }
        ////////////////////////

        //shorten address if long//
        if(followingAddressList.get(position).length() >= 32){
            for(int x = 0; x < 32; x++){
                addressText += followingAddressList.get(position).charAt(x);
            }
            addressText += "...";
        }else{
            addressText = followingAddressList.get(position);
        }
        ///////////////////////////

        Glide.with(context).load(followingImageList.get(position)).into(holder.image);
        holder.name.setText(nameText);
        holder.address.setText(addressText);
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(context,ShopActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("currentLatitude",currentLatitude);
                intent.putExtra("currentLongitude",currentLongitude);
                intent.putExtra("businessID",followingIdList.get(position));
                intent.putExtra("businessGenre",followingGenreList.get(position));
                intent.putExtra("request","openShop");
                context.startActivity(intent);

            }
        });

    }


    @Override
    public int getItemCount() {
        return followingNameList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private ImageView image;
        private TextView name,address;
        private ConstraintLayout layout;

        public ViewHolder(View itemView) {
            super(itemView);

            image = (ImageView) itemView.findViewById(R.id.imageViewFollowingListImage);
            name = (TextView) itemView.findViewById(R.id.textViewFollowingListName);
            address = (TextView) itemView.findViewById(R.id.textViewFollowingListAddress);
            layout = (ConstraintLayout) itemView.findViewById(R.id.constraintLayoutFollowingListLayout);

        }
    }

}
