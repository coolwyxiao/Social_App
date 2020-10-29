package com.lisaxiao.eventreporter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.silencedut.asynctaskscheduler.SingleAsyncTask;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

public class EventReportActivity extends AppCompatActivity {

    private static final String TAG = EventReportActivity.class.getSimpleName();
    private EditText mEditTextLocation;
    private EditText mEditTextTitle;
    private EditText mEditTextContent;
    private ImageView mImageViewSend;
    private ImageView mImageViewCamera;
    private DatabaseReference database;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private LocationTracker mLocationTracker;

    // ready for uploading images
    private FirebaseStorage storage;
    private StorageReference storageRef;

    // ready for picking images
    private static int RESULT_LOAD_IMAGE = 1;
    private ImageView  img_event_picture;
    private Uri mImgUri; // local device uri

    @SuppressLint("StaticFieldLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_report);

        mEditTextLocation = (EditText) findViewById(R.id.edit_text_event_location);
        mEditTextTitle = (EditText) findViewById(R.id.edit_text_event_title);
        mEditTextContent = (EditText) findViewById(R.id.edit_text_event_content);
        mImageViewCamera = (ImageView) findViewById(R.id.img_event_camera); // need to have on-click action on each of them
        mImageViewSend = (ImageView) findViewById(R.id.img_event_report);

        database = FirebaseDatabase.getInstance().getReference();
        img_event_picture = (ImageView) findViewById(R.id.img_event_picture_capture);

        // initial cloud storage
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        mImageViewSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String key = uploadEvent(); // key是每一个event对应的unique的id
                uploadImage(key);
                mImgUri = null;
            }
        });

        mImageViewCamera.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(
                        Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, RESULT_LOAD_IMAGE);// 调用onActivityResult
            }
            });



        // 如果可以获取地址，将地址自动填入location栏中 // use doInBackground and onPostExecute in AsyncTask?
        mLocationTracker = new LocationTracker(this);
        mLocationTracker.getLocation();
        final double latitude = mLocationTracker.getLatitude();
        final double longitude = mLocationTracker.getLongitude();

        SingleAsyncTask singleAsyncTask = new SingleAsyncTask<Void,Void>() {
            private List<String> mAddressList = new ArrayList<String>();
            @Override
            protected Void doInBackground() {
                try {
                    mAddressList = mLocationTracker.getCurrentLocationViaJSON(latitude,longitude);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                return null;
            }
            @Override
            public void onExecuteSucceed(Void input){
                if (mAddressList.size() >= 3) {
                    String temp = mAddressList.get(0) + ", " + mAddressList.get(1) +
                            ", " + mAddressList.get(2) + ", " + mAddressList.get(3);
                    mEditTextLocation.setText(temp);
                }
            }
        };
        singleAsyncTask.executeSingle();


        //auth, 与firebase进行通信，告诉firebase有用户sign in
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.d(TAG, "onAuthStateChangedwenya:signed_in:" + user.getUid());
                } else {
                    Log.d(TAG, "onAuthStateChanged:signed_out"); }
            } };
        mAuth.signInAnonymously().addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Log.d(TAG, "signInAnonymously:onComplete:" + task.isSuccessful());
                if (!task.isSuccessful()) {
                    Log.w(TAG, "signInAnonymously", task.getException()); }
            } });

    }

    /**
     * Gather information inserted by user and create event for uploading. Then clear those widgets if user uploads one
     * @return the key of the event needs to be returned as link against Cloud storage */
    private String uploadEvent() {
        String title = mEditTextTitle.getText().toString();
        String location = mEditTextLocation.getText().toString(); // need write as: xxx, xxx, xxx
        String description = mEditTextContent.getText().toString();
        if (location.equals("") || description.equals("") ||
                title.equals("") || Utils.username == null) { //如果是空的，无法传上去
            return null;
        }

        //create event instance
        Event event = new Event();
        event.setTitle(title);
        event.setAddress(location);
        event.setDescription(description);
        event.setTime(System.currentTimeMillis());
        event.setLatitute(mLocationTracker.getLatitude());
        event.setLongtitute(mLocationTracker.getLongitude());
        event.setUsername(Utils.username);
        String key = database.child("events").push().getKey(); //先将数据push进去，得到一个unique的id，然后set过去
        event.setId(key);
        database.child("events").child(key).setValue(event, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError != null) {
                        Toast toast = Toast.makeText(getBaseContext(),
                                "The event is failed, please check your network status.", Toast.LENGTH_SHORT);
                        toast.show();
                    } else {
                        Toast toast = Toast.makeText(getBaseContext(), "The event is reported", Toast.LENGTH_SHORT);
                        toast.show();
                        mEditTextTitle.setText(""); // 设置回到初始值
                        mEditTextLocation.setText("");
                        mEditTextContent.setText("");
                    } }
                });
        return key;
    }

    /*
    upload image picked up from gallery to Firebase cloud storage
     */
    private  void uploadImage(final String eventId){
        if(mImgUri == null){
            return;
        }
        StorageReference imgRef = storageRef.child("images/" + mImgUri.getLastPathSegment()+"_"+System.currentTimeMillis());
        UploadTask uploadTask = imgRef.putFile(mImgUri); // 直接将uri传输过去，避免直接传输图片需要耗空间

        //Register observes to listen for when the upload is done or it it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //@SuppressWarnings("VisibleForTests")
                Task<Uri> downloadUri = taskSnapshot.getStorage().getDownloadUrl(); // 得到firebase存储时候对应的url
                while(!downloadUri.isComplete());
                Uri url = downloadUri.getResult();
                Log.i(TAG,"upload successfully" + eventId);
                database.child("events").child(eventId).child("imgUri").setValue(url.toString());
                img_event_picture.setImageDrawable(null);
                img_event_picture.setVisibility(View.GONE);
            }
        });
    }


    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }
    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null) {
                Uri selectedImage = data.getData(); //当前device中所携带的image的位置
                img_event_picture.setVisibility(View.VISIBLE);
                img_event_picture.setImageURI(selectedImage);
                mImgUri = selectedImage;

            }
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }
}