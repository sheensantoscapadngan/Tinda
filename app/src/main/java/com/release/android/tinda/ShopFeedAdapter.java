package com.release.android.tinda;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;

public class ShopFeedAdapter extends RecyclerView.Adapter<ShopFeedAdapter.ViewHolder>{

    private ArrayList<String> postTimeList = new ArrayList<>();
    private ArrayList<String> postHeaderList = new ArrayList<>();
    private ArrayList<String> postImageList = new ArrayList<>();
    private ArrayList<String> postDescriptionList = new ArrayList<>();
    private ArrayList<String> postLikesList= new ArrayList<>();
    private ArrayList<String> postCommentsList= new ArrayList<>();
    private Context context;
    private String userID, userName,businessID;
    private ArrayList<String> postIdList = new ArrayList<>();

    public ShopFeedAdapter(ArrayList<String> postTimeList, ArrayList<String> postHeaderList, ArrayList<String> postImageList, ArrayList<String> postDescriptionList, ArrayList<String> postLikesList, ArrayList<String> postCommentsList, Context context, String userID, String userName, ArrayList<String> postIdList, String businessID) {
        this.postTimeList = postTimeList;
        this.postHeaderList = postHeaderList;
        this.postImageList = postImageList;
        this.postDescriptionList = postDescriptionList;
        this.postLikesList = postLikesList;
        this.postCommentsList = postCommentsList;
        this.context = context;
        this.userID = userID;
        this.userName = userName;
        this.postIdList = postIdList;
        this.businessID = businessID;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.shopfeedlayout,parent,false);
        ShopFeedAdapter.ViewHolder viewHolder = new ShopFeedAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        String timeAgo = getTimeAgo(postTimeList.get(position));
        holder.time.setText(timeAgo);
        holder.header.setText(postHeaderList.get(position));
        holder.description.setText(postDescriptionList.get(position));
        holder.likeCount.setText(postLikesList.get(position));
        holder.commentCount.setText(postCommentsList.get(position));

        Glide.with(context).load(postImageList.get(position)).into(holder.image);

        holder.like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                checkIfLiked(position,holder);

            }
        });

        holder.comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(context,CommentActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("businessID",businessID);
                intent.putExtra("postID",postIdList.get(position));
                intent.putExtra("userName",userName);
                context.startActivity(intent);

            }
        });

        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(context,ImageActivity.class);
                intent.putExtra("imageUri",postImageList.get(position));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);

            }
        });

        checkLikeState(holder,position);
    }

    private void checkLikeState(final ViewHolder holder, int position) {

        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        final DatabaseReference likeRef = rootRef.child("Posts").child(businessID).child(postIdList.get(position));
        likeRef.child("Post_likes").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Boolean likeState = false;

                for(DataSnapshot userSnapshot : dataSnapshot.getChildren()){
                    if(userSnapshot.getKey().equals(userID)){
                        likeState = true;
                    }
                }

                if(likeState){
                    Glide.with(context).load(R.drawable.feed_like_colored).into(holder.like);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void checkIfLiked(final int position,final ViewHolder holder) {

        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        final DatabaseReference likeRef = rootRef.child("Posts").child(businessID).child(postIdList.get(position));
        likeRef.child("Post_likes").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Boolean likeState = false;

                for(DataSnapshot userSnapshot : dataSnapshot.getChildren()){
                    if(userSnapshot.getKey().equals(userID)){
                        likeState = true;
                    }
                }

                if(likeState){
                    //remove liked status//
                    postLikesList.set(position,Integer.toString(Integer.parseInt(postLikesList.get(position)) - 1));
                    likeRef.child("Post_likes").child(userID).setValue(null);
                    notifyDataSetChanged();
                    Glide.with(context).load(R.drawable.feed_like).into(holder.like);

                }else{

                    Glide.with(context).load(R.drawable.feed_like_colored).into(holder.like);
                    addLikeToDB(position);

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void addLikeToDB(int position) {

        postLikesList.set(position,Integer.toString(Integer.parseInt(postLikesList.get(position)) + 1));

        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference likeRef = rootRef.child("Posts").child(businessID).child(postIdList.get(position));
        likeRef.child("Post_likes").child(userID).setValue(userName);

        notifyDataSetChanged();

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

        if(postCommentsList.size() < postLikesList.size()){
            return postCommentsList.size();
        }else
            return postLikesList.size();

    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView time,header,description,likeCount,commentCount;
        private ImageView image,like,comment;

        public ViewHolder(View itemView) {
            super(itemView);

            time = (TextView) itemView.findViewById(R.id.textViewShopFeedTime);
            header = (TextView) itemView.findViewById(R.id.textViewShopFeedHeader);
            description = (TextView) itemView.findViewById(R.id.textViewShopFeedDescription);
            likeCount = (TextView) itemView.findViewById(R.id.textViewShopFeedLikeCount);
            commentCount = (TextView) itemView.findViewById(R.id.textViewShopFeedCommentCount);
            image = (ImageView) itemView.findViewById(R.id.imageViewShopFeedImage);
            like = (ImageView) itemView.findViewById(R.id.imageViewShopFeedLike);
            comment = (ImageView) itemView.findViewById(R.id.imageViewShopFeedComment);

        }
    }

}
