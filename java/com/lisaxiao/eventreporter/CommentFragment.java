package com.lisaxiao.eventreporter;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class CommentFragment extends Fragment {

    private GridView mGridView;
    public CommentFragment newInstance() {
        CommentFragment fragment = new CommentFragment();
        Bundle bundle = new Bundle(); // 在activity重启的时候，希望之前的fragment仍在里面显示（可以将之前保存的东西重新显示出来）
        bundle.putString("name", "test");
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_comment, container, false);
        mGridView = (GridView) view.findViewById(R.id.comment_grid);
        mGridView.setAdapter(new EventAdapter(getActivity()));
        return view;
    }

    // Change background color if the item is selected
     public void onItemSelected(int position){
        for (int i = 0; i < mGridView.getChildCount(); i++){
            if (position == i) {
                mGridView.getChildAt(i).setBackgroundColor(Color.GREEN);
            } else {
                mGridView.getChildAt(i).setBackgroundColor(Color.parseColor("#FAFAFA"));
            }
    } }

//    private String mParam1;
//    private String mParam2;
//
//    public CommentFragment() {
//        // Required empty public constructor
//    }
//
//    /**
//     * Use this factory method to create a new instance of
//     * this fragment using the provided parameters.
//     *
//     * @param param1 Parameter 1.
//     * @param param2 Parameter 2.
//     * @return A new instance of fragment CommentFragment.
//     */
//    // TODO: Rename and change types and number of parameters
//    public static CommentFragment newInstance(String param1, String param2) {
//        CommentFragment fragment = new CommentFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
//        return fragment;
//    }

//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
//    }


}