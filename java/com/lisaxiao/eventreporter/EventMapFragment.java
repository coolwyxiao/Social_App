package com.lisaxiao.eventreporter;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.silencedut.asynctaskscheduler.SingleAsyncTask;

import java.util.ArrayList;
import java.util.List;

public class EventMapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener, GoogleMap.OnMarkerClickListener {
    private MapView mMapView;
    private View mView;
    private DatabaseReference database;
    private List<Event> events;
    private GoogleMap mGoogleMap;
    private Marker lastClicked;

    public EventMapFragment(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        mView = inflater.inflate(R.layout.fragment_event_map,container,false);
        database = FirebaseDatabase.getInstance().getReference();
        events = new ArrayList<Event>();
        return mView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInsanceState){
        super.onViewCreated(view, savedInsanceState);
        mMapView = (MapView) mView.findViewById(R.id.event_map_view);
        if(mMapView != null){
            mMapView.onCreate(null);
            mMapView.onResume();
            mMapView.getMapAsync(this);
        }
    }

    @Override
    /////////////////////////
    //configure google map
    public void onMapReady(GoogleMap googleMap) {
        MapsInitializer.initialize(getContext());
        mGoogleMap = googleMap;
        mGoogleMap.setOnInfoWindowClickListener(this);
        mGoogleMap.setOnMapClickListener((GoogleMap.OnMapClickListener) this);
        final LocationTracker locationTracker = new LocationTracker(getActivity());
        locationTracker.getLocation();
        double latitute = locationTracker.getLatitude();
        double longtitute = locationTracker.getLongitude();



        //set up camera, set it to specific position and zoom to 12
        CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(latitute,longtitute)).zoom(12).build();

        //animate the zoom process
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        setUpMarkersCloseToCurLocation(googleMap,latitute,longtitute);

    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    private void setUpMarkersCloseToCurLocation(final GoogleMap googleMap, final double curLatitute, final double curLongtitute){
        events.clear();
        database.child("events").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot noteDataSnapshot : dataSnapshot.getChildren()){
                    Event event = noteDataSnapshot.getValue(Event.class);
                    double destLatitute = event.getLatitute();
                    double destLongtitute = event.getLongtitute();
                    int distance = Utils.distanceBetweenTwoLocations(curLatitute,curLongtitute,destLatitute,destLongtitute);
                    if(distance<=10){
                        events.add(event);
                    }
                }

                //set up every event
                for(Event event:events){
                    //create marker
                    MarkerOptions marker = new MarkerOptions().position(
                            new LatLng(event.getLatitute(), event.getLongtitute())).title(event.getTitle());

                    //changing marker icon
                    marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE));

                    //adding marker
                    Marker mker = googleMap.addMarker(marker);
                    mker.setTag(event);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Event event = (Event)marker.getTag();
        Intent intent = new Intent(getContext(),CommentActivity.class);
        String eventId = event.getId();
        intent.putExtra("EventID",eventId);
        getContext().startActivity(intent);
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        final Event event = (Event)marker.getTag();
        if(lastClicked !=null && lastClicked.equals(marker) ){
            lastClicked = null;
            marker.hideInfoWindow();
            marker.setIcon(null);
            return true;
        }else{
            lastClicked = marker;
            SingleAsyncTask singleAsyncTask = new SingleAsyncTask<Void, Bitmap>() {
                @Override
                public Bitmap doInBackground() {
                    Bitmap bitmap = Utils.getBitmapFromURL(event.getImgUri());
                    return bitmap;
                }

                @Override
                public void onExecuteSucceed(Bitmap bitmap) {
                    super.onExecuteSucceed(bitmap);
                    if(bitmap != null){
                        marker.setIcon((BitmapDescriptorFactory.fromBitmap(bitmap)));
                        marker.setTitle(event.getTitle());
                    }
                }

            };
            singleAsyncTask.executeSingle();
        }
        return false;
    }
}
