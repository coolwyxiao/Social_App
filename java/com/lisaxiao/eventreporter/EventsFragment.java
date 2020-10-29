package com.lisaxiao.eventreporter;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class EventsFragment extends Fragment {
    private ImageView mImageViewAdd;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private DatabaseReference database;
    private List<Event> events;

    public EventsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

        // Inflate the layout for this fragment，add connections between this activity and eventReport
        View view = inflater.inflate(R.layout.fragment_events, container, false);
        mImageViewAdd = (ImageView) view.findViewById(R.id.img_event_add);
        mImageViewAdd.setOnClickListener(new View.OnClickListener() { //在➕图标中添加点击事件
            @Override
            public void onClick(View view) {
                Intent eventReportIntent = new Intent(getActivity(), EventReportActivity.class);
                startActivity(eventReportIntent); }
        });

        recyclerView = (RecyclerView) view.findViewById(R.id.event_recycler_view);
        database = FirebaseDatabase.getInstance().getReference();
        recyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        setAdapter();

        return view;
    }

    public void setAdapter(){
        events = new ArrayList<Event>();
        database.child("events").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot noteDataSnapshot: dataSnapshot.getChildren()){
                    Event event = noteDataSnapshot.getValue(Event.class);
                    events.add(event);
                }
                mAdapter = new EventListAdapter(events,getActivity());
                recyclerView.setAdapter(mAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Do something
            }
        });
    }

}