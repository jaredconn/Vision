package com.example.j.vision;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;

import android.widget.ImageView;
import android.widget.TextView;


import java.io.File;

/**
 * Created by jwc374 on 2/21/2018.
 */

public class PassPhoto extends AppCompatActivity {

    // private static final String TAG = "PassPhoto.java";

    private static final String CLOUD_VISION_API_KEY = "AIzaSyCPeO7TayOnyDMwaarp_HKV9g3guJN2qi4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pass_photo);

        final ImageView IMG;
        IMG = (ImageView) findViewById(R.id.img);

        Intent intent = getIntent();
        String path = intent.getStringExtra("path");
        File imageFile = new File(path);
        Bitmap myBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath()); //retrieving file


        IMG.setImageBitmap(myBitmap);

    }
}




