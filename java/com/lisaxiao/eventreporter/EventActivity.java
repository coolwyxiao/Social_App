package com.lisaxiao.eventreporter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment; // change from fragment to v4 fragment

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;


// 加入了广告和map的功能
public class EventActivity extends AppCompatActivity {
    // 将event fragment的内容放进来
    private Fragment mEventsFragment;
    private Fragment mEventMapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        if (mEventsFragment == null) {
            mEventsFragment = new EventsFragment();
        }


//        //将eventFragment放到这个界面中,eventFragment中的xml格式会覆盖在这个上
//        //getSupportFragmentManager().beginTransaction().add(R.id.relativelayout_event, mEventsFragment).commit();
//        getSupportFragmentManager().beginTransaction().add(R.id.relativelayout_event, mEventMapFragment).commit();

//        /////////////////////
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);

        navigation.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {

                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_event_list:
                                getSupportFragmentManager().beginTransaction().replace(R.id.relativelayout_event, mEventsFragment).commit();
                                break;
                            case R.id.action_event_map:
                                if (mEventMapFragment == null) {
                                    mEventMapFragment = new EventMapFragment();
                                }
                                getSupportFragmentManager().beginTransaction().replace(R.id.relativelayout_event, mEventsFragment).commit(); // need to change mEvent Fragment to mEventMapFragment to implement map
                        }
                        return false;
                    }
                });



    }

}