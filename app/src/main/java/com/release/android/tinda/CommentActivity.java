package com.release.android.tinda;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;

public class CommentActivity extends AppCompatActivity implements CommentPageAdapter.AdapterCallback{

    private String businessID,postID;
    private ImageView back;
    private EditText comment;
    private CommentPageAdapter adapter;
    private RecyclerView recyclerView;
    private ArrayList<String> commentNameList,commentContentList,commentTimeList,commentIdList,commentUserIdList;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference rootRef;
    private String uid,commentText,userNameText;
    private Button post;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        initializeVariables();
        setupViews();
        activateListeners();
        setupRecyclerView();
        loadCommentsFromDB();

    }

    private void initializeVariables() {

        businessID = getIntent().getStringExtra("businessID");
        postID = getIntent().getStringExtra("postID");
        userNameText = getIntent().getStringExtra("userName");
    }

    private void setupViews() {

        back = (ImageView) findViewById(R.id.imageViewCommentBack);
        comment = (EditText) findViewById(R.id.editTextCommentComment);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerViewComment);
        post = (Button) findViewById(R.id.buttonCommentListPost);

        commentNameList = new ArrayList<>();
        commentContentList = new ArrayList<>();
        commentTimeList = new ArrayList<>();
        commentIdList = new ArrayList<>();
        commentUserIdList = new ArrayList<>();

        firebaseAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
        uid = firebaseAuth.getCurrentUser().getUid();

    }

    private void setupRecyclerView() {

        adapter = new CommentPageAdapter(commentNameList,commentContentList,commentTimeList,commentIdList,commentUserIdList,uid,this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

    }

    private void activateListeners() {

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();

            }
        });

        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                commentText = comment.getText().toString();
                if(commentText.length() != 0){
                    addCommentToDB();
                }else{
                    comment.setError("This cannot be left empty");
                    comment.requestFocus();
                    return;
                }

            }
        });

    }

    private void addCommentToDB() {

        //this function is for adding comment to post DB//

        long now = System.currentTimeMillis();
        //this function is for adding the comment to DB//
        DatabaseReference commentRef = rootRef.child("Posts").child(businessID).child(postID).child("Post_comments").push();
        commentRef.child("Comment_name").setValue(userNameText);
        commentRef.child("Comment_content").setValue(commentText);
        commentRef.child("Comment_timestamp").setValue(now);
        commentRef.child("Comment_uid").setValue(uid);

        Toast.makeText(this, "Comment added", Toast.LENGTH_SHORT).show();

        commentNameList.add(userNameText);
        commentContentList.add(commentText);
        commentTimeList.add(Long.toString(now));
        commentIdList.add(commentRef.getKey());
        commentUserIdList.add(uid);

        adapter.notifyDataSetChanged();
    }

    private void loadCommentsFromDB() {

        //this is for loading comments from DB//

        DatabaseReference commentRef = rootRef.child("Posts").child(businessID).child(postID).child("Post_comments");
        commentRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(DataSnapshot ds : dataSnapshot.getChildren()){

                    commentNameList.add(ds.child("Comment_name").getValue().toString());
                    commentContentList.add(ds.child("Comment_content").getValue().toString());
                    commentTimeList.add(ds.child("Comment_timestamp").getValue().toString());
                    commentIdList.add(ds.getKey());
                    commentUserIdList.add(ds.child("Comment_uid").getValue().toString());


                    adapter.notifyDataSetChanged();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void deleteComment(String commentID, int position) {

        //this function is for deleting the selecgted comment from Comment Page Adapter//

        DatabaseReference commentRef = rootRef.child("Posts").child(businessID).child(postID).child("Post_comments").child(commentID);
        commentRef.setValue(null);

        commentNameList.remove(position);
        commentUserIdList.remove(position);
        commentContentList.remove(position);
        commentIdList.remove(position);
        commentTimeList.remove(position);

        adapter.notifyDataSetChanged();

        Toast.makeText(this, "Comment deleted", Toast.LENGTH_SHORT).show();


    }



}
