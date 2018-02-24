package com.example.j.vision;

//https://developer.android.com/things/training/doorbell/cloud-vision.html

import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;

import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jwc374 on 2/21/2018.
 */

public class PassPhoto extends AppCompatActivity {

    private static final String TAG = "PassPhoto.java";

    private static final String CLOUD_VISION_API_KEY = "AIzaSyCPeO7TayOnyDMwaarp_HKV9g3guJN2qi4";
    // private static final MAX_LABEL_RESULTS = 10;

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

        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        Bitmap rotatedBitmap = Bitmap.createBitmap(myBitmap, 0, 0, myBitmap.getWidth(), myBitmap.getHeight(), matrix, true);
        //when placing a bitmap in image view from gallery, its rotated on its side :(
        IMG.setImageBitmap(rotatedBitmap);
        //IMG.setImageBitmap(myBitmap);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        myBitmap = Bitmap.createScaledBitmap(myBitmap, 160, 160, true);
        myBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        final byte[] byteArray = stream.toByteArray();

        final TextView tv = (TextView) findViewById(R.id.resultTv);


        //doing all this in a new thread
        new Thread(new Runnable() {
            public void run() {

                DoorbellActivity.onPictureTaken(byteArray);
            }
        }).start();
    }


    public static Map<String, Float> annotateImage(byte[] imageBytes) throws IOException {
        // Construct the Vision API instance
        HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
        VisionRequestInitializer initializer = new VisionRequestInitializer(CLOUD_VISION_API_KEY);
        Vision vision = new Vision.Builder(httpTransport, jsonFactory, null).setVisionRequestInitializer(initializer).setApplicationName("vision").build();

        // Create the image request
        AnnotateImageRequest imageRequest = new AnnotateImageRequest();
        Image image = new Image();
        image.encodeContent(imageBytes);
        imageRequest.setImage(image);

        // Add the features we want
        Feature labelDetection = new Feature();
        labelDetection.setType("LABEL_DETECTION");
        labelDetection.setMaxResults(10);
        imageRequest.setFeatures(Collections.singletonList(labelDetection));

        // Batch and execute the request
        BatchAnnotateImagesRequest requestBatch = new BatchAnnotateImagesRequest();
        requestBatch.setRequests(Collections.singletonList(imageRequest));
        BatchAnnotateImagesResponse response = vision.images().annotate(requestBatch).setDisableGZipContent(true).execute();

        return convertResponseToMap(response);
        // More code here
    }

    private static Map<String, Float> convertResponseToMap(BatchAnnotateImagesResponse response) {
        Map<String, Float> annotations = new HashMap<>();

        // Convert response into a readable collection of annotations
        List<EntityAnnotation> labels = response.getResponses().get(0).getLabelAnnotations();
        if (labels != null) {
            for (EntityAnnotation label : labels) {
                annotations.put(label.getDescription(), label.getScore());
            }
        }

        return annotations;
    }

    public static class DoorbellActivity extends Activity {

        private static void onPictureTaken(final byte[] imageBytes) {
            if (imageBytes != null) {

                String a;

                try {
                    // Process the image using Cloud Vision
                    Map<String, Float> annotations = annotateImage(imageBytes);
                    Log.d(TAG, "cloud vision annotations:" + annotations);
                } catch (IOException e) {
                    Log.e(TAG, "Cloud Vison API error: ", e);
                }
            }
        }
    }
}








