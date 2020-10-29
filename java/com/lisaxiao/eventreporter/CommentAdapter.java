package com.lisaxiao.eventreporter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.silencedut.asynctaskscheduler.SingleAsyncTask;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private final static int TYPE_EVENT = 0;
    private final static int TYPE_COMMENT = 1;
    private List<Comment> commentList;
    private Event event;

    private DatabaseReference databaseReference;
    private LayoutInflater inflater;

    public CommentAdapter(Context context){
        this.context = context;
        commentList = new ArrayList<>();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        inflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
    }

    public class CommentViewHolder extends RecyclerView.ViewHolder{
        public TextView commentUser; //time description
        public TextView commentTime;
        public TextView commentDescription;
        public View layout;

        public CommentViewHolder(View v) {
            super(v);
            commentUser = (TextView) v.findViewById(R.id.comment_item_user);
            commentTime = (TextView) v.findViewById(R.id.comment_item_time);
            commentDescription = (TextView) v.findViewById(R.id.comment_item_description);
        }
    }
    // have 2 different views to show in the recycler view
    public class EventViewHolder extends RecyclerView.ViewHolder{
        public TextView eventUser;
        public TextView eventTitle;
        public TextView eventLocation;
        public TextView eventDescription;
        public TextView eventTime;
        public ImageView eventImgView;
        public ImageView eventImgViewGood;
        public ImageView eventImgViewComment;

        public TextView eventLikeNumber;
        public TextView eventCommentNumber;
        public View layout;

        public EventViewHolder(View v){
            super(v);
            layout = v;
            eventUser = (TextView) v.findViewById(R.id.comment_main_user);
            eventTitle = (TextView) v.findViewById(R.id.comment_main_title);
            eventLocation = (TextView) v.findViewById(R.id.comment_main_location);
            eventDescription = (TextView) v.findViewById(R.id.comment_main_description);
            eventTime = (TextView) v.findViewById(R.id.comment_main_time);
            eventImgView = (ImageView) v.findViewById(R.id.comment_main_image);
            eventImgViewGood = (ImageView) v.findViewById(R.id.comment_main_like_img);
            eventImgViewComment = (ImageView) v.findViewById(R.id.comment_main_comment_img);
            eventLikeNumber = (TextView) v.findViewById(R.id.comment_main_like_number);
            eventCommentNumber = (TextView) v.findViewById(R.id.comment_main_comment_number);
        }
    }

    public void setEvent(final Event event){
        this.event = event;
    }

    public void setComments(final List<Comment> comments){
        this.commentList = comments;
    }

    @Override
    public int getItemViewType(int position){
        return position == 0? TYPE_EVENT : TYPE_COMMENT;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v;
        switch (viewType){
            case TYPE_EVENT: //评论的时候的界面
                v=inflater.inflate(R.layout.comment_main,parent,false);
                viewHolder = new EventViewHolder(v);
                break;
            case TYPE_COMMENT: //评论之后的界面
                v=inflater.inflate(R.layout.comment_item,parent,false);
                viewHolder = new CommentViewHolder(v);
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder( RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case TYPE_EVENT:
                EventViewHolder viewHolderEvent = (EventViewHolder) holder;
                configureEventView(viewHolderEvent);
                break;
            case TYPE_COMMENT:
                CommentViewHolder viewHolderAds = (CommentViewHolder) holder;
                configureCommentView(viewHolderAds,position);
                break;
        }

    }

    private void configureEventView(final EventViewHolder holder) {
        holder.eventUser.setText(event.getUsername());
        holder.eventTitle.setText(event.getTitle());
        String[] locations = event.getAddress().split(",");
        holder.eventLocation.setText(locations[1] + "," + locations[2]);
        holder.eventDescription.setText(event.getDescription());
        holder.eventTime.setText(Utils.timeTransformer(event.getTime()));
        holder.eventCommentNumber.setText(String.valueOf(event.getCommentNumber()));
        holder.eventLikeNumber.setText(String.valueOf(event.getLike()));

        if (event.getImgUri() != null) {
            final String url = event.getImgUri();
            holder.eventImgView.setVisibility(View.VISIBLE);
            SingleAsyncTask singleAsyncTask = new SingleAsyncTask<Void, Bitmap>() {
                @Override
                public Bitmap doInBackground() {
                    return Utils.getBitmapFromURL(url);
                }

                @Override
                public void onExecuteSucceed(Bitmap result) {
                    holder.eventImgView.setImageBitmap(result);
                }

            };
            singleAsyncTask.executeSingle();
            //singleAsyncTask.cancel(true);

//            URL url = null;
//
//            new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        URL url = null;
//                        try {
//                            url = new URL(event.getImgUri());
//                        } catch (MalformedURLException e) {
//                            e.printStackTrace();
//                        }
//                        holder.imgview.setVisibility(View.VISIBLE);
//                        HttpURLConnection connection = null;
//                        try {
//                            connection = (HttpURLConnection) url.openConnection();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                        connection.setDoInput(true);
//                        try {
//                            connection.connect();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                        InputStream input = null;
//                        try {
//                            input = connection.getInputStream();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                        final Bitmap bitmap = BitmapFactory.decodeStream(input);
//
//
//                                /////////////////////
//
//                        Activity.runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                holder.imgview.setImageBitmap(bitmap);
//                            }
//                        });
//
//                    }
//                }).start();

        } else {
            holder.eventImgView.setVisibility(View.GONE);
        }

        holder.eventImgViewGood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                databaseReference.child("events").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Event recorededevent = snapshot.getValue(Event.class);
                            if (recorededevent.getId().equals(event.getId())) { // 根据event id唯一指定该event
                                int number = recorededevent.getLike();
                                holder.eventLikeNumber.setText(String.valueOf(number + 1));
                                snapshot.getRef().child("like").setValue(number + 1);
                                break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });


    }

    private void configureCommentView(final CommentViewHolder commentHolder, final int position){
        final Comment comment = commentList.get(position-1);
        commentHolder.commentUser.setText(comment.getCommenter());
        commentHolder.commentDescription.setText(comment.getDescription());
        commentHolder.commentTime.setText(Utils.timeTransformer(comment.getTime()));
    }

    @Override
    public int getItemCount() {
        return commentList.size()+1;
    }


}



