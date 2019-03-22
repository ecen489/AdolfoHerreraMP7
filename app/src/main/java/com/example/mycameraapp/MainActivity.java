package com.example.mycameraapp;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.TextureView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_CODE = 1000;
    private static final int IMAGE_CAPTURE_CODE = 1001;
    ImageView mImageView;
    Uri image_uri;
    TextView imageName;
    DatabaseHandler db;
    EditText saveName;
    private static Integer storeInfo = 1;

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy-hh-mm-ss");


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {


            switch (item.getItemId()) {
                case R.id.navigation_home:
                    //Open Image
                    System.out.println(saveName.getText());
                    String text = saveName.getText().toString();
                    if (text.equals("") || !(text.matches("[0-9]+"))) {
                        Toast.makeText(getApplicationContext(),"Invalid Input!", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    Integer num = Integer.parseInt(saveName.getText().toString());
                    if (num == null) {
                        Toast.makeText(getApplicationContext(),"Invalid Input!", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    imageName.setText(saveName.getText().toString());

                    mImageView.setImageBitmap(db.getImage(num));

                    return true;
                case R.id.navigation_dashboard:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
                            String[] permission = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                            requestPermissions(permission, PERMISSION_CODE);
                        }
                        else {
                            openCamera();
                        }
                    }
                    else {
                        openCamera();
                    }
                    return true;
                case R.id.navigation_notifications:

                    String x = getPath(image_uri);
                    try {
                        if (db.insertimage(x, storeInfo)) {
                            Toast.makeText(getApplicationContext(), "Saved Image!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Unsuccessful Save!", Toast.LENGTH_SHORT).show();
                        }
                    }catch (Exception e) {
                        return false;
                    }
                    storeInfo++;

                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DatabaseHandler(this);
        storeInfo = db.getRows();
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);



        mImageView = findViewById(R.id.image_view);
        imageName = (TextView) findViewById(R.id.textView);
        saveName = (EditText) findViewById(R.id.editText);
        navigation.setSelectedItemId(R.id.navigation_dashboard);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        imageName.setText("Currently " + storeInfo + " Images Saved!");
    }

    private void openCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"New Picture");
        values.put(MediaStore.Images.Media.TITLE,"From the Camera");
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);
        //camera intent
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_CODE:{
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    openCamera();
                }
                else {
                    Toast.makeText(this,"Permission Denied!", Toast.LENGTH_SHORT).show();

                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            String format = simpleDateFormat.format(new Date());
            mImageView.setImageURI(image_uri);
            imageName.setText("Will be saved as: "+storeInfo.toString());

        }

    }

    //Helper function from youtube.
    //https://www.youtube.com/watch?v=6wZeSJ0U1t4
    public String getPath(Uri uri) {
        if(uri == null) return null;
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(uri,projection,null,null,null);
        if (cursor!=null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        return uri.getPath();
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("storeInfo", storeInfo);
        super.onSaveInstanceState(outState);
    }
}
