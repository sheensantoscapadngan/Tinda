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

import java.util.ArrayList;

public class NotificationPageAdapter extends RecyclerView.Adapter<NotificationPageAdapter.ViewHolder>{

    private ArrayList<String> notificationContentList = new ArrayList<>();
    private ArrayList<String> notificationTimeList = new ArrayList<>();
    private ArrayList<String> notificationIdList = new ArrayList<>();
    private ArrayList<String> notificationImageList = new ArrayList<>();
    private ArrayList<String> notificationGenreList = new ArrayList<>();
    private Context context;
    private String currentLatitude,currentLongitude;

    public NotificationPageAdapter(ArrayList<String> notificationContentList, ArrayList<String> notificationTimeList, ArrayList<String> notificationIdList, ArrayList<String> notificationImageList, ArrayList<String> notificationGenreList, Context context, String currentLatitude, String currentLongitude) {
        this.notificationContentList = notificationContentList;
        this.notificationTimeList = notificationTimeList;
        this.notificationIdList = notificationIdList;
        this.notificationImageList = notificationImageList;
        this.notificationGenreList = notificationGenreList;
        this.context = context;
        this.currentLatitude = currentLatitude;
        this.currentLongitude = currentLongitude;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notificationlistlayout,parent,false);
        NotificationPageAdapter.ViewHolder viewHolder = new NotificationPageAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {

        String timeAgo = getTimeAgo(notificationTimeList.get(position));
        holder.content.setText(notificationContentList.get(position) + " has a new post. Go check it out!");
        holder.time.setText(timeAgo);
        Glide.with(context).load(notificationImageList.get(position)).into(holder.image);
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(context,ShopActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("currentLatitude",currentLatitude);
                intent.putExtra("currentLongitude",currentLongitude);
                intent.putExtra("businessID",notificationIdList.get(position));
                intent.putExtra("businessGenre",notificationGenreList.get(position));
                intent.putExtra("request","openShop");
                context.startActivity(intent);

            }
        });

    }

    public String getTimeAgo(String time){

        int SECOND_MILLIS = 1000;
        int MINUTE_MILLIS = 60 * SECOND_MILLIS;
        int HOUR_MILLIS = 60 * MINUTE_MILLIS;
        int DAY_MILLIS = 24 * HOUR_MILLIS;

        long timeVal = Long.parseLong(time);
        long now = System.currentTimeMillis();
        long diff = now - timeVal;

        if (diff < MINUTE_MILLIS) {
            return "Just now";
        } else if (diff < 2 * MINUTE_MILLIS) {
            return "A minute ago";
        } else if (diff < 50 * MINUTE_MILLIS) {
            return diff / MINUTE_MILLIS + " minutes ago";
        } else if (diff < 90 * MINUTE_MILLIS) {
            return "An hour ago";
        } else if (diff < 24 * HOUR_MILLIS) {
            return diff / HOUR_MILLIS + " hours ago";
        } else if (diff < 48 * HOUR_MILLIS) {
            return "Yesterday";
        } else {
            return diff / DAY_MILLIS + " days ago";
        }

    }


    @Override
    public int getItemCount() {
        return notificationContentList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView content,time;
        private ConstraintLayout layout;
        private ImageView image;

        public ViewHolder(View itemView) {
            super(itemView);

            content = (TextView) itemView.findViewById(R.id.textViewNotificationListContent);
            time = (TextView) itemView.findViewById(R.id.textViewNotificationListTime);
            layout = (ConstraintLayout )itemView.findViewById(R.id.constraintLayoutNotificationList);
            image = (ImageView) itemView.findViewById(R.id.imageViewNotificationListImage);

        }
    }
}
