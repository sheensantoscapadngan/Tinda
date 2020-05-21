package com.release.android.tinda;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class CommentPageAdapter extends RecyclerView.Adapter<CommentPageAdapter.ViewHolder> {

    private ArrayList<String> commentNameList = new ArrayList<>();
    private ArrayList<String> commentContentList = new ArrayList<>();
    private ArrayList<String> commentTimeList = new ArrayList<>();
    private ArrayList<String> commentIdList = new ArrayList<>();
    private ArrayList<String> commentUserIdList = new ArrayList<>();
    private String myID;
    private AdapterCallback adapterCallback;
    private Context context;

    public CommentPageAdapter(ArrayList<String> commentNameList, ArrayList<String> commentContentList, ArrayList<String> commentTimeList, ArrayList<String> commentIdList, ArrayList<String> commentUserIdList,String myID, Context context) {
        this.commentNameList = commentNameList;
        this.commentContentList = commentContentList;
        this.commentTimeList = commentTimeList;
        this.commentIdList = commentIdList;
        this.commentUserIdList = commentUserIdList;
        this.myID = myID;
        adapterCallback = ((AdapterCallback)context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.commentlistlayout,parent,false);
        CommentPageAdapter.ViewHolder viewHolder = new CommentPageAdapter.ViewHolder(view);
        return viewHolder;

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {

        String timeAgo = getTimeAgo(commentTimeList.get(position));

        holder.name.setText(commentNameList.get(position));
        holder.content.setText(commentContentList.get(position));
        holder.time.setText(timeAgo);

        if(commentUserIdList.get(position).equals(myID)){
            holder.delete.setVisibility(View.VISIBLE);
        }

        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                adapterCallback.deleteComment(commentIdList.get(position),position);

            }
        });

    }

    @Override
    public int getItemCount() {
        return commentNameList.size();
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


    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView name,content,time,delete;

        public ViewHolder(View itemView) {
            super(itemView);

            name = (TextView) itemView.findViewById(R.id.textViewCommentListName);
            content = (TextView) itemView.findViewById(R.id.textViewCommentListContent);
            time = (TextView) itemView.findViewById(R.id.textViewCommentListTime);
            delete = (TextView) itemView.findViewById(R.id.textViewCommentListDelete);

        }
    }

    public static interface AdapterCallback{
        void deleteComment(String commentID,int position);
    }

}
