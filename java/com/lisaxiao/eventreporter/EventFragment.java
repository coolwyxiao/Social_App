package com.lisaxiao.eventreporter;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class EventFragment extends Fragment {


    public EventFragment newInstance() {
        EventFragment fragment = new EventFragment();
        Bundle bundle = new Bundle(); // 在activity重启的时候，希望之前的fragment仍在里面显示（可以将之前保存的东西重新显示出来）
        bundle.putString("name", "test");
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event, container, false);
        ListView listView = (ListView) view.findViewById(R.id.event_list);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                getActivity(), android.R.layout.simple_list_item_1, getEventNames());
        // Assign adapter to ListView.
        listView.setAdapter(adapter);
        //用户点击EventFragment中的内容，通过main activity连接到CommentFragment，然后触发OnItemSelected函数，在CommentFragment的对应部分显示颜色
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mCallback.onItemSelected(i);
            }
        });


        return view;

    }


    private String[] getEventNames() { String[] names = {
            "Event1", "Event2", "Event3",
            "Event4", "Event5", "Event6",
            "Event7", "Event8", "Event9",
            "Event10", "Event11", "Event12"};
        return names;
    }

    OnItemSelectListener mCallback; //相当于指针，指向implements这个interface的class，所以就可以吧eventfragment和commentfragment indirectly联系起来
    // Container Activity must implement this interface
    public interface OnItemSelectListener {
        public void onItemSelected(int position);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (OnItemSelectListener) context;
        } catch (ClassCastException e) {
            //do something
        }
        }

}