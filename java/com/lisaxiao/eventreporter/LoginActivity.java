package com.lisaxiao.eventreporter;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {
    private EditText mUsernameEditText;
    private EditText mPasswordEditText;
    private Button mSubmitButton;
    private Button mRegisterButton;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Firebase uses singleton to initialize the sdk
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mUsernameEditText = (EditText) findViewById(R.id.editTextLogin);


        mPasswordEditText = (EditText) findViewById(R.id.editTextPassword);
        mRegisterButton = (Button) findViewById(R.id.register); //register
        mSubmitButton = (Button) findViewById(R.id.submit); // login

        // test Google admo
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        // try to register any account, and check the firebase to verify if it is successfully registered
        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              final String username = mUsernameEditText.getText().toString();
              final String password = Utils.md5Encryption(mPasswordEditText.getText().toString());
              final User user = new User(username, password, System.currentTimeMillis());
              mDatabase.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild(username)) {
                        Toast.makeText(getBaseContext(),"username is already registered, please change one", Toast.LENGTH_SHORT).show();
                    } else if (!username.equals("") && !password.equals("")){
                    // put username as key to set value
                        mDatabase.child("users").child(user.getUsername()).setValue(user);
                        Toast.makeText(getBaseContext(),"Successfully registered",
                        Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                } });
        } });

        //login activity
        mSubmitButton.setOnClickListener(new View.OnClickListener() { @Override
        public void onClick(View v) {
            final String username = mUsernameEditText.getText().toString();
            final String password = Utils.md5Encryption(mPasswordEditText.getText().toString());
            mDatabase.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild(username) &&
                            (password.equals(dataSnapshot.child(username).child("password").getValue()))) {
                        Log.i( " Your log", "You successfully login");
                        Intent intent = new Intent(LoginActivity.this, EventActivity.class); // explicit intent, 连接activity的头和尾
                        Utils.username = username;
                        startActivity(intent);
                        finish();
                        //Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                        //sharingIntent.setType("text/plain");
                        //String shareBody = "From TinNews: \n" + "www.google.com";
                        //sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                        //startActivity(Intent.createChooser(sharingIntent, "Share TinNews")); // implicit way, 不固定intent连接的尾端，所以可以share到其他APP
                    } else {
                        Toast.makeText(getBaseContext(),"Please login again", Toast.LENGTH_SHORT).show();
                    } }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                } });
        } });
    }
}