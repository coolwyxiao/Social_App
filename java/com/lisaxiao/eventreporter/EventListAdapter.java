package com.lisaxiao.eventreporter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.formats.NativeAd;
import com.google.android.gms.ads.formats.NativeContentAd;
import com.google.android.gms.ads.formats.NativeContentAdView;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAdView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.silencedut.asynctaskscheduler.SingleAsyncTask;

import java.lang.annotation.Native;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private List<Event> eventList;
    private DatabaseReference databaseReference;
    private Context context;
    private static final int TYPE_ITEM = 0;
    private static final int TYPE_ADS = 1;

    private AdLoader.Builder builder;
    private LayoutInflater inflater;
    private static final String ADMOB_AD_UNIT_ID = "";
    private static final String ADMOB_APP_ID = "";

    private Map<Integer,Object> map = new HashMap<Integer, Object>();// keep position of the ads in the list

    public EventListAdapter(List<Event> events, Context context){
        databaseReference = FirebaseDatabase.getInstance().getReference();
        eventList = events;
        this.context = context;

        inflater = (LayoutInflater)context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        eventList = new ArrayList<Event>(); //put events and ads all in the arraylist, put ads in and report their locations to map
        int count =0;
        for(int i=0; i<events.size(); i++){
            if(i%2 == 1){
                map.put(i+count, new Object());
                count++;
                eventList.add(new Event());
            }
            eventList.add(events.get(i));
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v;
        switch (viewType) {
            case TYPE_ITEM:
                v = inflater.inflate(R.layout.event_list_item, parent, false);
                viewHolder = new ViewHolder(v);
                break;

            case TYPE_ADS:
                v = inflater.inflate(R.layout.ads_container_layout, parent, false);
                viewHolder = new ViewHolderAds(v);
                break;
        }
        return viewHolder;
    }

//    @Override
//    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
//
//    }

    public void configureItemView(final ViewHolder holder, int position){
        final Event event = eventList.get(position);
        holder.title.setText(event.getTitle());
        String[] locations = event.getAddress().split(",");
        holder.location.setText(locations[1]+","+locations[2]); //need to auto get location
        holder.description.setText(event.getDescription());
        holder.time.setText(String.valueOf(event.getTime())); // time is not the format I want

        holder.good_number.setText(String.valueOf(event.getLike()));

        if(event.getImgUri() != null){
            final String url = event.getImgUri();
            holder.imgview.setVisibility(View.VISIBLE);
            SingleAsyncTask singleAsyncTask = new SingleAsyncTask<Void, Bitmap>() {
                @Override
                public Bitmap doInBackground() {
                    return Utils.getBitmapFromURL(url);
                }

                @Override
                public void onExecuteSucceed(Bitmap result){
                    holder.imgview.setImageBitmap(result);
                }

            };
            singleAsyncTask.executeSingle();


        }else{
            holder.imgview.setVisibility(View.GONE);
        }


        holder.img_view_good.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                databaseReference.child("events").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                            Event recorededevent = snapshot.getValue(Event.class);
                            if(recorededevent.getId().equals(event.getId())){ // 根据event id唯一指定该event
                                int number = recorededevent.getLike();
                                holder.good_number.setText(String.valueOf(number+1));
                                snapshot.getRef().child("like").setValue(number+1);
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


        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context,CommentActivity.class); //将现在的activity转到下一个activity中
                String eventId = event.getId();
                intent.putExtra("EventID",eventId);
                context.startActivity(intent);
            }
        });
    }


    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()){
            case TYPE_ITEM:
                ViewHolder viewHolderItem = (ViewHolder) holder;
                configureItemView(viewHolderItem,position);
                break;
//            case TYPE_ADS:
//                ViewHolderAds viewHolderAds = (ViewHolderAds) holder;
//                refreshAd(viewHolderAds.frameLayout);
//                break;
            //need to add ads whenever need it
        }

    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    /////////////////////

    public void refreshAd(final FrameLayout frameLayout){
        AdLoader.Builder builder = new AdLoader.Builder(context, ADMOB_AD_UNIT_ID);
        builder.forUnifiedNativeAd(new UnifiedNativeAd.OnUnifiedNativeAdLoadedListener() {
            @Override
            public void onUnifiedNativeAdLoaded(UnifiedNativeAd ad) {
                UnifiedNativeAdView adView = (UnifiedNativeAdView) inflater.inflate(R.layout.ad_contain,null);
                populateContentAdView(ad,adView);
                frameLayout.removeAllViews();
                frameLayout.addView(adView);
            }
        });

        AdLoader adLoader = builder.withAdListener(new AdListener(){
            @Override
            public void onAdFailedToLoad(int errorCode){

            }
        }).build();

        adLoader.loadAd(new AdRequest.Builder().build());
    }


    public void populateContentAdView(UnifiedNativeAd nativeContentAd, UnifiedNativeAdView adView){
        adView.setHeadlineView(adView.findViewById(R.id.ads_headline));
        adView.setImageView(adView.findViewById(R.id.ads_image));
        adView.setBodyView(adView.findViewById(R.id.ads_body));
        adView.setAdvertiserView(adView.findViewById(R.id.ads_advertiser));

        ((TextView) adView.getHeadlineView()).setText(nativeContentAd.getHeadline());
        ((TextView) adView.getBodyView()).setText(nativeContentAd.getBody());
        ((TextView) adView.getAdvertiserView()).setText(nativeContentAd.getAdvertiser());

        List<NativeAd.Image> images = nativeContentAd.getImages();

        if(images.size()>0){
            ((ImageView) adView.getImageView()).setImageDrawable(images.get(0).getDrawable());
        }

        adView.setNativeAd(nativeContentAd); // assign native ad object to native view

    }


    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView title;
        public TextView location;
        public TextView description;
        public TextView time;
        public ImageView imgview;
        public ImageView img_view_good;
        public ImageView img_view_comment;

        public TextView good_number;
        public TextView comment_number;

        public View layout;

        public ViewHolder(View v) {
            super(v);
            layout = v;
            title = (TextView) v.findViewById(R.id.event_item_title);
            location = (TextView) v.findViewById(R.id.event_item_location);
            description = (TextView) v.findViewById(R.id.event_item_description);
            time = (TextView) v.findViewById(R.id.event_item_time);
            imgview = (ImageView) v.findViewById(R.id.event_item_img);

            img_view_good = (ImageView) v.findViewById(R.id.event_good_img);
            img_view_comment = (ImageView) v.findViewById(R.id.event_comment_img);
            good_number = (TextView) v.findViewById(R.id.event_good_number);
            comment_number = (TextView) v.findViewById(R.id.event_comment_number);
        }
    }

    //viewholder for ads
    public class ViewHolderAds extends RecyclerView.ViewHolder{
        public FrameLayout frameLayout;
        ViewHolderAds(View v){
            super(v);
            frameLayout = (FrameLayout) v;
        }
    }

    @Override
    public int getItemViewType(int position){
        return map.containsKey(position)? TYPE_ADS: TYPE_ITEM;
    }
}
