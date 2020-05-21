package com.release.android.tinda;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.release.android.tinda.R;

import java.util.ArrayList;

public class MainPageAdapter extends RecyclerView.Adapter<MainPageAdapter.ViewHolder>{

    private ArrayList<String> businessList = new ArrayList<>();
    private Context context;
    private String currentLatitude,currentLongitude;

    public MainPageAdapter(ArrayList<String> businessList,Context context, String currentLatitude, String currentLongitude) {
        this.businessList = businessList;
        this.context = context;
        this.currentLatitude = currentLatitude;
        this.currentLongitude = currentLongitude;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.mainlistlayout,viewGroup,false);
        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int i) {

        viewHolder.business.setText(businessList.get(i));
        viewHolder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(context,SearchActivity.class);
                intent.putExtra("businessName",businessList.get(i));
                intent.putExtra("currentLatitude",currentLatitude);
                intent.putExtra("currentLongitude",currentLongitude);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);

            }
        });

    }

    @Override
    public int getItemCount() {
        return businessList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView business;
        private ConstraintLayout layout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            business = (TextView) itemView.findViewById(R.id.textViewMainListLayoutBusiness);
            layout = (ConstraintLayout) itemView.findViewById(R.id.constraintLayoutMainRecycler);


        }

    }


}
