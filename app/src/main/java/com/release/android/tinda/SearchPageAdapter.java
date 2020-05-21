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
import com.release.android.tinda.R;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class SearchPageAdapter extends RecyclerView.Adapter<SearchPageAdapter.ViewHolder>{

    private ArrayList<String> businessName = new ArrayList<>();
    private ArrayList<String> businessAddress = new ArrayList<>();
    private ArrayList<String> businessID = new ArrayList<>();
    private ArrayList<String> businessDistance = new ArrayList<>();
    private ArrayList<String> businessImageList = new ArrayList<>();
    private ArrayList<String> businessNumber = new ArrayList<>();
    private ArrayList<String> businessLatitude = new ArrayList<>();
    private ArrayList<String> businessLongitude = new ArrayList<>();
    private ArrayList<String> businessGenreList = new ArrayList<>();

    private Context context;
    private String currentLatitude, currentLongitude;

    public SearchPageAdapter(ArrayList<String> businessName, ArrayList<String> businessAddress, ArrayList<String> businessID, ArrayList<String> businessDistance,
    ArrayList<String> businessImageList,Context context, ArrayList<String> businessNumber, String currentLatitude, String currentLongitude,
                             ArrayList<String> businessLatitude, ArrayList<String> businessLongitude,ArrayList<String> businessGenreList) {
        this.businessName = businessName;
        this.businessAddress = businessAddress;
        this.businessID = businessID;
        this.businessDistance = businessDistance;
        this.businessImageList = businessImageList;
        this.context = context;
        this.businessNumber = businessNumber;
        this.currentLatitude = currentLatitude;
        this.currentLongitude = currentLongitude;
        this.businessLatitude = businessLatitude;
        this.businessLongitude = businessLongitude;
        this.businessGenreList = businessGenreList;

    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.searchlistlayout,viewGroup,false);
        SearchPageAdapter.ViewHolder viewHolder = new SearchPageAdapter.ViewHolder(view);
        return viewHolder;

    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int i) {

        //round off for distance//
        Double distance= Double.valueOf(businessDistance.get(i));
        DecimalFormat df = new DecimalFormat("#.000");
        final String distanceText = df.format(distance);

        //edit name
        String nameText = businessName.get(i);
        if(nameText.length() > 25){
            String newNameText = "";
            for(int x = 0; x < 24; x++){
                newNameText += nameText.charAt(x);
            }
            nameText = newNameText + "...";
        }

        viewHolder.name.setText(nameText);
        viewHolder.distance.setText(distanceText + " km away");
        viewHolder.address.setText(businessAddress.get(i));
        viewHolder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(context,ShopActivity.class);
                intent.putExtra("businessName",businessName.get(i));
                intent.putExtra("businessAddress",businessAddress.get(i));
                intent.putExtra("businessDistance",distanceText);
                intent.putExtra("businessPicture",businessImageList.get(i));
                intent.putExtra("businessNumber",businessNumber.get(i));
                intent.putExtra("businessID",businessID.get(i));
                intent.putExtra("currentLatitude",currentLatitude);
                intent.putExtra("currentLongitude",currentLongitude);
                intent.putExtra("destinationLatitude",businessLatitude.get(i));
                intent.putExtra("destinationLongitude",businessLongitude.get(i));
                intent.putExtra("businessGenre",businessGenreList.get(i));

                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);

            }
        });

        Glide.with(context).load(businessImageList.get(i)).into(viewHolder.image);

    }

    @Override
    public int getItemCount() {
        return businessName.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private ImageView image;
        private TextView name,address,distance;
        private ConstraintLayout layout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            image = (ImageView) itemView.findViewById(R.id.imageViewSearchListPicture);
            name = (TextView) itemView.findViewById(R.id.textViewSearchListName);
            address = (TextView) itemView.findViewById(R.id.textViewSearchListAddress);
            distance = (TextView) itemView.findViewById(R.id.textViewSearchListDistance);
            layout = (ConstraintLayout) itemView.findViewById(R.id.constraintLayoutSearchListLayout);

        }
    }

}
