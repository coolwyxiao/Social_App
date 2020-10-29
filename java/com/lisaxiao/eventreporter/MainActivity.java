package com.lisaxiao.eventreporter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity
        implements EventFragment.OnItemSelectListener {
    private EventFragment mListFragment;
    private CommentFragment mGridFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        //add list view
        mListFragment= new EventFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.event_container, mListFragment).commit();
        //add Gridview
        if (isTablet()) {
        mGridFragment = new CommentFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.comment_container, mGridFragment).commit();
    }
//        // Show different fragments based on screen size.
//        if (findViewById(R.id.fragment_container) != null) {
//        Fragment fragment = isTablet() ? new CommentFragment() : new EventFragment(); //commentfragment 和 eventfragment控制是哪种显示界面
//        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, fragment).commit();
//        }
    }

    private boolean isTablet() {
        return (getApplicationContext().getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK) >=
                Configuration.SCREENLAYOUT_SIZE_LARGE; //xml boolean value depends on screen size
    }


//    /**
//     * A dummy function to get fake event names. *
//     * @return an array of fake event names.
//     */
//    private String[] getEventNames() {
//        String[] names = {
//                "Event1", "Event2", "Event3", "Event4", "Event5", "Event6", "Event7", "Event8", "Event9", "Event10", "Event11", "Event12"};
//        return names;
//    }
    @Override
    protected void onStart() {
        super.onStart();
        Log.e("Life cycle test", "We are at onStart()"); }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("Life cycle test", "We are at onResume()"); }

    @Override
    protected void onPause() {
        super.onPause();
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.fragment_container); if(isChangingConfigurations() && fragment != null) {
            fragmentManager.beginTransaction().remove(fragment).commit(); }
        Log.e("Life cycle test", "We are at onPause()");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e("Life cycle test", "We are at onStop()"); }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("Life cycle test", "We are at onDestroy()"); }

    @Override
    public void onItemSelected(int position){
        mGridFragment.onItemSelected(position); }
}